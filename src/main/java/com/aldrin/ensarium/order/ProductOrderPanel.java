package com.aldrin.ensarium.order;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.ui.widgets.StyledOptionPane;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import com.aldrin.ensarium.util.SwingUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

public class ProductOrderPanel extends JPanel {

    private final Session session;
    private final OrderDraftManager draftManager;
    private final ProductOrderDao dao = new ProductOrderDao();
    private final OrderDraftTableModel tableModel;
    private final JTable table;
    private final JComboBox<SupplierOption> supplierCombo = new JComboBox<>();
    private final JTextArea notesArea = new JTextArea(3, 40);
    private final JLabel draftSummaryLabel = new JLabel("Draft has 0 line(s)");
    private final JLabel statusLabel = new JLabel("Ready");
    private final StyledButton saveOrderButton = new StyledButton("Save");
    private Long currentOrderId;
    private String currentOrderNo = "";
    private boolean dirty;
    private boolean ignoreSupplierEvents;
    private boolean suppressDirtyEvents;

    private JPanel centerPanel = new JPanel(new BorderLayout());
    private JPanel westPanel = new JPanel();
    private JPanel eastPanel = new JPanel();

    Icon iconReload = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);
    Icon iconOpen = FaSwingIcons.icon(FontAwesomeIcon.FOLDER_OPEN, 24, Color.WHITE);
    Icon iconRemove = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 24, Color.WHITE);
    Icon iconMinus = FaSwingIcons.icon(FontAwesomeIcon.MINUS_CIRCLE, 24, Color.WHITE);
    Icon iconPrint = FaSwingIcons.icon(FontAwesomeIcon.PRINT, 24, Color.WHITE);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);

    public ProductOrderPanel(Session session, OrderDraftManager draftManager) {
        this.session = session;
        this.draftManager = draftManager;
        this.tableModel = new OrderDraftTableModel(draftManager);
        this.table = new JTable(tableModel);
        initUi();
        applyComboBoxAutoFill(supplierCombo);
        loadSuppliers();

        draftManager.addListener(() -> SwingUtilities.invokeLater(() -> {
            markDirty();
            refreshDraftView();
        }));

        notesArea.getDocument().addDocumentListener(new DirtyListener());

        supplierCombo.addActionListener(e -> {
            if (!ignoreSupplierEvents) {
                markDirty();
                refreshDraftView();
            }
        });

        refreshDraftView();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        supplierCombo.setPreferredSize(new Dimension(230, 30));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        StyledButton reloadSuppliersButton = new StyledButton("Suppliers");
        reloadSuppliersButton.setIcon(iconReload);

        StyledButton openSavedOrderButton = new StyledButton("Open");
        openSavedOrderButton.setIcon(iconOpen);

        StyledButton removeLineButton = new StyledButton("Remove");
        removeLineButton.setIcon(iconMinus);

        StyledButton clearDraftButton = new StyledButton("Clear");
        clearDraftButton.setIcon(iconRemove);

        StyledButton printOrderButton = new StyledButton("Print");
        printOrderButton.setIcon(iconPrint);

        saveOrderButton.setIcon(iconSave);

        JLabel lblSupplierl = new JLabel("Supplier:");
        lblSupplierl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        north.add(lblSupplierl);
        supplierCombo.setPrototypeDisplayValue(new SupplierOption(999L, "Supplier Sample Name Here"));
        supplierCombo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        north.add(supplierCombo);
        north.add(reloadSuppliersButton);
        north.add(openSavedOrderButton);
        north.add(removeLineButton);
        north.add(clearDraftButton);
        north.add(saveOrderButton);
        north.add(printOrderButton);
        north.add(draftSummaryLabel);
        centerPanel.add(north, BorderLayout.NORTH);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(34);
        BootstrapTableStyle.install(table);
        int[] widths = {55, 0, 110, 340, 150, 70, 90, 90, 110, 90, 90, 260, 220};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.hideColumns(table, 1);
        BootstrapTableStyle.setColumnRight(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);
        BootstrapTableStyle.setColumnLeft(table, 5);
        BootstrapTableStyle.setColumnRight(table, 6);
        BootstrapTableStyle.setColumnRight(table, 7);
        BootstrapTableStyle.setColumnRight(table, 8);
        BootstrapTableStyle.setColumnRight(table, 9);
        BootstrapTableStyle.setColumnRight(table, 10);
        BootstrapTableStyle.setColumnLeft(table, 11);
        BootstrapTableStyle.setColumnLeft(table, 12);

        BootstrapTableStyle.setHeaderRight(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
        BootstrapTableStyle.setHeaderLeft(table, 5);
        BootstrapTableStyle.setHeaderRight(table, 6);
        BootstrapTableStyle.setHeaderRight(table, 7);
        BootstrapTableStyle.setHeaderRight(table, 8);
        BootstrapTableStyle.setHeaderRight(table, 9);
        BootstrapTableStyle.setHeaderRight(table, 10);

//        table.getColumnModel().getColumn(11).setCellRenderer(new ExpiryHtmlRenderer());
        table.getColumnModel().getColumn(11).setCellRenderer(new ExpiryHtmlRenderer());
//        installRenderers();
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(10, 10));
        notesArea.setBorder(BorderFactory.createTitledBorder("Order Notes"));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        south.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(statusLabel);
        south.add(footer, BorderLayout.SOUTH);

        centerPanel.add(south, BorderLayout.SOUTH);

        add(westPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        reloadSuppliersButton.addActionListener(e -> loadSuppliers());
        openSavedOrderButton.addActionListener(e -> openSavedOrder());
        removeLineButton.addActionListener(e -> removeSelectedLine());
        clearDraftButton.addActionListener(e -> clearDraft());
        saveOrderButton.addActionListener(e -> saveFinalOrder());
        printOrderButton.addActionListener(e -> printOrderReport());

    }

    private void installRenderers() {
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        for (int i : new int[]{0}) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        for (int i : new int[]{2, 3, 4, 5}) {
            table.getColumnModel().getColumn(i).setCellRenderer(left);
        }
        for (int i : new int[]{6, 7, 8}) {
            table.getColumnModel().getColumn(i).setCellRenderer(new StripeNumberRenderer());
        }
    }

    private void refreshDraftView() {
        tableModel.fireTableDataChanged();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (OrderDraftLine line : draftManager.snapshot()) {
            if (line.getQtyToOrder() != null) {
                totalQty = totalQty.add(line.getQtyToOrder());
            }
        }
        draftSummaryLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        draftSummaryLabel.setText("Draft has " + draftManager.size() + " line(s), total qty " + SwingUtils.formatQty(totalQty));
        updateSaveButtonCaption();
        statusLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        if (draftManager.size() == 0) {
            statusLabel.setText("Partial order is empty.");
        } else if (currentOrderId != null) {
            statusLabel.setText((dirty ? "Editing saved order " : "Saved order ") + currentOrderNo + (dirty ? " (has unsaved changes)." : " is up to date."));
        } else {
            statusLabel.setText("Partial order ready for review/editing.");
        }
    }

    private void updateSaveButtonCaption() {
//        saveOrderButton.setText(currentOrderId == null ? "Save Final Order" : (dirty ? "Update Final Order" : "Save Final Order"));
        saveOrderButton.setText(currentOrderId == null ? "Save" : (dirty ? "Save" : "Save"));
    }

    private void markDirty() {
        if (suppressDirtyEvents) {
            return;
        }
        if (draftManager.size() == 0) {
            dirty = false;
            return;
        }
        dirty = true;
    }

    private void resetSavedOrderState() {
        currentOrderId = null;
        currentOrderNo = "";
        dirty = false;
        updateSaveButtonCaption();
    }

    private void loadSuppliers() {
        final SupplierOption selectedBefore = resolveSelectedSupplier();
        statusLabel.setText("Loading suppliers...");

        new SwingWorker<List<SupplierOption>, Void>() {
            @Override
            protected List<SupplierOption> doInBackground() throws Exception {
                return dao.findSuppliers();
            }

            @Override
            protected void done() {
                try {
                    List<SupplierOption> suppliers = get();

                    ignoreSupplierEvents = true;

                    DefaultComboBoxModel<SupplierOption> model = new DefaultComboBoxModel<>();
                    for (SupplierOption s : suppliers) {
                        model.addElement(s);
                    }
                    supplierCombo.setModel(model);

                    // very important: re-attach autofill after resetting the model
                    applyComboBoxAutoFill(supplierCombo);

                    if (selectedBefore != null) {
                        selectSupplierById(selectedBefore.getId());
                    } else if (supplierCombo.getItemCount() > 0) {
                        supplierCombo.setSelectedIndex(0);
                    }

                    ignoreSupplierEvents = false;
                    statusLabel.setText("Supplier list loaded.");
                    refreshDraftView();
                } catch (Exception ex) {
                    ignoreSupplierEvents = false;
                    SwingUtils.showError(ProductOrderPanel.this, "Failed to load suppliers.",
                            ex instanceof Exception ? (Exception) ex : null);
                    statusLabel.setText("Failed to load suppliers.");
                }
            }
        }.execute();
    }

    private void removeSelectedLine() {
        int row = table.getSelectedRow();
        int choice = StyledOptionPane.showConfirm(
                this,
                "Replace the current partial order with a previously saved order?",
                "Open Saved Order"
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        draftManager.removeAt(table.convertRowIndexToModel(row));
        if (draftManager.size() == 0) {
            resetSavedOrderState();
            notesArea.setText("");
        }
        refreshDraftView();
    }

    private void openSavedOrder() {
        if (draftManager.size() > 0 && (dirty || currentOrderId == null)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Replace the current partial order with a previously saved order?",
                    "Open Saved Order",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            List<ProductOrderDao.SavedOrderSummary> recent = dao.findRecentOrders(30);
            if (recent.isEmpty()) {
                StyledOptionPane.showInfo(
                        this,
                        "No saved orders were found in the last 30 days.",
                        "Information"
                );
                return;
            }

            ProductOrderDao.SavedOrderSummary selected = StyledOptionPane.showSelection(
                    this,
                    "Select the saved order to edit:\n(Recent orders include yesterday and other recent days.)",
                    "Open Saved Order",
                    recent.toArray(new ProductOrderDao.SavedOrderSummary[0]),
                    recent.get(0)
            );

            if (selected == null) {
                return;
            }

            ProductOrderDao.LoadedOrder loaded = dao.loadOrderForEdit(selected.orderId());
            suppressDirtyEvents = true;
            ignoreSupplierEvents = true;
            try {
                currentOrderId = loaded.orderId();
                currentOrderNo = loaded.orderNo();
                draftManager.replaceAll(loaded.lines());
                selectOrAppendSupplier(loaded.supplier());
                notesArea.setText(loaded.notes());
                dirty = false;
            } finally {
                ignoreSupplierEvents = false;
                suppressDirtyEvents = false;
            }
            refreshDraftView();
            statusLabel.setText("Loaded saved order " + currentOrderNo + " for editing.");
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to open saved order.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    @SuppressWarnings("unchecked")
    private void selectOrAppendSupplier(SupplierOption target) {
        if (target == null || target.getId() == null) {
            if (supplierCombo.getItemCount() > 0) {
                supplierCombo.setSelectedIndex(0);
            }
            return;
        }

        DefaultComboBoxModel<SupplierOption> model
                = (DefaultComboBoxModel<SupplierOption>) supplierCombo.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            SupplierOption row = model.getElementAt(i);
            if (row != null && Objects.equals(row.getId(), target.getId())) {
                supplierCombo.setSelectedItem(row);
                return;
            }
        }

        model.addElement(target);
        supplierCombo.setSelectedItem(target);

        // keep autofill working even after model mutation
        applyComboBoxAutoFill(supplierCombo);
    }


    private void clearDraft() {
        if (draftManager.size() == 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Clear the current partial order?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            draftManager.clear();
            notesArea.setText("");
            resetSavedOrderState();
            refreshDraftView();
        }
    }

    private void saveFinalOrder() {
        List<OrderDraftLine> lines = draftManager.snapshot();
        if (lines.isEmpty()) {
            StyledOptionPane.showInfo(this, "No products in the partial order.", "Information");
            return;
        }
        try {
            boolean updateMode = currentOrderId != null;
//            ProductOrderDao.SavedOrder saved = dao.saveFinalOrder(currentOrderId, currentOrderNo, lines, (SupplierOption) supplierCombo.getSelectedItem(), notesArea.getText(), session);
            ProductOrderDao.SavedOrder saved = dao.saveFinalOrder(
                    currentOrderId, currentOrderNo, lines,
                    resolveSelectedSupplier(),
                    notesArea.getText(), session
            );
            currentOrderId = saved.orderId();
            currentOrderNo = saved.orderNo();
            dirty = false;
            refreshDraftView();
            statusLabel.setText((updateMode ? "Updated final order " : "Saved final order ") + currentOrderNo);
            StyledOptionPane.showInfo(
                    this,
                    (updateMode ? "Final order updated successfully.\n" : "Final order saved successfully.\n")
                    + "Order No: " + currentOrderNo + "\n"
                    + "You can still review, edit, update, or print this order before clearing it.",
                    updateMode ? "Order Updated" : "Order Saved"
            );
        } catch (Exception ex) {
            SwingUtils.showError(this, currentOrderId == null ? "Failed to save final order." : "Failed to update final order.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private void printOrderReport() {
        try {
            String selectedOrderNo = chooseOrderNoForPrinting();
            if (selectedOrderNo == null || selectedOrderNo.isBlank()) {
                return;
            }
            JasperProductOrderReports.showOrderReportByOrderNo(this, selectedOrderNo, session);
            statusLabel.setText("Displayed Jasper product order report for Order No " + selectedOrderNo + ".");
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to display Jasper report.", ex instanceof Exception ? (Exception) ex : null);
        }
    }

    private String chooseOrderNoForPrinting() throws Exception {
        if (currentOrderNo != null && !currentOrderNo.isBlank() && currentOrderId != null) {
            int answer = StyledOptionPane.showConfirm(
                    this,
                    "Print the currently loaded saved order?\nOrder No: " + currentOrderNo,
                    "Print Order Report",
                    true
            );
            if (answer == JOptionPane.YES_OPTION) {
                return currentOrderNo;
            }
            if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
                return null;
            }
        }

        List<ProductOrderDao.SavedOrderSummary> recent = dao.findRecentOrders(30);
        if (recent.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No saved orders were found in the last 30 days.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        ProductOrderDao.SavedOrderSummary preselected = recent.get(0);
        if (currentOrderNo != null && !currentOrderNo.isBlank()) {
            for (ProductOrderDao.SavedOrderSummary row : recent) {
                if (currentOrderNo.equalsIgnoreCase(row.orderNo())) {
                    preselected = row;
                    break;
                }
            }
        }
//        ProductOrderDao.SavedOrderSummary selected = (ProductOrderDao.SavedOrderSummary) JOptionPane.showInputDialog(
//                this,
//                "Select Order No to print:",
//                "Print Order Report",
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                recent.toArray(),
//                preselected);
//        return selected == null ? null : selected.orderNo();
        ProductOrderDao.SavedOrderSummary selected = StyledOptionPane.showSelection(
                this,
                "Select Order No to print:",
                "Print Order Report",
                recent.toArray(new ProductOrderDao.SavedOrderSummary[0]),
                preselected
        );
        return selected == null ? null : selected.orderNo();
    }

    private class DirtyListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            markDirty();
            refreshDraftView();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            markDirty();
            refreshDraftView();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            markDirty();
            refreshDraftView();
        }
    }

    private static class StripeNumberRenderer extends DefaultTableCellRenderer {

        StripeNumberRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(column >= 6 && column <= 10 ? (column == 8 ? SwingUtils.formatQty(value) : (column == 6 || column == 7 ? SwingUtils.formatQty(value) : SwingUtils.formatMoney(value))) : String.valueOf(value));
            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    private static class ExpiryHtmlRenderer extends DefaultTableCellRenderer {

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

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }

    private SupplierOption resolveSelectedSupplier() {
        Object selected = supplierCombo.getSelectedItem();

        if (selected instanceof SupplierOption s) {
            return s;
        }

        if (selected instanceof String text) {
            String typed = text.trim();
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                SupplierOption item = supplierCombo.getItemAt(i);
                if (item != null && item.getName().equalsIgnoreCase(typed)) {
                    return item;
                }
            }
        }

        return null;
    }

    private void selectSupplierById(Long supplierId) {
        if (supplierId == null) {
            if (supplierCombo.getItemCount() > 0) {
                supplierCombo.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0; i < supplierCombo.getItemCount(); i++) {
            SupplierOption item = supplierCombo.getItemAt(i);
            if (item != null && Objects.equals(item.getId(), supplierId)) {
                supplierCombo.setSelectedItem(item);
                return;
            }
        }

        if (supplierCombo.getItemCount() > 0) {
            supplierCombo.setSelectedIndex(0);
        }
    }
}
