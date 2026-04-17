package com.aldrin.ensarium.dashboard;

public record StoreOption(Integer id, String code, String name) {
    @Override
    public String toString() {
        if (id == null) {
            return name;
        }
        return code + " - " + name;
    }
}
