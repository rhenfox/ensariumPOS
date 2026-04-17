package com.aldrin.ensarium.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public final class UIUtils {

    private UIUtils() {
    }

    public static JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, JButton... buttons) {
        JPanel outer = new JPanel(new BorderLayout(10, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel("Search"));
        left.add(txtSearch);
        left.add(new JLabel("Limit"));
        left.add(spnLimit);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (JButton button : buttons) {
            right.add(button);
        }

        outer.add(left, BorderLayout.WEST);
        outer.add(right, BorderLayout.EAST);
        return outer;
    }

    public static JPanel simpleForm(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(fields[i], gbc);
        }

        return panel;
    }

    public static JPanel buttonBar(JButton... buttons) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        panel.add(Box.createHorizontalGlue());
        for (int i = 0; i < buttons.length; i++) {
            if (i > 0) {
                panel.add(Box.createHorizontalStrut(8));
            }
            panel.add(buttons[i]);
        }
        return panel;
    }

    public static void hideColumn(JTable table, int modelIndex) {
        TableColumnModel columnModel = table.getColumnModel();
        int viewIndex = table.convertColumnIndexToView(modelIndex);
        if (viewIndex >= 0) {
            columnModel.removeColumn(columnModel.getColumn(viewIndex));
        }
    }

    public static void centerHeader(JTable table, int modelIndex) {
        int viewIndex = table.convertColumnIndexToView(modelIndex);
        if (viewIndex < 0) {
            return;
        }
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static Long selectedLongId(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = model.getValueAt(modelRow, 0);
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    public static void reselect(JTable table, DefaultTableModel model, Long keepId) {
        if (keepId == null) {
            return;
        }
        for (int modelRow = 0; modelRow < model.getRowCount(); modelRow++) {
            Object value = model.getValueAt(modelRow, 0);
            if (Objects.equals(String.valueOf(value), String.valueOf(keepId))) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                }
                break;
            }
        }
    }

    public static void applyTableStyle(JTable table) {
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static JScrollPane scrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        return scrollPane;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message, Exception ex) {
        JOptionPane.showMessageDialog(parent,
                message + "\n\n" + (ex == null ? "" : ex.getMessage()),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
