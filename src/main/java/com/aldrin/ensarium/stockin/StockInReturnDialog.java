package com.aldrin.ensarium.stockin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

public class StockInReturnDialog extends JDialog {

    private final StockInDao stockInDao = new StockInDao();
    private final JComboBox<LookupOption> cboReturnedBy = new JComboBox<>();
    private final JTextField txtCreatedBy = new JTextField(24);
    private final JComboBox<LookupOption> cboReason = new JComboBox<>();
    private final JTextArea txtNotes = new JTextArea(3, 30);
    private final ReturnLineTableModel model;
    private final JTable table;
    private final JLabel lblSelectedProduct = new JLabel("Select a line");
    private final JLabel lblAvailableQty = new JLabel("0.0000");
    private final JFormattedTextField txtReturnQty = buildQtyField();
    private final JLabel lblTotalRows = new JLabel("0");
    private final JLabel lblTotalQty = new JLabel("0.0000");
    private Integer createdByUserId;
    private boolean processed;

//    public StockInReturnDialog(Frame owner, List<ReturnableLine> lines) {
//        super(owner, "Return Stock-In", true);
//        this.model = new ReturnLineTableModel(lines);
//        this.table = new JTable(model);
//        initUi();
//        loadLookups();
//        bindCurrentUser();
//        if (model.getRowCount() > 0) {
//            table.setRowSelectionInterval(0, 0);
//            loadSelectedRowIntoEditor();
//        }
//        refreshSummary();
//    }
    
