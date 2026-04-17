package com.aldrin.ensarium.model;

public record PaymentMethodRow(
        int id,
        String code,
        String name,
        boolean active
) {
}
