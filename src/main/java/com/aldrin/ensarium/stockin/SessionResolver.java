package com.aldrin.ensarium.stockin;

import com.aldrin.ensarium.db.AppConfig;
import com.aldrin.ensarium.security.Session;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class SessionResolver {

    private static final String[] ID_NAMES = {"getUserId", "userId", "getId", "id"};
    private static final String[] USERNAME_NAMES = {"getUsername", "username", "getUserName", "userName"};
    private static final String[] FULLNAME_NAMES = {"getFullName", "fullName", "getName", "name"};

    private SessionResolver() {
    }

    public static CurrentUser resolveCurrentUser() {
        CurrentUser reflected = resolveFromExistingSessionClass();
        if (reflected != null && reflected.isResolved()) {
            return reflected;
        }
        return resolveFromConfig();
    }

    private static CurrentUser resolveFromExistingSessionClass() {
        try {
            Class<?> sessionClass = Class.forName("com.aldrin.ensarium.security.Session");

            Object idValue = readStaticValue(sessionClass, ID_NAMES);
            Object usernameValue = readStaticValue(sessionClass, USERNAME_NAMES);
            Object fullNameValue = readStaticValue(sessionClass, FULLNAME_NAMES);

            if (idValue == null && usernameValue == null && fullNameValue == null) {
                Object userObject = readStaticValue(sessionClass, new String[]{"getUser", "user", "getCurrentUser", "currentUser"});
                if (userObject != null) {
                    idValue = readInstanceValue(userObject, ID_NAMES);
                    usernameValue = readInstanceValue(userObject, USERNAME_NAMES);
                    fullNameValue = readInstanceValue(userObject, FULLNAME_NAMES);
                }
            }

            Long id = toLong(idValue);
            String username = trimToNull(usernameValue);
            String fullName = trimToNull(fullNameValue);
            CurrentUser user = new CurrentUser(id, username, fullName);
            return user.isResolved() ? user : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static CurrentUser resolveFromConfig() {

//        Long id = toLong(AppConfig.get("session.user.id", AppConfig.get("current.user.id", "")));
//        String username = trimToNull(AppConfig.get("session.username", AppConfig.get("current.user.username", "")));
//        String fullName = trimToNull(AppConfig.get("session.full_name", AppConfig.get("current.user.full_name", "")));
        Session session =Session.session;
        Long id = Long.valueOf(Session.session.getUserId());
        String username = Session.session.getUsername();
        String fullname= Session.session.getFullName();
        return new CurrentUser(id, username, fullname);
    }

    private static Object readStaticValue(Class<?> type, String[] names) {
        for (String name : names) {
            Object value = invokeStaticMethod(type, name);
            if (value != null) {
                return value;
            }
            value = readStaticField(type, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object readInstanceValue(Object target, String[] names) {
        Class<?> type = target.getClass();
        for (String name : names) {
            Object value = invokeInstanceMethod(type, target, name);
            if (value != null) {
                return value;
            }
            value = readInstanceField(type, target, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object invokeStaticMethod(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0) {
                method.setAccessible(true);
                return method.invoke(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Object invokeInstanceMethod(Class<?> type, Object target, String name) {
        try {
            Method method = type.getMethod(name);
            if (method.getParameterCount() == 0) {
                method.setAccessible(true);
                return method.invoke(target);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Object readStaticField(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                return field.get(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Object readInstanceField(Class<?> type, Object target, String name) {
        try {
            Field field = type.getField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            String text = value.toString();
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return Long.valueOf(text.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String trimToNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
