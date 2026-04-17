package com.aldrin.ensarium.shift;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import com.aldrin.ensarium.security.Session;

public class RecordSaleDialog extends JDialog {

    private final long shiftId;
    private final Session session;
    private final SalesDao salesDao = new SalesDao();

    private final JComboBox<SalesDao.PaymentMethod> cmbPaymentMethod = new JComboBox<>();
    private final JTextField txtAmount = new JTextField("0.00", 12);
    private final JTextField txtNote = new JTextField(20);

    private boolean saved;

    public RecordSaleDialog(Frame owner, long shiftId, Session session) {
        super(owner, "Record Sale", true);
        this.shiftId = shiftId;
        this.session = session;
        buildUi();
        loadPaymentMethods();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0;
        gc.gridy = 0;
        center.add(new JLabel("Payment method:"), gc);
        gc.gridx = 1;
        center.add(cmbPaymentMethod, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        center.add(new JLabel("Amount:"), gc);
        gc.gridx = 1;
        center.add(txtAmount, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        center.add(new JLabel("Note:"), gc);
        gc.gridx = 1;
        center.add(txtNote, gc);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnCancel);
        south.add(btnSave);

        setLayout(new BorderLayout());
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSave);
    }

    private void loadPaymentMethods() {
        try {
            List<SalesDao.PaymentMethod> methods = salesDao.findActivePaymentMethods();
            DefaultComboBoxModel<SalesDao.PaymentMethod> model = new DefaultComboBoxModel<>();
            for (SalesDao.PaymentMethod method : methods) {
                model.addElement(method);
            }
            cmbPaymentMethod.setModel(model);

            if (methods.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No active payment methods found. Insert rows in payment_method first.",
                        "Missing setup",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load payment methods: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void save() {
        try {
            SalesDao.PaymentMethod method = (SalesDao.PaymentMethod) cmbPaymentMethod.getSelectedItem();
            if (method == null) {
                JOptionPane.showMessageDialog(this, "Select a payment method.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
            salesDao.recordSimpleSale(new AuthDao().getSession(), shiftId, method.id(), amount, txtNote.getText().trim());
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be numeric.", "Invalid amount", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Save error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
