package com.aldrin.ensarium.txn;

import java.sql.Timestamp;

public class InventoryTxn {
    private long id; private int storeId; private String txnType; private String refNo; private Long saleId; private Long salesReturnId; private Long purchaseReceiptId; private String notes; private Integer createdBy; private String createdByName; private Timestamp createdAt;
    public long getId() { return id; } public void setId(long id) { this.id = id; }
    public int getStoreId() { return storeId; } public void setStoreId(int storeId) { this.storeId = storeId; }
    public String getTxnType() { return txnType; } public void setTxnType(String txnType) { this.txnType = txnType; }
    public String getRefNo() { return refNo; } public void setRefNo(String refNo) { this.refNo = refNo; }
    public Long getSaleId() { return saleId; } public void setSaleId(Long saleId) { this.saleId = saleId; }
    public Long getSalesReturnId() { return salesReturnId; } public void setSalesReturnId(Long salesReturnId) { this.salesReturnId = salesReturnId; }
    public Long getPurchaseReceiptId() { return purchaseReceiptId; } public void setPurchaseReceiptId(Long purchaseReceiptId) { this.purchaseReceiptId = purchaseReceiptId; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Integer getCreatedBy() { return createdBy; } public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public Timestamp getCreatedAt() { return createdAt; } public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
