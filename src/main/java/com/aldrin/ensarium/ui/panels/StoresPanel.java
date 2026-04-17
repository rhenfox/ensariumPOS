package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.LookupOption;
import com.aldrin.ensarium.model.ShiftRow;
import com.aldrin.ensarium.model.StoreRow;
import com.aldrin.ensarium.model.TerminalRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.StoreAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.ComboBoxAutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;
import com.aldrin.ensarium.util.TableStyleSupport;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StoresPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a");

    private final Session session;
    private final StoreAdminService service = new StoreAdminService();
    private final boolean canWrite;

    private final StoreTab storeTab;
    private final TerminalTab terminalTab;
    private final ShiftTab shiftTab;

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

    Icon iconOpenShift = FaSwingIcons.icon(FontAwesomeIcon.SIGN_IN, 24, color);
    Icon iconCloseShift = FaSwingIcons.icon(FontAwesomeIcon.SIGN_OUT, 24, color);
    Icon iconMoney = FaSwingIcons.icon(FontAwesomeIcon.MONEY, 24, color);
    Icon iconUserAlt = FaSwingIcons.icon(FontAwesomeIcon.USER_ALT, 24, color);
    Icon iconUserCircleAlt = FaSwingIcons.icon(FontAwesomeIcon.USER_CIRCLE_ALT, 24, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public StoresPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.STORE);

        this.storeTab = new StoreTab();
        this.terminalTab = new TerminalTab();
        this.shiftTab = new ShiftTab();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Stores");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Store", storeTab);
        tabs.addTab("Terminal", terminalTab);
        tabs.addTab("Shift", shiftTab);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);
        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        storeTab.refresh();
        terminalTab.refresh();
        shiftTab.refresh();
    }

    private void hideColumn(JTable table, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= table.getColumnModel().getColumnCount()) {
            return;
        }
        var column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setWidth(0);
    }

    private JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, StyledButton... rightButtons) {
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        leftTools.add(searchl);
        txtSearch.setPreferredSize(new Dimension(250, 30));
        txtSearch.setFont(font14Plain);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(font14Bold);
        leftTools.add(limitl);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        spnLimit.setFont(font14Plain);
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (StyledButton b : rightButtons) {
            rightTools.add(b);
        }

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        return toolbar;
    }

    private abstract class BaseTab extends JPanel {

        final JTextField txtSearch = new JTextField(22);

        final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));

        BaseTab() {
            setLayout(new BorderLayout(0, 10));
            setBorder(new EmptyBorder(6, 0, 0, 0));
            txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    applyFilter();
                }
            });
            spnLimit.addChangeListener(e -> applyFilter());
        }

        abstract void refresh();

        abstract void applyFilter();

        abstract List<String> suggestions();

        void installSuggest() {
            AutoSuggestSupport.install(txtSearch, this::suggestions);
        }

        boolean match(String haystack, String q) {
            return q.isEmpty() || haystack.toLowerCase().contains(q);
        }

        int limit() {
            return (Integer) spnLimit.getValue();
        }

        String query() {
            return txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        }
    }

    private final class StoreTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Address", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                return columnIndex == 4 ? Boolean.class : Object.class;
//            }
        };
        final JTable table = new JTable(model);
        final List<StoreRow> allRows = new ArrayList<>();

        StoreTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            ////            table.setRowHeight(28);
//            table.getColumnModel().getColumn(1).setPreferredWidth(200);
//            table.getColumnModel().getColumn(2).setPreferredWidth(250);
//            table.getColumnModel().getColumn(3).setPreferredWidth(400);
//            table.getColumnModel().getColumn(4).setPreferredWidth(80);
//            table.setAutoCreateRowSorter(true);
//            TableStyleSupport.apply(table);
//            hideColumn(table, 0);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 200, 250, 400, 100};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listStores());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (StoreRow row : allRows) {
                String hay = (row.code() + " " + row.name() + " " + nv(row.address())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.code(), row.name(), row.address(), row.active() ? "Yes" : "No"});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (StoreRow r : allRows) {
                out.add(r.code());
                out.add(r.name());
                out.add(r.address());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Integer selectedId() {
            return selectedIntId(table, model);
        }

        void onAdd() {
            StoreDialog d = new StoreDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createStore(session.userId(), d.code(), d.name(), d.address(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to create store.", (Exception) ex);
            }
        }

        void onEdit() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(StoresPanel.this, "Select a store first.");
                return;
            }
            StoreRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            StoreDialog d = new StoreDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateStore(session.userId(), id, d.code(), d.name(), d.address(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update store.", (Exception) ex);
            }
        }

        void onDelete() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(StoresPanel.this, "Select a store first.");
                return;
            }
            if (!SwingUtils.confirm(StoresPanel.this, "Delete selected store?")) {
                return;
            }
            try {
                service.deleteStore(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to delete store.", (Exception) ex);
            }
        }
    }

    private final class TerminalTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "STORE ID", "Store code", "Store name", "Code", "Name", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
