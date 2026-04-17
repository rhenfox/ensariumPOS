package com.aldrin.ensarium.txn;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class InventoryTxnTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Store ID", "Transaction Type", "Reference No", "Sale ID", "Return ID", "Receipt ID", "Created By", "Created At", "Notes"};
    private List<InventoryTxn> rows = new ArrayList<>();
    public void setRows(List<InventoryTxn> rows) { this.rows = rows == null ? new ArrayList<>() : rows; fireTableDataChanged(); }
    public InventoryTxn getRow(int rowIndex) { return rows.get(rowIndex); }
    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }
    @Override public Object getValueAt(int rowIndex, int columnIndex) { InventoryTxn row = rows.get(rowIndex); return switch (columnIndex) { case 0 -> row.getId(); case 1 -> row.getStoreId(); case 2 -> row.getTxnType(); case 3 -> row.getRefNo(); case 4 -> row.getSaleId(); case 5 -> row.getSalesReturnId(); case 6 -> row.getPurchaseReceiptId(); case 7 -> row.getCreatedByName(); case 8 -> SwingUtils.formatDateTime(row.getCreatedAt()); case 9 -> row.getNotes(); default -> null; }; }
}
