package com.aldrin.ensarium.dispense;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

public class HotkeySettingsDialog extends JDialog {
    private final HotkeyService service;
    private final HotkeyTableModel model = new HotkeyTableModel();
    private final JTable table = new JTable(model);
    private final JTextField txtCapture = new JTextField();
    private boolean changed;

    public HotkeySettingsDialog(java.awt.Window owner, HotkeyService service) throws Exception {
        super(owner, "Hotkey Settings", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        setLayout(new BorderLayout(10, 10));

        JLabel lblInfo = new JLabel("Select an action, click Capture, then press the new key combination.");
        add(lblInfo, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel capturePanel = new JPanel(new BorderLayout(8, 8));
        txtCapture.setEditable(false);
        txtCapture.setHorizontalAlignment(SwingConstants.CENTER);
        txtCapture.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtCapture.setPreferredSize(new Dimension(240, 42));
        JButton btnCapture = new JButton("Capture");
        JButton btnReset = new JButton("Reset Selected");
        capturePanel.add(txtCapture, BorderLayout.CENTER);
        JPanel captureButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        captureButtons.add(btnCapture);
        captureButtons.add(btnReset);
        capturePanel.add(captureButtons, BorderLayout.EAST);
        add(capturePanel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSaveAll = new JButton("Save All");
        JButton btnClose = new JButton("Close");
        bottom.add(btnSaveAll);
        bottom.add(btnClose);
        capturePanel.add(bottom, BorderLayout.SOUTH);

        loadRows();
        table.getSelectionModel().addListSelectionListener(e -> syncSelection());
        btnCapture.addActionListener(e -> captureSelected());
        btnReset.addActionListener(e -> resetSelected());
        btnSaveAll.addActionListener(e -> saveAll());
        btnClose.addActionListener(e -> dispose());

        setSize(720, 420);
        setLocationRelativeTo(owner);
        if (model.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
    }

    private void loadRows() throws Exception {
        Map<String, KeyStroke> keys = service.loadEffectiveHotkeys();
        Map<String, String> labels = HotkeyService.actionLabels();
        model.rows.clear();
        for (Map.Entry<String, KeyStroke> e : keys.entrySet()) {
            HotkeyRow r = new HotkeyRow();
            r.action = e.getKey();
            r.actionLabel = labels.getOrDefault(e.getKey(), e.getKey());
            r.keyStroke = e.getValue();
            r.defaultKeyStroke = HotkeyService.defaultHotkeys().get(e.getKey());
            model.rows.add(r);
        }
        model.fireTableDataChanged();
    }

    private void syncSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            txtCapture.setText("");
            return;
        }
        txtCapture.setText(HotkeyService.toHuman(model.rows.get(row).keyStroke));
    }

    private void captureSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an action first.");
            return;
        }
        KeyStroke ks = captureKeyStroke();
        if (ks == null) return;
        for (int i = 0; i < model.rows.size(); i++) {
            if (i != row && ks.equals(model.rows.get(i).keyStroke)) {
                JOptionPane.showMessageDialog(this,
                        "That shortcut is already assigned to " + model.rows.get(i).actionLabel + ". Choose another one.");
                return;
            }
        }
        model.rows.get(row).keyStroke = ks;
        model.fireTableRowsUpdated(row, row);
        syncSelection();
    }

    private KeyStroke captureKeyStroke() {
        final KeyStroke[] captured = new KeyStroke[1];
        JDialog dlg = new JDialog(this, "Press Shortcut", true);
        dlg.setLayout(new BorderLayout(8, 8));
        JLabel lbl = new JLabel("Press the shortcut now...", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dlg.add(lbl, BorderLayout.CENTER);
        JTextField field = new JTextField();
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.BOLD, 20));
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                captured[0] = KeyStroke.getKeyStrokeForEvent(e);
                field.setText(HotkeyService.toHuman(captured[0]));
                e.consume();
                dlg.dispose();
            }
        });
        dlg.add(field, BorderLayout.SOUTH);
        dlg.setSize(360, 160);
        dlg.setLocationRelativeTo(this);
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                field.requestFocusInWindow();
            }
        });
        dlg.setVisible(true);
        return captured[0];
    }

    private void resetSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        HotkeyRow r = model.rows.get(row);
        r.keyStroke = r.defaultKeyStroke;
        model.fireTableRowsUpdated(row, row);
        syncSelection();
    }

    private void saveAll() {
        try {
            for (HotkeyRow r : model.rows) {
                service.saveHotkey(r.action, r.keyStroke, true);
            }
            changed = true;
            JOptionPane.showMessageDialog(this, "Hotkeys saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return changed;
    }

    private static class HotkeyRow {
        String action;
        String actionLabel;
        KeyStroke keyStroke;
        KeyStroke defaultKeyStroke;
    }

    private static class HotkeyTableModel extends AbstractTableModel {
        private final String[] cols = {"Action", "Key Stroke", "Default"};
        private final List<HotkeyRow> rows = new ArrayList<>();
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            HotkeyRow x = rows.get(r);
            return switch (c) {
                case 0 -> x.actionLabel;
                case 1 -> HotkeyService.toHuman(x.keyStroke);
                default -> HotkeyService.toHuman(x.defaultKeyStroke);
            };
        }
    }
}
