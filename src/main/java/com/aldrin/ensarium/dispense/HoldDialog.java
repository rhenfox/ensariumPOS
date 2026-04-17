package com.aldrin.ensarium.dispense;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class HoldDialog extends JDialog {
    private final DispenseService service;
    private final HoldTableModel model = new HoldTableModel();
    private final JTable table = new JTable(model);
    private Long result;

    public HoldDialog(java.awt.Window owner, DispenseService service) throws Exception {
        super(owner, "Held Tickets", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        JButton btnLoad = new JButton("Load");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnLoad); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);
        btnLoad.addActionListener(e -> choose());
        btnCancel.addActionListener(e -> dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) choose(); }
        });
        model.setRows(service.listHolds());
        setSize(650, 320);
        setLocationRelativeTo(owner);
    }

    private void choose() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        result = model.get(modelRow).id;
        dispose();
    }

    public Long showDialog() {
        setVisible(true);
        return result;
    }

    private static class HoldTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Held At", "Customer", "Items"};
        private List<HeldSale> rows = new ArrayList<>();
        public void setRows(List<HeldSale> rows) { this.rows = rows; fireTableDataChanged(); }
        public HeldSale get(int i) { return rows.get(i); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            HeldSale h = rows.get(r);
            return switch (c) {
                case 0 -> h.id;
                case 1 -> h.heldAt;
                case 2 -> h.customerName == null ? "Walk-in" : h.customerName;
                default -> h.lines.size();
            };
        }
    }
}