//
//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                return columnIndex == 6 ? Boolean.class : Object.class;
//            }
        };
        final JTable table = new JTable(model);
        final List<TerminalRow> allRows = new ArrayList<>();

        TerminalTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            ////            table.setRowHeight(28);
//            table.getColumnModel().getColumn(2).setPreferredWidth(250);
//            table.getColumnModel().getColumn(3).setPreferredWidth(250);
//            table.getColumnModel().getColumn(4).setPreferredWidth(250);
//            table.getColumnModel().getColumn(5).setPreferredWidth(80);
//            table.setAutoCreateRowSorter(true);
//            TableStyleSupport.apply(table);
//            hideColumn(table, 0);
//            hideColumn(table, 1);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 0, 250, 250, 100, 250, 100};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.hideColumns(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);

            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderLeft(table, 5);
            BootstrapTableStyle.setHeaderLeft(table, 6);

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listTerminals());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (TerminalRow row : allRows) {
                String hay = (row.storeCode() + " " + row.storeName() + " " + row.code() + " " + nv(row.name())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.storeId(), row.storeCode(), row.storeName(), row.code(), row.name(), row.active() ? "Yes" : "No"});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (TerminalRow r : allRows) {
                out.add(r.storeCode());
                out.add(r.storeName());
                out.add(r.code());
                out.add(r.name());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Integer selectedId() {
            return selectedIntId(table, model);
        }

        void onAdd() {
            TerminalDialog d = new TerminalDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createTerminal(session.userId(), d.storeId(), d.code(), d.name(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to create terminal.", (Exception) ex);
            }
        }

        void onEdit() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(StoresPanel.this, "Select a terminal first.");
                return;
            }
            TerminalRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            TerminalDialog d = new TerminalDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateTerminal(session.userId(), id, d.storeId(), d.code(), d.name(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update terminal.", (Exception) ex);
            }
        }

        void onDelete() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(StoresPanel.this, "Select a terminal first.");
                return;
            }
            if (!SwingUtils.confirm(StoresPanel.this, "Delete selected terminal?")) {
                return;
            }
            try {
                service.deleteTerminal(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to delete terminal.", (Exception) ex);
            }
        }
    }

    private final class ShiftTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "STORE ID", "TERMINAL ID", "OPENED BY ID", "CLOSED BY ID",
            "Store", "Terminal", "Opened by", "Opened at", "Closed by", "Closed at",
            "Opening cash", "Closing cash", "Status", "Remarks"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        final List<ShiftRow> allRows = new ArrayList<>();

        ShiftTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnOpen = new StyledButton("Open Shift");
            StyledButton btnClose = new StyledButton("Close Shift");
            StyledButton btnOpeningCash = new StyledButton("Opening Cash");
            StyledButton btnClosingCash = new StyledButton("Closing Cash");
            StyledButton btnOpenedBy = new StyledButton("Opened By");
            StyledButton btnClosedBy = new StyledButton("Closed By");
            StyledButton btnRefresh = new StyledButton("Refresh");

            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
