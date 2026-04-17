package com.aldrin.ensarium.txn;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class SummaryTableModel extends AbstractTableModel {
    private final String[] columns = {"Field", "Value"};
    private List<SummaryRow> rows = new ArrayList<>();
    public void setRows(List<SummaryRow> rows) { this.rows = rows == null ? new ArrayList<>() : rows; fireTableDataChanged(); }
    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }
    @Override public Object getValueAt(int rowIndex, int columnIndex) { SummaryRow row = rows.get(rowIndex); return columnIndex == 0 ? row.label() : row.value(); }
}
