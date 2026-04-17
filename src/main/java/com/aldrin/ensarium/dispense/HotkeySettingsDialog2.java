/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class HotkeySettingsDialog2 extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HotkeySettingsDialog2.class.getName());

    private final HotkeyService service;
    private final HotkeySettingsDialog2.HotkeyTableModel model = new HotkeySettingsDialog2.HotkeyTableModel();
//    private final JTable table = new JTable(model);
//    private final JTextField txtCapture = new JTextField();
    private boolean changed;

//    public HotkeySettingsDialog2(java.awt.Frame parent, boolean modal) {
//        super(parent, modal);
//        initComponents();
//    }
    public HotkeySettingsDialog2(java.awt.Window owner, HotkeyService service) throws Exception {
        super(owner, "Hotkey Settings", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        initComponents();

//        JLabel lblInfo = new JLabel("Select an action, click Capture, then press the new key combination.");
//        add(lblInfo, BorderLayout.NORTH);

        tablePanel.add(new RoundedScrollPane(table), BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
//         add(new JScrollPane(table), BorderLayout.CENTER);

        btnClose.setDanger();

        Icon iconKeyboard = FaSwingIcons.icon(FontAwesomeIcon.KEYBOARD_ALT, 18, Color.WHITE);
        Icon iconReset = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 18, Color.WHITE);
        Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, Color.WHITE);
        Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, Color.WHITE);

        btnCapture.setIcon(iconKeyboard);
        btnReset.setIcon(iconReset);
        btnSaveAll.setIcon(iconSave);
        btnClose.setIcon(iconCancel);

        loadRows();
        table.getSelectionModel().addListSelectionListener(e -> syncSelection());
        btnCapture.addActionListener(e -> captureSelected());
        btnReset.addActionListener(e -> resetSelected());
        btnSaveAll.addActionListener(e -> saveAll());
        btnClose.addActionListener(e -> dispose());

//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

//        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xD6E9FF));
        table.setSelectionForeground(Color.BLACK);

        table.getColumnModel().getColumn(0).setCellRenderer(left);
        table.getColumnModel().getColumn(1).setCellRenderer(left);
        table.getColumnModel().getColumn(2).setCellRenderer(left);

        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE))
        );

        final TableCellRenderer hdrRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = hdrRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);

            if (c instanceof JLabel l) {
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE)));
                if (col == 0) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 1) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 2) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }
            }
            return c;
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        installTableHover();
    }

    private int hoveredRow = -1;
    private final JTable table = new JTable(model) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            if (!isRowSelected(row)) {
                if (row == hoveredRow) {
                    c.setBackground(new Color(0xE8F2FF)); // hover color
                } else {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(0xF7F9FC)); // stripes
                }
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(getSelectionBackground());
                c.setForeground(getSelectionForeground());
            }

            if (c instanceof JComponent jc) {
                jc.setOpaque(true);
            }

            return c;
        }
    };

    private void installTableHover() {
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    table.repaint();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtCapture = new javax.swing.JTextField();
        btnCapture = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnReset = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnSaveAll = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnClose = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        tablePanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(tablePanel, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(0, 30));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 10));

        jLabel1.setText("Select an action, click Capture, then press   the new key combination.");
        jPanel3.add(jLabel1);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        txtCapture.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCapture.setText("F1");
        txtCapture.setPreferredSize(new java.awt.Dimension(100, 22));
        jPanel4.add(txtCapture);

        btnCapture.setText("Capture");
        jPanel4.add(btnCapture);

        btnReset.setText("Reset selected");
        jPanel4.add(btnReset);

        btnSaveAll.setText("Select all");
        jPanel4.add(btnSaveAll);

        btnClose.setText("Cancel");
        jPanel4.add(btnClose);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 428));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setPreferredSize(new java.awt.Dimension(10, 100));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel6, java.awt.BorderLayout.EAST);

        setSize(new java.awt.Dimension(685, 437));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCapture;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnClose;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnReset;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnSaveAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextField txtCapture;
    // End of variables declaration//GEN-END:variables

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
        if (ks == null) {
            return;
        }
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
            @Override
            public void keyPressed(KeyEvent e) {
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
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                field.requestFocusInWindow();
            }
        });
        dlg.setVisible(true);
        return captured[0];
    }

    private void resetSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
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

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            HotkeyRow x = rows.get(r);
            return switch (c) {
                case 0 ->
                    x.actionLabel;
                case 1 ->
                    HotkeyService.toHuman(x.keyStroke);
                default ->
                    HotkeyService.toHuman(x.defaultKeyStroke);
            };
        }
    }

}
