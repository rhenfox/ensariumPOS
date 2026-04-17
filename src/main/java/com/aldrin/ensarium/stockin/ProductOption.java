package com.aldrin.ensarium.stockin;

public class ProductOption extends LookupOption {

    private Long baseUomId;
    private String baseUomCode;

    public ProductOption() {
    }

    public ProductOption(Long id, String sku, String name, Long baseUomId, String baseUomCode) {
        super(id, sku, name);
        this.baseUomId = baseUomId;
        this.baseUomCode = baseUomCode;
    }

    public String getSku() {
        return getCode();
    }

    public void setSku(String sku) {
        setCode(sku);
    }

    public Long getBaseUomId() {
        return baseUomId;
    }

    public void setBaseUomId(Long baseUomId) {
        this.baseUomId = baseUomId;
    }

    public String getBaseUomCode() {
        return baseUomCode;
    }

    public void setBaseUomCode(String baseUomCode) {
        this.baseUomCode = baseUomCode;
    }
}
