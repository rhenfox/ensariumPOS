package com.aldrin.ensarium.inventory.product;

import java.math.BigDecimal;

public class ProductInventoryItem {

    private long productId;
    private String sku;
    private String productName;
    private String categoryName;
    private String barcodes;
    private String uomCode;
    private String expirySummary;
    private String expiryDisplayHtml;
    private BigDecimal onhandQty;
    private BigDecimal buyingPrice;
    private BigDecimal sellingPrice;
    private BigDecimal taxRate;
    private boolean priceIncludesTax;
    private BigDecimal qtySoldAllTime;
    private BigDecimal qtySold30Days;
    private BigDecimal discountPerUnit;
    private Integer daysToExpire;
    private String daysToExpireDisplay;
    private String daysToExpireDisplayHtml;
    private BigDecimal profitWithoutTaxWithoutDiscount;
    private BigDecimal profitWithTaxAndDiscount;
    private BigDecimal markupWithoutTaxWithoutDiscount;
    private BigDecimal markupWithTaxAndDiscount;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(String barcodes) {
        this.barcodes = barcodes;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getExpirySummary() {
        return expirySummary;
    }

    public void setExpirySummary(String expirySummary) {
        this.expirySummary = expirySummary;
    }

    public String getExpiryDisplayHtml() {
        return expiryDisplayHtml;
    }

    public void setExpiryDisplayHtml(String expiryDisplayHtml) {
        this.expiryDisplayHtml = expiryDisplayHtml;
    }

    public BigDecimal getOnhandQty() {
        return onhandQty;
    }

    public void setOnhandQty(BigDecimal onhandQty) {
        this.onhandQty = onhandQty;
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

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public boolean isPriceIncludesTax() {
        return priceIncludesTax;
    }

    public void setPriceIncludesTax(boolean priceIncludesTax) {
        this.priceIncludesTax = priceIncludesTax;
    }

    public BigDecimal getQtySoldAllTime() {
        return qtySoldAllTime;
    }

    public void setQtySoldAllTime(BigDecimal qtySoldAllTime) {
        this.qtySoldAllTime = qtySoldAllTime;
    }

    public BigDecimal getQtySold30Days() {
        return qtySold30Days;
    }

    public void setQtySold30Days(BigDecimal qtySold30Days) {
        this.qtySold30Days = qtySold30Days;
    }

    public BigDecimal getDiscountPerUnit() {
        return discountPerUnit;
    }

    public void setDiscountPerUnit(BigDecimal discountPerUnit) {
        this.discountPerUnit = discountPerUnit;
    }

    public Integer getDaysToExpire() {
        return daysToExpire;
    }

    public void setDaysToExpire(Integer daysToExpire) {
        this.daysToExpire = daysToExpire;
    }

    public String getDaysToExpireDisplay() {
        return daysToExpireDisplay;
    }

    public void setDaysToExpireDisplay(String daysToExpireDisplay) {
        this.daysToExpireDisplay = daysToExpireDisplay;
    }

    public String getDaysToExpireDisplayHtml() {
        return daysToExpireDisplayHtml;
    }

    public void setDaysToExpireDisplayHtml(String daysToExpireDisplayHtml) {
        this.daysToExpireDisplayHtml = daysToExpireDisplayHtml;
    }

    public BigDecimal getProfitWithoutTaxWithoutDiscount() {
        return profitWithoutTaxWithoutDiscount;
    }

    public void setProfitWithoutTaxWithoutDiscount(BigDecimal profitWithoutTaxWithoutDiscount) {
        this.profitWithoutTaxWithoutDiscount = profitWithoutTaxWithoutDiscount;
    }

    public BigDecimal getProfitWithTaxAndDiscount() {
        return profitWithTaxAndDiscount;
    }

    public void setProfitWithTaxAndDiscount(BigDecimal profitWithTaxAndDiscount) {
        this.profitWithTaxAndDiscount = profitWithTaxAndDiscount;
    }

    public BigDecimal getMarkupWithoutTaxWithoutDiscount() {
        return markupWithoutTaxWithoutDiscount;
    }

    public void setMarkupWithoutTaxWithoutDiscount(BigDecimal markupWithoutTaxWithoutDiscount) {
        this.markupWithoutTaxWithoutDiscount = markupWithoutTaxWithoutDiscount;
    }

    public BigDecimal getMarkupWithTaxAndDiscount() {
        return markupWithTaxAndDiscount;
    }

    public void setMarkupWithTaxAndDiscount(BigDecimal markupWithTaxAndDiscount) {
        this.markupWithTaxAndDiscount = markupWithTaxAndDiscount;
    }
}
