package com.aldrin.ensarium.inventory.product;

import com.aldrin.ensarium.order.OrderDraftManager;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.util.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ProductInventoryPanel extends JPanel {

    private final ProductInventoryDao dao = new ProductInventoryDao();
    private final ProductInventoryTableModel tableModel = new ProductInventoryTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(24);
    private final JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
    private final JLabel statusLabel = new JLabel("Ready");
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final Session session;
    private final OrderDraftManager draftManager;

    public ProductInventoryPanel(Session session, OrderDraftManager draftManager) {
        this.session = session;
        this.draftManager = draftManager;
        initUi();
        loadInventory();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton refreshButton = new JButton("Refresh");
        JButton damageButton = new JButton("Damage");
        JButton expiredButton = new JButton("Expired");
        JButton returnedButton = new JButton("Returned");
        JButton shrinkageButton = new JButton("Shrinkage");
        JButton stockOnHandReportButton = new JButton("Print Stock on Hand");
        JButton physicalCountReportButton = new JButton("Print Physical Count");
        JButton logsButton = new JButton("View Price Logs");
        JButton addToOrderButton = new JButton("Add to Partial Order");
        north.add(new JLabel("Search:"));
        searchField.putClientProperty("JTextField.placeholderText", "Search SKU or product");
        north.add(searchField);
        north.add(new JLabel("Rows:"));
        north.add(limitSpinner);
        north.add(refreshButton);
        north.add(damageButton);
        north.add(expiredButton);
        north.add(returnedButton);
        north.add(shrinkageButton);
        north.add(stockOnHandReportButton);
        north.add(physicalCountReportButton);
        north.add(logsButton);
        north.add(addToOrderButton);
        add(north, BorderLayout.NORTH);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setComponentPopupMenu(popupMenu);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(table);
        table.setRowHeight(36);

        int[] widths = {55, 0, 110, 300, 135, 80, 110, 150, 110, 110, 195, 195, 230, 230, 240, 150};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.hideColumns(table, 1);
        BootstrapTableStyle.setColumnRight(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);
        BootstrapTableStyle.setColumnLeft(table, 5);
        for (int i = 6; i <= 13; i++) {
            BootstrapTableStyle.setColumnRight(table, i);
        }
        BootstrapTableStyle.setColumnLeft(table, 14);
        BootstrapTableStyle.setColumnLeft(table, 15);
        installCustomRenderers();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(statusLabel);
        add(south, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadInventory());
        damageButton.addActionListener(e -> openRecordCondition("DAMAGED"));
        expiredButton.addActionListener(e -> openRecordCondition("EXPIRED"));
        returnedButton.addActionListener(e -> openRecordCondition("RETURNED"));
        shrinkageButton.addActionListener(e -> openRecordShrinkage());
        stockOnHandReportButton.addActionListener(e -> printStockOnHandReport());
        physicalCountReportButton.addActionListener(e -> printPhysicalCountReport());
        logsButton.addActionListener(e -> showSelectedLogs());
        addToOrderButton.addActionListener(e -> addSelectedRowsToPartialOrder());
        searchField.addActionListener(e -> loadInventory());
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadInventory();
            }
        });
        limitSpinner.addChangeListener(e -> loadInventory());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectPopupRow(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectPopupRow(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDetails();
                }
            }
        });

        JMenuItem viewDetails = new JMenuItem("View Monthly Details");
        JMenuItem updatePrice = new JMenuItem("Update Selling Price");
        JMenuItem viewLogs = new JMenuItem("View Selling Price Logs");
        JMenuItem damage = new JMenuItem("Record Damage Qty");
        JMenuItem expired = new JMenuItem("Record Expired Qty");
        JMenuItem returned = new JMenuItem("Record Returned Qty");
        JMenuItem shrinkage = new JMenuItem("Record Shrinkage Qty");
        JMenuItem addToPartialOrder = new JMenuItem("Add to Partial Order");
        JMenuItem printStockOnHand = new JMenuItem("Print Stock on Hand Report");
        JMenuItem printPhysicalCount = new JMenuItem("Print Physical Count Report");
        JMenuItem refresh = new JMenuItem("Refresh");
        viewDetails.addActionListener(e -> openDetails());
        updatePrice.addActionListener(e -> openUpdateSellingPrice());
        viewLogs.addActionListener(e -> showSelectedLogs());
        damage.addActionListener(e -> openRecordCondition("DAMAGED"));
        expired.addActionListener(e -> openRecordCondition("EXPIRED"));
        returned.addActionListener(e -> openRecordCondition("RETURNED"));
        shrinkage.addActionListener(e -> openRecordShrinkage());
        addToPartialOrder.addActionListener(e -> addSelectedRowsToPartialOrder());
        printStockOnHand.addActionListener(e -> printStockOnHandReport());
        printPhysicalCount.addActionListener(e -> printPhysicalCountReport());
        refresh.addActionListener(e -> loadInventory());
        popupMenu.add(viewDetails);
        popupMenu.add(updatePrice);
        popupMenu.add(viewLogs);
        popupMenu.addSeparator();
        popupMenu.add(damage);
        popupMenu.add(expired);
        popupMenu.add(returned);
        popupMenu.add(shrinkage);
        popupMenu.addSeparator();
        popupMenu.add(addToPartialOrder);
        popupMenu.addSeparator();
        popupMenu.add(printStockOnHand);
        popupMenu.add(printPhysicalCount);
        popupMenu.addSeparator();
        popupMenu.add(refresh);
    }

    
    private void installCustomRenderers() {
        table.getColumnModel().getColumn(0).setCellRenderer(new RowNumberRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new QtyRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new QtyRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new MoneyRenderer());
//        table.getColumnModel().getColumn(9).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new ProfitBarRenderer(tableModel, true));
        table.getColumnModel().getColumn(10).setCellRenderer(new ProfitBarRenderer(tableModel, false));
        table.getColumnModel().getColumn(12).setCellRenderer(new PercentRenderer());
        table.getColumnModel().getColumn(13).setCellRenderer(new PercentRenderer());
        table.getColumnModel().getColumn(14).setCellRenderer(new ExpiryHtmlRenderer());
//    table.getColumnModel().getColumn(15).setCellRenderer(new HtmlCellRenderer());
    }

    private void selectPopupRow(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = table.rowAtPoint(e.getPoint());
            if (row >= 0 && !table.isRowSelected(row)) {
                table.setRowSelectionInterval(row, row);
            }
        }
    }

    private void loadInventory() {
        String keyword = searchField.getText();
        int limit = (Integer) limitSpinner.getValue();
        statusLabel.setText("Loading...");
        new SwingWorker<List<ProductInventoryItem>, Void>() {
            @Override
            protected List<ProductInventoryItem> doInBackground() throws Exception {
                return dao.findInventory(keyword, limit);
            }

            @Override
            protected void done() {
                try {
                    List<ProductInventoryItem> rows = get();
                    tableModel.setRows(rows);
                    statusLabel.setText(rows.size() + " product row(s) loaded.");
                } catch (Exception ex) {
                    statusLabel.setText("Load failed.");
                    SwingUtils.showError(ProductInventoryPanel.this, "Failed to load product inventory list.", ex instanceof Exception ? (Exception) ex : null);
                }
            }
        }.execute();
    }

    private ProductInventoryItem getSelectedItem() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return tableModel.getRow(table.convertRowIndexToModel(selected));
    }

    private void openDetails() {
        ProductInventoryItem row = getSelectedItem();
        if (row == null) {
            return;
        }
        ProductInventoryDetailsDialog dialog = new ProductInventoryDetailsDialog(SwingUtilities.getWindowAncestor(this));
        dialog.open(row.getProductId(), row.getProductName());
    }

    private void openUpdateSellingPrice() {
        ProductInventoryItem row = getSelectedItem();
        if (row == null) {
            return;
        }
        UpdateSellingPriceDialog dialog = new UpdateSellingPriceDialog(SwingUtilities.getWindowAncestor(this), row.getProductName(), row.getSellingPrice());
        dialog.setVisible(true);
        if (dialog.getApprovedValue() == null) {
            return;
        }
        try {
            dao.updateSellingPrice(row.getProductId(), dialog.getApprovedValue(), session);
            loadInventory();
            JOptionPane.showMessageDialog(this,
                    "Selling price updated.\n\nTracked in table: audit_log\nAction code: UPDATE_SELLING_PRICE",
                    "Selling Price Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to update selling price.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void openRecordCondition(String targetStatus) {
        ProductInventoryItem row = getSelectedItem();
        if (row == null) {
            return;
        }
        String actionLabel = switch (targetStatus) {
            case "DAMAGED" ->
                "Damage";
            case "EXPIRED" ->
                "Expired";
            case "RETURNED" ->
                "Returned";
            default ->
                targetStatus;
        };
        RecordProductConditionDialog dialog = new RecordProductConditionDialog(
                SwingUtilities.getWindowAncestor(this), actionLabel, row.getProductName(), row.getOnhandQty());
        dialog.setVisible(true);
        if (dialog.getApprovedQty() == null) {
            return;
        }
        try {
            dao.recordInventoryCondition(row.getProductId(), dialog.getApprovedQty(), targetStatus, session, dialog.getApprovedNotes());
            loadInventory();
            JOptionPane.showMessageDialog(this,
                    actionLabel + " qty recorded successfully.\n\nTracked in inventory_txn, inventory_txn_line, stock_balance, and audit_log.",
                    actionLabel + " Recorded",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to record " + actionLabel.toLowerCase() + " qty.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void openRecordShrinkage() {
        ProductInventoryItem row = getSelectedItem();
        if (row == null) {
            return;
        }
        RecordShrinkageDialog dialog = new RecordShrinkageDialog(SwingUtilities.getWindowAncestor(this), row.getProductName(), row.getOnhandQty());
        dialog.setVisible(true);
        if (dialog.getApprovedQty() == null) {
            return;
        }
        try {
            dao.recordInventoryShrinkage(row.getProductId(), dialog.getApprovedQty(), dialog.getApprovedReason(), session, dialog.getApprovedNotes());
            loadInventory();
            JOptionPane.showMessageDialog(this,
                    "Shrinkage recorded.\n\nTracked in: inventory_txn, inventory_txn_line, stock_balance, audit_log\nReason: " + dialog.getApprovedReason(),
                    "Shrinkage Recorded",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to record shrinkage qty.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void showSelectedLogs() {
        ProductInventoryItem row = getSelectedItem();
        if (row == null) {
            return;
        }
        try {
            List<String> logs = dao.findRecentSellingPriceLogs(row.getProductId(), 50);
            String message = logs.isEmpty() ? "No selling price update logs found for this product." : String.join("\n\n", logs);
            JOptionPane.showMessageDialog(this, message, "Selling Price Logs", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to load selling price logs.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void printStockOnHandReport() {
        showJasperReport(true);
    }

    private void printPhysicalCountReport() {
        showJasperReport(false);
    }

    private void showJasperReport(boolean stockOnHand) {
        List<ProductInventoryItem> rows = snapshotCurrentRows();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No product rows to include in the report.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        try {
            if (stockOnHand) {
                JasperProductInventoryReports.showStockOnHandReport(this, rows, keyword, session);
                statusLabel.setText("Displayed Jasper report: Stock On Hand");
            } else {
                JasperProductInventoryReports.showPhysicalCountReport(this, rows, keyword, session);
                statusLabel.setText("Displayed Jasper report: Physical Count");
            }
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to display Jasper report.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void addSelectedRowsToPartialOrder() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows == null || selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one product first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int added = 0;
        for (int rowIndex : selectedRows) {
            ProductInventoryItem row = tableModel.getRow(table.convertRowIndexToModel(rowIndex));
            if (row != null) {
                draftManager.addProduct(row);
                added++;
            }
        }
        statusLabel.setText(added + " product(s) added to partial order.");
        JOptionPane.showMessageDialog(this,
                added + " product(s) added to the partial order.\nDefault order qty was copied from Qty Sold (30 Days).",
                "Added to Partial Order",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private List<ProductInventoryItem> snapshotCurrentRows() {
        List<ProductInventoryItem> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            rows.add(tableModel.getRow(i));
        }
        return rows;
    }


    public static class ExpiryHtmlRenderer extends DefaultTableCellRenderer {

        ExpiryHtmlRenderer() {
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(value == null ? "" : String.valueOf(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.LEFT);
            return this;
        }
    }


    private static class HtmlCellRenderer extends DefaultTableCellRenderer {

        HtmlCellRenderer() {
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(value == null ? "" : String.valueOf(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.LEFT);
            return this;
        }
    }


    private static class RowNumberRenderer extends DefaultTableCellRenderer {

        RowNumberRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(value == null ? "" : String.valueOf(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
            return this;
        }
    }


    private static class QtyRenderer extends DefaultTableCellRenderer {

        QtyRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(SwingUtils.formatMoney(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
            return this;
        }
    }


    private static class MoneyRenderer extends DefaultTableCellRenderer {

        MoneyRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(SwingUtils.formatMoney(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
            return this;
        }
    }


    private static class PercentRenderer extends DefaultTableCellRenderer {

        PercentRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setText(SwingUtils.formatPercent(value));
            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
            return this;
        }
    }


    private static class ProfitBarRenderer extends JProgressBar implements TableCellRenderer {

        private final ProductInventoryTableModel model;
        private final boolean noTaxColumn;
        private BigDecimal currentValue = BigDecimal.ZERO;
        private boolean selected;
        private Color rowBaseColor = Color.WHITE;

        ProfitBarRenderer(ProductInventoryTableModel model, boolean noTaxColumn) {
            super(0, 100);
            this.model = model;
            this.noTaxColumn = noTaxColumn;
            setStringPainted(false);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            currentValue = value instanceof BigDecimal bd ? bd : BigDecimal.ZERO;
            selected = isSelected;

            double max = findMaxProfit();
            double current = Math.max(0d, currentValue.doubleValue());
            int percent = max <= 0d ? 0 : (int) Math.round((current / max) * 100d);
            setValue(Math.max(0, Math.min(100, percent)));

            BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
            rowBaseColor = getBackground();

            setForeground(currentValue.signum() >= 0 ? new Color(0x198754) : new Color(0xDC3545));
            return this;
        }

        private double findMaxProfit() {
            double max = 0d;
            for (int i = 0; i < model.getRowCount(); i++) {
                ProductInventoryItem item = model.getRow(i);
                BigDecimal candidate = noTaxColumn
                        ? item.getProfitWithoutTaxWithoutDiscount()
                        : item.getProfitWithTaxAndDiscount();
                if (candidate != null) {
                    max = Math.max(max, Math.max(0d, candidate.doubleValue()));
                }
            }
            return max;
        }

        @Override
        protected void paintComponent(Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                int w = getWidth();
                int h = getHeight();

                g2.setColor(rowBaseColor);
                g2.fillRect(0, 0, w, h);

                int innerX = 10;
                int innerY = 7;
                int innerW = Math.max(0, w - 20);
                int innerH = Math.max(8, h - 14);
                int arc = Math.min(innerH, 12);

                Color trackColor = selected
                        ? new Color(rowBaseColor.getRed(), rowBaseColor.getGreen(), rowBaseColor.getBlue(), 180)
                        : new Color(255, 255, 255, 95);
                g2.setColor(trackColor);
                g2.fillRoundRect(innerX, innerY, innerW, innerH, arc, arc);

                int fillW = innerW <= 0 ? 0 : (int) Math.round(innerW * (getValue() / 100d));
                if (fillW > 0) {
                    Color fg = getForeground();
                    Color barColor = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), selected ? 170 : 125);
                    g2.setColor(barColor);
                    g2.fillRoundRect(innerX, innerY, fillW, innerH, arc, arc);
                }

                g2.setColor(new Color(0, 0, 0, selected ? 30 : 22));
                g2.drawRoundRect(innerX, innerY, Math.max(0, innerW - 1), Math.max(0, innerH - 1), arc, arc);

                String text = SwingUtils.formatMoney(currentValue);
                if (currentValue.signum() < 0) {
                    g2.setColor(new Color(0xDC3545));
                } else if (selected) {
                    g2.setColor(getForeground());
                } else {
                    g2.setColor(Color.BLACK);
                }

                FontMetrics fm = g2.getFontMetrics();
                int x = w - fm.stringWidth(text) - 12;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, Math.max(12, x), y);
            } finally {
                g2.dispose();
            }
        }
    }
}
