/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.aldrin.ensarium.shift;

import com.aldrin.ensarium.db.AppConfig;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class ShiftPanel2 extends javax.swing.JPanel {

    /**
     * Creates new form ShiftPanel2
     */
    private final ShiftDao shiftDao = new ShiftDao();
    private final SalesDao salesDao = new SalesDao();

    private Shift openShift;

    private final TotalsTableModel totalsModel = new TotalsTableModel();
    private final JTable table = new JTable(totalsModel);
    private final JButton btnViewDetails = new JButton("View Details");
    private final JPopupMenu tablePopup = new JPopupMenu();

    private final NumberFormat money = NumberFormat.getNumberInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm a");
    private final com.aldrin.ensarium.security.Session session;

    public ShiftPanel2(com.aldrin.ensarium.security.Session session) throws SQLException {
        this.session = session;
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);
        initComponents();

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        hideColumn(0);

        Icon iconOpenShift = FaSwingIcons.icon(FontAwesomeIcon.SIGN_IN, 24, Color.WHITE);
        Icon iconCloseShift = FaSwingIcons.icon(FontAwesomeIcon.SIGN_OUT, 24, Color.WHITE);
        Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);

        btnOpenShift.setIcon(iconOpenShift);
        btnCloseShift.setIcon(iconCloseShift);
        btnRefresh.setIcon(iconRefresh);

        refreshHeaderLabels();

        tablePanel.add(new com.aldrin.ensarium.ui.widgets.RoundedScrollPane(table), BorderLayout.CENTER);
        refreshData();

        btnOpenShift.addActionListener(e -> openShift());
        btnCloseShift.addActionListener(e -> closeShift());
        btnRefresh.addActionListener(e -> refreshData());
        installTablePopup();

        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnRight(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnRight(table, 3);

        BootstrapTableStyle.setHeaderRight(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderRight(table, 3);

        table.setFocusable(false);
        installTableHover();
    }

    private int hoveredRow = -1;

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

    private void hideColumn(int i) {
        table.getColumnModel().getColumn(i).setMinWidth(0);
        table.getColumnModel().getColumn(i).setMaxWidth(0);
        table.getColumnModel().getColumn(i).setPreferredWidth(0);
        table.getColumnModel().getColumn(i).setResizable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        lblStoreTerminal = new javax.swing.JLabel();
        lblShift = new javax.swing.JLabel();
        lblOpenedAt = new javax.swing.JLabel();
        lblSalesCount = new javax.swing.JLabel();
        lblDispenseTotal = new javax.swing.JLabel();
        lblGrandTotal = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        btnOpenShift = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnCloseShift = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnRefresh = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.BorderLayout());

        tablePanel.setLayout(new java.awt.BorderLayout());
        jPanel6.add(tablePanel, java.awt.BorderLayout.CENTER);

        jPanel9.setPreferredSize(new java.awt.Dimension(1282, 10));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1348, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel6.add(jPanel9, java.awt.BorderLayout.NORTH);

        jPanel1.add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel7.setPreferredSize(new java.awt.Dimension(1282, 200));
        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel17.setPreferredSize(new java.awt.Dimension(220, 200));
        jPanel17.setLayout(new java.awt.GridLayout(0, 1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("User:");
        jPanel17.add(jLabel1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Store/Terminal:");
        jPanel17.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Shift:");
        jPanel17.add(jLabel3);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Opened at:");
        jPanel17.add(jLabel4);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Sales count:");
        jPanel17.add(jLabel5);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Total dispense (sales):");
        jPanel17.add(jLabel6);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Grand total payments received:");
        jPanel17.add(jLabel7);

        jPanel7.add(jPanel17, java.awt.BorderLayout.WEST);

        jPanel18.setLayout(new java.awt.GridLayout(0, 1));

        lblUser.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUser.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblUser);

        lblStoreTerminal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStoreTerminal.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblStoreTerminal);

        lblShift.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblShift.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblShift);

        lblOpenedAt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblOpenedAt.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblOpenedAt);

        lblSalesCount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSalesCount.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblSalesCount);

        lblDispenseTotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDispenseTotal.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblDispenseTotal);

        lblGrandTotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblGrandTotal.setPreferredSize(new java.awt.Dimension(500, 16));
        jPanel18.add(lblGrandTotal);

        jPanel7.add(jPanel18, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(1302, 45));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("USER SHIFT DETAILS");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel8.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel8.add(jLabel8);

        jPanel2.add(jPanel8, java.awt.BorderLayout.CENTER);

        jPanel10.setPreferredSize(new java.awt.Dimension(1368, 10));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1368, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel10, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setPreferredSize(new java.awt.Dimension(1302, 40));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        btnOpenShift.setText("Open shift");
        jPanel3.add(btnOpenShift);

        btnCloseShift.setText("Close shift");
        btnCloseShift.addActionListener(this::btnCloseShiftActionPerformed);
        jPanel3.add(btnCloseShift);

        btnRefresh.setText("Refresh");
        jPanel3.add(btnRefresh);

        add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel4.setPreferredSize(new java.awt.Dimension(10, 594));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 522, Short.MAX_VALUE)
        );

        add(jPanel4, java.awt.BorderLayout.EAST);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 594));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 522, Short.MAX_VALUE)
        );

        add(jPanel5, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseShiftActionPerformed
//      refreshSessionInfo();
    }//GEN-LAST:event_btnCloseShiftActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCloseShift;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnOpenShift;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnRefresh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lblDispenseTotal;
    private javax.swing.JLabel lblGrandTotal;
    private javax.swing.JLabel lblOpenedAt;
    private javax.swing.JLabel lblSalesCount;
    private javax.swing.JLabel lblShift;
    private javax.swing.JLabel lblStoreTerminal;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel tablePanel;
    // End of variables declaration//GEN-END:variables
    private void installTablePopup() {
        JMenuItem miViewDetails = new JMenuItem("View Details");
        Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.SEARCH, 24, new Color(0xD0D7DE));
        miViewDetails.setIcon(iconClose);
        miViewDetails.addActionListener(e -> viewSelectedPaymentMethodDetails());
        tablePopup.add(miViewDetails);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleTableMouse(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleTableMouse(e);
            }
        });
    }

    private void handleTableMouse(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }

        boolean leftClickOnRow = SwingUtilities.isLeftMouseButton(e) && row >= 0 && e.getClickCount() == 1;
        if (e.isPopupTrigger() || leftClickOnRow) {
            tablePopup.show(table, e.getX(), e.getY());
        }
    }


    public void refreshData() {
        try {
            refreshHeaderLabels();

            int userId = currentUserId();
            int storeId = currentStoreId();
            int terminalId = currentTerminalId();

            // Keep this 2-arg call if your ShiftDao still has the old signature.
            openShift = shiftDao.findOpenShiftForUser(userId,  terminalId);

            if (openShift == null) {
                clearShiftSummary();
                return;
            }

            BigDecimal openingCash = openShift.openingCash() == null
                    ? BigDecimal.ZERO
                    : openShift.openingCash();

            lblShift.setText(openShift.id() + " / " + openShift.status() + " / " + money.format(openingCash));
            lblOpenedAt.setText(openShift.openedAt() != null ? openShift.openedAt().format(dtf) : "-");

            List<SalesDao.MethodTotal> rows = salesDao.totalsPerPaymentMethod(openShift.id(), userId);
            totalsModel.setRows(rows);
            btnViewDetails.setEnabled(!rows.isEmpty());

            if (!rows.isEmpty()) {
                table.setRowSelectionInterval(0, 0);
            }

            int cnt = salesDao.countSales(openShift.id(), userId);

            BigDecimal grand = salesDao.grandTotalPayments(openShift.id(), userId);
            if (grand == null) {
                grand = BigDecimal.ZERO;
            }

            lblSalesCount.setText(String.valueOf(cnt));
            lblDispenseTotal.setText(money.format(grand));
            lblGrandTotal.setText(money.format(grand.add(openingCash)));

        } catch (Exception ex) {
            clearShiftSummary();
            JOptionPane.showMessageDialog(this,
                    "Refresh error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void viewSelectedPaymentMethodDetails() {
        try {
            if (openShift == null) {
                JOptionPane.showMessageDialog(this, "Open a shift first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a payment method first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            SalesDao.MethodTotal methodTotal = totalsModel.getRow(row);
            int rowSelected = table.getSelectedRow();
            int methodId = 0;
            if (rowSelected != -1) {
                methodId = (int) table.getValueAt(rowSelected, 0); // <-- fixed column (e.g., ID)
            }
            List<SalesDao.PaymentDetail> details = salesDao.paymentDetails(session.userId(), methodId, openShift.id());

            PaymentMethodDetailsDialog dialog = new PaymentMethodDetailsDialog(SwingUtilities.getWindowAncestor(this), methodTotal, details);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "View details error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void openShift() {
        try {
            refreshHeaderLabels();

            if (openShift != null) {
                JOptionPane.showMessageDialog(this,
                        "You already have an OPEN shift (#" + openShift.id() + ").",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String cash = JOptionPane.showInputDialog(this, "Opening cash amount:", "0.00");
            if (cash == null) {
                return;
            }

            BigDecimal opening = new BigDecimal(cash.trim());
            Shift s = shiftDao.openShift(session, opening, "Opened via ShiftPanel");
            openShift = s;
            refreshData();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Opening cash must be numeric.",
                    "Invalid value",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Open shift error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void closeShift() {
        try {
            if (openShift == null) {
                JOptionPane.showMessageDialog(this, "No open shift.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String cash = JOptionPane.showInputDialog(this, "Closing cash amount:", "0.00");
            if (cash == null) {
                return;
            }

            BigDecimal closing = new BigDecimal(cash.trim());
            shiftDao.closeShift(openShift.id(), currentUserId(), closing);
            openShift = null;
            refreshData();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Closing cash must be numeric.",
                    "Invalid value",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Close shift error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void recordSale() {
        if (openShift == null) {
            JOptionPane.showMessageDialog(this, "Open a shift first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        RecordSaleDialog dlg = new RecordSaleDialog(null, openShift.id(), session);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshData();
        }
    }

    private void logout() {
        var window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
//        EnsariumShiftApp.launchLoginFlow();
    }

    private class TotalsTableModel extends AbstractTableModel {

        private final String[] cols = {"ID", "Payment Code", "Payment Name", "Subtotal"};
        private List<SalesDao.MethodTotal> rows = new ArrayList<>();

        public void setRows(List<SalesDao.MethodTotal> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public SalesDao.MethodTotal getRow(int rowIndex) {
            return rows.get(rowIndex);
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
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SalesDao.MethodTotal r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    r.methodId();
                case 1 ->
                    r.methodCode();
                case 2 ->
                    r.methodName();
//                case 3 ->
//                    money.format(r.amount());
                case 3 ->
                    money.format(r.amount());
                default ->
                    "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }
    }

    private int currentUserId() {
        int id = session.userId();
        if (id <= 0) {
            throw new IllegalStateException("No logged-in user found in session.");
        }
        return id;
    }

    private int currentStoreId() {
        return session.getStoreId() > 0
                ? session.getStoreId()
                : AppConfig.getInt("app.storeId", 1);
    }

    private int currentTerminalId() {
        return session.getTerminalId() > 0
                ? session.getTerminalId()
                : AppConfig.getInt("app.terminalId", 1);
    }

    private String nz(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private void refreshHeaderLabels() {
        int storeId = currentStoreId();
        int terminalId = currentTerminalId();

        String fullName = nz(session.fullName(), "Unknown User");
        String username = nz(session.username(), "unknown");

        String storeCode = nz(session.getStoreCode(), "STORE-" + storeId);
        String storeName = nz(session.getStoreName(), "Store " + storeId);
        String terminalCode = nz(session.getTerminalCode(), "TERM-" + terminalId);
        String terminalName = nz(session.getTerminalName(), "Terminal " + terminalId);

        lblUser.setText(fullName + " (" + username + ")");
        lblStoreTerminal.setText(storeCode + " - " + storeName
                + " / " + terminalCode + " - " + terminalName);
    }

    private void clearShiftSummary() {
        openShift = null;
        lblShift.setText("(no open shift)");
        lblOpenedAt.setText("-");
        lblSalesCount.setText("0");
        lblDispenseTotal.setText(money.format(BigDecimal.ZERO));
        lblGrandTotal.setText(money.format(BigDecimal.ZERO));
        totalsModel.setRows(List.of());
        btnViewDetails.setEnabled(false);
    }

}
