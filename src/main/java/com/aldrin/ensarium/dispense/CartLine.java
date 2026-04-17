package com.aldrin.ensarium.dispense;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartLine implements Serializable {
    private static final long serialVersionUID = 1L;
    public long productId;
    public Long lotId;
    public String barcode;
    public String sku;
    public String productName;
    public String uomCode;
    public BigDecimal qtyUom = BigDecimal.ONE;
    public BigDecimal unitPrice = BigDecimal.ZERO;
    public BigDecimal buyingPrice = BigDecimal.ZERO;
    public BigDecimal taxRate = BigDecimal.ZERO;
    public boolean priceIncludesTax = true;
    public long taxId;
    public BigDecimal costTotal = BigDecimal.ZERO;
    public String benefitType;
    public String benefitMode = "NONE";
    public boolean benefitVatExempt;
    public BigDecimal benefitDiscountRate = BigDecimal.ZERO;

    public BigDecimal gross() {
        return unitPrice.multiply(qtyUom);
    }

    public CartLine copy() {
        CartLine c = new CartLine();
        c.productId = productId;
        c.lotId = lotId;
        c.barcode = barcode;
        c.sku = sku;
        c.productName = productName;
        c.uomCode = uomCode;
        c.qtyUom = qtyUom;
        c.unitPrice = unitPrice;
        c.buyingPrice = buyingPrice;
        c.taxRate = taxRate;
        c.priceIncludesTax = priceIncludesTax;
        c.taxId = taxId;
        c.costTotal = costTotal;
        c.benefitType = benefitType;
        c.benefitMode = benefitMode;
        c.benefitVatExempt = benefitVatExempt;
        c.benefitDiscountRate = benefitDiscountRate;
        return c;
    }
}
