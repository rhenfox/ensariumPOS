package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;

public class ReturnLineInput {
    public long saleLineId;
    public long productId;
    public Long lotId;
    public String productName;
    public String uomCode;
    public BigDecimal soldQty = BigDecimal.ZERO;
    public BigDecimal returnQty = BigDecimal.ZERO;
    public BigDecimal unitPrice = BigDecimal.ZERO;
    public BigDecimal taxAmount = BigDecimal.ZERO;
    public BigDecimal discountAmount = BigDecimal.ZERO;
    public BigDecimal refundableAmount = BigDecimal.ZERO;
    public String saleReference;
    public String invoiceReference;
}
