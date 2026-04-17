package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;

public class SaleDetailLine {
    public int lineNo;
    public String productName;
    public String sku;
    public String barcode;
    public BigDecimal qty = BigDecimal.ZERO;
    public BigDecimal unitPrice = BigDecimal.ZERO;
    public BigDecimal lineTotal = BigDecimal.ZERO;
}
