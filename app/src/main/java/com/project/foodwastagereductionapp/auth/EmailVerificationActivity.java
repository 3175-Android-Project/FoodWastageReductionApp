package com.project.foodwastagereductionapp.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.ForegroundColorSpan;

import com.project.foodwastagereductionapp.BuildConfig;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppDatabaseHelper;
import com.project.foodwastagereductionapp.data.User;

import java.net.SocketTimeoutException;

public class EmailVerificationActivity extends AppCompatActivity {
    private static final String STATE_SEND_STARTED = "sendStarted";

    private Button verifyButton;
    private Button sendButton;
    private EditText codeInput;
    private TextView message;
    private SharedPreferences pendingCode;
    private String verificationEmail;
    private boolean sending;
    private String challengeId;
    private long resendAt;
    private boolean busy;
    private boolean sendStarted;
    private final Runnable refreshControls = this::updateControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendStarted = savedInstanceState != null && savedInstanceState.getBoolean(STATE_SEND_STARTED);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            return insets;
        });

        long userId = getIntent().getLongExtra(AuthNavigator.EXTRA_USER_ID, -1);
        AppDatabaseHelper db = AppDatabaseHelper.getInstance(this);
        verifyButton = findViewById(R.id.btnVerificationContinue);
        sendButton = findViewById(R.id.btnResendVerification);
        codeInput = findViewById(R.id.editVerificationCode);
        message = findViewById(R.id.txtVerificationMessage);
        verifyButton.setEnabled(false);
        sendButton.setEnabled(false);

        AuthTasks.run(this, null, () -> db.getUser(userId), user -> {
            if (user == null) {
                AuthNavigator.logout(this);
            } else if (user.emailVerified) {
                continueAfterVerification(user);
            } else {
                showEmailCodeForm(db, user);
            }
        });
    }

    private void showEmailCodeForm(AppDatabaseHelper db, User user) {
        ((TextView) findViewById(R.id.txtVerificationTitle)).setText(R.string.emailCodeTitle);
        verifyButton.setText(R.string.btnVerifyEmailCode);
        sendButton.setText(R.string.btnSendEmailCode);
        if (BuildConfig.EMAIL_VERIFICATION_URL.isEmpty()) {
            message.setText(R.string.msgEmailServerUnconfigured);
            return;
        }
        EmailVerificationClient client = new EmailVerificationClient(BuildConfig.EMAIL_VERIFICATION_URL);
        verificationEmail = user.email;
        pendingCode = getSharedPreferences("grabbit_pending_email_code", MODE_PRIVATE);
        long now = SystemClock.elapsedRealtime();
        if (BuildConfig.EMAIL_VERIFICATION_URL.equals(pendingCode.getString("server", null))
                && user.email.equals(pendingCode.getString("email", null))
                && now >= pendingCode.getLong("issuedAt", Long.MAX_VALUE)
                && now < pendingCode.getLong("expiresAt", 0)) {
            challengeId = pendingCode.getString("challengeId", null);
            resendAt = pendingCode.getLong("resendAt", 0);
        }
        showCodeMessage();
        codeInput.setVisibility(View.VISIBLE);
        sendButton.setOnClickListener(view -> requestCode(client));
        verifyButton.setOnClickListener(view -> {
            String code = codeInput.getText().toString().trim();
            if (!code.matches("[0-9]{6}")) {
                codeInput.setError(getString(R.string.msgEnterEmailCode));
                return;
            }
            String id = challengeId;
            busy = true;
            updateControls();
            AuthTasks.run(this, null, () -> {
                client.verifyCode(user.email, id, code);
                User confirmed = db.confirmEmailLocally(user.id);
                pendingCode.edit().clear().apply();
                return confirmed;
            }, this::continueAfterVerification, this::showRequestError);
        });
        updateControls();
        if (!sendStarted && challengeId == null) requestCode(client);
    }

    private void requestCode(EmailVerificationClient client) {
        sendStarted = true;
        sending = true;
        busy = true;
        updateControls();
        showCodeMessage();
        AuthTasks.run(this, null, () -> {
            EmailVerificationClient.Challenge challenge = client.requestCode(verificationEmail);
            long received = SystemClock.elapsedRealtime();
            long nextSend = received + challenge.retryAfter * 1000L;
            // Store the challenge before the UI callback so it survives a rotation during delivery.
            pendingCode.edit().putString("server", BuildConfig.EMAIL_VERIFICATION_URL)
                    .putString("email", verificationEmail)
                    .putString("challengeId", challenge.id)
                    .putLong("issuedAt", received)
                    .putLong("expiresAt", received + challenge.expiresIn * 1000L)
                    .putLong("resendAt", nextSend).apply();
            return challenge.id;
        }, id -> {
            challengeId = id;
            resendAt = pendingCode.getLong("resendAt", 0);
            codeInput.setText("");
            codeInput.setError(null);
            sending = false;
            busy = false;
            showCodeMessage();
            updateControls();
        }, this::showRequestError);
    }

    private void showCodeMessage() {

        int text = R.string.emailCodePrompt;

        if (sending) {
            text = R.string.emailCodeSending;

        } else if (challengeId != null) {
            text = R.string.emailCodeSent;
        }

        String fullText =
                getString(
                        text,
                        verificationEmail
                );

        SpannableStringBuilder spannable =
                new SpannableStringBuilder(fullText);

        if (verificationEmail != null
                && !verificationEmail.isEmpty()) {

            int start =
                    fullText.indexOf(
                            verificationEmail
                    );

            if (start >= 0) {

                int end =
                        start + verificationEmail.length();

                // Bold
                spannable.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                // Black color
                spannable.setSpan(
                        new ForegroundColorSpan(
                                getColor(R.color.text_primary)
                        ),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        message.setText(spannable);
    }

    private void updateControls() {
        sendButton.removeCallbacks(refreshControls);
        long remaining = resendAt - SystemClock.elapsedRealtime();
        if (sending) {
            sendButton.setText(R.string.btnSendingEmailCode);
        } else if (remaining > 0) {
            sendButton.setText(getString(R.string.btnEmailCodeWait, (remaining + 999) / 1000));
            sendButton.postDelayed(refreshControls, Math.min(remaining, 1000));
        } else {
            sendButton.setText(challengeId == null ? R.string.btnSendEmailCode : R.string.btnResendEmailCode);
        }
        sendButton.setEnabled(!busy && remaining <= 0);
        verifyButton.setEnabled(!busy && challengeId != null);
        codeInput.setEnabled(!busy && challengeId != null);
    }

    private void showRequestError(Exception error) {
        sending = false;
        busy = false;
        Log.w("GrabbitEmail", "Verification request failed: " + error.getClass().getSimpleName());
        int messageId = error instanceof SocketTimeoutException
                ? R.string.msgEmailServerTimedOut : R.string.msgEmailServerUnavailable;
        if (error instanceof EmailVerificationClient.RequestException) {
            EmailVerificationClient.RequestException failure = (EmailVerificationClient.RequestException) error;
            if (failure.retryAfter > 0) {
                resendAt = SystemClock.elapsedRealtime() + failure.retryAfter * 1000L;
            }
            switch (failure.reason) {
                case "invalid_code":
                    updateControls();
                    codeInput.setError(getString(R.string.msgIncorrectEmailCode));
                    return;
                case "attempts_exhausted":
                case "expired_code":
                case "invalid_challenge":
                    challengeId = null;
                    pendingCode.edit().clear().apply();
                    messageId = R.string.msgRequestNewEmailCode;
                    break;
                case "certificate_error":
                    messageId = R.string.msgEmailCertificateError;
                    break;
                case "delivery_failed":
                    messageId = R.string.msgEmailDeliveryFailed;
                    break;
                case "too_many_requests":
                    messageId = R.string.msgEmailCodeWait;
                    break;
                case "invalid_email":
                    messageId = R.string.msgInvalidEmail;
                    break;
                default:
                    break;
            }
        }
        showCodeMessage();
        updateControls();
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    private void continueAfterVerification(User user) {
        if (User.MANAGER.equals(user.role)) {
            new SessionManager(this).start(user.id, false);
            AuthNavigator.open(this, RestaurantInformationActivity.class, user.id);
        } else {
            new SessionManager(this).clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("email", user.email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean(STATE_SEND_STARTED, sendStarted);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        sendButton.removeCallbacks(refreshControls);
        super.onDestroy();
    }
}
