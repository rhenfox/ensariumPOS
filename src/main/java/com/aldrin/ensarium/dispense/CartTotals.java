package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;

public class CartTotals {
    public BigDecimal grossSales = BigDecimal.ZERO;
    public BigDecimal subtotal = BigDecimal.ZERO;
    public BigDecimal lineDiscounts = BigDecimal.ZERO;
    public BigDecimal saleDiscount = BigDecimal.ZERO;
    public BigDecimal tax = BigDecimal.ZERO;
    public BigDecimal vatableSales = BigDecimal.ZERO;
    public BigDecimal vatExemptSales = BigDecimal.ZERO;
    public BigDecimal zeroRatedSales = BigDecimal.ZERO;
    public BigDecimal withholdingTaxAmount = BigDecimal.ZERO;
    public BigDecimal benefitEligibleAmount = BigDecimal.ZERO;
    public BigDecimal benefitVatExemptAmount = BigDecimal.ZERO;
    public BigDecimal benefitDiscountAmount = BigDecimal.ZERO;
    public BigDecimal grandTotal = BigDecimal.ZERO;
}
