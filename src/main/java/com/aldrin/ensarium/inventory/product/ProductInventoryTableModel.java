package com.aldrin.ensarium.inventory.product;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ProductInventoryTableModel extends AbstractTableModel {

    private final String[] columns = {
        "No", "ID", "SKU", "Product", "Barcode", "UOM",
        "Onhand Qty", "Qty Sold (30 Days)", "Buying Price", "Selling Price",
        "Profit (w/ Tax & Discount)","Profit (No Tax & Discount)", 
        "Markup(No Tax & Discount)", "Markup(w/ Tax & Discount)",
        "Expiry / Remaining", "Days to Expire"
    };

    private List<ProductInventoryItem> rows = new ArrayList<>();

    public void setRows(List<ProductInventoryItem> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
        fireTableDataChanged();
    }

    public ProductInventoryItem getRow(int row) {
        return rows.get(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductInventoryItem row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> row.getProductId();
            case 2 -> row.getSku();
            case 3 -> row.getProductName();
            case 4 -> row.getBarcodes();
            case 5 -> row.getUomCode();
            case 6 -> row.getOnhandQty().setScale(2, RoundingMode.HALF_UP);
            case 7 -> row.getQtySold30Days();
            case 8 -> row.getBuyingPrice();
            case 9 -> row.getSellingPrice();
            case 10 -> row.getProfitWithoutTaxWithoutDiscount();
            case 11 -> row.getProfitWithTaxAndDiscount();
            case 12 -> row.getMarkupWithoutTaxWithoutDiscount();
            case 13 -> row.getMarkupWithTaxAndDiscount();
            case 14 -> row.getExpiryDisplayHtml() == null || row.getExpiryDisplayHtml().isBlank()
                    ? row.getExpirySummary() : row.getExpiryDisplayHtml();
            case 15 -> row.getDaysToExpireDisplayHtml() == null || row.getDaysToExpireDisplayHtml().isBlank()
                    ? row.getDaysToExpireDisplay() : row.getDaysToExpireDisplayHtml();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Integer.class;
            case 1 -> Long.class;
            case 6, 7, 8, 9, 10, 11, 12, 13 -> java.math.BigDecimal.class;
            default -> String.class;
        };
    }
}
