package com.aldrin.ensarium.txn;

import java.math.BigDecimal;

public class InventoryTxnLine {
    private long id; private long productId; private String sku; private String productName; private String lotNo; private String fromStatus; private String toStatus; private BigDecimal qtyInBase; private BigDecimal unitCost; private BigDecimal totalCost;
    public long getId() { return id; } public void setId(long id) { this.id = id; }
    public long getProductId() { return productId; } public void setProductId(long productId) { this.productId = productId; }
    public String getSku() { return sku; } public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; } public void setProductName(String productName) { this.productName = productName; }
    public String getLotNo() { return lotNo; } public void setLotNo(String lotNo) { this.lotNo = lotNo; }
    public String getFromStatus() { return fromStatus; } public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; } public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public BigDecimal getQtyInBase() { return qtyInBase; } public void setQtyInBase(BigDecimal qtyInBase) { this.qtyInBase = qtyInBase; }
    public BigDecimal getUnitCost() { return unitCost; } public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalCost() { return totalCost; } public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}
