package com.aldrin.ensarium.model;

public record BarcodeRow(long id, long productId, String productName, String barcode, boolean primary, boolean active) {}
