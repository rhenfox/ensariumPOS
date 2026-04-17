package com.aldrin.ensarium.model;

import java.math.BigDecimal;

public record ProductRow(
        long id,
        String sku,
        String name,
        Integer categoryId,
        String categoryName,
        int unitId,
        String unitCode,
        BigDecimal buyingPrice,
        BigDecimal sellingPrice,
        Integer taxId,
        String taxCode,
        boolean active
) {}
