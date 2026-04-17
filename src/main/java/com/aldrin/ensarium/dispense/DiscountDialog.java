package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class DiscountDialog extends JDialog {
    private static final String MANUAL = "<Manual Discount>";

    private final DiscountTypeDao discountTypeDao = new DiscountTypeDao();
    private final JComboBox<Object> cmbDiscountType = new JComboBox<>();
    private final JTextField txtName = new JTextField("Manual Discount");
    private final JTextField txtValue = new JTextField("0");
    private final JRadioButton rbPercent = new JRadioButton("Percent (example: 0.10)");
    private final JRadioButton rbAmount = new JRadioButton("Amount");
    private final JLabel lblHint = new JLabel("Choose any active discount type or enter a manual discount.");
    private SaleDiscountInfo result;
    private final List<DiscountTypeRef> loadedTypes = new ArrayList<>();

    public DiscountDialog(java.awt.Window owner, DispenseService service) {
        super(owner, "Discount", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;

        int y = 0;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Discount Type"), gc);
        gc.gridx = 1; gc.weightx = 1; form.add(cmbDiscountType, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; form.add(new JLabel("Name"), gc);
        gc.gridx = 1; gc.weightx = 1; form.add(txtName, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; form.add(new JLabel("Value"), gc);
        gc.gridx = 1; gc.weightx = 1; form.add(txtValue, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; form.add(new JLabel("Kind"), gc);
        JPanel kinds = new JPanel();
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbPercent); bg.add(rbAmount);
        rbAmount.setSelected(true);
        kinds.add(rbPercent);
        kinds.add(rbAmount);
        gc.gridx = 1; gc.weightx = 1; form.add(kinds, gc);

        y++;
        gc.gridx = 1; gc.gridy = y; gc.weightx = 1; form.add(lblHint, gc);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnOk = new JButton("Apply");
        JButton btnClear = new JButton("Clear");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnOk); bottom.add(btnClear); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        btnOk.addActionListener(e -> apply());
        btnClear.addActionListener(e -> { result = null; dispose(); });
        btnCancel.addActionListener(e -> dispose());
        cmbDiscountType.addActionListener(e -> syncSelectedType());

        txtName.setEditable(true);
        loadDiscountTypes();
        SearchableComboBoxSupport.makeSearchable(cmbDiscountType);

        setSize(560, 250);
        setLocationRelativeTo(owner);
    }

    private void loadDiscountTypes() {
        SearchableComboBoxSupport.beginBulkUpdate(cmbDiscountType);
        try {
            cmbDiscountType.removeAllItems();
            loadedTypes.clear();
            cmbDiscountType.addItem(MANUAL);
            try (Connection conn = Db.getConnection()) {
                conn.setAutoCommit(false);
                loadedTypes.addAll(discountTypeDao.listActive(conn));
                conn.commit();
                for (DiscountTypeRef type : loadedTypes) {
                    cmbDiscountType.addItem(type);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not load discount types: " + ex.getMessage(), "Discount", JOptionPane.WARNING_MESSAGE);
            }
            if (cmbDiscountType.getItemCount() > 0) {
                cmbDiscountType.setSelectedIndex(0);
            }
            syncSelectedType();
        } finally {
            SearchableComboBoxSupport.endBulkUpdate(cmbDiscountType);
        }
    }

    private void syncSelectedType() {
        Object selected = cmbDiscountType.getSelectedItem();
        if (!(selected instanceof DiscountTypeRef type)) {
            restoreManualMode();
            return;
        }
        txtName.setText(type.name == null ? "Discount" : type.name);
        txtName.setEditable(false);
        if (type.isPercent()) rbPercent.setSelected(true); else rbAmount.setSelected(true);
        rbPercent.setEnabled(false);
        rbAmount.setEnabled(false);
        lblHint.setText("Code: " + safe(type.code) + " | Applies To: " + safe(type.appliesTo));
    }

    private void restoreManualMode() {
        Object selected = cmbDiscountType.getSelectedItem();
        boolean manual = !(selected instanceof DiscountTypeRef);
        txtName.setEditable(manual);
        rbPercent.setEnabled(manual);
        rbAmount.setEnabled(manual);
        if (manual) {
            lblHint.setText("Choose any active discount type or enter a manual discount.");
        }
    }

    private void apply() {
        try {
            SaleDiscountInfo di = new SaleDiscountInfo();
            Object selected = cmbDiscountType.getSelectedItem();
            if (selected instanceof DiscountTypeRef type) {
                di.discountTypeId = type.id;
                di.discountCode = type.code;
                di.appliesTo = type.appliesTo;
                di.name = type.name == null || type.name.isBlank() ? "Discount" : type.name;
                di.kind = type.kind;
            } else {
                di.name = txtName.getText().trim().isBlank() ? "Manual Discount" : txtName.getText().trim();
                di.kind = rbPercent.isSelected() ? SaleDiscountInfo.KIND_PERCENT : SaleDiscountInfo.KIND_AMOUNT;
            }
            di.value = new BigDecimal(txtValue.getText().trim());
            if (di.value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Discount must not be negative.");
            }
            result = di;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid discount value.");
        }
    }

    public SaleDiscountInfo showDialog(SaleDiscountInfo current) {
        loadDiscountTypes();
        if (current != null) {
            txtName.setText(current.name == null ? "Manual Discount" : current.name);
            txtValue.setText(current.value == null ? "0" : current.value.toPlainString());
            if (current.isPercent()) rbPercent.setSelected(true); else rbAmount.setSelected(true);
            boolean matched = false;
            if (current.discountTypeId != null) {
                for (int i = 0; i < cmbDiscountType.getItemCount(); i++) {
                    Object item = cmbDiscountType.getItemAt(i);
                    if (item instanceof DiscountTypeRef type && current.discountTypeId.equals(type.id)) {
                        cmbDiscountType.setSelectedIndex(i);
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                cmbDiscountType.setSelectedIndex(0);
            }
        } else {
            txtName.setText("Manual Discount");
            txtValue.setText("0");
            rbAmount.setSelected(true);
            cmbDiscountType.setSelectedIndex(0);
        }
        restoreManualMode();
        setVisible(true);
        return result;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
