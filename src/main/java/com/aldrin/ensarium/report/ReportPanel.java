package com.aldrin.ensarium.report;

import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

public class ReportPanel extends JPanel {

    @FunctionalInterface
    public interface RowActionHandler {
        void handle(Component parent, ReportPanel panel, int modelRowIndex) throws Exception;
    }

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final DecimalFormat INTEGER = new DecimalFormat("#,##0");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MMM-dd");

    private final String reportTitle;
    private final String jrxmlName;
    private final BiFunction<Date, Date, DefaultTableModel> loader;
    private final ReportService reportService;
    private final JTable table;
    private final JDateChooser fromDate;
    private final JDateChooser toDate;
    private final JTextField txtSearch;
    private final JSpinner spnLimit;
    private final LinkedHashMap<String, String> totalsConfig;
    private final LinkedHashMap<String, JLabel> totalLabels;
    private final String rowActionName;
    private final RowActionHandler rowActionHandler;

    public ReportPanel(String reportTitle,
            String jrxmlName,
            BiFunction<Date, Date, DefaultTableModel> loader,
            ReportService reportService) {
        this(reportTitle, jrxmlName, loader, reportService, (Map<String, String>) null, null, null, false);
    }

    public ReportPanel(String reportTitle,
            String jrxmlName,
            BiFunction<Date, Date, DefaultTableModel> loader,
            ReportService reportService,
            String totalColumnName,
            String totalLabelPrefix) {
        this(reportTitle, jrxmlName, loader, reportService, singleTotal(totalColumnName, totalLabelPrefix), null, null, false);
    }

    public ReportPanel(String reportTitle,
            String jrxmlName,
            BiFunction<Date, Date, DefaultTableModel> loader,
            ReportService reportService,
            String totalColumnName,
            String totalLabelPrefix,
            String rowActionName,
            RowActionHandler rowActionHandler) {
        this(reportTitle, jrxmlName, loader, reportService, singleTotal(totalColumnName, totalLabelPrefix), rowActionName, rowActionHandler, false);
    }

    public ReportPanel(String reportTitle,
            String jrxmlName,
            BiFunction<Date, Date, DefaultTableModel> loader,
            ReportService reportService,
            Map<String, String> totalsConfig) {
        this(reportTitle, jrxmlName, loader, reportService, totalsConfig, null, null, false);
    }

    public ReportPanel(String reportTitle,
            String jrxmlName,
            BiFunction<Date, Date, DefaultTableModel> loader,
            ReportService reportService,
            Map<String, String> totalsConfig,
            String rowActionName,
            RowActionHandler rowActionHandler,
            boolean includeRefreshPopup) {
        this.reportTitle = reportTitle;
        this.jrxmlName = jrxmlName;
        this.loader = loader;
        this.reportService = reportService;
        this.table = new JTable();
        this.fromDate = new JDateChooser(Dates.defaultFromDate());
        this.toDate = new JDateChooser(Dates.defaultToDate());
        this.txtSearch = new JTextField(24);
        this.spnLimit = new JSpinner(new SpinnerNumberModel(1000, 1, 1000000, 100));
        this.totalsConfig = totalsConfig == null ? new LinkedHashMap<>() : new LinkedHashMap<>(totalsConfig);
        this.totalLabels = new LinkedHashMap<>();
        this.rowActionName = rowActionName;
        this.rowActionHandler = rowActionHandler;
        initUi(includeRefreshPopup);
        loadData();
    }

