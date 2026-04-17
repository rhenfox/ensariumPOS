package com.aldrin.ensarium.dispense;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

public class SaleHistoryDialog extends JDialog {
    private final DispenseService service;
    private final ReceiptService receiptService = new ReceiptService();
    private final JTextField txtSearch = new JTextField();
    private final JSpinner spnFromDate = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnToDate = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 25));
    private final SaleTableModel model = new SaleTableModel();
    private final JTable table = new JTable(model);

    public SaleHistoryDialog(java.awt.Window owner, DispenseService service) {
        super(owner, "Sale History", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        setLayout(new BorderLayout(8, 8));

        spnFromDate.setEditor(new JSpinner.DateEditor(spnFromDate, "yyyy-MM-dd"));
        spnToDate.setEditor(new JSpinner.DateEditor(spnToDate, "yyyy-MM-dd"));
        initDefaultDates();

        JPanel top = new JPanel(new BorderLayout(6, 6));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnFind = new JButton("Find");
        JButton btnToday = new JButton("Today");
        JButton btnThisMonth = new JButton("This Month");
        JButton btnLast30 = new JButton("Last 30 Days");

        filters.add(new JLabel("Search"));
        txtSearch.setColumns(24);
        filters.add(txtSearch);
        filters.add(new JLabel("From"));
        filters.add(spnFromDate);
        filters.add(new JLabel("To"));
        filters.add(spnToDate);
        filters.add(new JLabel("Rows"));
        filters.add(spnLimit);
        filters.add(btnFind);
        filters.add(btnToday);
        filters.add(btnThisMonth);
        filters.add(btnLast30);
        top.add(filters, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        JButton btnView = new JButton("View Details");
        JButton btnReceipt = new JButton("Print Selected Receipt");
        JButton btnClose = new JButton("Close");
        bottom.add(btnView);
        bottom.add(btnReceipt);
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        btnFind.addActionListener(e -> search());
        btnToday.addActionListener(e -> { setTodayFilter(); search(); });
        btnThisMonth.addActionListener(e -> { setThisMonthFilter(); search(); });
        btnLast30.addActionListener(e -> { initDefaultDates(); search(); });
        txtSearch.addActionListener(e -> search());
        btnView.addActionListener(e -> viewDetails());
        btnReceipt.addActionListener(e -> printSelectedReceipt());
        btnClose.addActionListener(e -> dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewDetails();
            }
        });
        setSize(1180, 460);
        setLocationRelativeTo(owner);
        search();
    }

    private void initDefaultDates() {
        LocalDate today = LocalDate.now();
        setSpinnerDate(spnFromDate, java.sql.Date.valueOf(today.minusDays(30)));
        setSpinnerDate(spnToDate, java.sql.Date.valueOf(today));
    }

    private void setTodayFilter() {
        LocalDate today = LocalDate.now();
        setSpinnerDate(spnFromDate, java.sql.Date.valueOf(today));
        setSpinnerDate(spnToDate, java.sql.Date.valueOf(today));
    }

    private void setThisMonthFilter() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        setSpinnerDate(spnFromDate, java.sql.Date.valueOf(first));
        setSpinnerDate(spnToDate, java.sql.Date.valueOf(today));
    }

    private static void setSpinnerDate(JSpinner spinner, Date date) {
        spinner.setValue(date);
    }

    private Timestamp startOfDay(Date date) {
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Timestamp.valueOf(ld.atStartOfDay());
    }

    private Timestamp startOfNextDay(Date date) {
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1);
        return Timestamp.valueOf(ld.atStartOfDay());
    }

    private void search() {
        try {
            Timestamp from = startOfDay((Date) spnFromDate.getValue());
            Timestamp toExclusive = startOfNextDay((Date) spnToDate.getValue());
            int limit = ((Number) spnLimit.getValue()).intValue();
            model.setRows(service.findHistory(txtSearch.getText().trim(), from, toExclusive, limit));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SaleHistoryRow selectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return model.get(table.convertRowIndexToModel(row));
    }

    private void viewDetails() {
        SaleHistoryRow row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a sale first.", "View Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            SaleDetailView detail = service.getSaleDetail(row.saleId);
            new SaleDetailDialog(this, detail).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "View Details Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printSelectedReceipt() {
        SaleHistoryRow row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a sale first.", "Receipt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            receiptService.showSaleReceiptDialog(this, row.saleId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Receipt Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showDialog() {
        setVisible(true);
    }

    private static class SaleTableModel extends AbstractTableModel {
        private final String[] cols = {"Sale No", "Date", "Customer", "Invoice", "Returns", "Total"};
        private List<SaleHistoryRow> rows = new ArrayList<>();
        public void setRows(List<SaleHistoryRow> rows) { this.rows = rows; fireTableDataChanged(); }
        public SaleHistoryRow get(int i) { return rows.get(i); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            SaleHistoryRow x = rows.get(r);
            return switch (c) {
                case 0 -> x.saleNo;
                case 1 -> x.soldAt;
                case 2 -> x.customerName;
                case 3 -> x.invoiceNo == null ? "-" : x.invoiceNo;
                case 4 -> x.returnCount;
                default -> x.total;
            };
        }
    }
}
