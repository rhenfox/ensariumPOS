package com.aldrin.ensarium.benefit;

import java.time.LocalDate;

public class ProductBenefitRuleRow {

    private final long id;
    private final long productId;
    private final String productSku;
    private final String productName;
    private final String benefitType;
    private final String benefitMode;
    private final boolean vatExempt;
    private final boolean active;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    public ProductBenefitRuleRow(long id, long productId, String productSku, String productName,
            String benefitType, String benefitMode, boolean vatExempt, boolean active,
            LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.benefitType = benefitType;
        this.benefitMode = benefitMode;
        this.vatExempt = vatExempt;
        this.active = active;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getProductName() {
        return productName;
    }

    public String getBenefitType() {
        return benefitType;
    }

    public String getBenefitMode() {
        return benefitMode;
    }

    public boolean isVatExempt() {
        return vatExempt;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
}
