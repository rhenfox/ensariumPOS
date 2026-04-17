package com.aldrin.ensarium.dispense;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ButtonColumn extends DefaultCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

    private static final String KEY_HOVER_ROW = "bootstrap.table.hoverRow";

    private final JTable table;
    private final AbstractAction action;
    private final JButton renderButton;
    private final JButton editButton;
    private final JPanel renderPanel;
    private final JPanel editPanel;

    private Object editorValue;
    private final int column;

    private Icon icon1;
    private Icon icon2;

    private final Color borderColor = new Color(222, 226, 230);
    private final Color rowEvenColor = Color.WHITE;
    private final Color rowOddColor = new Color(248, 249, 250);
    private final Color hoverRowColor = new Color(233, 245, 255);
    private final Color selectionBg = new Color(13, 110, 253);
    private final Color selectionFg = Color.WHITE;

    public ButtonColumn(JTable table, AbstractAction action, int column) {
        super(new JCheckBox());
        this.table = table;
        this.action = action;
        this.column = column;

        renderButton = new JButton();
        editButton = new JButton();

        initButton(renderButton);
        initButton(editButton);

        renderPanel = createPanel(renderButton);
        editPanel = createPanel(editButton);

        editButton.addActionListener(this);

        table.getColumnModel().getColumn(column).setCellRenderer(this);
        table.getColumnModel().getColumn(column).setCellEditor(this);
    }

    private void initButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createPanel(JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.add(button, BorderLayout.CENTER);
        panel.setBorder(createCellBorder());
        return panel;
    }

    public void setIcons(Icon icon1, Icon icon2) {
        this.icon1 = icon1;
        this.icon2 = icon2;
    }

    private int getHoverRow() {
        Object value = table.getClientProperty(KEY_HOVER_ROW);
        return value instanceof Integer ? (Integer) value : -1;
    }

    private Color getRowBackground(int row, boolean isSelected) {
        if (isSelected) {
            return selectionBg;
        }

        if (row == getHoverRow()) {
            return hoverRowColor;
        }

        return (row % 2 == 0) ? rowEvenColor : rowOddColor;
    }

    private Border createCellBorder() {
        Border rowLine = BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor);
        Border padding = BorderFactory.createEmptyBorder(0, 6, 0, 6);
        return BorderFactory.createCompoundBorder(rowLine, padding);
    }

    private void configureButton(JButton button, Object value, boolean editing, boolean selected) {
        button.setForeground(selected ? selectionFg : table.getForeground());

        if (editing && icon2 != null) {
            button.setIcon(icon2);
            button.setText("");
        } else if (icon1 != null) {
            button.setIcon(icon1);
            button.setText("");
        } else {
            button.setIcon(null);
            button.setText(value == null ? "" : value.toString());
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Color bg = getRowBackground(row, isSelected);

        renderPanel.setBackground(bg);
        configureButton(renderButton, value, false, isSelected);

        return renderPanel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        editorValue = value;

        Color bg = getRowBackground(row, true);
        editPanel.setBackground(bg);
        configureButton(editButton, value, true, true);

        return editPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return editorValue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int viewRow = table.getEditingRow();
        int viewCol = table.getEditingColumn();

        if (viewRow < 0 || viewCol < 0) {
            fireEditingCanceled();
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        fireEditingStopped();

        ActionEvent event = new ActionEvent(
                table,
                ActionEvent.ACTION_PERFORMED,
                String.valueOf(modelRow)
        );
        action.actionPerformed(event);
    }
}