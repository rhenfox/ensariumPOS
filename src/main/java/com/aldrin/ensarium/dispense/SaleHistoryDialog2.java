/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.formdev.flatlaf.util.ColorFunctions;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class SaleHistoryDialog2 extends javax.swing.JDialog {

    private final DispenseService service;
    private final ReceiptService receiptService = new ReceiptService();
//    private final JSpinner spnFromDate = new JSpinner(new SpinnerDateModel());
//    private final JSpinner spnToDate = new JSpinner(new SpinnerDateModel());
//    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(200, 1, 1000, 25));
    private final SaleHistoryDialog2.SaleTableModel model = new SaleHistoryDialog2.SaleTableModel();
//    private final JTable table = new JTable(model);

    Icon iconSearch = FaSwingIcons.icon(FontAwesomeIcon.SEARCH, 24, Color.WHITE);
    Icon iconView = FaSwingIcons.icon(FontAwesomeIcon.FILE_TEXT_ALT, 24, Color.WHITE);
    Icon iconReceipt = FaSwingIcons.icon(FontAwesomeIcon.PRINT, 24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);
    Icon iconSun = FaSwingIcons.icon(FontAwesomeIcon.SUN_ALT, 24, Color.WHITE);
    Icon iconCaledar = FaSwingIcons.icon(FontAwesomeIcon.CALENDAR_ALT, 24, Color.WHITE);
    Icon icon30Days = FaSwingIcons.icon(FontAwesomeIcon.CALENDAR, 24, Color.WHITE);

    public SaleHistoryDialog2(java.awt.Window owner, DispenseService service) {
        super(owner, "Sale History", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        initComponents();

        spnFromDate.setEditor(new JSpinner.DateEditor(spnFromDate, "yyyy-MM-dd"));
        spnToDate.setEditor(new JSpinner.DateEditor(spnToDate, "yyyy-MM-dd"));
        initDefaultDates();
        System.out.print("1767838399409400-4--2989990328383777");

        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");

        tablePanel.add(new RoundedScrollPane(table), BorderLayout.CENTER);

        btnReceipt.setSecondary();
        btnClose.setDanger();

        btnFind.setIcon(iconSearch);
        btnView.setIcon(iconView);
        btnReceipt.setIcon(iconReceipt);
        btnClose.setIcon(iconCancel);
        btnToday.setIcon(iconSun);
        btnThisMonth.setIcon(iconCaledar);
        btnLast30.setIcon(icon30Days);

//        btnFind.addActionListener(e -> search());
//        btnToday.addActionListener(e -> {
//            setTodayFilter();
//            search();
//        });
//        btnThisMonth.addActionListener(e -> {
//            setThisMonthFilter();
//            search();
//        });
//        btnLast30.addActionListener(e -> {
//            initDefaultDates();
//            search();
//        });
//        txtSearch.addActionListener(e -> search());
//        btnView.addActionListener(e -> viewDetails());
//        btnReceipt.addActionListener(e -> printSelectedReceipt());
//        btnClose.addActionListener(e -> dispose());
//        table.addMouseListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    viewDetails();
//                }
//            }
//        });
        
        
        
        btnFind.addActionListener(e -> searchWithLoading("Searching sale history..."));

        btnToday.addActionListener(e -> {
            setTodayFilter();
            searchWithLoading("Searching sale history...");
        });

        btnThisMonth.addActionListener(e -> {
            setThisMonthFilter();
            searchWithLoading("Searching sale history...");
        });

        btnLast30.addActionListener(e -> {
            initDefaultDates();
            searchWithLoading("Searching sale history...");
        });

        txtSearch.addActionListener(e -> searchWithLoading("Searching sale history..."));

        btnView.addActionListener(e -> viewDetailsWithLoading());
        btnReceipt.addActionListener(e -> printSelectedReceiptWithLoading());
        btnClose.addActionListener(e -> dispose());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDetailsWithLoading();
                }
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnCenter(table, 4);
        BootstrapTableStyle.setColumnRight(table, 5);

        BootstrapTableStyle.setHeaderLeft(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderCenter(table, 4);
        BootstrapTableStyle.setHeaderRight(table, 5);
        
        installTableHover();

//        search();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        spnFromDate = new javax.swing.JSpinner(new SpinnerDateModel());
        jLabel3 = new javax.swing.JLabel();
        spnToDate = new javax.swing.JSpinner(new SpinnerDateModel());
        jLabel1 = new javax.swing.JLabel();
        spLimit = new javax.swing.JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
        btnFind = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnToday = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnThisMonth = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnLast30 = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel4 = new javax.swing.JPanel();
        btnView = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnReceipt = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnClose = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        tablePanel.setLayout(new java.awt.BorderLayout());
        jPanel2.add(tablePanel, java.awt.BorderLayout.CENTER);

        jPanel8.setPreferredSize(new java.awt.Dimension(10, 292));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 434, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel8, java.awt.BorderLayout.WEST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(0, 55));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 10));

        txtSearch.setPreferredSize(new java.awt.Dimension(300, 30));
        jPanel3.add(txtSearch);

        jLabel2.setText("From");
        jPanel3.add(jLabel2);

        spnFromDate.setPreferredSize(new java.awt.Dimension(100, 28));
        jPanel3.add(spnFromDate);

        jLabel3.setText("To");
        jPanel3.add(jLabel3);

        spnToDate.setPreferredSize(new java.awt.Dimension(100, 28));
        jPanel3.add(spnToDate);

        jLabel1.setText("Limit");
        jPanel3.add(jLabel1);

        spLimit.setPreferredSize(new java.awt.Dimension(64, 28));
        jPanel3.add(spLimit);

        btnFind.setText("Find");
        jPanel3.add(btnFind);

        btnToday.setText("Today");
        jPanel3.add(btnToday);

        btnThisMonth.setText("This month");
        jPanel3.add(btnThisMonth);

        btnLast30.setText("Last 30 days");
        jPanel3.add(btnLast30);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel4.setPreferredSize(new java.awt.Dimension(0, 60));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        btnView.setText("View Details");
        jPanel4.add(btnView);

        btnReceipt.setText("Print Receipt");
        jPanel4.add(btnReceipt);

        btnClose.setText("Close");
        jPanel4.add(btnClose);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel6.setPreferredSize(new java.awt.Dimension(10, 407));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 549, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel6, java.awt.BorderLayout.EAST);

        setSize(new java.awt.Dimension(1182, 558));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnClose;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnFind;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnLast30;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnReceipt;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnThisMonth;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnToday;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnView;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JSpinner spLimit;
    private javax.swing.JSpinner spnFromDate;
    private javax.swing.JSpinner spnToDate;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    public void initDefaultDates() {
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

//    public void search() {
//        try {
//            Timestamp from = startOfDay((Date) spnFromDate.getValue());
//            Timestamp toExclusive = startOfNextDay((Date) spnToDate.getValue());
//            int limit = ((Number) spLimit.getValue()).intValue();
//            model.setRows(service.findHistory(txtSearch.getText().trim(), from, toExclusive, limit));
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    public void search() {
        searchWithLoading("Searching sale history...");
    }

    private SaleHistoryRow selectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return model.get(table.convertRowIndexToModel(row));
    }

    public void viewDetails() {
        SaleHistoryRow row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a sale first.", "View Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            SaleDetailView detail = service.getSaleDetail(row.saleId);
            new SaleDetailDialog2(this, detail).setVisible(true);
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

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm a");

    private static class SaleTableModel extends AbstractTableModel {

        private final String[] cols = {"Sale No", "Date", "Customer", "Invoice", "Returns", "Total"};
        private List<SaleHistoryRow> rows = new ArrayList<>();

        public void setRows(List<SaleHistoryRow> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public SaleHistoryRow get(int i) {
            return rows.get(i);
        }

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
            SaleHistoryRow x = rows.get(r);
            return switch (c) {
                case 0 ->
                    x.saleNo;
                case 1 ->
                    x.soldAt.toLocalDateTime().format(formatter);
                case 2 ->
                    x.customerName;
                case 3 ->
                    x.invoiceNo == null ? "-" : x.invoiceNo;
                case 4 ->
                    x.returnCount;
                default ->
                    x.total.setScale(2, RoundingMode.HALF_UP);
            };
        }
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

    public void loadHistoryData() throws Exception {
        Timestamp from = startOfDay((Date) spnFromDate.getValue());
        Timestamp toExclusive = startOfNextDay((Date) spnToDate.getValue());
        int limit = ((Number) spLimit.getValue()).intValue();
        model.setRows(service.findHistory(txtSearch.getText().trim(), from, toExclusive, limit));

    }

    private void setBusy(boolean busy) {
        btnFind.setEnabled(!busy);
        btnToday.setEnabled(!busy);
        btnThisMonth.setEnabled(!busy);
        btnLast30.setEnabled(!busy);
        btnView.setEnabled(!busy);
        btnReceipt.setEnabled(!busy);
        btnClose.setEnabled(!busy);

        txtSearch.setEnabled(!busy);
        spnFromDate.setEnabled(!busy);
        spnToDate.setEnabled(!busy);
        spLimit.setEnabled(!busy);
        table.setEnabled(!busy);
    }

    private List<SaleHistoryRow> loadHistoryRows() throws Exception {
        Timestamp from = startOfDay((Date) spnFromDate.getValue());
        Timestamp toExclusive = startOfNextDay((Date) spnToDate.getValue());
        int limit = ((Number) spLimit.getValue()).intValue();
        return service.findHistory(txtSearch.getText().trim(), from, toExclusive, limit);
    }

    private void searchWithLoading(String message) {
        SaleHistoryDialog2.DonutLoadingDialog loading
                = new SaleHistoryDialog2.DonutLoadingDialog(this, message);

        setBusy(true);

        SwingWorker<List<SaleHistoryRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SaleHistoryRow> doInBackground() throws Exception {
                return loadHistoryRows();
            }

            @Override
            protected void done() {
                try {
                    model.setRows(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SaleHistoryDialog2.this,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                    loading.dispose();
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }

    private void viewDetailsWithLoading() {
        SaleHistoryRow row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a sale first.", "View Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SaleHistoryDialog2.DonutLoadingDialog loading
                = new SaleHistoryDialog2.DonutLoadingDialog(this, "Loading sale details...");

        setBusy(true);

        SwingWorker<SaleDetailView, Void> worker = new SwingWorker<>() {
            @Override
            protected SaleDetailView doInBackground() throws Exception {
                return service.getSaleDetail(row.saleId);
            }

            @Override
            protected void done() {
                try {
                    SaleDetailView detail = get();
                    new SaleDetailDialog2(SaleHistoryDialog2.this, detail).setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SaleHistoryDialog2.this,
                            ex.getMessage(),
                            "View Details Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                    loading.dispose();
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }

    private void printSelectedReceiptWithLoading() {
        SaleHistoryRow row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a sale first.", "Receipt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SaleHistoryDialog2.DonutLoadingDialog loading
                = new SaleHistoryDialog2.DonutLoadingDialog(this, "Loading receipt...");

        setBusy(true);
        loading.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            try {
                receiptService.showSaleReceiptDialog(SaleHistoryDialog2.this, row.saleId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        SaleHistoryDialog2.this,
                        ex.getMessage(),
                        "Receipt Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                setBusy(false);
                loading.dispose();
            }
        });
    }

    static class DonutLoadingDialog extends JDialog {

        private final DonutSpinner spinner;

        public DonutLoadingDialog(Window owner, String message) {
            super(owner, "Loading", Dialog.ModalityType.MODELESS);
            setUndecorated(true);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD0D7DE)),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            panel.setBackground(Color.WHITE);

            spinner = new DonutSpinner();

            JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
            lblMessage.setForeground(new Color(60, 60, 60));

            panel.add(spinner, BorderLayout.CENTER);
            panel.add(lblMessage, BorderLayout.SOUTH);

            setContentPane(panel);
            setSize(220, 180);
            setLocationRelativeTo(owner);

            spinner.start();
        }

        @Override
        public void dispose() {
            spinner.stop();
            super.dispose();
        }
    }

    static class DonutSpinner extends JPanel {

        private int angle = 0;
        private final Timer timer;

        public DonutSpinner() {
            setOpaque(false);
            setPreferredSize(new Dimension(90, 90));

            timer = new Timer(20, e -> {
                angle += 8;
                if (angle >= 360) {
                    angle = 0;
                }
                repaint();
            });
        }

        public void start() {
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        public void stop() {
            if (timer.isRunning()) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 14;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // background ring
            g2.setColor(new Color(230, 235, 240));
            g2.drawArc(x, y, size, size, 0, 360);

            // animated arc
            g2.setColor(new Color(0, 120, 215));
            g2.drawArc(x, y, size, size, angle, 110);

            g2.dispose();
        }
    }
    
    

}