    private void initUi(boolean includeRefreshPopup) {
        setLayout(new BorderLayout(8, 8));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLoad = new JButton("Load");
        JButton btnPrint = new JButton("Print Report");

        north.add(new JLabel("From:"));
        fromDate.setDateFormatString("yyyy-MMM-dd");
        north.add(fromDate);
        north.add(new JLabel("To:"));
        toDate.setDateFormatString("yyyy-MMM-dd");
        north.add(toDate);
        north.add(new JLabel("Search:"));
        north.add(txtSearch);
        north.add(new JLabel("Limit:"));
        JComponent editor = ((JSpinner.DefaultEditor) spnLimit.getEditor());
        editor.setPreferredSize(new java.awt.Dimension(90, editor.getPreferredSize().height));
        north.add(spnLimit);
        north.add(btnLoad);
        north.add(btnPrint);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        installRenderers();
        installRowActionSupport(includeRefreshPopup);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        if (totalsConfig.isEmpty()) {
            JLabel spacer = new JLabel(" ");
            south.add(spacer);
        } else {
            for (String labelPrefix : totalsConfig.values()) {
                JLabel lbl = new JLabel(labelPrefix + " 0.00");
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                totalLabels.put(labelPrefix, lbl);
                south.add(lbl);
            }
        }
        add(south, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadData());
        btnPrint.addActionListener(e -> previewReport());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyTableFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyTableFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyTableFilters();
            }
        });
        spnLimit.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyTableFilters();
            }
        });
    }

    private void installRenderers() {
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.RIGHT);
                return this;
            }

            @Override
            protected void setValue(Object value) {
                if (value == null) {
                    setText("");
                } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
                    setText(INTEGER.format(((Number) value).longValue()));
                } else if (value instanceof Number number) {
                    setText(MONEY.format(number.doubleValue()));
                } else {
                    super.setValue(value);
                }
            }
        };

        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                BootstrapTableStyle.applyCellStyle(table, this, isSelected, row, column, SwingConstants.LEFT);
                return this;
            }

            @Override
            protected void setValue(Object value) {
                if (value instanceof java.util.Date date) {
                    setText(DATE_FMT.format(date));
                } else {
                    super.setValue(value);
                }
            }
        };

        table.setDefaultRenderer(Number.class, numberRenderer);
        table.setDefaultRenderer(java.util.Date.class, dateRenderer);
        table.setDefaultRenderer(java.sql.Date.class, dateRenderer);
        table.setDefaultRenderer(java.sql.Timestamp.class, dateRenderer);
    }

    private void installRowActionSupport(boolean includeRefreshPopup) {
        if (!includeRefreshPopup && (rowActionHandler == null || rowActionName == null || rowActionName.isBlank())) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        JMenuItem viewDetails = null;
        if (rowActionHandler != null && rowActionName != null && !rowActionName.isBlank()) {
            viewDetails = new JMenuItem(rowActionName);
            viewDetails.addActionListener(e -> invokeRowAction());
            popup.add(viewDetails);
        }
        if (includeRefreshPopup) {
            if (viewDetails != null) {
                popup.addSeparator();
            }
            JMenuItem refreshData = new JMenuItem("Refresh Data");
            refreshData.addActionListener(e -> loadData());
            popup.add(refreshData);
        }
        final JMenuItem finalViewDetails = viewDetails;

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        invokeRowAction();
                    }
                }
            }

            private void maybeShowPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                } else {
                    table.clearSelection();
                }
                if (finalViewDetails != null) {
                    finalViewDetails.setEnabled(table.getSelectedRow() >= 0);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void invokeRowAction() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.", reportTitle, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        try {
            rowActionHandler.handle(this, this, modelRow);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), reportTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try {
            DefaultTableModel model = loader.apply(requireDate(fromDate.getDate(), "from"), requireDate(toDate.getDate(), "to"));
            table.setModel(model);
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);
            applyBootstrapStyle();
            installRenderers();
            hideInternalColumns();
            applyTableFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), reportTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hideInternalColumns() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            String name = model.getColumnName(i);
            if (name != null && name.startsWith("__")) {
                try {
                    BootstrapTableStyle.hideColumn(table, i);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void applyBootstrapStyle() {
        BootstrapTableStyle.Style style = BootstrapTableStyle.Style.defaultStyle()
                .stripedRows(Color.WHITE, new Color(248, 249, 250))
                .hoverColor(new Color(233, 245, 255))
                .selection(new Color(13, 110, 253), Color.WHITE)
                .headerColors(new Color(248, 249, 250), new Color(33, 37, 41))
                .borderColor(new Color(222, 226, 230))
                .cellPadding(14, 14)
                .headerPadding(14, 14)
                .rowHeight(36)
                .headerHeight(38);

        BootstrapTableStyle.install(table, style);

        String key = jrxmlName == null ? "" : jrxmlName.toLowerCase();
        if (key.contains("bir_invoice_list")) {
            styleBirInvoiceList();
        } else if (key.contains("tax_summary")) {
            styleTaxSummary();
        } else if (key.contains("monthly_sales_machine")) {
            styleMonthlyMachine();
        } else if (key.contains("pos_profit")) {
            stylePosProfit();
        } else if (key.contains("financial_statement")) {
            styleFinancialStatement();
        }
    }

    private void styleBirInvoiceList() {
        hideModelColumn("__SALE_ID");
        setWidthByColumnName("Invoice No", 120, true);
        setWidthByColumnName("Date", 110, true);
        setWidthByColumnName("Store", 110, false);
        setWidthByColumnName("Terminal", 110, false);
        setWidthByColumnName("Seller TIN/Branch", 170, false);
        setWidthByColumnName("Buyer", 220, false);
        setWidthByColumnName("Buyer TIN", 140, false);
        setWidthByColumnName("PTU No", 130, false);
        setWidthByColumnName("ATP No", 130, false);
        setWidthByColumnName("Gross Sales", 120, false);
        setWidthByColumnName("Vatable Sales", 120, false);
        setWidthByColumnName("VAT Amount", 120, false);
        setWidthByColumnName("VAT Exempt", 120, false);
        setWidthByColumnName("Zero Rated", 120, false);
        setWidthByColumnName("Discount", 120, false);
        setWidthByColumnName("Total Due", 120, false);

        setIndentByColumnName("Seller TIN/Branch", 18, 12);
        setIndentByColumnName("Buyer", 18, 12);

        rightAlignByColumnName("Gross Sales");
        rightAlignByColumnName("Vatable Sales");
        rightAlignByColumnName("VAT Amount");
        rightAlignByColumnName("VAT Exempt");
        rightAlignByColumnName("Zero Rated");
        rightAlignByColumnName("Discount");
        rightAlignByColumnName("Total Due");
    }

    private void styleTaxSummary() {
        setWidthByColumnName("Date", 110, true);
        setWidthByColumnName("Store", 110, false);
        setWidthByColumnName("Terminal", 110, false);
        setWidthByColumnName("Invoice Count", 110, true);
        setWidthByColumnName("Void Count", 100, true);
        setWidthByColumnName("Gross Sales", 120, false);
        setWidthByColumnName("Vatable Sales", 120, false);
        setWidthByColumnName("VAT Amount", 120, false);
        setWidthByColumnName("VAT Exempt", 120, false);
        setWidthByColumnName("Zero Rated", 120, false);
        setWidthByColumnName("Withholding Tax", 130, false);
        setWidthByColumnName("Discount", 120, false);
        setWidthByColumnName("Total Due", 120, false);

        rightAlignByColumnName("Invoice Count");
        rightAlignByColumnName("Void Count");
        rightAlignByColumnName("Gross Sales");
        rightAlignByColumnName("Vatable Sales");
        rightAlignByColumnName("VAT Amount");
        rightAlignByColumnName("VAT Exempt");
        rightAlignByColumnName("Zero Rated");
        rightAlignByColumnName("Withholding Tax");
        rightAlignByColumnName("Discount");
        rightAlignByColumnName("Total Due");
    }

    private void styleMonthlyMachine() {
        setWidthByColumnName("Year", 80, true);
        setWidthByColumnName("Month", 80, true);
        setWidthByColumnName("Store", 100, false);
        setWidthByColumnName("Machine", 110, false);
        setWidthByColumnName("Seller TIN/Branch", 170, false);
        setWidthByColumnName("Invoice Count", 110, true);
        setWidthByColumnName("First Serial", 110, true);
        setWidthByColumnName("Last Serial", 110, true);
        setWidthByColumnName("Gross Sales", 120, false);
        setWidthByColumnName("Vatable Sales", 120, false);
        setWidthByColumnName("VAT Amount", 120, false);
        setWidthByColumnName("VAT Exempt", 120, false);
        setWidthByColumnName("Zero Rated", 120, false);
        setWidthByColumnName("Discount", 120, false);
        setWidthByColumnName("Withholding Tax", 130, false);
        setWidthByColumnName("Total Due", 120, false);

        setIndentByColumnName("Seller TIN/Branch", 18, 12);

        rightAlignByColumnName("Invoice Count");
        rightAlignByColumnName("First Serial");
        rightAlignByColumnName("Last Serial");
        rightAlignByColumnName("Gross Sales");
        rightAlignByColumnName("Vatable Sales");
        rightAlignByColumnName("VAT Amount");
        rightAlignByColumnName("VAT Exempt");
        rightAlignByColumnName("Zero Rated");
        rightAlignByColumnName("Discount");
        rightAlignByColumnName("Withholding Tax");
        rightAlignByColumnName("Total Due");
    }

    private void stylePosProfit() {
        hideModelColumn("__SALE_ID");
        setWidthByColumnName("Sale No", 180, true);
        setWidthByColumnName("Date", 110, true);
        setWidthByColumnName("Store", 100, false);
        setWidthByColumnName("Terminal", 100, false);
        setWidthByColumnName("Invoice No", 120, false);
        setWidthByColumnName("Customer", 220, false);
        setWidthByColumnName("Gross Amount", 120, false);
        setWidthByColumnName("Discount", 110, false);
        setWidthByColumnName("Tax Total", 110, false);
        setWidthByColumnName("Net Sales", 120, false);
        setWidthByColumnName("COGS", 110, false);
        setWidthByColumnName("Gross Profit", 120, false);
        setWidthByColumnName("Profit Margin %", 150, false);

        setIndentByColumnName("Customer", 18, 12);

        rightAlignByColumnName("Gross Amount");
        rightAlignByColumnName("Discount");
        rightAlignByColumnName("Tax Total");
        rightAlignByColumnName("Net Sales");
        rightAlignByColumnName("COGS");
        rightAlignByColumnName("Gross Profit");
        rightAlignByColumnName("Profit Margin %");
    }

    private void styleFinancialStatement() {
        setWidthByColumnName("Date", 110, true);
        setWidthByColumnName("Gross Sales", 120, false);
        setWidthByColumnName("Sales Discount", 120, false);
        setWidthByColumnName("Sales Net", 120, false);
        setWidthByColumnName("Output VAT", 120, false);
        setWidthByColumnName("Sales Returns", 120, false);
        setWidthByColumnName("Net Sales After Returns", 160, false);
        setWidthByColumnName("Net COGS", 120, false);
        setWidthByColumnName("Gross Profit", 120, false);
        setWidthByColumnName("Purchases", 120, false);
        setWidthByColumnName("Return Cost", 120, false);
        setWidthByColumnName("Damage", 110, false);
        setWidthByColumnName("Expire", 110, false);
        setWidthByColumnName("Adjustment", 120, false);
        setWidthByColumnName("Row Total", 120, false);

        rightAlignByColumnName("Gross Sales");
        rightAlignByColumnName("Sales Discount");
        rightAlignByColumnName("Sales Net");
        rightAlignByColumnName("Output VAT");
        rightAlignByColumnName("Sales Returns");
        rightAlignByColumnName("Net Sales After Returns");
        rightAlignByColumnName("Net COGS");
        rightAlignByColumnName("Gross Profit");
        rightAlignByColumnName("Purchases");
        rightAlignByColumnName("Return Cost");
        rightAlignByColumnName("Damage");
        rightAlignByColumnName("Expire");
        rightAlignByColumnName("Adjustment");
        rightAlignByColumnName("Row Total");
    }

    private int findModelColumn(String columnName) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(model.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findViewColumn(String columnName) {
        int modelIndex = findModelColumn(columnName);
        if (modelIndex < 0) {
            return -1;
        }
        try {
            return table.convertColumnIndexToView(modelIndex);
        } catch (Exception ex) {
            return -1;
        }
    }

    private void hideModelColumn(String columnName) {
        int modelIndex = findModelColumn(columnName);
        if (modelIndex >= 0) {
            BootstrapTableStyle.hideColumn(table, modelIndex);
        }
    }

    private void setIndentByColumnName(String columnName, int left, int right) {
        int modelIndex = findModelColumn(columnName);
        if (modelIndex >= 0) {
            BootstrapTableStyle.setColumnIndent(table, modelIndex, left, right);
            BootstrapTableStyle.setHeaderIndent(table, modelIndex, left, right);
        }
    }

    private void rightAlignByColumnName(String columnName) {
        int modelIndex = findModelColumn(columnName);
        if (modelIndex >= 0) {
            BootstrapTableStyle.setColumnAlignment(table, modelIndex, SwingConstants.RIGHT);
            BootstrapTableStyle.setHeaderAlignment(table, modelIndex, SwingConstants.RIGHT);
        }
    }

    private void setWidthByColumnName(String columnName, int width, boolean fixed) {
        int viewIndex = findViewColumn(columnName);
        if (viewIndex < 0) {
            return;
        }
        if (fixed) {
            BootstrapTableStyle.setFixedColumnWidth(table, viewIndex, width);
        } else {
            BootstrapTableStyle.setColumnWidth(table, viewIndex, width);
        }
    }

    private void updateGrandTotal() {
        if (totalsConfig.isEmpty()) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (Map.Entry<String, String> entry : totalsConfig.entrySet()) {
            String columnName = entry.getKey();
            String labelPrefix = entry.getValue();
            JLabel label = totalLabels.get(labelPrefix);
            if (label == null) {
                continue;
            }
            int targetColumn = -1;
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (columnName.equals(model.getColumnName(i))) {
                    targetColumn = i;
                    break;
                }
            }
            if (targetColumn < 0) {
                label.setText(labelPrefix + " N/A");
                continue;
            }
            BigDecimal total = BigDecimal.ZERO;
            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                total = total.add(toBigDecimal(model.getValueAt(modelRow, targetColumn)));
            }
            label.setText(labelPrefix + " " + MONEY.format(total));
        }
    }

    private void applyTableFilters() {
        if (!(table.getRowSorter() instanceof TableRowSorter<?> rawSorter)) {
            updateGrandTotal();
            return;
        }
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) rawSorter;

        final String search = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        final int limit = ((Number) spnLimit.getValue()).intValue();

        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                Integer identifier = entry.getIdentifier();
                int modelRow = identifier == null ? 0 : identifier.intValue();
                if (modelRow >= limit) {
                    return false;
                }
                if (search.isBlank()) {
                    return true;
                }
                for (int i = 0; i < entry.getValueCount(); i++) {
                    String columnName = entry.getModel().getColumnName(i);
                    if (columnName != null && columnName.startsWith("__")) {
                        continue;
                    }
                    Object value = entry.getValue(i);
                    if (value != null) {
                        String text = formatForSearch(value);
                        if (text.toLowerCase().contains(search)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        updateGrandTotal();
    }

    private String formatForSearch(Object value) {
        if (value instanceof java.util.Date date) {
            return DATE_FMT.format(date);
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return INTEGER.format(((Number) value).longValue());
        }
        if (value instanceof Number number) {
            return MONEY.format(number.doubleValue());
        }
        return String.valueOf(value);
    }

    private static Map<String, String> singleTotal(String columnName, String totalLabelPrefix) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (columnName != null && !columnName.isBlank() && totalLabelPrefix != null && !totalLabelPrefix.isBlank()) {
            map.put(columnName, totalLabelPrefix);
        }
        return map;
    }

    public Object getModelValue(int modelRow, String columnName) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (columnName.equals(model.getColumnName(i))) {
                return model.getValueAt(modelRow, i);
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    public long getLongModelValue(int modelRow, String columnName) {
        Object value = getModelValue(modelRow, columnName);
        if (value == null) {
            throw new IllegalArgumentException("Selected row has no value for " + columnName);
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString().replace(",", "").trim());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private void previewReport() {
        try {
            reportService.previewReport(jrxmlName, reportTitle, requireDate(fromDate.getDate(), "from"), requireDate(toDate.getDate(), "to"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), reportTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private Date requireDate(Date value, String label) {
        if (value == null) {
            throw new IllegalArgumentException("Please select the " + label + " date.");
        }
        return value;
    }
}
