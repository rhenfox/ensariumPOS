package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SaleDetailsDialog extends JDialog {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    public SaleDetailsDialog(java.awt.Window owner, String title, String headerText, DefaultTableModel model) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        initUi(headerText, model);
    }

    private void initUi(String headerText, DefaultTableModel model) {
        setLayout(new BorderLayout(8, 8));

        JLabel header = new JLabel("<html><b>" + escape(headerText) + "</b></html>");
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(header, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(24);
        installRenderers(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel(" ");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        int netCol = findColumn(model, "Net Amount");
        if (netCol >= 0) {
            BigDecimal total = BigDecimal.ZERO;
            for (int row = 0; row < model.getRowCount(); row++) {
                Object value = model.getValueAt(row, netCol);
                if (value instanceof Number number) {
                    total = total.add(BigDecimal.valueOf(number.doubleValue()));
                } else if (value != null) {
                    try {
                        total = total.add(new BigDecimal(value.toString()));
                    } catch (Exception ignored) {
                    }
                }
            }
            totalLabel.setText("Grand Total Net Amount: " + MONEY.format(total));
        }
        south.add(totalLabel);
        add(south, BorderLayout.SOUTH);

        setSize(980, 460);
        setLocationRelativeTo(getOwner());
    }

    private void installRenderers(JTable table) {
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number number) {
                    setText(MONEY.format(number.doubleValue()));
                } else {
                    super.setValue(value);
                }
            }
        };
        numberRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.setDefaultRenderer(Number.class, numberRenderer);
    }

    private int findColumn(DefaultTableModel model, String columnName) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (columnName.equals(model.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
