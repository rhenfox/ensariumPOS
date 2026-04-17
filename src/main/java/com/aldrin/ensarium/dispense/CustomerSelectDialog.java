package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

public class CustomerSelectDialog extends JDialog {
    private final CustomerDao dao = new CustomerDao();
    private final JTextField txtSearch = new JTextField();
    private final CustomerTableModel model = new CustomerTableModel();
    private final JTable table = new JTable(model);
    private CustomerRef result;
    private boolean cleared;

    public CustomerSelectDialog(java.awt.Window owner) {
        super(owner, "Select Customer", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(txtSearch, BorderLayout.CENTER);
        JButton btnFind = new JButton("Find");
        top.add(btnFind, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        JButton btnSelect = new JButton("Select");
        JButton btnWalkIn = new JButton("Walk-in");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnSelect); bottom.add(btnWalkIn); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);
        btnFind.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        btnSelect.addActionListener(e -> choose());
        btnWalkIn.addActionListener(e -> { cleared = true; dispose(); });
        btnCancel.addActionListener(e -> dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) choose(); }
        });
        setSize(860, 400);
        setLocationRelativeTo(owner);
        search();
    }

    private void search() {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            model.setRows(dao.search(conn, txtSearch.getText().trim()));
            conn.commit();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void choose() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        result = model.get(row);
        dispose();
    }

    public CustomerRef showDialog() {
        setVisible(true);
        return result;
    }

    public boolean isCleared() { return cleared; }

    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] cols = {"No", "Name", "Type", "TIN", "Address"};
        private List<CustomerRef> rows = new ArrayList<>();
        public void setRows(List<CustomerRef> rows) { this.rows = rows; fireTableDataChanged(); }
        public CustomerRef get(int i) { return rows.get(i); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            CustomerRef x = rows.get(r);
            return switch (c) {
                case 0 -> x.customerNo;
                case 1 -> x.fullName;
                case 2 -> x.benefitLabel();
                case 3 -> x.tinNo;
                default -> x.address;
            };
        }
    }
}
