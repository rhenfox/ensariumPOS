package com.aldrin.ensarium.stockin;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class ReceiptChooserDialog extends JDialog {

    private final StockInDao stockInDao = new StockInDao();
    private final ReceiptTableModel model = new ReceiptTableModel();
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10));

    private final ReceiptLineTableModel detailModel = new ReceiptLineTableModel();
    private final JTable detailTable = new JTable(detailModel);
    private final JTextField txtDetailReceiptId = new JTextField(10);
    private final JTextField txtDetailReceiptNo = new JTextField(16);
    private final JTextField txtDetailStore = new JTextField(24);
    private final JTextField txtDetailSupplier = new JTextField(24);
    private final JTextField txtDetailReceivedBy = new JTextField(24);
    private final JTextField txtDetailLineCount = new JTextField(8);
    private final JTextField txtDetailTotal = new JTextField(14);
    private final JTextArea txtDetailNotes = new JTextArea(3, 40);

    private Long selectedReceiptId;

    private JPanel centerPanel = new JPanel(new BorderLayout());
    private JPanel westPanel = new JPanel();
    private JPanel eastPanel = new JPanel();
    private JPanel southPanel = new JPanel();

    public ReceiptChooserDialog(Frame owner) {
        this(owner, "Open Stock-In Receipt");
    }

    public ReceiptChooserDialog(java.awt.Window owner, String title) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        westPanel.setPreferredSize(new Dimension(10, 10));
        eastPanel.setPreferredSize(new Dimension(10, 10));
        southPanel.setPreferredSize(new Dimension(10, 10));
        initUi();
        loadReceipts();
    }

    Icon iconFile = FaSwingIcons.icon(FontAwesomeIcon.FILE_TEXT_ALT, 23, Color.WHITE);
    Icon iconFolder = FaSwingIcons.icon(FontAwesomeIcon.FOLDER_OPEN, 23, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 23, Color.WHITE);

    private void initUi() {
        centerPanel.add(buildFilterPanel(), BorderLayout.NORTH);
        centerPanel.add(buildCenterPanel(), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        StyledButton btnViewDetails = new StyledButton("Details");
        StyledButton btnOpen = new StyledButton("Open");
        StyledButton btnCancel = new StyledButton("Cancel");
        btnViewDetails.setToolTipText("View details");
        btnOpen.setToolTipText("Open stock-in");
        btnViewDetails.setIcon(iconFile);
        btnOpen.setIcon(iconFolder);
        btnCancel.setIcon(iconCancel);
        btnCancel.setDanger();
        btnViewDetails.addActionListener(e -> loadSelectedReceiptDetails(true));
        btnOpen.addActionListener(e -> openSelected());
        btnCancel.addActionListener(e -> dispose());
        actions.add(btnViewDetails);
        actions.add(btnCancel);
        actions.add(btnOpen);
        centerPanel.add(actions, BorderLayout.SOUTH);

        add(westPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        configureMainTable();
        configureDetailTable();
        installListeners();

        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        BootstrapTableStyle.install(table);
        table.setRowHeight(36);

        // Optional: right-click header to show/hide columns
        BootstrapTableStyle.enableColumnHiding(table);

        // Sample widths - adjust to your actual StockIn columns
        int[] widths = {55, 180, 180, 180, 180, 180};
        for (int i = 0; i < Math.min(widths.length, table.getColumnModel().getColumnCount()); i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }

        BootstrapTableStyle.setHeaderRight(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
        BootstrapTableStyle.setHeaderLeft(table, 5);

        // Sample alignment - adjust by real column indexes
        BootstrapTableStyle.setColumnRight(table, 0); // 
        BootstrapTableStyle.setColumnLeft(table, 1);  // 
        BootstrapTableStyle.setColumnLeft(table, 2);  // 
        BootstrapTableStyle.setColumnLeft(table, 3);  // 
        BootstrapTableStyle.setColumnLeft(table, 4);  // 
        BootstrapTableStyle.setColumnLeft(table, 5); // 

        setSize(1320, 800);
        setLocationRelativeTo(getOwner());
    }

    Icon iconSearch = FaSwingIcons.icon(FontAwesomeIcon.SEARCH, 23, Color.WHITE);
    Icon iconTrash = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 23, Color.WHITE);

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        StyledButton btnSearch = new StyledButton("Search");
        btnSearch.setIcon(iconSearch);

        StyledButton btnClear = new StyledButton("Clear");
        btnClear.setIcon(iconTrash);
        btnClear.setSecondary();

        txtSearch.addActionListener(e -> loadReceipts());
        btnSearch.addActionListener(e -> loadReceipts());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            spnLimit.setValue(Integer.valueOf(100));
            loadReceipts();
        });
        JLabel searchl = new JLabel("Search");
        searchl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(searchl);
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setPreferredSize(new Dimension(300, 30));
        panel.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(limitl);
        spnLimit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        spnLimit.setPreferredSize(new Dimension(80, 30));
        panel.add(spnLimit);
        panel.add(btnSearch);
        panel.add(btnClear);
        return panel;
    }

    private JSplitPane buildCenterPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table),
                buildDetailsPanel());
        splitPane.setResizeWeight(0.50d);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(buildDetailHeaderPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(detailTable), BorderLayout.CENTER);
        panel.add(new JScrollPane(txtDetailNotes), BorderLayout.SOUTH);
        TitledBorder border = BorderFactory.createTitledBorder("Notes");
        border.setTitleFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtDetailNotes.setBorder(border);
        txtDetailNotes.setLineWrap(true);
        txtDetailNotes.setWrapStyleWord(true);
        txtDetailNotes.setEditable(false);
        return panel;
    }

    private JPanel buildDetailHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        makeReadOnly(txtDetailReceiptId, txtDetailReceiptNo, txtDetailStore,
                txtDetailSupplier, txtDetailReceivedBy, txtDetailLineCount, txtDetailTotal);

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel receiptId = new JLabel("Receipt ID");
        receiptId.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(receiptId, gbc);
        gbc.gridx = 1;
        txtDetailReceiptId.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailReceiptId, gbc);
        gbc.gridx = 2;
        JLabel receiptNo = new JLabel("Receipt No");
        receiptNo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(receiptNo, gbc);
        gbc.gridx = 3;
        txtDetailReceiptNo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailReceiptNo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel storel = new JLabel("Receipt No");
        storel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(storel, gbc);
        gbc.gridx = 1;
        txtDetailStore.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailStore, gbc);
        gbc.gridx = 2;
        JLabel supplierl = new JLabel("Supplier");
        supplierl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(supplierl, gbc);
        gbc.gridx = 3;
        txtDetailSupplier.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailSupplier, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel receivedByl = new JLabel("Received By");
        receivedByl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(receivedByl, gbc);
        gbc.gridx = 1;
        txtDetailReceivedBy.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailReceivedBy, gbc);
        gbc.gridx = 2;
        JLabel lineCountl = new JLabel("Line Count");
        lineCountl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(lineCountl, gbc);
        gbc.gridx = 3;
        txtDetailLineCount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailLineCount, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel grandTotall = new JLabel("Grand Total");
        grandTotall.setFont(new java.awt.Font("Segoe UI", 1, 14));
        panel.add(grandTotall, gbc);
        gbc.gridx = 1;
        txtDetailTotal.setFont(new java.awt.Font("Segoe UI", 0, 14));
        panel.add(txtDetailTotal, gbc);
        TitledBorder border = BorderFactory.createTitledBorder("Receipt Details");
        border.setTitleFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.setBorder(border);
        return panel;
    }

    private void makeReadOnly(JTextField... fields) {
        for (JTextField field : fields) {
            field.setEditable(false);
        }
    }

    private void configureMainTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
    }

    private void configureDetailTable() {
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        detailTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        BootstrapTableStyle.install(detailTable);
        detailTable.setRowHeight(36);

        // Optional: right-click header to show/hide columns
        BootstrapTableStyle.enableColumnHiding(detailTable);

        // Sample widths - adjust to your actual StockIn columns
        int[] widths = {150, 300, 100, 120, 100, 100, 100, 100};
        for (int i = 0; i < Math.min(widths.length, detailTable.getColumnModel().getColumnCount()); i++) {
            BootstrapTableStyle.setColumnWidth(detailTable, i, widths[i]);
        }

        BootstrapTableStyle.setHeaderLeft(detailTable, 0);
        BootstrapTableStyle.setHeaderLeft(detailTable, 1);
        BootstrapTableStyle.setHeaderLeft(detailTable, 2);
        BootstrapTableStyle.setHeaderLeft(detailTable, 3);
        BootstrapTableStyle.setHeaderLeft(detailTable, 4);
        BootstrapTableStyle.setHeaderRight(detailTable, 5);
        BootstrapTableStyle.setHeaderRight(detailTable, 6);
        BootstrapTableStyle.setHeaderRight(detailTable, 7);

        // Sample alignment - adjust by real column indexes
        BootstrapTableStyle.setColumnLeft(detailTable, 0); // 
        BootstrapTableStyle.setColumnLeft(detailTable, 1);  // 
        BootstrapTableStyle.setColumnLeft(detailTable, 2);  // 
        BootstrapTableStyle.setColumnLeft(detailTable, 3);  // 
        BootstrapTableStyle.setColumnLeft(detailTable, 4);  // 
        BootstrapTableStyle.setColumnRight(detailTable, 5); // 
        BootstrapTableStyle.setColumnRight(detailTable, 6); // 
        BootstrapTableStyle.setColumnRight(detailTable, 7); // 
    }

    private void installListeners() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadReceipts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadReceipts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadReceipts();
            }
        });
        spnLimit.addChangeListener(e -> loadReceipts());
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedReceiptDetails(false);
                }
            }
        });
    }

    private void loadReceipts() {
        try {
            model.setRows(stockInDao.listReceipts(txtSearch.getText(), ((Number) spnLimit.getValue()).intValue()));
            if (model.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
                loadSelectedReceiptDetails(false);
            } else {
                clearDetails();
            }
        } catch (Exception ex) {
            clearDetails();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Receipts", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedReceiptDetails(boolean showMessageIfNoneSelected) {
        int row = table.getSelectedRow();
        if (row < 0) {
            clearDetails();
            if (showMessageIfNoneSelected) {
                JOptionPane.showMessageDialog(this, "Select a receipt first.");
            }
            return;
        }
        ReceiptLookup selected = model.getRow(row);
        try {
            LoadedStockIn loaded = stockInDao.loadReceipt(selected.getId().longValue());
            if (loaded == null || loaded.getHeader() == null) {
                clearDetails();
                if (showMessageIfNoneSelected) {
                    JOptionPane.showMessageDialog(this, "Stock-in details not found.");
                }
                return;
            }
            showDetails(selected, loaded);
        } catch (Exception ex) {
            clearDetails();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Receipt Details", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails(ReceiptLookup selected, LoadedStockIn loaded) {
        StockInHeader header = loaded.getHeader();
        txtDetailReceiptId.setText(header.getReceiptId() == null ? "" : String.valueOf(header.getReceiptId()));
        txtDetailReceiptNo.setText(nvl(header.getReceiptNo()));
        txtDetailStore.setText(nvl(selected == null ? null : selected.getStoreName()));
        txtDetailSupplier.setText(nvl(selected == null ? null : selected.getSupplierName()));
        txtDetailReceivedBy.setText(nvl(selected == null ? null : selected.getReceivedByName()));
        txtDetailLineCount.setText(String.valueOf(loaded.getLines() == null ? 0 : loaded.getLines().size()));
        txtDetailTotal.setText(formatMoney(totalOf(loaded.getLines())));
        txtDetailNotes.setText(nvl(header.getNotes()));
        detailModel.setRows(loaded.getLines());
    }

    private void clearDetails() {
        txtDetailReceiptId.setText("");
        txtDetailReceiptNo.setText("");
        txtDetailStore.setText("");
        txtDetailSupplier.setText("");
        txtDetailReceivedBy.setText("");
        txtDetailLineCount.setText("0");
        txtDetailTotal.setText("0.00");
        txtDetailNotes.setText("");
        detailModel.setRows(new ArrayList<>());
    }

    private BigDecimal totalOf(List<StockInLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        if (lines != null) {
            for (StockInLine line : lines) {
                total = total.add(line == null ? BigDecimal.ZERO : line.getTotal());
            }
        }
        return total;
    }

    private String formatMoney(BigDecimal value) {
        return new DecimalFormat("#,##0.00").format((value == null ? BigDecimal.ZERO : value));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a receipt first.");
            return;
        }
        selectedReceiptId = model.getRow(row).getId();
        dispose();
    }

    public Long getSelectedReceiptId() {
        return selectedReceiptId;
    }

    private static class ReceiptTableModel extends AbstractTableModel {

        private final String[] cols = {"ID", "Receipt No", "Store", "Supplier", "Received By", "Received At"};
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
        private List<ReceiptLookup> rows = new ArrayList<>();

        public void setRows(List<ReceiptLookup> rows) {
            this.rows = rows == null ? new ArrayList<>() : rows;
            fireTableDataChanged();
        }

        public ReceiptLookup getRow(int row) {
            return rows.get(row);
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
            ReceiptLookup row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    row.getId();
                case 1 ->
                    row.getReceiptNo();
                case 2 ->
                    row.getStoreName();
                case 3 ->
                    row.getSupplierName();
                case 4 ->
                    row.getReceivedByName() == null ? "" : row.getReceivedByName();
                case 5 ->
                    row.getReceivedAt() == null ? "" : sdf.format(row.getReceivedAt());
                default ->
                    "";
            };
        }
    }

    private static class ReceiptLineTableModel extends AbstractTableModel {

        private final String[] cols = {"SKU", "Product", "Lot No", "Expiry Date", "Unit", "Qty Base", "Unit Cost", "Total"};
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        private final DecimalFormat money = new DecimalFormat("#,##0.00");
        private List<StockInLine> rows = new ArrayList<>();

        public void setRows(List<StockInLine> rows) {
            this.rows = rows == null ? new ArrayList<>() : rows;
            fireTableDataChanged();
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
            StockInLine line = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    line.getProduct() == null ? "" : line.getProduct().getSku();
                case 1 ->
                    line.getProduct() == null ? "" : line.getProduct().getName();
                case 2 ->
                    line.getLotNo();
                case 3 ->
                    line.getExpiryDate() == null ? "" : sdf.format(line.getExpiryDate());
                case 4 ->
                    line.getUnit() == null ? "" : line.getUnit().getCode();
                case 5 ->
                    line.getQuantityInBase().setScale(2, RoundingMode.HALF_UP);
                case 6 ->
                    money.format(line.getUnitCost());
                case 7 ->
                    money.format(line.getTotal());
                default ->
                    "";
            };
        }
    }
}
