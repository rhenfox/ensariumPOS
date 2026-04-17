package com.aldrin.ensarium.dispense;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductOption implements Serializable {
    public long productId;
    public String barcode;
    public String sku;
    public String productName;
    public String uomCode;
    public BigDecimal unitPrice = BigDecimal.ZERO;
    public BigDecimal buyingPrice = BigDecimal.ZERO;
    public BigDecimal taxRate = BigDecimal.ZERO;
    public boolean priceIncludesTax = true;
    public long taxId = 0;
    public BigDecimal onHandQty = BigDecimal.ZERO;

    @Override
    public String toString() {
        return productName + " [" + sku + "]";
    }
}
