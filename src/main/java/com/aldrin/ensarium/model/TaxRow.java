package com.aldrin.ensarium.model;

import java.math.BigDecimal;

public record TaxRow(int id, String code, String name, BigDecimal rate, boolean active) {}
