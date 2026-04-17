package com.aldrin.ensarium.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.table.AbstractTableModel;

public class OrderDraftTableModel extends AbstractTableModel {

    private final String[] columns = {
        "No", "Product ID", "SKU", "Product", "Barcode", "UOM", "Onhand", "Sold 30D", "Qty To Order", "Buying", "Selling", "Expiry", "Notes"
    };
    private final OrderDraftManager manager;

    public OrderDraftTableModel(OrderDraftManager manager) {
        this.manager = manager;
    }

    @Override
    public int getRowCount() {
        return manager.size();
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
        OrderDraftLine row = manager.get(rowIndex);
        return switch (columnIndex) {
            case 0 ->
                rowIndex + 1;
            case 1 ->
                row.getProductId();
            case 2 ->
                row.getSku();
            case 3 ->
                row.getProductName();
            case 4 ->
                row.getBarcode();
            case 5 ->
                row.getUomCode();
            case 6 ->
                row.getOnhandQty().setScale(1, RoundingMode.HALF_UP);
            case 7 ->
                row.getQtySold30Days().setScale(1, RoundingMode.HALF_UP);
            case 8 ->
                row.getQtyToOrder().setScale(1, RoundingMode.HALF_UP);
            case 9 ->
                row.getBuyingPrice().setScale(2, RoundingMode.HALF_UP);
            case 10 ->
                row.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
            case 11 -> row.getExpirySummary();
//            case 11 ->
//                row.getExpiryDisplayHtml();
            case 12 ->
                row.getNotes();
            default ->
                null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 8 || columnIndex == 12;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 8) {
            try {
                manager.updateQty(rowIndex, new BigDecimal(String.valueOf(aValue).trim()));
            } catch (Exception ignored) {
                manager.updateQty(rowIndex, BigDecimal.ONE);
            }
        } else if (columnIndex == 12) {
            manager.updateNotes(rowIndex, aValue == null ? null : String.valueOf(aValue));
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 ->
                Integer.class;
            case 1 ->
                Long.class;
            case 6, 7, 8, 9, 10 ->
                BigDecimal.class;
            default ->
                String.class;
        };
    }
}
