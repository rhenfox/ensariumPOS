package com.aldrin.ensarium.dispense;

import java.io.Serializable;

public class ProductBenefitRule implements Serializable {
    public long id;
    public long productId;
    public String benefitType;
    public String benefitMode = "NONE";
    public boolean vatExempt;
    public boolean active;

    public boolean isDiscount20() {
        return "DISCOUNT_20".equalsIgnoreCase(benefitMode);
    }

    public boolean isDiscount5Bnpc() {
        return "DISCOUNT_5_BNPC".equalsIgnoreCase(benefitMode);
    }
}
