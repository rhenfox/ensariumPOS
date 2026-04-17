package com.aldrin.ensarium.txn;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class TraceRowTableModel extends AbstractTableModel {
    private final String[] columns = {"Document", "Product", "Lot No", "From", "To", "Before Qty", "Txn Qty", "After Qty", "Unit Cost", "Total Cost", "Unit Price", "Gross", "Tax", "Discount", "Net", "Buying Price", "Cost Snapshot", "Notes"};
    private List<TraceRow> rows = new ArrayList<>();
    public void setRows(List<TraceRow> rows) { this.rows = rows == null ? new ArrayList<>() : rows; fireTableDataChanged(); }
    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }
    @Override public Object getValueAt(int rowIndex, int columnIndex) { TraceRow row = rows.get(rowIndex); return switch (columnIndex) { case 0 -> row.getDocumentNo(); case 1 -> row.getProductName(); case 2 -> row.getLotNo(); case 3 -> row.getFromStatus(); case 4 -> row.getToStatus(); case 5 -> SwingUtils.formatQty(row.getBeforeQty()); case 6 -> SwingUtils.formatQty(row.getTxnQty()); case 7 -> SwingUtils.formatQty(row.getAfterQty()); case 8 -> SwingUtils.formatMoney(row.getUnitCost()); case 9 -> SwingUtils.formatMoney(row.getTotalCost()); case 10 -> SwingUtils.formatMoney(row.getUnitPrice()); case 11 -> SwingUtils.formatMoney(row.getLineGross()); case 12 -> SwingUtils.formatMoney(row.getTaxAmount()); case 13 -> SwingUtils.formatMoney(row.getDiscountAmount()); case 14 -> SwingUtils.formatMoney(row.getNetAmount()); case 15 -> SwingUtils.formatMoney(row.getBuyingPrice()); case 16 -> SwingUtils.formatMoney(row.getCostSnapshot()); case 17 -> row.getTraceNotes(); default -> null; }; }
}
