package com.aldrin.ensarium.inventory.product;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class RecordShrinkageDialog extends JDialog {
    private final JLabel productValue = new JLabel();
    private final JLabel onhandValue = new JLabel();
    private final JComboBox<String> reasonCombo = new JComboBox<>(new String[]{"THEFT", "ERROR", "MISCOUNT"});
    private final JTextField qtyField = new JTextField("1", 16);
    private final JTextArea notesArea = new JTextArea(4, 26);
    private BigDecimal approvedQty;
    private String approvedReason;
    private String approvedNotes;

    public RecordShrinkageDialog(java.awt.Window owner, String productName, BigDecimal onhandQty) {
        super(owner, "Record Shrinkage Qty", Dialog.ModalityType.APPLICATION_MODAL);
        initUi(productName, onhandQty);
    }

    private void initUi(String productName, BigDecimal onhandQty) {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 4, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addLabel(form, gbc, "Action:");
        addValue(form, gbc, new JLabel("Shrinkage"));

        addLabel(form, gbc, "Product:");
        productValue.setText(productName == null ? "" : productName);
        addValue(form, gbc, productValue);

        addLabel(form, gbc, "Onhand Qty:");
        onhandValue.setText(onhandQty == null ? "0" : onhandQty.stripTrailingZeros().toPlainString());
        addValue(form, gbc, onhandValue);

        addLabel(form, gbc, "Reason:");
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(reasonCombo, gbc);
        gbc.gridy++;

        addLabel(form, gbc, "Qty:");
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(qtyField, gbc);
        gbc.gridy++;

        addLabel(form, gbc, "Notes:");
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(new JScrollPane(notesArea), gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttons.add(saveButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveButton);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, String text) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        panel.add(label, gbc);
    }

    private void addValue(JPanel panel, GridBagConstraints gbc, JLabel valueLabel) {
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(valueLabel, gbc);
        gbc.gridy++;
    }

    private void onSave() {
        try {
            BigDecimal qty = new BigDecimal(qtyField.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Qty must be greater than zero.");
            }
            approvedQty = qty;
            approvedReason = String.valueOf(reasonCombo.getSelectedItem());
            approvedNotes = notesArea.getText() == null ? "" : notesArea.getText().trim();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid qty greater than zero.",
                    "Invalid Qty",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public BigDecimal getApprovedQty() {
        return approvedQty;
    }

    public String getApprovedReason() {
        return approvedReason;
    }

    public String getApprovedNotes() {
        return approvedNotes;
    }
}
