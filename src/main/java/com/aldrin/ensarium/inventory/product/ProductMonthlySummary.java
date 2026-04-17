package com.aldrin.ensarium.inventory.product;

import java.math.BigDecimal;

public class ProductMonthlySummary {
    private BigDecimal onhandQty = BigDecimal.ZERO;
    private BigDecimal qtySold = BigDecimal.ZERO;
    private BigDecimal qtyReturned = BigDecimal.ZERO;
    private int salesCount;
    private BigDecimal grossSales = BigDecimal.ZERO;
    private BigDecimal discountTotal = BigDecimal.ZERO;
    private BigDecimal taxTotal = BigDecimal.ZERO;
    private BigDecimal netSalesWithTax = BigDecimal.ZERO;
    private BigDecimal netSalesWithoutTax = BigDecimal.ZERO;
    private BigDecimal costTotal = BigDecimal.ZERO;
    private BigDecimal profitWithoutTax = BigDecimal.ZERO;
    private BigDecimal profitWithTax = BigDecimal.ZERO;
    private BigDecimal averageUnitPrice = BigDecimal.ZERO;

    public BigDecimal getOnhandQty() { return onhandQty; }
    public void setOnhandQty(BigDecimal onhandQty) { this.onhandQty = onhandQty; }
    public BigDecimal getQtySold() { return qtySold; }
    public void setQtySold(BigDecimal qtySold) { this.qtySold = qtySold; }
    public BigDecimal getQtyReturned() { return qtyReturned; }
    public void setQtyReturned(BigDecimal qtyReturned) { this.qtyReturned = qtyReturned; }
    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
    public BigDecimal getGrossSales() { return grossSales; }
    public void setGrossSales(BigDecimal grossSales) { this.grossSales = grossSales; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getNetSalesWithTax() { return netSalesWithTax; }
    public void setNetSalesWithTax(BigDecimal netSalesWithTax) { this.netSalesWithTax = netSalesWithTax; }
    public BigDecimal getNetSalesWithoutTax() { return netSalesWithoutTax; }
    public void setNetSalesWithoutTax(BigDecimal netSalesWithoutTax) { this.netSalesWithoutTax = netSalesWithoutTax; }
    public BigDecimal getCostTotal() { return costTotal; }
    public void setCostTotal(BigDecimal costTotal) { this.costTotal = costTotal; }
    public BigDecimal getProfitWithoutTax() { return profitWithoutTax; }
    public void setProfitWithoutTax(BigDecimal profitWithoutTax) { this.profitWithoutTax = profitWithoutTax; }
    public BigDecimal getProfitWithTax() { return profitWithTax; }
    public void setProfitWithTax(BigDecimal profitWithTax) { this.profitWithTax = profitWithTax; }
    public BigDecimal getAverageUnitPrice() { return averageUnitPrice; }
    public void setAverageUnitPrice(BigDecimal averageUnitPrice) { this.averageUnitPrice = averageUnitPrice; }
}
