package com.aldrin.ensarium.model;

public record DiscountTypeRow(
        int id,
        String code,
        String name,
        String kind,
        String appliesTo,
        boolean active
) {
}
