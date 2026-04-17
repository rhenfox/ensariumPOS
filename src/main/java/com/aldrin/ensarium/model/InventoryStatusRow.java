package com.aldrin.ensarium.model;

public record InventoryStatusRow(
        String code,
        String name,
        boolean sellable
) {
}
