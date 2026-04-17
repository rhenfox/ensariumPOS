package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() {}

    public static BigDecimal bd(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public static BigDecimal percentOf(BigDecimal base, BigDecimal rate) {
        return safe(base).multiply(safe(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}
