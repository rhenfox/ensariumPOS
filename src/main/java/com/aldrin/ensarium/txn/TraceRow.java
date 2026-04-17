package com.aldrin.ensarium.txn;

import java.math.BigDecimal;

public class TraceRow {
    private String documentNo; private long productId; private String productName; private String lotNo; private String fromStatus; private String toStatus; private BigDecimal beforeQty; private BigDecimal txnQty; private BigDecimal afterQty; private BigDecimal qty; private BigDecimal unitCost; private BigDecimal totalCost; private BigDecimal unitPrice; private BigDecimal lineGross; private BigDecimal taxAmount; private BigDecimal discountAmount; private BigDecimal netAmount; private BigDecimal buyingPrice; private BigDecimal costSnapshot; private String traceNotes;
    public String getDocumentNo() { return documentNo; } public void setDocumentNo(String documentNo) { this.documentNo = documentNo; }
    public long getProductId() { return productId; } public void setProductId(long productId) { this.productId = productId; }
    public String getProductName() { return productName; } public void setProductName(String productName) { this.productName = productName; }
    public String getLotNo() { return lotNo; } public void setLotNo(String lotNo) { this.lotNo = lotNo; }
    public String getFromStatus() { return fromStatus; } public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; } public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public BigDecimal getBeforeQty() { return beforeQty; } public void setBeforeQty(BigDecimal beforeQty) { this.beforeQty = beforeQty; }
    public BigDecimal getTxnQty() { return txnQty; } public void setTxnQty(BigDecimal txnQty) { this.txnQty = txnQty; }
    public BigDecimal getAfterQty() { return afterQty; } public void setAfterQty(BigDecimal afterQty) { this.afterQty = afterQty; }
    public BigDecimal getQty() { return qty; } public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getUnitCost() { return unitCost; } public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalCost() { return totalCost; } public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getUnitPrice() { return unitPrice; } public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineGross() { return lineGross; } public void setLineGross(BigDecimal lineGross) { this.lineGross = lineGross; }
    public BigDecimal getTaxAmount() { return taxAmount; } public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; } public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getNetAmount() { return netAmount; } public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getBuyingPrice() { return buyingPrice; } public void setBuyingPrice(BigDecimal buyingPrice) { this.buyingPrice = buyingPrice; }
    public BigDecimal getCostSnapshot() { return costSnapshot; } public void setCostSnapshot(BigDecimal costSnapshot) { this.costSnapshot = costSnapshot; }
    public String getTraceNotes() { return traceNotes; } public void setTraceNotes(String traceNotes) { this.traceNotes = traceNotes; }
}
