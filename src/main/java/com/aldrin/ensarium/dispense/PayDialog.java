package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PayDialog extends JDialog {
    private final BigDecimal total;
    private final JComboBox<PaymentMethodRef> cboMethod = new JComboBox<>();
    private final JTextField txtRendered = new JTextField();
    private final JTextField txtReference = new JTextField();
    private Result result;

    public PayDialog(java.awt.Window owner, BigDecimal total) throws Exception {
        super(owner, "Pay", Dialog.ModalityType.APPLICATION_MODAL);
        this.total = total;
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new java.awt.GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Total")); form.add(new JLabel(total.toPlainString()));
        form.add(new JLabel("Rendered")); form.add(txtRendered);
        form.add(new JLabel("Method")); form.add(cboMethod);
        form.add(new JLabel("Reference")); form.add(txtReference);
        add(form, BorderLayout.CENTER);
        loadMethods();
        SearchableComboBoxSupport.makeSearchable(cboMethod);
        txtRendered.setText(total.toPlainString());
        JPanel bottom = new JPanel();
        JButton btnOk = new JButton("Pay");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnOk); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);
        btnOk.addActionListener(e -> apply());
        btnCancel.addActionListener(e -> dispose());
        setSize(460, 200);
        setLocationRelativeTo(owner);
    }

    private void loadMethods() throws Exception {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            List<PaymentMethodRef> rows = new PaymentMethodDao().findAll(conn);
            conn.commit();
            DefaultComboBoxModel<PaymentMethodRef> m = new DefaultComboBoxModel<>();
            for (PaymentMethodRef p : rows) m.addElement(p);
            cboMethod.setModel(m);
        }
    }

    private void apply() {
        try {
            PaymentMethodRef pm = (PaymentMethodRef) cboMethod.getSelectedItem();
            if (pm == null) throw new IllegalStateException("Select payment method.");
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
