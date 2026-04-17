/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class PayDialog2 extends javax.swing.JDialog {

    private final BigDecimal total;
    private PayDialog2.Result result;

    Icon iconMoneyCheck = Icons.moneyCheckAlt(24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);
    
    
    public PayDialog2(java.awt.Window owner, BigDecimal total) throws Exception {
        super(owner, "Pay", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        this.total = total;
        
        txtTotal.setText(total.toPlainString());
        
        loadMethods();
        
        txtRendered.setText(total.toPlainString());
        btnOk.setPrimary();
        btnCancel.setDanger();
        
        btnOk.setIcon(iconMoneyCheck);
        btnCancel.setIcon(iconCancel);

        applyComboBoxAutoFill(cboMethod);
        
        btnOk.addActionListener(e -> apply());
        btnCancel.addActionListener(e -> dispose());
        
        
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
        jPanel7 = new javax.swing.JPanel();
        txtTotal = new javax.swing.JTextField();
        txtRendered = new javax.swing.JTextField();
        cboMethod = new javax.swing.JComboBox<PaymentMethodRef>();
        txtReference = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        btnOk = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnCancel = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridLayout(0, 1, 0, 10));

        txtTotal.setEditable(false);
        txtTotal.setFocusable(false);
        jPanel7.add(txtTotal);
        jPanel7.add(txtRendered);
        jPanel7.add(cboMethod);
        jPanel7.add(txtReference);

        jPanel2.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel8.setPreferredSize(new java.awt.Dimension(90, 217));
        jPanel8.setLayout(new java.awt.GridLayout(0, 1, 0, 10));

        jLabel1.setText("Total");
        jPanel8.add(jLabel1);

        jLabel2.setText("Rendered");
        jPanel8.add(jLabel2);

        jLabel3.setText("Method");
        jPanel8.add(jLabel3);

        jLabel4.setText("Reference");
        jPanel8.add(jLabel4);

        jPanel2.add(jPanel8, java.awt.BorderLayout.WEST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(0, 10));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel4.setPreferredSize(new java.awt.Dimension(0, 55));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 15));

        btnOk.setText("Pay");
        btnOk.setMargin(new java.awt.Insets(2, 5, 3, 5));
        btnOk.setPreferredSize(new java.awt.Dimension(90, 32));
        jPanel4.add(btnOk);

        btnCancel.setText("Cancel");
        btnCancel.setMargin(new java.awt.Insets(2, 5, 3, 5));
        btnCancel.setPreferredSize(new java.awt.Dimension(90, 32));
        jPanel4.add(btnCancel);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 349));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setPreferredSize(new java.awt.Dimension(10, 349));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel6, java.awt.BorderLayout.EAST);

        setSize(new java.awt.Dimension(455, 249));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCancel;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnOk;
    private javax.swing.JComboBox<PaymentMethodRef> cboMethod;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JTextField txtReference;
    private javax.swing.JTextField txtRendered;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables

    private void loadMethods() throws Exception {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            List<PaymentMethodRef> rows = new PaymentMethodDao().findAll(conn);
            conn.commit();
            DefaultComboBoxModel<PaymentMethodRef> m = new DefaultComboBoxModel<>();
            for (PaymentMethodRef p : rows) {
                m.addElement(p);
            }
            cboMethod.setModel(m);
        }
    }
    
    private void apply() {
        try {
            PaymentMethodRef pm = (PaymentMethodRef) cboMethod.getSelectedItem();
            if (pm == null) {
                throw new IllegalStateException("Select payment method.");
            }
            Result r = new Result();
            r.amountRendered = new BigDecimal(txtRendered.getText().trim());
            r.methodId = pm.id;
            r.referenceNo = txtReference.getText().trim();
            result = r;
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

        public BigDecimal amountRendered;
        public int methodId;
        public String referenceNo;
    }
}


