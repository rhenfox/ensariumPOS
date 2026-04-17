package com.aldrin.ensarium.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPoint(LocalDate date, BigDecimal amount) {
}
