package com.aldrin.ensarium.dispense;

import java.io.Serializable;
import java.math.BigDecimal;

public class SaleDiscountInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String KIND_PERCENT = "PERCENT";
    public static final String KIND_AMOUNT = "AMOUNT";

    public Integer discountTypeId;
    public String discountCode;
    public String appliesTo;
    public String name = "Manual Discount";
    public String kind = KIND_AMOUNT;
    public BigDecimal value = BigDecimal.ZERO;

    public boolean isPercent() {
        return KIND_PERCENT.equalsIgnoreCase(kind);
    }

    public SaleDiscountInfo copy() {
        SaleDiscountInfo d = new SaleDiscountInfo();
        d.discountTypeId = discountTypeId;
        d.discountCode = discountCode;
        d.appliesTo = appliesTo;
        d.name = name;
        d.kind = kind;
        d.value = value;
        return d;
    }
}
