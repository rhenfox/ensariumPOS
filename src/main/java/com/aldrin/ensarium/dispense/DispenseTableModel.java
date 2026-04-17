package com.aldrin.ensarium.dispense;


import java.text.DecimalFormat;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class DispenseTableModel extends AbstractTableModel {
    private final String[] cols = {"Barcode", "SKU", "#", "Product", "Qty", "Unit Price", "Line Total", "", ""};
    private final List<CartLine> lines;
    private final DecimalFormat fmt = new DecimalFormat("#,##0.00");
    private int hoverRow = -1;
    private int hoverCol = -1;

    public DispenseTableModel(List<CartLine> lines) {
        this.lines = lines;
    }

    @Override public int getRowCount() { return lines.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CartLine l = lines.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> l.barcode;
            case 1 -> l.sku;
            case 2 -> rowIndex + 1;
            case 3 -> l.productName;
            case 4 -> fmt.format(l.qtyUom);
            case 5 -> fmt.format(l.unitPrice);
            case 6 -> fmt.format(l.unitPrice.multiply(l.qtyUom));
            case 7 -> "Edit";
            case 8 -> "Del";
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 7 || columnIndex == 8;
    }

    public void fireAll() {
        fireTableDataChanged();
    }

    public void setHoverCell(int row, int col) {
        this.hoverRow = row;
        this.hoverCol = col;
        if (getRowCount() > 0) fireTableRowsUpdated(0, getRowCount() - 1);
    }

    public boolean isHoverCell(int row, int col) {
        return hoverRow == row && hoverCol == col;
    }
}
