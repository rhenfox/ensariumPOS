package com.aldrin.ensarium.inventory.product;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class UpdateSellingPriceDialog extends JDialog {
    private final JTextField priceField = new JTextField(16);
    private BigDecimal approvedValue;

    public UpdateSellingPriceDialog(Window owner, String productName, BigDecimal currentSellingPrice) {
        super(owner, "Update Selling Price", ModalityType.APPLICATION_MODAL);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.add(new JLabel("Product:"));
        form.add(new JLabel(productName));
        form.add(new JLabel("Current:"));
        form.add(new JLabel(currentSellingPrice == null ? "0.00" : currentSellingPrice.toPlainString()));
        form.add(new JLabel("New Selling Price:"));
        priceField.setText(currentSellingPrice == null ? "0.00" : currentSellingPrice.toPlainString());
        form.add(priceField);

        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(save);
        south.add(cancel);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        try {
            approvedValue = new BigDecimal(priceField.getText().trim());
            if (approvedValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid non-negative selling price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
        }
    }

    public BigDecimal getApprovedValue() { return approvedValue; }
}