//            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnOpen.setIcon(iconOpenShift);
            btnClose.setIcon(iconCloseShift);
            btnOpeningCash.setIcon(iconMoney);
            btnClosingCash.setIcon(iconMoney);
            btnOpenedBy.setIcon(iconUserAlt);
            btnClosedBy.setIcon(iconUserCircleAlt);

            btnAdd.setMargin(new Insets(2, 5, 2, 5));
            btnEdit.setMargin(new Insets(2, 5, 2, 5));
            btnRefresh.setMargin(new Insets(2, 5, 2, 5));
            btnOpen.setMargin(new Insets(2, 5, 2, 5));
            btnClose.setMargin(new Insets(2, 5, 2, 5));
            btnOpeningCash.setMargin(new Insets(2, 5, 2, 5));
            btnClosingCash.setMargin(new Insets(2, 5, 2, 5));
            btnOpenedBy.setMargin(new Insets(2, 5, 2, 5));
            btnClosedBy.setMargin(new Insets(2, 5, 2, 5));

            for (StyledButton b : new StyledButton[]{btnAdd, btnEdit, btnOpen, btnClose, btnOpeningCash, btnClosingCash, btnOpenedBy, btnClosedBy}) {
                b.setEnabled(canWrite);
            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 0, 0, 0, 0, 150, 150, 150, 180, 150, 180, 150, 150, 150, 250};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.hideColumns(table, 1);
            BootstrapTableStyle.hideColumns(table, 2);
            BootstrapTableStyle.hideColumns(table, 3);
            BootstrapTableStyle.hideColumns(table, 4);

            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);
            BootstrapTableStyle.setColumnLeft(table, 8);
            BootstrapTableStyle.setColumnLeft(table, 9);
            BootstrapTableStyle.setColumnLeft(table, 10);
            BootstrapTableStyle.setColumnLeft(table, 11);
            BootstrapTableStyle.setColumnLeft(table, 12);
            BootstrapTableStyle.setColumnLeft(table, 13);
            BootstrapTableStyle.setColumnLeft(table, 14);

            BootstrapTableStyle.setHeaderLeft(table, 5);
            BootstrapTableStyle.setHeaderLeft(table, 6);
            BootstrapTableStyle.setHeaderLeft(table, 7);
            BootstrapTableStyle.setHeaderLeft(table, 8);
            BootstrapTableStyle.setHeaderLeft(table, 9);
            BootstrapTableStyle.setHeaderLeft(table, 10);
            BootstrapTableStyle.setHeaderLeft(table, 11);
            BootstrapTableStyle.setHeaderLeft(table, 12);
            BootstrapTableStyle.setHeaderLeft(table, 13);
            BootstrapTableStyle.setHeaderLeft(table, 14);

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnOpen, btnClose, btnOpeningCash, btnClosingCash, btnOpenedBy, btnClosedBy, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnOpen.addActionListener(e -> onOpenShift());
            btnClose.addActionListener(e -> onCloseShift());
            btnOpeningCash.addActionListener(e -> onEditOpeningCash());
            btnClosingCash.addActionListener(e -> onEditClosingCash());
            btnOpenedBy.addActionListener(e -> onEditOpenedBy());
            btnClosedBy.addActionListener(e -> onEditClosedBy());
            btnRefresh.addActionListener(e -> refreshAll());
            installSuggest();
            installPopupMenu();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listShifts());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (ShiftRow row : allRows) {
                String hay = (row.storeName() + " " + row.terminalCode() + " " + row.openedByName() + " " + nv(row.closedByName()) + " " + row.status() + " " + nv(row.remarks())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{
                    row.id(), row.storeId(), row.terminalId(), row.openedBy(), row.closedBy(),
                    row.storeName(), row.terminalCode(), row.openedByName(), fmt(row.openedAt()), row.closedByName(), fmt(row.closedAt()),
                    row.openingCash(), row.closingCash(), row.status(), row.remarks()
                });
                if (++count >= limit()) {
                    break;
                }
            }
            reselectLong(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (ShiftRow r : allRows) {
                out.add(r.storeName());
                out.add(r.terminalCode());
                out.add(r.openedByName());
                out.add(r.closedByName());
                out.add(r.status());
                out.add(r.remarks());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Long selectedId() {
            return selectedLongId(table, model);
        }

        ShiftRow selectedRow() {
            Long id = selectedId();
            if (id == null) {
                return null;
            }
            return allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        }

        void onAdd() {
            ShiftDialog d = new ShiftDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createShift(session.userId(), d.storeId(), d.terminalId(), d.openedById(), d.openedAt(), d.closedById(), d.closedAt(), d.openingCash(), d.closingCash(), d.status(), d.remarks());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to create shift.", (Exception) ex);
            }
        }

        void onEdit() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            ShiftDialog d = new ShiftDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateShift(session.userId(), row.id(), d.storeId(), d.terminalId(), d.openedById(), d.openedAt(), d.closedById(), d.closedAt(), d.openingCash(), d.closingCash(), d.status(), d.remarks());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update shift.", (Exception) ex);
            }
        }

        void onDelete() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            if (!SwingUtils.confirm(StoresPanel.this, "Delete selected shift?")) {
                return;
            }
            try {
                service.deleteShift(session.userId(), row.id());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to delete shift.", (Exception) ex);
            }
        }

        void onOpenShift() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            OpenShiftDialog d = new OpenShiftDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.openShift(session.userId(), row.id(), d.userId(), d.openedAt(), d.openingCash());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to open shift.", (Exception) ex);
            }
        }

        void onCloseShift() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            if ("CLOSED".equalsIgnoreCase(row.status())) {
                SwingUtils.info(StoresPanel.this, "Selected shift is already closed.");
                return;
            }
            CloseShiftDialog d = new CloseShiftDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.closeShift(session.userId(), row.id(), d.userId(), d.closedAt(), d.closingCash());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to close shift.", (Exception) ex);
            }
        }

        void onEditOpeningCash() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            MoneyDialog d = new MoneyDialog("Edit Opening Cash", row.openingCash());
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateShiftOpeningCash(session.userId(), row.id(), d.amount());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update opening cash.", (Exception) ex);
            }
        }

        void onEditClosingCash() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            if (!"CLOSED".equalsIgnoreCase(row.status())) {
                SwingUtils.info(StoresPanel.this, "Close the shift first before editing closing cash.");
                return;
            }
            MoneyDialog d = new MoneyDialog("Edit Closing Cash", row.closingCash());
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateShiftClosingCash(session.userId(), row.id(), d.amount());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update closing cash.", (Exception) ex);
            }
        }

        void onEditOpenedBy() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            UserSelectDialog d = new UserSelectDialog("Edit Opened By", row.openedBy(), false);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateShiftOpenedBy(session.userId(), row.id(), d.userId());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update opened by.", (Exception) ex);
            }
        }

        void onEditClosedBy() {
            ShiftRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(StoresPanel.this, "Select a shift first.");
                return;
            }
            if (!"CLOSED".equalsIgnoreCase(row.status())) {
                SwingUtils.info(StoresPanel.this, "Close the shift first before editing closed by.");
                return;
            }
            UserSelectDialog d = new UserSelectDialog("Edit Closed By", row.closedBy(), true);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateShiftClosedBy(session.userId(), row.id(), d.selectedUserId());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(StoresPanel.this, "Failed to update closed by.", (Exception) ex);
            }
        }

        private void installPopupMenu() {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem miOpen = new JMenuItem("Open Shift");
            JMenuItem miClose = new JMenuItem("Close Shift");
            JMenuItem miOpeningCash = new JMenuItem("Edit Opening Cash");
            JMenuItem miClosingCash = new JMenuItem("Edit Closing Cash");
            JMenuItem miOpenedBy = new JMenuItem("Edit Opened By");
            JMenuItem miClosedBy = new JMenuItem("Edit Closed By");
            JMenuItem miEdit = new JMenuItem("Edit Shift");
            JMenuItem miDelete = new JMenuItem("Delete Shift");
            for (JMenuItem mi : new JMenuItem[]{miOpen, miClose, miOpeningCash, miClosingCash, miOpenedBy, miClosedBy, miEdit, miDelete}) {
                mi.setEnabled(canWrite);
            }
            miOpen.addActionListener(e -> onOpenShift());
            miClose.addActionListener(e -> onCloseShift());
            miOpeningCash.addActionListener(e -> onEditOpeningCash());
            miClosingCash.addActionListener(e -> onEditClosingCash());
            miOpenedBy.addActionListener(e -> onEditOpenedBy());
            miClosedBy.addActionListener(e -> onEditClosedBy());
            miEdit.addActionListener(e -> onEdit());
            miDelete.addActionListener(e -> onDelete());
            popup.add(miOpen);
            popup.add(miClose);
            popup.addSeparator();
            popup.add(miOpeningCash);
            popup.add(miClosingCash);
            popup.add(miOpenedBy);
            popup.add(miClosedBy);
            popup.addSeparator();
            popup.add(miEdit);
            popup.add(miDelete);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShow(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShow(e);
                }

                private void maybeShow(MouseEvent e) {
                    if (!e.isPopupTrigger()) {
                        return;
                    }
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                    }
                    popup.show(table, e.getX(), e.getY());
                }
            });
        }
    }

    private final class StoreDialog extends JDialog {

        final JTextField txtCode = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        final JTextArea txtAddress = new JTextArea(4, 20);
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        StoreDialog(StoreRow row) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), row == null ? "Add Store" : "Edit Store", ModalityType.APPLICATION_MODAL);
            txtAddress.setLineWrap(true);
            txtAddress.setWrapStyleWord(true);
            txtCode.setFont(font14Plain);
            txtCode.putClientProperty("JTextField.placeholderText", "Code");
            txtCode.setPreferredSize(new Dimension(250, 30));
            txtName.setFont(font14Plain);
            txtName.putClientProperty("JTextField.placeholderText", "Name");
            txtName.setPreferredSize(new Dimension(250, 30));
            txtAddress.setFont(font14Plain);
            txtAddress.putClientProperty("JTextField.placeholderText", "Address");
            txtAddress.setPreferredSize(new Dimension(250, 30));
            if (row != null) {
                txtCode.setText(row.code());
                txtName.setText(row.name());
                txtAddress.setText(row.address());
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(new String[]{"Code", "Name", "Address", "Status"}, new JComponent[]{txtCode, txtName, new JScrollPane(txtAddress), chkActive});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (code().isBlank() || name().isBlank()) {
                    SwingUtils.info(this, "Code and name are required.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        String code() {
            return txtCode.getText().trim();
        }

        String name() {
            return txtName.getText().trim();
        }

        String address() {
            return txtAddress.getText().trim();
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private final class TerminalDialog extends JDialog {

        final JComboBox<LookupOption> cboStore = new JComboBox<>();
        final JTextField txtCode = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        TerminalDialog(TerminalRow row) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), row == null ? "Add Terminal" : "Edit Terminal", ModalityType.APPLICATION_MODAL);

            cboStore.setFont(font14Plain);
            cboStore.setPreferredSize(new Dimension(250, 30));

            txtCode.setFont(font14Plain);
            txtCode.putClientProperty("JTextField.placeholderText", "Code");
            txtCode.setPreferredSize(new Dimension(250, 30));

            txtName.setFont(font14Plain);
            txtName.putClientProperty("JTextField.placeholderText", "Name");
            txtCode.setPreferredSize(new Dimension(250, 30));

            chkActive.setFont(font14Plain);

            for (LookupOption option : service.storeOptions()) {
                cboStore.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboStore);
            if (row != null) {
                selectLookup(cboStore, row.storeId());
                txtCode.setText(row.code());
                txtName.setText(row.name());
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(new String[]{"Store", "Code", "Name", "Status"}, new JComponent[]{cboStore, txtCode, txtName, chkActive});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (storeId() == null || code().isBlank()) {
                    SwingUtils.info(this, "Store and code are required.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        Integer storeId() {
            return selectedLookupId(cboStore);
        }

        String code() {
            return txtCode.getText().trim();
        }

        String name() {
            return txtName.getText().trim();
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private final class ShiftDialog extends JDialog {

        final JComboBox<LookupOption> cboStore = new JComboBox<>();
        final JComboBox<LookupOption> cboTerminal = new JComboBox<>();
        final JComboBox<LookupOption> cboOpenedBy = new JComboBox<>();
        final JComboBox<Object> cboClosedBy = new JComboBox<>();
        final JSpinner spnOpenedAt = dateTimeSpinner();
        final JSpinner spnClosedAt = dateTimeSpinner();
        final JTextField txtOpeningCash = new JTextField("0", 20);
        final JTextField txtClosingCash = new JTextField(20);
        final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"OPEN", "CLOSED"});
        final JTextArea txtRemarks = new JTextArea(4, 20);
        boolean saved;

        ShiftDialog(ShiftRow row) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), row == null ? "Add Shift" : "Edit Shift", ModalityType.APPLICATION_MODAL);

            loadStoreOptions();
            loadUserOptions();
            ComboBoxAutoSuggestSupport.install(cboStore);
            ComboBoxAutoSuggestSupport.install(cboTerminal);
            ComboBoxAutoSuggestSupport.install(cboOpenedBy);
            ComboBoxAutoSuggestSupport.install(cboClosedBy);
            txtRemarks.setLineWrap(true);
            txtRemarks.setWrapStyleWord(true);
            cboStore.addActionListener(e -> loadTerminalOptions(storeId()));
            cboStatus.addActionListener(e -> applyStatusState());

            cboStore.setFont(font14Plain);
            cboStore.setPreferredSize(new Dimension(250, 30));

            cboTerminal.setFont(font14Plain);
            cboTerminal.setPreferredSize(new Dimension(250, 30));

            cboOpenedBy.setFont(font14Plain);
            cboOpenedBy.setPreferredSize(new Dimension(250, 30));

            cboClosedBy.setFont(font14Plain);
            cboClosedBy.setPreferredSize(new Dimension(250, 30));

            txtOpeningCash.setFont(font14Plain);
            txtOpeningCash.putClientProperty("JTextField.placeholderText", "Opening cash");
            txtOpeningCash.setPreferredSize(new Dimension(250, 30));

            txtClosingCash.setFont(font14Plain);
            txtClosingCash.putClientProperty("JTextField.placeholderText", "Closing cash");
            txtClosingCash.setPreferredSize(new Dimension(250, 30));

            cboStatus.setFont(font14Plain);
            cboStatus.setPreferredSize(new Dimension(250, 30));

            txtRemarks.setFont(font14Plain);

            if (row != null) {
                selectLookup(cboStore, row.storeId());
                loadTerminalOptions(row.storeId());
                selectLookup(cboTerminal, row.terminalId());
                selectLookup(cboOpenedBy, row.openedBy());
                selectLookupObj(cboClosedBy, row.closedBy());
                spnOpenedAt.setValue(row.openedAt() != null ? new Date(row.openedAt().getTime()) : new Date());
                spnClosedAt.setValue(row.closedAt() != null ? new Date(row.closedAt().getTime()) : new Date());
                txtOpeningCash.setText(row.openingCash() == null ? "0" : row.openingCash().toPlainString());
                txtClosingCash.setText(row.closingCash() == null ? "" : row.closingCash().toPlainString());
                cboStatus.setSelectedItem(row.status());
                txtRemarks.setText(row.remarks());
            } else {
                if (cboStore.getItemCount() > 0) {
                    cboStore.setSelectedIndex(0);
                }
                loadTerminalOptions(storeId());
                if (cboOpenedBy.getItemCount() > 0) {
                    cboOpenedBy.setSelectedIndex(0);
                }
                cboClosedBy.setSelectedIndex(0);
                spnOpenedAt.setValue(new Date());
                spnClosedAt.setValue(new Date());
            }
            applyStatusState();

            JPanel form = simpleForm(
                    new String[]{"Store", "Terminal", "Opened By", "Opened At", "Opening Cash", "Status", "Closed By", "Closed At", "Closing Cash", "Remarks"},
                    new JComponent[]{cboStore, cboTerminal, cboOpenedBy, spnOpenedAt, txtOpeningCash, cboStatus, cboClosedBy, spnClosedAt, txtClosingCash, new JScrollPane(txtRemarks)}
            );
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> onSave());
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        private void loadStoreOptions() {
            cboStore.removeAllItems();
            for (LookupOption option : service.storeOptions()) {
                cboStore.addItem(option);
            }
        }

        private void loadTerminalOptions(Integer storeId) {
            cboTerminal.removeAllItems();
            for (LookupOption option : service.terminalOptions(storeId)) {
                cboTerminal.addItem(option);
            }
            ComboBoxAutoSuggestSupport.refresh(cboTerminal);
        }

        private void loadUserOptions() {
            cboOpenedBy.removeAllItems();
            cboClosedBy.removeAllItems();
            cboClosedBy.addItem("(None)");
            for (LookupOption option : service.userOptions()) {
                cboOpenedBy.addItem(option);
                cboClosedBy.addItem(option);
            }
        }

        private void applyStatusState() {
            boolean closed = "CLOSED".equals(status());
            cboClosedBy.setEnabled(closed);
            spnClosedAt.setEnabled(closed);
            txtClosingCash.setEnabled(closed);
            if (!closed) {
                cboClosedBy.setSelectedIndex(0);
                txtClosingCash.setText("");
            }
        }

        private void onSave() {
            if (storeId() == null || terminalId() == null || openedById() == null) {
                SwingUtils.info(this, "Store, terminal, and opened by are required.");
                return;
            }
            if (openingCash() == null) {
                SwingUtils.info(this, "Opening cash is invalid.");
                return;
            }
            if ("CLOSED".equals(status()) && closingCash() == null) {
                SwingUtils.info(this, "Closing cash is invalid.");
                return;
            }
            saved = true;
            dispose();
        }

        Integer storeId() {
            return selectedLookupId(cboStore);
        }

        Integer terminalId() {
            return selectedLookupId(cboTerminal);
        }

        Integer openedById() {
            return selectedLookupId(cboOpenedBy);
        }

        Integer closedById() {
            Object selected = cboClosedBy.getSelectedItem();
            if (selected instanceof LookupOption option) {
                return option.id();
            }
            return null;
        }

        Timestamp openedAt() {
            return new Timestamp(((Date) spnOpenedAt.getValue()).getTime());
        }

        Timestamp closedAt() {
            if (!"CLOSED".equals(status())) {
                return null;
            }
            return new Timestamp(((Date) spnClosedAt.getValue()).getTime());
        }

        BigDecimal openingCash() {
            return parseMoney(txtOpeningCash.getText());
        }

        BigDecimal closingCash() {
            if (!"CLOSED".equals(status())) {
                return null;
            }
            return parseMoney(txtClosingCash.getText());
        }

        String status() {
            return (String) cboStatus.getSelectedItem();
        }

        String remarks() {
            return txtRemarks.getText().trim();
        }
    }

    private final class OpenShiftDialog extends JDialog {

        final JComboBox<LookupOption> cboUser = new JComboBox<>();
        final JSpinner spnOpenedAt = dateTimeSpinner();
        final JTextField txtOpeningCash = new JTextField(20);
        boolean saved;

        OpenShiftDialog(ShiftRow row) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), "Open Shift", ModalityType.APPLICATION_MODAL);
            for (LookupOption option : service.userOptions()) {
                cboUser.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboUser);
            selectLookup(cboUser, row.openedBy());
            spnOpenedAt.setValue(row.openedAt() != null ? new Date(row.openedAt().getTime()) : new Date());
            txtOpeningCash.setText(row.openingCash() == null ? "0" : row.openingCash().toPlainString());
            JPanel form = simpleForm(new String[]{"Opened By", "Opened At", "Opening Cash"}, new JComponent[]{cboUser, spnOpenedAt, txtOpeningCash});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (userId() == null || openingCash() == null) {
                    SwingUtils.info(this, "Opened by and valid opening cash are required.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        Integer userId() {
            return selectedLookupId(cboUser);
        }

        Timestamp openedAt() {
            return new Timestamp(((Date) spnOpenedAt.getValue()).getTime());
        }

        BigDecimal openingCash() {
            return parseMoney(txtOpeningCash.getText());
        }
    }

    private final class CloseShiftDialog extends JDialog {

        final JComboBox<LookupOption> cboUser = new JComboBox<>();
        final JSpinner spnClosedAt = dateTimeSpinner();
        final JTextField txtClosingCash = new JTextField(20);
        boolean saved;

        CloseShiftDialog(ShiftRow row) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), "Close Shift", ModalityType.APPLICATION_MODAL);
            for (LookupOption option : service.userOptions()) {
                cboUser.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboUser);
            if (row.closedBy() != null) {
                selectLookup(cboUser, row.closedBy());
            } else if (session != null) {
                selectLookup(cboUser, session.userId());
            }
            spnClosedAt.setValue(row.closedAt() != null ? new Date(row.closedAt().getTime()) : new Date());
            txtClosingCash.setText(row.closingCash() == null ? "0" : row.closingCash().toPlainString());
            JPanel form = simpleForm(new String[]{"Closed By", "Closed At", "Closing Cash"}, new JComponent[]{cboUser, spnClosedAt, txtClosingCash});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> {
                if (userId() == null || closingCash() == null) {
                    SwingUtils.info(this, "Closed by and valid closing cash are required.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        Integer userId() {
            return selectedLookupId(cboUser);
        }

        Timestamp closedAt() {
            return new Timestamp(((Date) spnClosedAt.getValue()).getTime());
        }

        BigDecimal closingCash() {
            return parseMoney(txtClosingCash.getText());
        }
    }

    private final class MoneyDialog extends JDialog {

        final JTextField txtAmount = new JTextField(20);
        boolean saved;

        MoneyDialog(String title, BigDecimal amount) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), title, ModalityType.APPLICATION_MODAL);
            txtAmount.setText(amount == null ? "0" : amount.toPlainString());
            JPanel form = simpleForm(new String[]{"Amount"}, new JComponent[]{txtAmount});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> {
                if (amount() == null) {
                    SwingUtils.info(this, "Enter a valid amount.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        BigDecimal amount() {
            return parseMoney(txtAmount.getText());
        }
    }

    private final class UserSelectDialog extends JDialog {

        final JComboBox<Object> cboUser = new JComboBox<>();
        boolean saved;

        UserSelectDialog(String title, Integer selectedUserId, boolean allowNone) {
            super(SwingUtilities.getWindowAncestor(StoresPanel.this), title, ModalityType.APPLICATION_MODAL);
            if (allowNone) {
                cboUser.addItem("(None)");
            }
            for (LookupOption option : service.userOptions()) {
                cboUser.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboUser);
            if (allowNone) {
                selectLookupObj(cboUser, selectedUserId);
            } else {
                @SuppressWarnings("unchecked")
                JComboBox<LookupOption> typed = (JComboBox<LookupOption>) (JComboBox<?>) cboUser;
                selectLookup(typed, selectedUserId);
            }
            JPanel form = simpleForm(new String[]{"User"}, new JComponent[]{cboUser});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (selectedUserId() == null && !allowNone) {
                    SwingUtils.info(this, "Select a user.");
                    return;
                }
                saved = true;
                dispose();
            });
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(StoresPanel.this);
        }

        Integer selectedUserId() {
            Object selected = cboUser.getSelectedItem();
            if (selected instanceof LookupOption option) {
                return option.id();
            }
            return null;
        }

        int userId() {
            return selectedUserId();
        }
    }
