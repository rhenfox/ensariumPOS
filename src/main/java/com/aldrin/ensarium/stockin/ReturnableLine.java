package com.aldrin.ensarium.stockin;

import java.math.BigDecimal;
import java.util.Date;

public class ReturnableLine {

    private Long receiptLineId;
    private Long productId;
    private Long lotId;
    private String sku;
    private String productName;
    private String lotNo;
    private Date expiryDate;
    private BigDecimal purchasedQty = BigDecimal.ZERO;
    private BigDecimal returnedQty = BigDecimal.ZERO;
    private BigDecimal availableQty = BigDecimal.ZERO;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getPurchasedQty() {
        return purchasedQty;
    }

    public void setPurchasedQty(BigDecimal purchasedQty) {
        this.purchasedQty = purchasedQty;
    }

    public BigDecimal getReturnedQty() {
        return returnedQty;
    }

    public void setReturnedQty(BigDecimal returnedQty) {
        this.returnedQty = returnedQty;
    }

    public BigDecimal getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty) {
        this.availableQty = availableQty;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
