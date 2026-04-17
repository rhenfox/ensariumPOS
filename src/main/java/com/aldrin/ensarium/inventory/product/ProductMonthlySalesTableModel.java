package com.aldrin.ensarium.inventory.product;

import com.aldrin.ensarium.util.SwingUtils;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ProductMonthlySalesTableModel extends AbstractTableModel {

    private final String[] columns = {
        "Sale No", "Invoice No", "Sold At", "Customer", "Qty Sold", "Unit Price", "Gross",
        "Discount", "Tax", "Net", "Cost", "Profit(No Tax)", "Profit(with Tax)"
    };

    private List<ProductMonthlySalesRow> rows = new ArrayList<>();

    public void setRows(List<ProductMonthlySalesRow> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return rows.size(); }
    @Override
    public int getColumnCount() { return columns.length; }
    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductMonthlySalesRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getSaleNo();
            case 1 -> row.getInvoiceNo();
            case 2 -> SwingUtils.formatDateTime(row.getSoldAt());
            case 3 -> row.getCustomerName();
            case 4 -> SwingUtils.formatQty(row.getQtySold());
            case 5 -> SwingUtils.formatMoney(row.getUnitPrice());
            case 6 -> SwingUtils.formatMoney(row.getGrossAmount());
            case 7 -> SwingUtils.formatMoney(row.getDiscountAmount());
            case 8 -> SwingUtils.formatMoney(row.getTaxAmount());
            case 9 -> SwingUtils.formatMoney(row.getNetAmountWithTax());
            case 10 -> SwingUtils.formatMoney(row.getCostAmount());
            case 11 -> SwingUtils.formatMoney(row.getProfitWithoutTax());
            case 12 -> SwingUtils.formatMoney(row.getProfitWithTax());
            default -> null;
        };
    }
}