//        private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
//    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    private static JPanel simpleForm(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            panel.add(label, g);
            g.gridx = 1;
            g.weightx = 1;
            panel.add(fields[i], g);
        }
        return panel;
    }

    private static JPanel buttonBar(StyledButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        for (StyledButton b : buttons) {
            p.add(b);
        }
        return p;
    }

    private Integer selectedIntId(JTable table, DefaultTableModel model) {
        int view = table.getSelectedRow();
        if (view < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(view);
        Object value = model.getValueAt(modelRow, 0);
        return value == null ? null : ((Number) value).intValue();
    }

    private Long selectedLongId(JTable table, DefaultTableModel model) {
        int view = table.getSelectedRow();
        if (view < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(view);
        Object value = model.getValueAt(modelRow, 0);
        return value == null ? null : ((Number) value).longValue();
    }

    private void reselect(JTable table, DefaultTableModel model, Integer id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 0);
            if (v instanceof Number n && n.intValue() == id) {
                int view = table.convertRowIndexToView(i);
                if (view >= 0) {
                    table.setRowSelectionInterval(view, view);
                }
                break;
            }
        }
    }

    private void reselectLong(JTable table, DefaultTableModel model, Long id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 0);
            if (v instanceof Number n && n.longValue() == id) {
                int view = table.convertRowIndexToView(i);
                if (view >= 0) {
                    table.setRowSelectionInterval(view, view);
                }
                break;
            }
        }
    }

    private static String nv(String value) {
        return value == null ? "" : value;
    }

    private static String fmt(java.util.Date value) {
        return value == null ? "" : TS_FORMAT.format(value);
    }

    private static Integer selectedLookupId(JComboBox<?> combo) {
        Object selected = combo.getSelectedItem();
        if (selected instanceof LookupOption option) {
            return option.id();
        }
        return null;
    }

    private static void selectLookup(JComboBox<LookupOption> combo, Integer id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            LookupOption option = combo.getItemAt(i);
            if (option != null && option.id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void selectLookupObj(JComboBox<Object> combo, Integer id) {
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof LookupOption option && option.id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static JSpinner dateTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MMM-dd HH:mm:ss a"));
        return spinner;
    }

    private static BigDecimal parseMoney(String value) {
        try {
            String v = value == null ? "" : value.trim();
            if (v.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(v);
        } catch (Exception ex) {
            return null;
        }
    }
}
