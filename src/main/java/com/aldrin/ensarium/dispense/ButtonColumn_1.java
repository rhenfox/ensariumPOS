package com.aldrin.ensarium.dispense;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ButtonColumn_1 extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private final JTable table;
    private final Action action;
    private final JButton renderButton = new JButton();
    private final JButton editButton = new JButton();
    private Icon iconNormal;
    private Icon iconHover;
    private int editingRow = -1;
    private int editingColumn = -1;
    private boolean isButtonColumnEditor;

    public ButtonColumn_1(JTable table, Action action, int column) {
        this.table = table;
        this.action = action;

        configureBase(renderButton);
        configureBase(editButton);

        table.getColumnModel().getColumn(column).setCellRenderer(this);
        table.getColumnModel().getColumn(column).setCellEditor(this);

        editButton.addActionListener((ActionEvent e) -> {
            int row = editingRow;
            fireEditingStopped();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                SwingUtilities.invokeLater(() -> action.actionPerformed(
                        new ActionEvent(table, ActionEvent.ACTION_PERFORMED, String.valueOf(modelRow))));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (table.isEditing() && table.getCellEditor() == ButtonColumn_1.this) {
                    isButtonColumnEditor = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isButtonColumnEditor && table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                isButtonColumnEditor = false;
            }
        });
    }

    private void configureBase(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    public void setIcons(Icon normal, Icon hover) {
        this.iconNormal = normal;
        this.iconHover = hover;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        configure(renderButton, value, row, column);
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editingRow = row;
        editingColumn = column;
        configure(editButton, value, row, column);
        return editButton;
    }

    private void configure(JButton button, Object value, int row, int column) {
        Point hover = (Point) table.getClientProperty("hoverCell");
        boolean hovered = hover != null && hover.x == row && hover.y == column;
        Icon chosen = hovered && iconHover != null ? iconHover : iconNormal;
        button.setIcon(chosen);
        String fallback = value == null ? "" : String.valueOf(value);
        button.setText(chosen == null ? fallback : "");
        if (editingRow == row && editingColumn == column) {
            button.setToolTipText(fallback);
        } else {
            button.setToolTipText(fallback);
        }
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}
