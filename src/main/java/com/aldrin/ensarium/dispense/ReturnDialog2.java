/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class ReturnDialog2 extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ReturnDialog2.class.getName());

    private final DispenseService service;
    private final ReturnDialog2.ReturnLineTableModel model = new ReturnDialog2.ReturnLineTableModel();
    private final JTable table = new JTable(model);
    private ReturnDialog2.Result result;

    Icon iconSearch = FaSwingIcons.icon(FontAwesomeIcon.DOWNLOAD, 24, Color.WHITE);
    Icon iconReturn = FaSwingIcons.icon(FontAwesomeIcon.REPLY, 24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);

    public ReturnDialog2(java.awt.Window owner, DispenseService service) throws Exception {
        super(owner, "Return", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        initComponents();

        btnCancel.setDanger();
        btnLoad.setIcon(iconSearch);
        btnCancel.setIcon(iconCancel);
        btnPost.setIcon(iconReturn);

        tablePanel.add(new RoundedScrollPane(table), BorderLayout.CENTER);

        loadCombos();

        applyComboBoxAutoFill(cboReason);
        applyComboBoxAutoFill(cboStatus);
        applyComboBoxAutoFill(cboRefundMethod);

        btnLoad.addActionListener(e -> loadSale());
        txtSaleNo.addActionListener(e -> loadSale());
        btnPost.addActionListener(e -> apply());
        btnCancel.addActionListener(e -> dispose());
        txtSaleNo.putClientProperty("JTextField.placeholderText", "Sale number");
        txtRefundRef.putClientProperty("JTextField.placeholderText", "Refund refference");

//        hideColumn(0);
//        hideColumn(1);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnRight(table, 2);
        BootstrapTableStyle.setColumnRight(table, 3);
        BootstrapTableStyle.setColumnRight(table, 4);
        BootstrapTableStyle.setColumnRight(table, 5);

        BootstrapTableStyle.setHeaderLeft(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderRight(table, 2);
        BootstrapTableStyle.setHeaderRight(table, 3);
        BootstrapTableStyle.setHeaderRight(table, 4);
        BootstrapTableStyle.setHeaderRight(table, 5);

        installTableHover();

    }

    private void hideColumn(int i) {
        table.getColumnModel().getColumn(i).setMinWidth(0);
        table.getColumnModel().getColumn(i).setMaxWidth(0);
        table.getColumnModel().getColumn(i).setPreferredWidth(0);
        table.getColumnModel().getColumn(i).setResizable(false);
    }

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        cboReason = new javax.swing.JComboBox<>();
        cboRefundMethod = new javax.swing.JComboBox<>();
        jPanel13 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        cboStatus = new javax.swing.JComboBox<>();
        txtRefundRef = new javax.swing.JTextField();
        jPanel17 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        btnPost = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnCancel = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtSaleNo = new javax.swing.JTextField();
        btnLoad = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        tablePanel.setLayout(new java.awt.BorderLayout());
        jPanel2.add(tablePanel, java.awt.BorderLayout.CENTER);

        jPanel20.setPreferredSize(new java.awt.Dimension(970, 10));

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 970, Short.MAX_VALUE)
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel20, java.awt.BorderLayout.NORTH);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(970, 60));
        jPanel3.setLayout(new java.awt.GridLayout());

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel11.setLayout(new java.awt.GridLayout(0, 1, 0, 10));
        jPanel11.add(cboReason);
        jPanel11.add(cboRefundMethod);

        jPanel10.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel13.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel13.setLayout(new java.awt.GridLayout(0, 1));

        jLabel1.setText("Reason");
        jPanel13.add(jLabel1);

        jLabel2.setText("Refund method");
        jPanel13.add(jLabel2);

        jPanel10.add(jPanel13, java.awt.BorderLayout.WEST);

        jPanel9.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel12.setPreferredSize(new java.awt.Dimension(10, 10));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel12, java.awt.BorderLayout.WEST);

        jPanel3.add(jPanel9);

        jPanel14.setLayout(new java.awt.BorderLayout());

        jPanel15.setLayout(new java.awt.BorderLayout());

        jPanel16.setLayout(new java.awt.GridLayout(0, 1, 0, 10));

        cboStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "RETURNED", " DAMAGED", "ONHAND", "EXPIRED" }));
        jPanel16.add(cboStatus);
        jPanel16.add(txtRefundRef);

        jPanel15.add(jPanel16, java.awt.BorderLayout.CENTER);

        jPanel17.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel17.setLayout(new java.awt.GridLayout(0, 1));

        jLabel3.setText("Restock To");
        jPanel17.add(jLabel3);

        jLabel4.setText("Refund Ref");
        jPanel17.add(jLabel4);

        jPanel15.add(jPanel17, java.awt.BorderLayout.WEST);

        jPanel14.add(jPanel15, java.awt.BorderLayout.CENTER);

        jPanel18.setPreferredSize(new java.awt.Dimension(20, 10));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        jPanel14.add(jPanel18, java.awt.BorderLayout.WEST);

        jPanel3.add(jPanel14);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setPreferredSize(new java.awt.Dimension(970, 50));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        btnPost.setText("Post Return");
        jPanel4.add(btnPost);

        btnCancel.setText("Cancel");
        jPanel4.add(btnCancel);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 50));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 15));

        jLabel5.setText("Sale No");
        jLabel5.setPreferredSize(new java.awt.Dimension(80, 16));
        jPanel5.add(jLabel5);

        txtSaleNo.setPreferredSize(new java.awt.Dimension(250, 24));
        jPanel5.add(txtSaleNo);

        btnLoad.setText("Load Sale");
        jPanel5.add(btnLoad);

        getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel6.setPreferredSize(new java.awt.Dimension(990, 10));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 990, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel6, java.awt.BorderLayout.SOUTH);

        jPanel7.setPreferredSize(new java.awt.Dimension(10, 341));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 381, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel7, java.awt.BorderLayout.EAST);

        jPanel8.setPreferredSize(new java.awt.Dimension(10, 341));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 381, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel8, java.awt.BorderLayout.WEST);

        setSize(new java.awt.Dimension(1006, 450));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCancel;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnLoad;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnPost;
    private javax.swing.JComboBox<ReturnReasonRef> cboReason;
    private javax.swing.JComboBox<PaymentMethodRef> cboRefundMethod;
    private javax.swing.JComboBox<String> cboStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextField txtRefundRef;
    private javax.swing.JTextField txtSaleNo;
    // End of variables declaration//GEN-END:variables

    private void loadCombos() throws Exception {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            DefaultComboBoxModel<ReturnReasonRef> r = new DefaultComboBoxModel<>();
            for (ReturnReasonRef x : new ReturnReasonDao().findAll(conn)) {
                r.addElement(x);
            }
            cboReason.setModel(r);
            DefaultComboBoxModel<PaymentMethodRef> p = new DefaultComboBoxModel<>();
            for (PaymentMethodRef x : new PaymentMethodDao().findAll(conn)) {
                p.addElement(x);
            }
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
            if (reason == null || method == null || status == null) {
                throw new IllegalStateException("Complete all fields.");
            }
            List<ReturnLineInput> picked = new ArrayList<>();
            for (ReturnLineInput rl : model.rows) {
                if (rl.returnQty != null && rl.returnQty.compareTo(BigDecimal.ZERO) > 0) {
                    picked.add(rl);
                }
            }
            if (picked.isEmpty()) {
                throw new IllegalStateException("Enter at least one return quantity.");
            }
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

        public void setRows(List<ReturnLineInput> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 5;
        }

        @Override
        public Object getValueAt(int r, int c) {
            ReturnLineInput x = rows.get(r);
            return switch (c) {
                case 0 ->
                    x.productName;
                case 1 ->
                    x.uomCode;
                case 2 ->
                    x.soldQty;
                case 3 ->
                    x.unitPrice;
                case 4 ->
                    x.refundableAmount;
                default ->
                    x.returnQty;
            };
        }

        @Override
        public void setValueAt(Object aValue, int r, int c) {
            if (c == 5) {
                try {
                    rows.get(r).returnQty = new BigDecimal(String.valueOf(aValue));
                } catch (Exception ignored) {
                    rows.get(r).returnQty = BigDecimal.ZERO;
                }
            }
        }
    }

    private int hoveredRow = -1;

    private void installTableHover() {
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    table.repaint();
                }
            }
        });
    }
}
