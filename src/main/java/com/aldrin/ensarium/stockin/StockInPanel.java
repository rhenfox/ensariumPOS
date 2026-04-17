package com.aldrin.ensarium.stockin;

import com.aldrin.ensarium.dispense.ButtonColumn;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

public class StockInPanel extends JPanel {

    private final StockInDao stockInDao = new StockInDao();
    private final JTextField txtReceiptId = new JTextField(8);
    private final JTextField txtReceiptNo = new JTextField(14);
    private final JComboBox<LookupOption> cboStore = new JComboBox<>();
    private final JComboBox<LookupOption> cboSupplier = new JComboBox<>();
    private final JTextArea txtNotes = new JTextArea(3, 35);

    private enum FormMode {
        NEW, EDIT, RETURN
    }

    private final JLabel lblTotal = new JLabel("0.00", SwingConstants.RIGHT);
    private final LineTableModel lineModel = new LineTableModel();
    private final JTable table = new JTable(lineModel);
    private final StyledButton btnNew = new StyledButton("New");
    private final StyledButton btnAddProduct = new StyledButton("Product");
    private final StyledButton btnOpen = new StyledButton("Open");
    private final StyledButton btnReturn = new StyledButton("Return");
    private final StyledButton btnSave = new StyledButton("Save");
    private FormMode formMode = FormMode.NEW;

    private JPanel centerPanel = new JPanel();
    private JPanel eastPanel = new JPanel();
    private JPanel westPanel = new JPanel();

    private Session session;

    public StockInPanel(Session session) {
        this.session = session;
        initUi();
        loadLookups();
        clearForm();
        applyComboBoxAutoFill(cboStore);
        applyComboBoxAutoFill(cboSupplier);
//        .setFont(new java.awt.Font("Segoe UI", 1, 14)); 

//        StyledButton yes = new StyledButton("Yes");
//        yes.setButtonStyle(StyledButton.ButtonStyle.SUCCESS);
//        yes.setPreferredSize(new Dimension(100, 36));
//
//        StyledButton no = new StyledButton("No");
//        no.setButtonStyle(StyledButton.ButtonStyle.DANGER);
//        no.setPreferredSize(new Dimension(100, 36));
//
//        StyledButton cancel = new StyledButton("Cancel");
//        cancel.setButtonStyle(StyledButton.ButtonStyle.SECONDARY);
//        cancel.setPreferredSize(new Dimension(100, 36));
//
//        Object[] options = {yes, no, cancel};
//
//        int result = JOptionPane.showOptionDialog(
//                null,
//                "Do you want to save this record?",
//                "Confirm",
//                JOptionPane.DEFAULT_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                options,
//                yes
//        );
    }

    Icon iconNew = FaSwingIcons.icon(FontAwesomeIcon.FILE_ALT, 24, Color.WHITE);
    Icon iconPlus = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconReturn = FaSwingIcons.icon(FontAwesomeIcon.REPLY, 24, Color.WHITE);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        centerPanel.setLayout(new BorderLayout());

        txtNotes.putClientProperty("JTextField.placeholderText", "Enter notes...");

        eastPanel.setPreferredSize(new Dimension(10, 10));
        westPanel.setPreferredSize(new Dimension(10, 10));

        btnNew.setPreferredSize(new Dimension(80, 30));
        btnAddProduct.setPreferredSize(new Dimension(80, 30));
        btnOpen.setPreferredSize(new Dimension(80, 30));
        btnReturn.setPreferredSize(new Dimension(80, 30));
        btnSave.setPreferredSize(new Dimension(80, 30));

        btnNew.setIcon(iconNew);
        btnAddProduct.setIcon(iconPlus);
        btnOpen.setIcon(iconEdit);
        btnReturn.setIcon(iconReturn);
        btnSave.setIcon(iconSave);

        btnNew.setMargin(new Insets(2, 4, 2, 4));
        btnAddProduct.setMargin(new Insets(2, 4, 2, 4));
        btnOpen.setMargin(new Insets(2, 4, 2, 4));
        btnReturn.setMargin(new Insets(2, 4, 2, 4));
        btnSave.setMargin(new Insets(2, 4, 2, 4));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        btnNew.addActionListener(e -> clearForm());
        btnAddProduct.addActionListener(e -> addProduct());
        btnOpen.addActionListener(e -> openReceipt());
        btnReturn.addActionListener(e -> returnReceipt());
        btnSave.addActionListener(e -> saveReceipt());

