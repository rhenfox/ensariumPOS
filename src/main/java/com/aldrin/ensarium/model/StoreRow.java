package com.aldrin.ensarium.model;

public record StoreRow(
        int id,
        String code,
        String name,
        String address,
        boolean active
) {}
