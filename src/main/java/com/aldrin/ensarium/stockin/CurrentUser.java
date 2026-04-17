package com.aldrin.ensarium.stockin;

public class CurrentUser {

    private final Long id;
    private final String username;
    private final String fullName;

    public CurrentUser(Long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isResolved() {
        return id != null || hasText(username) || hasText(fullName);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
