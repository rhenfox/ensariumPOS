package com.aldrin.ensarium.model;

public record LookupOption(int id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
