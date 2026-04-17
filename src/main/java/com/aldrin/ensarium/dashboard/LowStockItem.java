package com.aldrin.ensarium.dashboard;

import java.math.BigDecimal;

public record LowStockItem(String sku, String name, String statusCode, BigDecimal qty) {
}
