package com.aldrin.ensarium.txn;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class InventoryTxnLineTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Product ID", "SKU", "Product Name", "Lot No", "From Status", "To Status", "Qty", "Unit Cost", "Total Cost"};
    private List<InventoryTxnLine> rows = new ArrayList<>();
    public void setRows(List<InventoryTxnLine> rows) { this.rows = rows == null ? new ArrayList<>() : rows; fireTableDataChanged(); }
    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }
    @Override public Object getValueAt(int rowIndex, int columnIndex) { InventoryTxnLine row = rows.get(rowIndex); return switch (columnIndex) { case 0 -> row.getId(); case 1 -> row.getProductId(); case 2 -> row.getSku(); case 3 -> row.getProductName(); case 4 -> row.getLotNo(); case 5 -> row.getFromStatus(); case 6 -> row.getToStatus(); case 7 -> SwingUtils.formatQty(row.getQtyInBase()); case 8 -> SwingUtils.formatMoney(row.getUnitCost()); case 9 -> SwingUtils.formatMoney(row.getTotalCost()); default -> null; }; }
}
