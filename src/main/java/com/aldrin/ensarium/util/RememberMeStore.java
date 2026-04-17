package com.aldrin.ensarium.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public final class RememberMeStore {
    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".ensarium-rbac-demo");
    private static final Path FILE = DIR.resolve("remember-me.properties");

    private RememberMeStore() {}

    public record SavedLogin(String username, String passwordHash) {}

    public static void save(String username, String passwordHash) {
        try {
            Files.createDirectories(DIR);
            Properties p = new Properties();
            p.setProperty("remember", "true");
            p.setProperty("username", username == null ? "" : username);
            p.setProperty("passwordHash", passwordHash == null ? "" : passwordHash);
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Ensarium remember me");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save remember-me settings", ex);
        }
    }

    public static Optional<SavedLogin> load() {
        if (!Files.exists(FILE)) {
            return Optional.empty();
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            if (!Boolean.parseBoolean(p.getProperty("remember", "false"))) {
                return Optional.empty();
            }
            String username = p.getProperty("username", "").trim();
            String passwordHash = p.getProperty("passwordHash", "").trim();
            if (username.isEmpty() || passwordHash.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new SavedLogin(username, passwordHash));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to clear remember-me settings", ex);
        }
    }
}
