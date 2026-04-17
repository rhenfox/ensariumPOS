package com.aldrin.ensarium.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {

    private static final String PREFIX = "PBKDF2";
    private static final int ITERATIONS = 12000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Passwords() {
    }

    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(valueOrEmpty(rawPassword).toCharArray(), salt, ITERATIONS, HASH_BYTES);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String raw = valueOrEmpty(rawPassword);
        String value = storedHash.trim();

        if (value.startsWith(PREFIX + "$")) {
            String[] parts = value.split("\\$", 4);
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                byte[] actual = pbkdf2(raw.toCharArray(), salt, iterations, expected.length);
                return MessageDigest.isEqual(expected, actual);
            } catch (RuntimeException ex) {
                return false;
            }
        }

        return value.equals(raw)
                || value.equalsIgnoreCase(legacySha256(raw));
    }

    public static boolean isModernHash(String storedHash) {
        return storedHash != null && storedHash.trim().startsWith(PREFIX + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int hashBytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, hashBytes * 8);
            try {
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                return skf.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
                Arrays.fill(password, '\0');
            }
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not hash the password.", ex);
        }
    }

    private static String legacySha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(valueOrEmpty(text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
