package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

public class ReturnDialog extends JDialog {
    private final DispenseService service;
    private final JTextField txtSaleNo = new JTextField();
    private final JComboBox<ReturnReasonRef> cboReason = new JComboBox<>();
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"RETURNED", "DAMAGED", "ONHAND", "EXPIRED"});
    private final JComboBox<PaymentMethodRef> cboRefundMethod = new JComboBox<>();
    private final JTextField txtRefundRef = new JTextField();
    private final ReturnLineTableModel model = new ReturnLineTableModel();
    private final JTable table = new JTable(model);
    private Result result;

    public ReturnDialog(java.awt.Window owner, DispenseService service) throws Exception {
        super(owner, "Return", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new java.awt.GridLayout(3, 4, 6, 6));
        JButton btnLoad = new JButton("Load Sale");
        top.add(new JLabel("Sale / Invoice No")); top.add(txtSaleNo); top.add(btnLoad); top.add(new JLabel());
        top.add(new JLabel("Reason")); top.add(cboReason); top.add(new JLabel("Restock To")); top.add(cboStatus);
        top.add(new JLabel("Refund Method")); top.add(cboRefundMethod); top.add(new JLabel("Refund Ref")); top.add(txtRefundRef);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnPost = new JButton("Post Return");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnPost); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        loadCombos();
        SearchableComboBoxSupport.makeSearchable(cboReason);
        SearchableComboBoxSupport.makeSearchable(cboStatus);
        SearchableComboBoxSupport.makeSearchable(cboRefundMethod);

        btnLoad.addActionListener(e -> loadSale());
        txtSaleNo.addActionListener(e -> loadSale());
        btnPost.addActionListener(e -> apply());
        btnCancel.addActionListener(e -> dispose());
        setSize(920, 450);
        setLocationRelativeTo(owner);
    }

    private void loadCombos() throws Exception {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            DefaultComboBoxModel<ReturnReasonRef> r = new DefaultComboBoxModel<>();
            for (ReturnReasonRef x : new ReturnReasonDao().findAll(conn)) r.addElement(x);
            cboReason.setModel(r);
            DefaultComboBoxModel<PaymentMethodRef> p = new DefaultComboBoxModel<>();
            for (PaymentMethodRef x : new PaymentMethodDao().findAll(conn)) p.addElement(x);
            cboRefundMethod.setModel(p);
            conn.commit();
        }
    }

    private void loadSale() {
        try {
            model.setRows(service.loadReturnableLines(txtSaleNo.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void apply() {
        try {
            ReturnReasonRef reason = (ReturnReasonRef) cboReason.getSelectedItem();
            PaymentMethodRef method = (PaymentMethodRef) cboRefundMethod.getSelectedItem();
            String status = (String) cboStatus.getSelectedItem();
            if (reason == null || method == null || status == null) throw new IllegalStateException("Complete all fields.");
            List<ReturnLineInput> picked = new ArrayList<>();
            for (ReturnLineInput rl : model.rows) {
                if (rl.returnQty != null && rl.returnQty.compareTo(BigDecimal.ZERO) > 0) picked.add(rl);
            }
            if (picked.isEmpty()) throw new IllegalStateException("Enter at least one return quantity.");
            result = new Result();
            result.saleNumber = txtSaleNo.getText().trim();
            result.selectedLines = picked;
            result.reasonId = reason.id;
            result.restockStatus = status;
            result.refundMethodId = method.id;
            result.refundReferenceNo = txtRefundRef.getText().trim();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Result showDialog() {
        setVisible(true);
        return result;
    }

    public static class Result {
        public String saleNumber;
        public List<ReturnLineInput> selectedLines;
        public int reasonId;
        public String restockStatus;
        public int refundMethodId;
        public String refundReferenceNo;
    }

    private static class ReturnLineTableModel extends AbstractTableModel {
        private final String[] cols = {"Product", "UOM", "Returnable Qty", "Unit Price", "Refundable", "Return Qty"};
        private List<ReturnLineInput> rows = new ArrayList<>();
        public void setRows(List<ReturnLineInput> rows) { this.rows = rows; fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 5; }
        @Override public Object getValueAt(int r, int c) {
            ReturnLineInput x = rows.get(r);
            return switch (c) {
                case 0 -> x.productName;
                case 1 -> x.uomCode;
                case 2 -> x.soldQty;
                case 3 -> x.unitPrice;
                case 4 -> x.refundableAmount;
                default -> x.returnQty;
            };
        }
        @Override public void setValueAt(Object aValue, int r, int c) {
            if (c == 5) {
                try {
                    rows.get(r).returnQty = new BigDecimal(String.valueOf(aValue));
                } catch (Exception ignored) {
                    rows.get(r).returnQty = BigDecimal.ZERO;
                }
            }
        }
    }
}
