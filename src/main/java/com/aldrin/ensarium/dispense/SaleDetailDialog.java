package com.aldrin.ensarium.dispense;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.text.DecimalFormat;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

public class SaleDetailDialog extends JDialog {
    private final JLabel lblSaleNo = new JLabel("-");
    private final JLabel lblDate = new JLabel("-");
    private final JLabel lblCustomer = new JLabel("-");
    private final JLabel lblCashier = new JLabel("-");
    private final JLabel lblInvoice = new JLabel("-");
    private final JLabel lblItems = new JLabel("0");
    private final JLabel lblTotal = new JLabel("0.00");
    private final JTextArea txtNotes = new JTextArea(3, 20);
    private final LineTableModel model = new LineTableModel();

    public SaleDetailDialog(java.awt.Window owner, SaleDetailView detail) {
        super(owner, "Dispense Details", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));

        JPanel hdr = new JPanel(new java.awt.GridLayout(4, 4, 8, 6));
        hdr.add(new JLabel("Sale No")); hdr.add(lblSaleNo);
        hdr.add(new JLabel("Date")); hdr.add(lblDate);
        hdr.add(new JLabel("Customer")); hdr.add(lblCustomer);
        hdr.add(new JLabel("Cashier")); hdr.add(lblCashier);
        hdr.add(new JLabel("Invoice")); hdr.add(lblInvoice);
        hdr.add(new JLabel("Items")); hdr.add(lblItems);
        hdr.add(new JLabel("Total")); hdr.add(lblTotal);
        hdr.add(new JLabel(""));
        add(hdr, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        txtNotes.setEditable(false);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JPanel notesPanel = new JPanel(new BorderLayout(4, 4));
        notesPanel.add(new JLabel("Notes"), BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);
        add(notesPanel, BorderLayout.SOUTH);

        bind(detail);
        setSize(920, 500);
        setLocationRelativeTo(owner);
    }

    private void bind(SaleDetailView d) {
        DecimalFormat money = new DecimalFormat("#,##0.00");
        lblSaleNo.setText(d.saleNo == null ? "-" : d.saleNo);
        lblDate.setText(d.soldAt == null ? "-" : d.soldAt.toString());
        lblCustomer.setText(d.customerName == null || d.customerName.isBlank() ? "Walk-in" : d.customerName);
        lblCashier.setText(d.cashierUsername == null || d.cashierUsername.isBlank() ? "-" : d.cashierUsername);
        lblInvoice.setText(d.invoiceNo == null || d.invoiceNo.isBlank() ? "-" : d.invoiceNo);
        lblItems.setText(String.valueOf(d.itemCount));
        lblTotal.setText(money.format(d.total));
        txtNotes.setText(d.notes == null ? "" : d.notes);
        model.setLines(d.lines);
    }

    private static class LineTableModel extends AbstractTableModel {
        private final String[] cols = {"#", "Product", "SKU", "Barcode", "Qty", "Unit Price", "Line Total"};
        private java.util.List<SaleDetailLine> lines = new java.util.ArrayList<>();
        private final DecimalFormat money = new DecimalFormat("#,##0.00");
        public void setLines(java.util.List<SaleDetailLine> lines) { this.lines = lines; fireTableDataChanged(); }
        @Override public int getRowCount() { return lines.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            SaleDetailLine x = lines.get(r);
            return switch (c) {
                case 0 -> x.lineNo;
                case 1 -> x.productName;
                case 2 -> x.sku;
                case 3 -> x.barcode;
                case 4 -> money.format(x.qty);
                case 5 -> money.format(x.unitPrice);
                default -> money.format(x.lineTotal);
            };
        }
    }
}
