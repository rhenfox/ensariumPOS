package com.aldrin.ensarium.model;

public record TerminalRow(
        int id,
        int storeId,
        String storeCode,
        String storeName,
        String code,
        String name,
        boolean active
) {}
