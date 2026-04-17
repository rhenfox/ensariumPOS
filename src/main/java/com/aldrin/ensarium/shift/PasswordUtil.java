package com.aldrin.ensarium.shift;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static boolean matches(String rawPassword, String storedHashOrPlain) {
        if (storedHashOrPlain == null) {
            return false;
        }
        String raw = rawPassword == null ? "" : rawPassword;
        return storedHashOrPlain.equals(raw) || storedHashOrPlain.equalsIgnoreCase(sha256(raw));
    }

    public static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash password", ex);
        }
    }
}
