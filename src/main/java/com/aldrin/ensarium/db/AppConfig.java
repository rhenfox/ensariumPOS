package com.aldrin.ensarium.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {}

    public static String get(String key, String def) {
        String v = PROPS.getProperty(key);
        return v == null ? def : v.trim();
    }

    public static int getInt(String key, int def) {
        try {
            return Integer.parseInt(get(key, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean getBool(String key, boolean def) {
        String v = get(key, String.valueOf(def));
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }
}
