package com.aldrin.ensarium.model;

public record RoleRow(int id, String name, String description) {
    @Override
    public String toString() {
        return name;
    }
}
