package com.aldrin.ensarium.security;

public final class AccessRules {
    private AccessRules() {}

    public static boolean canAccess(Session session, String pageKey) {
        if ("LOGOUT".equals(pageKey)) return true;
        if (session == null || pageKey == null || pageKey.isBlank()) return false;
        return session.has(pageKey);
    }
}
