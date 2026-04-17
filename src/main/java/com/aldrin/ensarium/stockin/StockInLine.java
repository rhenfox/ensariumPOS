package com.aldrin.ensarium.stockin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class StockInLine {

    private Long lineId;
    private ProductOption product;
    private Long lotId;
    private String lotNo;
    private Date expiryDate;
    private LookupOption unit;
    private BigDecimal quantityInBase = BigDecimal.ZERO;
    private BigDecimal unitCost = BigDecimal.ZERO;

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public ProductOption getProduct() {
        return product;
    }

    public void setProduct(ProductOption product) {
        this.product = product;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
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

    public LookupOption getUnit() {
        return unit;
    }

    public void setUnit(LookupOption unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantityInBase() {
        return quantityInBase == null ? BigDecimal.ZERO : quantityInBase.setScale(2, RoundingMode.HALF_UP);
    }

    public void setQuantityInBase(BigDecimal quantityInBase) {
        this.quantityInBase = quantityInBase;
    }

    public BigDecimal getUnitCost() {
        return unitCost == null ? BigDecimal.ZERO : unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotal() {
        return getQuantityInBase().multiply(getUnitCost()).setScale(4, RoundingMode.HALF_UP);
    }
}
