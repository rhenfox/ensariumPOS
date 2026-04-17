
package com.aldrin.ensarium.txn;

import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class InventoryTxnDetailsDialog extends JDialog {

    private final InventoryTxnDao dao = new InventoryTxnDao();
    private final InventoryTxnLineTableModel lineTableModel = new InventoryTxnLineTableModel();
    private final TraceRowTableModel traceTableModel = new TraceRowTableModel();
    private final SummaryTableModel summaryTableModel = new SummaryTableModel();

    private final JLabel idValue = new JLabel();
    private final JLabel storeValue = new JLabel();
    private final JLabel typeValue = new JLabel();
    private final JLabel refNoValue = new JLabel();
    private final JLabel saleIdValue = new JLabel();
    private final JLabel returnIdValue = new JLabel();
    private final JLabel receiptIdValue = new JLabel();
    private final JLabel createdByValue = new JLabel();
    private final JLabel createdAtValue = new JLabel();
    private final JTextArea notesArea = new JTextArea(4, 20);

    private final Font headerFontBold = new Font("Segoe UI", Font.BOLD, 13);
    private final Font headerFontPlain = new Font("Segoe UI", Font.PLAIN, 14);

    public InventoryTxnDetailsDialog(java.awt.Window owner) {
        super(owner, "Inventory Transaction Details", Dialog.ModalityType.APPLICATION_MODAL);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());

        idValue.setFont(headerFontPlain);
        storeValue.setFont(headerFontPlain);
        typeValue.setFont(headerFontPlain);
        refNoValue.setFont(headerFontPlain);
        saleIdValue.setFont(headerFontPlain);
        returnIdValue.setFont(headerFontPlain);
        receiptIdValue.setFont(headerFontPlain);
        createdByValue.setFont(headerFontPlain);
        createdAtValue.setFont(headerFontPlain);

        JPanel header = new JPanel(new GridLayout(0, 4, 8, 8));
        TitledBorder headerBorder = BorderFactory.createTitledBorder("Inventory Transaction Header");
        headerBorder.setTitleFont(headerFontBold);
        header.setBorder(headerBorder);
        addLabelValue(header, "ID", idValue);
        addLabelValue(header, "Store ID", storeValue);
        addLabelValue(header, "Transaction Type", typeValue);
        addLabelValue(header, "Reference No", refNoValue);
        addLabelValue(header, "Sale ID", saleIdValue);
        addLabelValue(header, "Return ID", returnIdValue);
        addLabelValue(header, "Receipt ID", receiptIdValue);
        addLabelValue(header, "Created By", createdByValue);
        addLabelValue(header, "Created At", createdAtValue);

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEditable(false);
        notesArea.setFocusable(false);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        TitledBorder notesBorder = BorderFactory.createTitledBorder("Notes");
        notesBorder.setTitleFont(headerFontBold);
        notesScrollPane.setBorder(notesBorder);

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.CENTER);
        north.add(notesScrollPane, BorderLayout.SOUTH);
        centerPanel.add(north, BorderLayout.NORTH);

        JTable summaryTable = buildSummaryTable();
        JTable traceTable = buildTraceTable();
        JTable rawTable = buildRawLineTable();

        JScrollPane summaryScroll = new JScrollPane(summaryTable);
        TitledBorder border = BorderFactory.createTitledBorder("Document Summary / Amounts");
        border.setTitleFont(headerFontBold);
        summaryScroll.setBorder(border);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Traceability", createRoundedScrollPane(traceTable));
        tabs.addTab("Raw Inventory Lines", createRoundedScrollPane(rawTable));
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault().accent(new Color(0x0D6EFD));
        BootstrapTabbedPaneStyle.install(tabs, style);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summaryScroll, tabs);
        splitPane.setResizeWeight(0.30);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1350, 720));
        pack();
        setLocationRelativeTo(getParent());
    }

    private JTable buildSummaryTable() {
        JTable table = new JTable(summaryTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        BootstrapTableStyle.install(table);
        BootstrapTableStyle.setColumnWidth(table, 0, 240);
        BootstrapTableStyle.setColumnWidth(table, 1, 880);
        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        return table;
    }

    private JTable buildTraceTable() {
        JTable table = new JTable(traceTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        BootstrapTableStyle.install(table);
        int[] widths = {250, 280, 120, 90, 90, 110, 110, 110, 110, 120, 110, 120, 110, 110, 110, 120, 120, 420};
        for (int i = 0; i < widths.length; i++) BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        for (int i = 0; i <= 4; i++) {
            BootstrapTableStyle.setColumnLeft(table, i);
            BootstrapTableStyle.setHeaderLeft(table, i);
        }
        for (int i = 5; i <= 16; i++) {
            BootstrapTableStyle.setColumnRight(table, i);
            BootstrapTableStyle.setHeaderRight(table, i);
        }
        BootstrapTableStyle.setColumnLeft(table, 17);
        BootstrapTableStyle.setHeaderLeft(table, 17);
        return table;
    }

    private JTable buildRawLineTable() {
        JTable table = new JTable(lineTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        BootstrapTableStyle.install(table);
        int[] widths = {80, 90, 160, 280, 140, 160, 150, 100, 120, 120};
        for (int i = 0; i < widths.length; i++) BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        BootstrapTableStyle.setColumnRight(table, 0);
        BootstrapTableStyle.setColumnRight(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);
        BootstrapTableStyle.setColumnLeft(table, 5);
        BootstrapTableStyle.setColumnLeft(table, 6);
        BootstrapTableStyle.setColumnRight(table, 7);
        BootstrapTableStyle.setColumnRight(table, 8);
        BootstrapTableStyle.setColumnRight(table, 9);
        
        BootstrapTableStyle.setHeaderRight(table, 0);
        BootstrapTableStyle.setHeaderRight(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
        BootstrapTableStyle.setHeaderLeft(table, 5);
        BootstrapTableStyle.setHeaderLeft(table, 6);
        BootstrapTableStyle.setHeaderRight(table, 7);
        BootstrapTableStyle.setHeaderRight(table, 8);
        BootstrapTableStyle.setHeaderRight(table, 9);
        BootstrapTableStyle.hideColumns(table, 0, 1);
        return table;
    }

    private void addLabelValue(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel lbl = new JLabel(labelText + ":");
        lbl.setFont(headerFontBold);
        panel.add(lbl);
        panel.add(valueLabel);
    }

    public void loadTransaction(long txnId) throws SQLException {
        InventoryTxn txn = dao.findById(txnId);
        if (txn == null) throw new SQLException("Transaction not found.");
        List<InventoryTxnLine> rawLines = dao.findDetailsByTxnId(txnId);
        List<SummaryRow> summaryRows = dao.findSummaryRows(txn);
        List<TraceRow> traceRows = dao.findTraceRows(txn);

        idValue.setText(String.valueOf(txn.getId()));
        storeValue.setText(String.valueOf(txn.getStoreId()));
        typeValue.setText(nullSafe(txn.getTxnType()));
        refNoValue.setText(nullSafe(txn.getRefNo()));
        saleIdValue.setText(longText(txn.getSaleId()));
        returnIdValue.setText(longText(txn.getSalesReturnId()));
        receiptIdValue.setText(longText(txn.getPurchaseReceiptId()));
        createdByValue.setText(nullSafe(txn.getCreatedByName()));
        createdAtValue.setText(SwingUtils.formatDateTime(txn.getCreatedAt()));
        notesArea.setText(nullSafe(txn.getNotes()));
        notesArea.setCaretPosition(0);

        lineTableModel.setRows(rawLines);
        summaryTableModel.setRows(summaryRows);
        traceTableModel.setRows(traceRows);
    }

    private String longText(Long value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private JScrollPane createRoundedScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setOpaque(false);
        table.setOpaque(false);
        return scrollPane;
    }
}