        topBar.add(btnNew);
        topBar.add(btnAddProduct);
        topBar.add(btnOpen);
        topBar.add(btnReturn);
        topBar.add(btnSave);
//        add(topBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(buildHeaderPanel(), BorderLayout.NORTH);

        table.setRowHeight(28);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
        table.getColumnModel().getColumn(9).setPreferredWidth(70);

        Icon iconEdit1 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 120, 215));
        Icon iconEdit2 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 90, 180));
        Icon iconDelete1 = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 23, new Color(220, 53, 69));
        Icon iconDelete2 = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 23, new Color(176, 42, 55));

        ButtonColumn colEdit = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
//                editQty(modelRow);
                editLine(Integer.parseInt(e.getActionCommand()));
            }
        }, 8);
        colEdit.setIcons(iconEdit1, iconEdit2);

        ButtonColumn colDel = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
//                deleteRow(modelRow);
                deleteLine(Integer.parseInt(e.getActionCommand()));
            }
        }, 9);
        colDel.setIcons(iconDelete1, iconDelete2);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JLabel grandTotall = new JLabel("Grand Total:");
        grandTotall.setFont(new java.awt.Font("Segoe UI", 1, 18));
        totalPanel.add(grandTotall);
        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblTotal.setPreferredSize(new Dimension(140, 24));
        totalPanel.add(lblTotal);
        bottom.add(totalPanel, BorderLayout.NORTH);
        centerPanel.add(topBar, BorderLayout.NORTH);
        centerPanel.add(center, BorderLayout.CENTER);
        centerPanel.add(bottom, BorderLayout.SOUTH);
        centerPanel.add(westPanel, BorderLayout.WEST);
        centerPanel.add(eastPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setPreferredSize(new Dimension(1280, 680));
//        pack();
        updateActionStates();
        styleStockInTable();
//        setLocationRelativeTo(null);
    }

    private void styleStockInTable() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        BootstrapTableStyle.install(table);
        table.setRowHeight(36);

        // Optional: right-click header to show/hide columns
        BootstrapTableStyle.enableColumnHiding(table);

        // Sample widths - adjust to your actual StockIn columns
        int[] widths = {55, 300, 100, 130, 100, 120, 120, 80, 80};
        for (int i = 0; i < Math.min(widths.length, table.getColumnModel().getColumnCount()); i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }

        BootstrapTableStyle.setHeaderRight(table, 0);
        BootstrapTableStyle.setHeaderRight(table, 5);
        BootstrapTableStyle.setHeaderRight(table, 6);
        BootstrapTableStyle.setHeaderRight(table, 7);
        BootstrapTableStyle.setHeaderCenter(table, 8);
        BootstrapTableStyle.setHeaderCenter(table, 9);

        // Sample alignment - adjust by real column indexes
        BootstrapTableStyle.setColumnRight(table, 0); // No
        BootstrapTableStyle.setColumnLeft(table, 1);  // Stock In No / Ref No
        BootstrapTableStyle.setColumnLeft(table, 2);  // Date
        BootstrapTableStyle.setColumnLeft(table, 3);  // Supplier
        BootstrapTableStyle.setColumnLeft(table, 4);  // Product / Remarks
        BootstrapTableStyle.setColumnRight(table, 5); // Qty
        BootstrapTableStyle.setColumnRight(table, 6); // Cost
        BootstrapTableStyle.setColumnRight(table, 7); // Total
        BootstrapTableStyle.setColumnLeft(table, 8);  // User / Status

        installStockInRenderers();

    }

    private void installStockInRenderers() {
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(new RightRenderer());
        }
        if (table.getColumnModel().getColumnCount() > 5) {
            table.getColumnModel().getColumn(5).setCellRenderer(new RightRenderer()); // qty
        }
        if (table.getColumnModel().getColumnCount() > 6) {
            table.getColumnModel().getColumn(6).setCellRenderer(new RightRenderer()); // cost
        }
        if (table.getColumnModel().getColumnCount() > 7) {
            table.getColumnModel().getColumn(7).setCellRenderer(new RightRenderer()); // total
        }
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtReceiptId.setEditable(false);

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel receiptIDl = new JLabel("Receipt ID");
        receiptIDl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(receiptIDl, gbc);
        gbc.gridx = 1;
        txtReceiptId.setPreferredSize(new Dimension(200, 30));
        txtReceiptId.putClientProperty("JTextField.placeholderText", "Enter receipt ID");
        panel.add(txtReceiptId, gbc);
        gbc.gridx = 2;
        JLabel receiptNol = new JLabel("Receipt No");
        receiptNol.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(receiptNol, gbc);
        gbc.gridx = 3;
        txtReceiptNo.setPreferredSize(new Dimension(200, 30));
        txtReceiptNo.putClientProperty("JTextField.placeholderText", "Enter receipt no");
        panel.add(txtReceiptNo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel storeIDl = new JLabel("Store ID");
        storeIDl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(storeIDl, gbc);
        gbc.gridx = 1;
        cboStore.setPreferredSize(new Dimension(200, 30));
        panel.add(cboStore, gbc);
        gbc.gridx = 2;
        JLabel supplierIDl = new JLabel("Supplier ID");
        supplierIDl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(supplierIDl, gbc);
        gbc.gridx = 3;
        cboSupplier.setPreferredSize(new Dimension(200, 30));
        panel.add(cboSupplier, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel notesl = new JLabel("Notes");
        notesl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(notesl, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtNotes.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(new JScrollPane(txtNotes), gbc);

        cboStore.setPrototypeDisplayValue(new LookupOption(999999L, "STORECODE", "Store Name"));
        cboSupplier.setPrototypeDisplayValue(new LookupOption(999999L, "SUPP-001", "Supplier Name"));
        ComboAutoFillSupport.install(cboStore, new ArrayList<>(), false);
        ComboAutoFillSupport.install(cboSupplier, new ArrayList<>(), false);

        return panel;
    }

    private void loadLookups() {
        try {
            setComboItems(cboStore, stockInDao.listStores());
            setComboItems(cboSupplier, stockInDao.listSuppliers());
        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this,
                    "Unable to load lookup values from database.\n" + ex.getMessage(),
                    "Load Header Lookups",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct() {
        StockInLine line = new StockInLine();
        StockInLineDialog dialog = new StockInLineDialog(SwingUtilities.getWindowAncestor(this), line);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            lineModel.addLine(line);
            refreshTotals();
        }
    }

    private void editLine(int row) {
        StockInLine line = lineModel.getLine(row);
        StockInLineDialog dialog = new StockInLineDialog(SwingUtilities.getWindowAncestor(this), line);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            lineModel.fireTableRowsUpdated(row, row);
            refreshTotals();
        }
    }

    private void deleteLine(int row) {
        if (JOptionPane.showConfirmDialog(this, "Delete this line?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            lineModel.removeLine(row);
            refreshTotals();
        }
    }

    private void openReceipt() {
        Long receiptId = chooseReceiptId("Open Stock-In Receipt");
        if (receiptId == null) {
            return;
        }
        try {
            loadReceiptIntoForm(receiptId);
            setFormMode(FormMode.EDIT);
        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage(), "Open Stock-In", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceipt() {
        try {
            LookupOption store = (LookupOption) cboStore.getSelectedItem();
            LookupOption supplier = (LookupOption) cboSupplier.getSelectedItem();
            if (store == null || store.getId() == null) {
                throw new IllegalArgumentException("Store ID is required.");
            }
            Integer receivedByUserId = resolveCurrentUserIdForSave();

            StockInHeader header = new StockInHeader();
            if (!txtReceiptId.getText().trim().isEmpty()) {
                header.setReceiptId(Long.valueOf(Long.parseLong(txtReceiptId.getText().trim())));
            }
            header.setReceiptNo(blankToNull(txtReceiptNo.getText()));
            header.setStoreId(store.getId().intValue());
            header.setSupplierId(supplier == null ? null : supplier.getId());
            header.setReceivedBy(receivedByUserId);
            header.setNotes(txtNotes.getText());

            long receiptId = stockInDao.saveReceipt(header, lineModel.getLines());
            JOptionPane.showMessageDialog(this, "Stock-in saved successfully. Receipt ID: " + receiptId);

            clearForm();
        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Stock-In", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnReceipt() {
        try {
            Long receiptId = resolveReceiptIdForReturn();
            if (receiptId == null) {
                return;
            }
            setFormMode(FormMode.RETURN);
            clearGridForReturnSelection();
            LookupOption store = (LookupOption) cboStore.getSelectedItem();
            if (store == null || store.getId() == null) {
                throw new IllegalArgumentException("Store ID is required.");
            }
            int storeId = store.getId().intValue();
            List<ReturnableLine> lines = stockInDao.listReturnableLines(receiptId);
            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No remaining quantities available for return.");

                return;
            }
//            StockInReturnDialog dialog = new StockInReturnDialog(this, lines);
            StockInReturnDialog dialog = new StockInReturnDialog(SwingUtilities.getWindowAncestor(this), lines);
            dialog.setVisible(true);
            if (!dialog.isProcessed()) {
                return;
            }
            long returnId = stockInDao.createReturn(
                    receiptId,
                    storeId,
                    dialog.getCreatedBy() == null ? resolveCurrentUserIdForSave() : dialog.getCreatedBy(),
                    dialog.getReturnedBy(),
                    dialog.getReturnedByName(),
                    dialog.getReasonId(),
                    dialog.getReasonName(),
                    dialog.getNotes(),
                    dialog.getEntries());
            JOptionPane.showMessageDialog(this, "Stock-in return saved. Inventory Txn ID: " + returnId);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Stock-In", JOptionPane.ERROR_MESSAGE);

        } finally {
            setFormMode(FormMode.NEW);
        }
    }

    private Long chooseReceiptId(String dialogTitle) {
//        ReceiptChooserDialog dialog = new ReceiptChooserDialog(this, dialogTitle);
        ReceiptChooserDialog dialog = new ReceiptChooserDialog(SwingUtilities.getWindowAncestor(this), dialogTitle);
        dialog.setVisible(true);
        return dialog.getSelectedReceiptId();
    }

    private Long resolveReceiptIdForReturn() throws Exception {
        if (!txtReceiptId.getText().trim().isEmpty()) {
            return Long.valueOf(Long.parseLong(txtReceiptId.getText().trim()));
        }
        Long receiptId = chooseReceiptId("Select Stock-In Receipt to Return");
        if (receiptId == null) {
            return null;
        }
        loadReceiptIntoForm(receiptId);
        return receiptId;
    }

    private void loadReceiptIntoForm(Long receiptId) throws Exception {
        LoadedStockIn loaded = stockInDao.loadReceipt(receiptId);
        if (loaded == null) {
            throw new IllegalArgumentException("Stock-in not found.");
        }
        txtReceiptId.setText(String.valueOf(loaded.getHeader().getReceiptId()));
        txtReceiptNo.setText(nvl(loaded.getHeader().getReceiptNo()));
        selectComboById(cboStore, Long.valueOf(loaded.getHeader().getStoreId()));
        selectComboById(cboSupplier, loaded.getHeader().getSupplierId());
        txtNotes.setText(nvl(loaded.getHeader().getNotes()));
        lineModel.setLines(loaded.getLines());
        refreshTotals();
    }

    private void clearGridForReturnSelection() {
        lineModel.setLines(new ArrayList<>());
        refreshTotals();
    }

    private void clearForm() {
        txtReceiptId.setText("");
        txtReceiptNo.setText("");
        if (cboStore.getItemCount() > 0) {
            cboStore.setSelectedIndex(0);
        }
        selectComboById(cboSupplier, null);
        txtNotes.setText("");
        lineModel.setLines(new ArrayList<>());
        refreshTotals();
        setFormMode(FormMode.NEW);
    }

    private Integer resolveCurrentUserIdForSave() {
        CurrentUser currentUser = SessionResolver.resolveCurrentUser();
        if (currentUser != null) {
            if (currentUser.getId() != null) {
                return Integer.valueOf(currentUser.getId().intValue());
            }
            try {
                for (LookupOption user : stockInDao.listUsers()) {
                    if (user == null || user.getId() == null) {
                        continue;
                    }
                    boolean usernameMatch = currentUser.getUsername() != null
                            && currentUser.getUsername().trim().equalsIgnoreCase(user.getCode() == null ? "" : user.getCode().trim());
                    boolean fullNameMatch = currentUser.getFullName() != null
                            && currentUser.getFullName().trim().equalsIgnoreCase(user.getName() == null ? "" : user.getName().trim());
                    if (usernameMatch || fullNameMatch) {
                        return Integer.valueOf(user.getId().intValue());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return Integer.valueOf(1);
    }

    private void setFormMode(FormMode mode) {
        formMode = mode == null ? FormMode.NEW : mode;
        updateActionStates();
    }

    private void updateActionStates() {
        boolean newMode = formMode == FormMode.NEW;
        boolean editMode = formMode == FormMode.EDIT;
        boolean returnMode = formMode == FormMode.RETURN;

        btnNew.setEnabled(true);
        btnOpen.setEnabled(!returnMode);
        btnReturn.setEnabled(!editMode);
        btnAddProduct.setEnabled(!returnMode);
        btnSave.setEnabled(!returnMode);
    }

    private void refreshTotals() {
        BigDecimal total = BigDecimal.ZERO;
        for (StockInLine line : lineModel.getLines()) {
            total = total.add(line.getTotal());
        }
        lblTotal.setText(formatMoney(total));
    }

    private String formatMoney(BigDecimal value) {
        return new DecimalFormat("#,##0.00").format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private String blankToNull(String value) {
        String v = value == null ? null : value.trim();
        return v == null || v.isEmpty() ? null : v;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void setComboItems(JComboBox<LookupOption> combo, List<LookupOption> items) {
        ComboAutoFillSupport.updateItems(combo, items);
    }

    private void selectComboById(JComboBox<LookupOption> combo, Long id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            LookupOption item = combo.getItemAt(i);
            if (id == null) {
                if (item == null || item.getId() == null) {
                    combo.setSelectedIndex(i);
                    return;
                }
            } else if (item != null && id.equals(item.getId())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private static class LineTableModel extends AbstractTableModel {

        private final String[] cols = {"No", "Product", "Lot No", "Expiry Date", "Unit", "Quantity", "Unit Cost", "Total", "Edit", "Delete"};
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        private List<StockInLine> lines = new ArrayList<>();

        public void addLine(StockInLine line) {
            lines.add(line);
            fireTableDataChanged();
        }

        public void removeLine(int index) {
            lines.remove(index);
            fireTableDataChanged();
        }

        public StockInLine getLine(int index) {
            return lines.get(index);
        }

        public List<StockInLine> getLines() {
            return lines;
        }

        public void setLines(List<StockInLine> lines) {
            this.lines = lines == null ? new ArrayList<>() : lines;
            fireTableDataChanged();
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
            return columnIndex == 8 || columnIndex == 9;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StockInLine line = lines.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    rowIndex + 1;
                case 1 ->
                    line.getProduct() == null ? "" : line.getProduct().getName();
                case 2 ->
                    line.getLotNo();
                case 3 ->
                    line.getExpiryDate() == null ? "" : sdf.format(line.getExpiryDate());
                case 4 ->
                    line.getUnit() == null ? "" : line.getUnit().getCode();
                case 5 ->
                    line.getQuantityInBase().setScale(1, RoundingMode.HALF_UP);
                case 6 ->
                    line.getUnitCost().setScale(2, RoundingMode.HALF_UP);
                case 7 ->
                    line.getTotal().setScale(2, RoundingMode.HALF_UP);
                case 8 ->
                    "Edit";
                case 9 ->
                    "Delete";
                default ->
                    "";
            };
        }
    }

    private static class RightRenderer extends DefaultTableCellRenderer {

        RightRenderer() {
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

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }


}
