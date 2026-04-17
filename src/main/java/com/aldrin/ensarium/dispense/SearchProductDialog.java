package com.aldrin.ensarium.dispense;


import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

public class SearchProductDialog extends JDialog {
    private final ProductDao dao = new ProductDao();
    private final JTextField txtSearch = new JTextField();
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 999999.0, 1.0));
    private final ProductTableModel model = new ProductTableModel();
    private final JTable table = new JTable(model);
    private Result result;

    public SearchProductDialog(java.awt.Window owner, String initialQuery) {
        super(owner, "Search Product", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(new JLabel("Search"), BorderLayout.WEST);
        top.add(txtSearch, BorderLayout.CENTER);
        JButton btnFind = new JButton("Find");
        top.add(btnFind, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 5, 6, 6));
        bottom.add(new JLabel("Qty"));
        bottom.add(spQty);
        bottom.add(new JLabel("Select item then choose quantity"));
        JButton btnSelect = new JButton("Select");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnSelect);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        txtSearch.setText(initialQuery == null ? "" : initialQuery);
        btnFind.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        btnSelect.addActionListener(e -> choose());
        btnCancel.addActionListener(e -> dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) choose(); }
        });

        setSize(920, 460);
        setLocationRelativeTo(owner);
        search();
    }

    private void search() {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            List<ProductOption> rows = dao.search(conn, txtSearch.getText().trim());
            conn.commit();
            model.setRows(rows);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void choose() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }
        ProductOption p = model.get(row);
        BigDecimal qty = BigDecimal.valueOf(((Number) spQty.getValue()).doubleValue());
        if (qty.compareTo(p.onHandQty) > 0) {
            JOptionPane.showMessageDialog(this,
                    "Only " + p.onHandQty.stripTrailingZeros().toPlainString() + " remaining for " + p.productName + ".",
                    "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new Result();
        result.product = p;
        result.qty = qty;
        dispose();
    }

    public Result showDialog() {
        setVisible(true);
        return result;
    }

    public static class Result {
        public ProductOption product;
        public BigDecimal qty;
    }

    private static class ProductTableModel extends AbstractTableModel {
        private final String[] cols = {"Barcode", "SKU", "Product", "UOM", "On Hand", "Price"};
        private List<ProductOption> rows = new ArrayList<>();
        public void setRows(List<ProductOption> rows) { this.rows = rows; fireTableDataChanged(); }
        public ProductOption get(int i) { return rows.get(i); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            ProductOption p = rows.get(r);
            return switch (c) {
                case 0 -> p.barcode;
                case 1 -> p.sku;
                case 2 -> p.productName;
                case 3 -> p.uomCode;
                case 4 -> p.onHandQty;
                default -> p.unitPrice;
            };
        }
    }
}
