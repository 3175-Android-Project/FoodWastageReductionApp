package com.project.foodwastagereductionapp.data;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local coursework credentials. PBKDF2-HMAC-SHA1 is available on every minSdk 24 device. */
public final class PasswordHasher {
    private static final int ITERATIONS = 120000;
    private static final String PREFIX = "pbkdf2_sha1";
    private PasswordHasher() { }

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return PREFIX + "$" + ITERATIONS + "$" + hex(salt) + "$"
                + hex(derive(password, salt, ITERATIONS));
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || password.isEmpty() || encoded == null) return false;
        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !PREFIX.equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 10000 || iterations > 1000000) return false;
            byte[] salt = unhex(parts[2]);
            byte[] expected = unhex(parts[3]);
            return salt.length == 16 && expected.length == 32
                    && MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(chars, '\0');
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(Character.forDigit((b & 255) >>> 4, 16));
            result.append(Character.forDigit(b & 15, 16));
        }
        return result.toString();
    }

    private static byte[] unhex(String value) {
        if (value.length() % 2 != 0) throw new IllegalArgumentException("Invalid hex");
        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int high = Character.digit(value.charAt(i * 2), 16);
            int low = Character.digit(value.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) throw new IllegalArgumentException("Invalid hex");
            bytes[i] = (byte) ((high << 4) | low);
        }
        return bytes;
    }
}
