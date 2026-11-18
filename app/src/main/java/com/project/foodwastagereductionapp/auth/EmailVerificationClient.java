package com.project.foodwastagereductionapp.auth;

import android.os.SystemClock;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class EmailVerificationClient {
    private final String baseUrl;

    EmailVerificationClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    Challenge requestCode(String email) throws IOException, JSONException {
        JSONObject response = post("/v1/email/request", new JSONObject().put("email", AppDatabaseHelper.normalizeEmail(email)), true);
        String id = response.getString("challengeId");
        if (!id.matches("[A-Za-z0-9_-]{32}")) throw new IOException("Invalid challenge response");
        int expiresIn = Math.max(1, Math.min(response.getInt("expiresIn"), 300));
        return new Challenge(id, retryAfter(response), expiresIn);
    }

    void verifyCode(String email, String challengeId, String code) throws IOException, JSONException {
        email = AppDatabaseHelper.normalizeEmail(email);
        JSONObject request = new JSONObject().put("email", email)
                .put("challengeId", challengeId).put("code", code);
        JSONObject response = post("/v1/email/verify", request, false);
        if (!Boolean.TRUE.equals(response.opt("verified")) || !email.equals(AppDatabaseHelper.normalizeEmail(response.optString("email")))) {
            throw new IOException("Unexpected verification response");
        }
    }

    private JSONObject post(String path, JSONObject request, boolean retryConnection)
            throws IOException, JSONException {
        URL base = new URL(baseUrl);
        // The anonymous Apps Script deployment accepts POSTs at /exec with a query route.
        URL url = new URL(baseUrl + (isAppsScriptUrl(base) ? "?route=" + path.substring(1) : path));
        byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = openPostConnection(url, body.length);
        try {
            try {
                connection.connect();
            } catch (ConnectException | SocketTimeoutException error) {
                if (!retryConnection) throw error;
                // The POST body has not been sent, so this retry cannot send another code.
                connection.disconnect();
                SystemClock.sleep(1000);
                connection = openPostConnection(url, body.length);
                connection.connect();
            }
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }
            int status = connection.getResponseCode();
            if (status == 302 || status == 303) {
                URL responseUrl = appsScriptResponseUrl(connection.getURL(), connection.getHeaderField("Location"));
                return fetchAppsScriptResponse(responseUrl);
            }
            return readResponse(connection, status);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection openPostConnection(URL url, int contentLength) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(45000);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(contentLength);
        return connection;
    }

    private static boolean isAppsScriptUrl(URL url) {
        return "https".equals(url.getProtocol()) && "script.google.com".equals(url.getHost())
                && url.getPath().matches("/macros/s/[^/]+/exec");
    }

    static URL appsScriptResponseUrl(URL request, String location) throws IOException {
        if (!isAppsScriptUrl(request) || location == null) {
            throw new IOException("Unexpected email server redirect");
        }
        URL response = new URL(request, location);
        if (!"https".equals(response.getProtocol()) || !"script.googleusercontent.com".equals(response.getHost())
                || response.getUserInfo() != null || (response.getPort() != -1 && response.getPort() != 443)) {
            throw new IOException("Unexpected Apps Script response URL");
        }
        return response;
    }

    private static JSONObject fetchAppsScriptResponse(URL url) throws IOException, JSONException {
        HttpURLConnection response = (HttpURLConnection) url.openConnection();
        try {
            // Content Service provides the result at a one-time URL retrieved with GET.
            response.setInstanceFollowRedirects(false);
            response.setConnectTimeout(8000);
            response.setReadTimeout(20000);
            response.setRequestProperty("Accept", "application/json");
            return readResponse(response, response.getResponseCode());
        } finally {
            response.disconnect();
        }
    }

    private static JSONObject readResponse(HttpURLConnection connection, int status) throws IOException, JSONException {
        JSONObject result = readJson(status == 200 ? connection.getInputStream() : connection.getErrorStream());
        if (status != 200 || result.has("error")) {
            throw new RequestException(result.optString("error"), retryAfter(result));
        }
        return result;
    }

    private static JSONObject readJson(InputStream stream) throws IOException, JSONException {
        if (stream == null) throw new IOException("Missing verification response");
        try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > 4096) throw new IOException("Verification response too large");
                output.write(buffer, 0, count);
            }
            return new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private static int retryAfter(JSONObject response) {
        return Math.max(0, Math.min(response.optInt("retryAfter"), 3600));
    }

    static final class Challenge {
        final String id;
        final int retryAfter;
        final int expiresIn;

        Challenge(String id, int retryAfter, int expiresIn) {
            this.id = id;
            this.retryAfter = retryAfter;
            this.expiresIn = expiresIn;
        }
    }

    static final class RequestException extends IOException {
        final String reason;
        final int retryAfter;

        RequestException(String reason, int retryAfter) {
            super(reason);
            this.reason = reason;
            this.retryAfter = retryAfter;
        }
    }
}