        public StockInReturnDialog(java.awt.Window owner, List<ReturnableLine> lines) {
//        super(owner, "Return Stock-In", true);
        super(owner, "Return Stock-In", Dialog.ModalityType.APPLICATION_MODAL);
        this.model = new ReturnLineTableModel(lines);
        this.table = new JTable(model);
        initUi();
        loadLookups();
        bindCurrentUser();
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadSelectedRowIntoEditor();
        }
        refreshSummary();
    }
    
        public StockInReturnDialog(Frame owner, List<ReturnableLine> lines) {
        super(owner, "Return Stock-In", true);
        this.model = new ReturnLineTableModel(lines);
        this.table = new JTable(model);
        initUi();
        loadLookups();
        bindCurrentUser();
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadSelectedRowIntoEditor();
        }
        refreshSummary();
    }


    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        add(buildHeaderPanel(), BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onRowSelectionChanged);
        installQtyPopupMenu();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        southPanel.add(buildSummaryPanel(), BorderLayout.CENTER);
        southPanel.add(buildActionPanel(), BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1180, 560));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtCreatedBy.setEditable(false);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Created By"), gbc);
        gbc.gridx = 1; panel.add(txtCreatedBy, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Reason"), gbc);
        gbc.gridx = 3; panel.add(cboReason, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Returned By"), gbc);
        gbc.gridx = 1; panel.add(cboReturnedBy, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Notes"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(new JScrollPane(txtNotes), gbc);

        return panel;
    }

    private JPanel buildQtyEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Selected Product"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(lblSelectedProduct, gbc);

        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Available Qty"), gbc);
        gbc.gridx = 1; panel.add(lblAvailableQty, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Return Qty"), gbc);
        gbc.gridx = 3; panel.add(txtReturnQty, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        panel.add(new JLabel("Tip: right-click a line to edit quantity, use max, or clear."), gbc);

        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.add(new JLabel("Lines with return qty:"));
        panel.add(lblTotalRows);
        panel.add(new JLabel("Total return qty:"));
        panel.add(lblTotalQty);
        panel.add(new JLabel("Right-click or double-click a row to edit quantity."));
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        javax.swing.JButton btnClearAll = new javax.swing.JButton("Clear All Qty");
        javax.swing.JButton btnProcess = new javax.swing.JButton("Process Return");
        javax.swing.JButton btnCancel = new javax.swing.JButton("Cancel");
        btnClearAll.addActionListener(e -> clearAllQty());
        btnProcess.addActionListener(e -> onProcess());
        btnCancel.addActionListener(e -> dispose());
        actions.add(btnClearAll);
        actions.add(btnCancel);
        actions.add(btnProcess);
        return actions;
    }

    private JFormattedTextField buildQtyField() {
        DecimalFormat format = new DecimalFormat("#,##0.####");
        format.setParseBigDecimal(true);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(BigDecimal.class);
        formatter.setMinimum(BigDecimal.ZERO);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        JFormattedTextField field = new JFormattedTextField(formatter);
        field.setColumns(12);
        field.setValue(BigDecimal.ZERO);
        return field;
    }

    private void loadLookups() {
        try {
            for (LookupOption item : stockInDao.listUsers()) {
                cboReturnedBy.addItem(item);
            }
            for (LookupOption item : stockInDao.listReturnReasons()) {
                cboReason.addItem(item);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Return Lookups", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bindCurrentUser() {
        CurrentUser currentUser = SessionResolver.resolveCurrentUser();
        if (currentUser == null || !currentUser.isResolved()) {
            txtCreatedBy.setText("");
            return;
        }
        createdByUserId = currentUser.getId() == null ? null : Integer.valueOf(currentUser.getId().intValue());
        txtCreatedBy.setText(buildCurrentUserDisplay(currentUser));

        for (int i = 0; i < cboReturnedBy.getItemCount(); i++) {
            LookupOption item = cboReturnedBy.getItemAt(i);
            if (item == null) {
                continue;
            }
            boolean idMatch = currentUser.getId() != null && currentUser.getId().equals(item.getId());
            boolean usernameMatch = equalsIgnoreCase(currentUser.getUsername(), item.getCode());
            boolean fullNameMatch = equalsIgnoreCase(currentUser.getFullName(), item.getName());
            if (idMatch || usernameMatch || fullNameMatch) {
                cboReturnedBy.setSelectedIndex(i);
                cboReturnedBy.setEnabled(false);
                return;
            }
        }

        if (currentUser.getId() != null || currentUser.getUsername() != null || currentUser.getFullName() != null) {
            LookupOption sessionUser = new LookupOption(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getFullName() == null ? buildCurrentUserDisplay(currentUser) : currentUser.getFullName());
            cboReturnedBy.insertItemAt(sessionUser, 0);
            cboReturnedBy.setSelectedIndex(0);
            cboReturnedBy.setEnabled(false);
        }
    }

    private void installQtyPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editQty = new JMenuItem("Edit Return Qty...");
        editQty.addActionListener(e -> showQtyEditorDialog());
        popupMenu.add(editQty);

        JMenuItem useMax = new JMenuItem("Use Full Available Qty");
        useMax.addActionListener(e -> useMaxQtyForSelected());
        popupMenu.add(useMax);

        JMenuItem clearSelected = new JMenuItem("Clear Selected Qty");
        clearSelected.addActionListener(e -> clearSelectedQty());
        popupMenu.add(clearSelected);

        popupMenu.addSeparator();
        JMenuItem clearAll = new JMenuItem("Clear All Qty");
        clearAll.addActionListener(e -> clearAllQty());
        popupMenu.add(clearAll);

        table.setComponentPopupMenu(popupMenu);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouse(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouse(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectRowAtPoint(e);
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    showQtyEditorDialog();
                }
            }

            private void handleMouse(MouseEvent e) {
                selectRowAtPoint(e);
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void selectRowAtPoint(MouseEvent event) {
        int viewRow = table.rowAtPoint(event.getPoint());
        if (viewRow >= 0) {
            table.setRowSelectionInterval(viewRow, viewRow);
        }
    }

    private void showQtyEditorDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.", "Return Stock-In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadSelectedRowIntoEditor();
        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    buildQtyEditorPanel(),
                    "Edit Return Quantity",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                applySelectedQty();
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Stock-In", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRowSelectionChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            loadSelectedRowIntoEditor();
        }
    }

    private void loadSelectedRowIntoEditor() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            lblSelectedProduct.setText("Select a line");
            lblAvailableQty.setText("0.0000");
            txtReturnQty.setValue(BigDecimal.ZERO);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        ReturnableLine row = model.getLine(modelRow);
        lblSelectedProduct.setText(row.getSku() + " - " + row.getProductName());
        lblAvailableQty.setText(formatQty(row.getAvailableQty()));
        txtReturnQty.setValue(model.getReturnQty(modelRow));
    }

    private void applySelectedQty() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.", "Return Stock-In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        ReturnableLine line = model.getLine(modelRow);
        BigDecimal qty = parseQtyField();
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Return quantity cannot be negative.");
        }
        if (qty.compareTo(line.getAvailableQty()) > 0) {
            throw new IllegalArgumentException("Return quantity exceeds available quantity for " + line.getProductName());
        }
        model.setReturnQty(modelRow, qty);
        table.repaint();
        refreshSummary();
    }

    private void useMaxQtyForSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.", "Return Stock-In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        BigDecimal maxQty = model.getLine(modelRow).getAvailableQty();
        model.setReturnQty(modelRow, maxQty);
        txtReturnQty.setValue(maxQty);
        table.repaint();
        refreshSummary();
    }

    private void clearSelectedQty() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.", "Return Stock-In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        model.setReturnQty(modelRow, BigDecimal.ZERO);
        txtReturnQty.setValue(BigDecimal.ZERO);
        table.repaint();
        refreshSummary();
    }

    private void clearAllQty() {
        model.clearAllReturnQty();
        txtReturnQty.setValue(BigDecimal.ZERO);
        table.repaint();
        refreshSummary();
    }

    private BigDecimal parseQtyField() {
        try {
            txtReturnQty.commitEdit();
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Invalid return quantity format.");
        }
        Object value = txtReturnQty.getValue();
        if (value == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.setScale(4, RoundingMode.HALF_UP);
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).setScale(4, RoundingMode.HALF_UP);
        }
        String text = txtReturnQty.getText() == null ? "" : txtReturnQty.getText().trim();
        if (text.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return new BigDecimal(text.replace(",", "")).setScale(4, RoundingMode.HALF_UP);
    }

    private void refreshSummary() {
        lblTotalRows.setText(String.valueOf(model.countRowsWithQty()));
        lblTotalQty.setText(formatQty(model.totalReturnQty()));
    }

    private String formatQty(BigDecimal value) {
        NumberFormat fmt = new DecimalFormat("#,##0.0000");
        return fmt.format(value == null ? BigDecimal.ZERO : value);
    }

    private void onProcess() {
        try {
            List<StockInReturnEntry> entries = getEntries();
            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Enter at least one return quantity.");
            }
            processed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Stock-In", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isProcessed() {
        return processed;
    }

    public Integer getCreatedBy() {
        return createdByUserId;
    }

    public Integer getReturnedBy() {
        LookupOption option = (LookupOption) cboReturnedBy.getSelectedItem();
        return option == null || option.getId() == null ? null : option.getId().intValue();
    }

    public String getReturnedByName() {
        LookupOption option = (LookupOption) cboReturnedBy.getSelectedItem();
        if (option == null) {
            return null;
        }
        String display = option.toString();
        return display == null || display.trim().isEmpty() ? null : display.trim();
    }

    public Long getReasonId() {
        LookupOption option = (LookupOption) cboReason.getSelectedItem();
        return option == null ? null : option.getId();
    }

    public String getReasonName() {
        LookupOption option = (LookupOption) cboReason.getSelectedItem();
        return option == null ? null : option.getName();
    }

    public String getNotes() {
        return txtNotes.getText();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private String buildCurrentUserDisplay(CurrentUser currentUser) {
        if (currentUser == null) {
            return "";
        }
        if (currentUser.getFullName() != null && !currentUser.getFullName().trim().isEmpty()) {
            if (currentUser.getUsername() != null && !currentUser.getUsername().trim().isEmpty()) {
                return currentUser.getFullName().trim() + " (" + currentUser.getUsername().trim() + ")";
            }
            return currentUser.getFullName().trim();
        }
        if (currentUser.getUsername() != null && !currentUser.getUsername().trim().isEmpty()) {
            return currentUser.getUsername().trim();
        }
        if (currentUser.getId() != null) {
            return "User ID " + currentUser.getId();
        }
        return "";
    }

    public List<StockInReturnEntry> getEntries() {
        List<StockInReturnEntry> entries = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            ReturnableLine row = model.getLine(i);
            BigDecimal qty = model.getReturnQty(i);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (qty.compareTo(row.getAvailableQty()) > 0) {
                throw new IllegalArgumentException("Return quantity exceeds available quantity for " + row.getProductName());
            }
            StockInReturnEntry entry = new StockInReturnEntry();
            entry.setReceiptLineId(row.getReceiptLineId());
            entry.setProductId(row.getProductId());
            entry.setLotId(row.getLotId());
            entry.setQuantity(qty);
            entry.setUnitCost(row.getUnitCost());
            entries.add(entry);
        }
        return entries;
    }

    private static class ReturnLineTableModel extends AbstractTableModel {
        private final String[] cols = {"SKU", "Product", "Lot No", "Expiry", "Purchased", "Already Returned", "Available", "Unit Cost", "Return Qty"};
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        private final List<ReturnableLine> lines;
        private final List<BigDecimal> returnQty = new ArrayList<>();

        public ReturnLineTableModel(List<ReturnableLine> lines) {
            this.lines = lines == null ? new ArrayList<>() : lines;
            for (int i = 0; i < this.lines.size(); i++) {
                returnQty.add(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            }
        }

        public ReturnableLine getLine(int row) {
            return lines.get(row);
        }

        public BigDecimal getReturnQty(int row) {
            return returnQty.get(row);
        }

        public void setReturnQty(int row, BigDecimal qty) {
            returnQty.set(row, safeQty(qty));
            fireTableRowsUpdated(row, row);
        }

        public void clearAllReturnQty() {
            for (int i = 0; i < returnQty.size(); i++) {
                returnQty.set(i, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            }
            fireTableDataChanged();
        }

        public int countRowsWithQty() {
            int count = 0;
            for (BigDecimal qty : returnQty) {
                if (safeQty(qty).compareTo(BigDecimal.ZERO) > 0) {
                    count++;
                }
            }
            return count;
        }

        public BigDecimal totalReturnQty() {
            BigDecimal total = BigDecimal.ZERO;
            for (BigDecimal qty : returnQty) {
                total = total.add(safeQty(qty));
            }
            return total.setScale(4, RoundingMode.HALF_UP);
        }

        @Override
        public int getRowCount() {
            return lines.size();
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
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ReturnableLine row = lines.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.getSku();
                case 1 -> row.getProductName();
                case 2 -> row.getLotNo();
                case 3 -> row.getExpiryDate() == null ? "" : sdf.format(row.getExpiryDate());
                case 4 -> row.getPurchasedQty();
                case 5 -> row.getReturnedQty();
                case 6 -> row.getAvailableQty();
                case 7 -> row.getUnitCost();
                case 8 -> safeQty(returnQty.get(rowIndex)).compareTo(BigDecimal.ZERO) == 0 ? "" : safeQty(returnQty.get(rowIndex)).toPlainString();
                default -> "";
            };
        }

        private BigDecimal safeQty(BigDecimal qty) {
            return qty == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : qty.setScale(4, RoundingMode.HALF_UP);
        }
    }
}
