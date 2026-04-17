package com.aldrin.ensarium.inventory.product;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProductMonthlySalesRow {
    private String saleNo;
    private String invoiceNo;
    private String customerName;
    private Timestamp soldAt;
    private BigDecimal qtySold;
    private BigDecimal unitPrice;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmountWithTax;
    private BigDecimal costAmount;
    private BigDecimal profitWithoutTax;
    private BigDecimal profitWithTax;

    public String getSaleNo() { return saleNo; }
    public void setSaleNo(String saleNo) { this.saleNo = saleNo; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Timestamp getSoldAt() { return soldAt; }
    public void setSoldAt(Timestamp soldAt) { this.soldAt = soldAt; }
    public BigDecimal getQtySold() { return qtySold; }
    public void setQtySold(BigDecimal qtySold) { this.qtySold = qtySold; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getNetAmountWithTax() { return netAmountWithTax; }
    public void setNetAmountWithTax(BigDecimal netAmountWithTax) { this.netAmountWithTax = netAmountWithTax; }
    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }
    public BigDecimal getProfitWithoutTax() { return profitWithoutTax; }
    public void setProfitWithoutTax(BigDecimal profitWithoutTax) { this.profitWithoutTax = profitWithoutTax; }
    public BigDecimal getProfitWithTax() { return profitWithTax; }
    public void setProfitWithTax(BigDecimal profitWithTax) { this.profitWithTax = profitWithTax; }
}
