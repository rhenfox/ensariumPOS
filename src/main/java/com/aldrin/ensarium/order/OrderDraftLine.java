package com.aldrin.ensarium.order;

import java.math.BigDecimal;

public class OrderDraftLine {

    private long productId;
    private String categoryName;
    private String sku;
    private String productName;
    private String barcode;
    private String uomCode;
    private BigDecimal onhandQty = BigDecimal.ZERO;
    private BigDecimal qtySold30Days = BigDecimal.ZERO;
    private BigDecimal qtyToOrder = BigDecimal.ONE;
    private BigDecimal buyingPrice = BigDecimal.ZERO;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private String expirySummary;
    private String notes;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public BigDecimal getOnhandQty() {
        return onhandQty;
    }

    public void setOnhandQty(BigDecimal onhandQty) {
        this.onhandQty = onhandQty;
    }

    public BigDecimal getQtySold30Days() {
        return qtySold30Days;
    }

    public void setQtySold30Days(BigDecimal qtySold30Days) {
        this.qtySold30Days = qtySold30Days;
    }

    public BigDecimal getQtyToOrder() {
        return qtyToOrder;
    }

    public void setQtyToOrder(BigDecimal qtyToOrder) {
        this.qtyToOrder = qtyToOrder;
    }

    public BigDecimal getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(BigDecimal buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getExpirySummary() {
        return expirySummary;
    }

    public void setExpirySummary(String expirySummary) {
        this.expirySummary = expirySummary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    private String expiryDisplayHtml;

    public String getExpiryDisplayHtml() {
        return expiryDisplayHtml == null ? "" : expiryDisplayHtml;
    }

    public void setExpiryDisplayHtml(String expiryDisplayHtml) {
        this.expiryDisplayHtml = expiryDisplayHtml;
    }
}
