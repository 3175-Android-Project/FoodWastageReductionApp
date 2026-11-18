package com.project.foodwastagereductionapp.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

final class ResetTokens {
    private ResetTokens() { }
    static String create() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return hex(bytes);
    }
    static String hash(String token) {
        if (token == null || !token.matches("[0-9a-f]{64}")) return null;
        try {
            return hex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        return result.toString();
    }
}
