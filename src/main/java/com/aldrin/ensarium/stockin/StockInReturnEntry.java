package com.aldrin.ensarium.stockin;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StockInReturnEntry {

    private Long receiptLineId;
    private Long productId;
    private Long lotId;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitCost = BigDecimal.ZERO;

    public Long getReceiptLineId() {
        return receiptLineId;
    }

    public void setReceiptLineId(Long receiptLineId) {
        this.receiptLineId = receiptLineId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public BigDecimal getQuantity() {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost == null ? BigDecimal.ZERO : unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return getQuantity().multiply(getUnitCost()).setScale(4, RoundingMode.HALF_UP);
    }
}
