package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.LookupOption;
import com.aldrin.ensarium.model.ReceiptSeriesRow;
import com.aldrin.ensarium.model.StoreFiscalProfileRow;
import com.aldrin.ensarium.model.TaxpayerProfileRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.FiscalBirService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.ComboBoxAutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;                            

import com.toedter.calendar.JDateChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FiscalBirPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

    private final Session session;
    private final FiscalBirService service = new FiscalBirService();
    private final boolean canWrite;

    private final TaxpayerTab taxpayerTab;
    private final StoreFiscalTab storeFiscalTab;
    private final ReceiptSeriesTab receiptSeriesTab;

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public FiscalBirPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.FISCAL_BIR);
        this.taxpayerTab = new TaxpayerTab();
        this.storeFiscalTab = new StoreFiscalTab();
        this.receiptSeriesTab = new ReceiptSeriesTab();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Fiscal (BIR)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tax Payer Profile", taxpayerTab);
        tabs.addTab("Store Fiscal Profile", storeFiscalTab);
        tabs.addTab("Receipt Series", receiptSeriesTab);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);
        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        taxpayerTab.refresh();
        storeFiscalTab.refresh();
        receiptSeriesTab.refresh();
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
        txtSearch.setFont(font14Plain);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
        txtSearch.setPreferredSize(new Dimension(250, 30));
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(font14Bold);
        leftTools.add(limitl);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setPreferredSize(new Dimension(80, 30));
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
            txtSearch.setFont(font14Plain);
            txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
            txtSearch.setPreferredSize(new Dimension(250, 30));
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

    private final class TaxpayerTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Registered name", "Trade name", "TIN", "Head office address", "VAT Type", "Active", "Created at"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                return columnIndex == 6 ? Boolean.class : Object.class;
//            }
        };
        final JTable table = new JTable(model);
        final List<TaxpayerProfileRow> allRows = new ArrayList<>();

        TaxpayerTab() {
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

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 300, 200, 150, 450, 160, 100, 200};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderLeft(table, 5);
            BootstrapTableStyle.setHeaderLeft(table, 6);
            BootstrapTableStyle.setHeaderLeft(table, 7);

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
            allRows.addAll(service.listTaxpayerProfiles());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (TaxpayerProfileRow row : allRows) {
                String hay = String.join(" ", nv(row.registeredName()), nv(row.tradeName()), nv(row.tinNo()), nv(row.headOfficeAddress()), nv(row.vatRegistrationType())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.registeredName(), row.tradeName(), row.tinNo(), row.headOfficeAddress(), row.vatRegistrationType(), row.active() ? "Yes" : "No", fmt(row.createdAt())});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (TaxpayerProfileRow r : allRows) {
                out.add(r.registeredName());
                out.add(r.tradeName());
                out.add(r.tinNo());
                out.add(r.headOfficeAddress());
                out.add(r.vatRegistrationType());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Integer selectedId() {
            return selectedIntId(table, model);
        }

        TaxpayerProfileRow selectedRow() {
            Integer id = selectedId();
            return id == null ? null : allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        }

        void onAdd() {
            TaxpayerDialog d = new TaxpayerDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createTaxpayerProfile(session.userId(), d.registeredName(), d.tradeName(), d.tinNo(), d.headOfficeAddress(), d.vatType(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to create taxpayer profile.", (Exception) ex);
            }
        }

        void onEdit() {
            TaxpayerProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a taxpayer profile first.");
                return;
            }
            TaxpayerDialog d = new TaxpayerDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateTaxpayerProfile(session.userId(), row.id(), d.registeredName(), d.tradeName(), d.tinNo(), d.headOfficeAddress(), d.vatType(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to update taxpayer profile.", (Exception) ex);
            }
        }

        void onDelete() {
            TaxpayerProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a taxpayer profile first.");
                return;
            }
            if (!SwingUtils.confirm(FiscalBirPanel.this, "Delete selected taxpayer profile?")) {
                return;
            }
            try {
                service.deleteTaxpayerProfile(session.userId(), row.id());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to delete taxpayer profile.", (Exception) ex);
            }
        }
    }

    private final class StoreFiscalTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Store ID",
            "Store Code",//1
            "Store Name",//2
            "Taxpayer ID",//3
            "Taxpayer",//4
            "Branch Code",//5
            "Business Address",//6
            "POS Vendor",//7
            "POS Vendor TIN",//8
            "POS Vendor Address",//9
            "Accreditation No",//10
            "Accreditation Issued",//11
            "Accreditation Valid Until",//12
            "BIR Permit",//13
            "Permit Issued",//14
            "ATP No",//15
            "ATP Issued",//16
            "Active",//17
            "Updated At"//18
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                return columnIndex == 17 ? Boolean.class : Object.class;
//            }
        };
        final JTable table = new JTable(model);
        final List<StoreFiscalProfileRow> allRows = new ArrayList<>();

        StoreFiscalTab() {
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

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 230, 200, 150, 280, 160, 250, 230, 150, 340, 150, 170, 200, 120, 120, 120, 100, 90, 200};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.hideColumns(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
//            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
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
            BootstrapTableStyle.setColumnLeft(table, 15);
            BootstrapTableStyle.setColumnLeft(table, 16);
            BootstrapTableStyle.setColumnLeft(table, 17);
            BootstrapTableStyle.setColumnLeft(table, 18);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
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
            BootstrapTableStyle.setHeaderLeft(table, 15);
            BootstrapTableStyle.setHeaderLeft(table, 16);
            BootstrapTableStyle.setHeaderLeft(table, 17);
            BootstrapTableStyle.setHeaderLeft(table, 18);

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
            allRows.addAll(service.listStoreFiscalProfiles());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedStoreId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (StoreFiscalProfileRow row : allRows) {
                String hay = String.join(" ", nv(row.storeCode()), nv(row.storeName()), nv(row.taxpayerRegisteredName()), nv(row.branchCode()),
                        nv(row.registeredBusinessAddress()), nv(row.posVendorName()), nv(row.posVendorTinNo()), nv(row.posVendorAddress()),
                        nv(row.supplierAccreditationNo()), nv(row.birPermitToUseNo()), nv(row.atpNo())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{
                    row.storeId(), row.storeCode(), row.storeName(), row.taxpayerProfileId(), row.taxpayerRegisteredName(), row.branchCode(), row.registeredBusinessAddress(),
                    row.posVendorName(), row.posVendorTinNo(), row.posVendorAddress(), row.supplierAccreditationNo(), fmtDate(row.accreditationIssuedAt()),
                    fmtDate(row.accreditationValidUntil()), row.birPermitToUseNo(), fmtDate(row.permitToUseIssuedAt()), row.atpNo(), fmtDate(row.atpIssuedAt()), row.active() ? "Yes" : "No", fmt(row.updatedAt())
                });
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (StoreFiscalProfileRow r : allRows) {
                out.add(r.storeCode());
                out.add(r.storeName());
                out.add(r.taxpayerRegisteredName());
                out.add(r.branchCode());
                out.add(r.registeredBusinessAddress());
                out.add(r.atpNo());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Integer selectedStoreId() {
            return selectedIntId(table, model);
        }

        StoreFiscalProfileRow selectedRow() {
            Integer id = selectedStoreId();
            return id == null ? null : allRows.stream().filter(r -> r.storeId() == id).findFirst().orElse(null);
        }

        void onAdd() {
            StoreFiscalDialog d = new StoreFiscalDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createStoreFiscalProfile(session.userId(), d.storeId(), d.taxpayerProfileId(), d.branchCode(), d.registeredBusinessAddress(), d.posVendorName(), d.posVendorTinNo(), d.posVendorAddress(), d.supplierAccreditationNo(), d.accreditationIssuedAt(), d.accreditationValidUntil(), d.birPermitToUseNo(), d.permitToUseIssuedAt(), d.atpNo(), d.atpIssuedAt(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to create store fiscal profile.", (Exception) ex);
            }
        }

        void onEdit() {
            StoreFiscalProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a store fiscal profile first.");
                return;
            }
            StoreFiscalDialog d = new StoreFiscalDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateStoreFiscalProfile(session.userId(), row.storeId(), d.taxpayerProfileId(), d.branchCode(), d.registeredBusinessAddress(), d.posVendorName(), d.posVendorTinNo(), d.posVendorAddress(), d.supplierAccreditationNo(), d.accreditationIssuedAt(), d.accreditationValidUntil(), d.birPermitToUseNo(), d.permitToUseIssuedAt(), d.atpNo(), d.atpIssuedAt(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to update store fiscal profile.", (Exception) ex);
            }
        }

        void onDelete() {
            StoreFiscalProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a store fiscal profile first.");
                return;
            }
            if (!SwingUtils.confirm(FiscalBirPanel.this, "Delete selected store fiscal profile?")) {
                return;
            }
            try {
                service.deleteStoreFiscalProfile(session.userId(), row.storeId());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to delete store fiscal profile.", (Exception) ex);
            }
        }
    }

    private final class ReceiptSeriesTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID",
            "Terminal ID",//1
            "Store Code",//2
            "Store Name",//3
            "Terminal",//4
            "Document Type",//5
            "Prefix",//6
            "Serial From",//7
            "Serial To",//8
            "Next Serial",//9
            "Active",//10
            "Created At"//11
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        final JTable table = new JTable(model);
        final List<ReceiptSeriesRow> allRows = new ArrayList<>();

        ReceiptSeriesTab() {
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

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 230, 200, 150, 250, 160, 250, 230, 150, 340, 150, 170};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.hideColumns(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
//            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);
            BootstrapTableStyle.setColumnLeft(table, 8);
            BootstrapTableStyle.setColumnLeft(table, 9);
            BootstrapTableStyle.setColumnLeft(table, 10);
            BootstrapTableStyle.setColumnLeft(table, 11);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderLeft(table, 5);
            BootstrapTableStyle.setHeaderLeft(table, 6);
            BootstrapTableStyle.setHeaderLeft(table, 7);
            BootstrapTableStyle.setHeaderLeft(table, 8);
            BootstrapTableStyle.setHeaderLeft(table, 9);
            BootstrapTableStyle.setHeaderLeft(table, 10);
            BootstrapTableStyle.setHeaderLeft(table, 11);

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
            allRows.addAll(service.listReceiptSeries());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (ReceiptSeriesRow row : allRows) {
                String hay = String.join(" ", nv(row.storeCode()), nv(row.storeName()), nv(row.terminalCode()), nv(row.docType()), nv(row.prefix())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.terminalId(), row.storeCode(), row.storeName(), row.terminalCode(), row.docType(), row.prefix(), row.serialFrom(), row.serialTo(), row.nextSerial(), row.active(), fmt(row.createdAt())});
                if (++count >= limit()) {
                    break;
                }
            }
            reselectLong(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (ReceiptSeriesRow r : allRows) {
                out.add(r.storeCode());
                out.add(r.storeName());
                out.add(r.terminalCode());
                out.add(r.docType());
                out.add(r.prefix());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Long selectedId() {
            return selectedLongId(table, model);
        }

        ReceiptSeriesRow selectedRow() {
            Long id = selectedId();
            return id == null ? null : allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        }

        void onAdd() {
            ReceiptSeriesDialog d = new ReceiptSeriesDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createReceiptSeries(session.userId(), d.terminalId(), d.docType(), d.prefix(), d.serialFrom(), d.serialTo(), d.nextSerial(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to create receipt series.", (Exception) ex);
            }
        }

        void onEdit() {
            ReceiptSeriesRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a receipt series first.");
                return;
            }
            ReceiptSeriesDialog d = new ReceiptSeriesDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateReceiptSeries(session.userId(), row.id(), d.terminalId(), d.docType(), d.prefix(), d.serialFrom(), d.serialTo(), d.nextSerial(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to update receipt series.", (Exception) ex);
            }
        }

        void onDelete() {
            ReceiptSeriesRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(FiscalBirPanel.this, "Select a receipt series first.");
                return;
            }
            if (!SwingUtils.confirm(FiscalBirPanel.this, "Delete selected receipt series?")) {
                return;
            }
            try {
                service.deleteReceiptSeries(session.userId(), row.id());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(FiscalBirPanel.this, "Failed to delete receipt series.", (Exception) ex);
            }
        }
    }

    private final class TaxpayerDialog extends JDialog {

        final JTextField txtRegisteredName = new JTextField(25);
        final JTextField txtTradeName = new JTextField(25);
        final JTextField txtTinNo = new JTextField(20);
        final JTextArea txtHeadOfficeAddress = new JTextArea(4, 25);
        final JComboBox<String> cboVatType = new JComboBox<>(new String[]{"VAT", "NONVAT"});
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        TaxpayerDialog(TaxpayerProfileRow row) {
            super(SwingUtilities.getWindowAncestor(FiscalBirPanel.this), row == null ? "Add Tax Payer Profile" : "Edit Tax Payer Profile", ModalityType.APPLICATION_MODAL);
            txtHeadOfficeAddress.setLineWrap(true);
            txtHeadOfficeAddress.setWrapStyleWord(true);

            cboVatType.setFont(font14Plain);
            cboVatType.setPreferredSize(new Dimension(250, 30));

            txtRegisteredName.setFont(font14Plain);
            txtRegisteredName.putClientProperty("JTextField.placeholderText", "Registered name");
            txtRegisteredName.setPreferredSize(new Dimension(250, 30));

            txtTradeName.setFont(font14Plain);
            txtTradeName.putClientProperty("JTextField.placeholderText", "Trade name");
            txtTradeName.setPreferredSize(new Dimension(250, 30));

            txtTinNo.setFont(font14Plain);
            txtTinNo.putClientProperty("JTextField.placeholderText", "TIN no");
            txtTinNo.setPreferredSize(new Dimension(250, 30));

            txtHeadOfficeAddress.setFont(font14Plain);

            chkActive.setFont(font14Plain);

            if (row != null) {
                txtRegisteredName.setText(row.registeredName());
                txtTradeName.setText(row.tradeName());
                txtTinNo.setText(row.tinNo());
                txtHeadOfficeAddress.setText(row.headOfficeAddress());
                cboVatType.setSelectedItem(row.vatRegistrationType());
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(new String[]{"Registered Name", "Trade Name", "TIN No", "Head Office Address", "VAT Type", "Status"},
                    new JComponent[]{txtRegisteredName, txtTradeName, txtTinNo, new JScrollPane(txtHeadOfficeAddress), cboVatType, chkActive});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (registeredName().isBlank() || tinNo().isBlank() || headOfficeAddress().isBlank()) {
                    SwingUtils.info(this, "Registered name, TIN, and head office address are required.");
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
            setLocationRelativeTo(FiscalBirPanel.this);
        }

        String registeredName() {
            return txtRegisteredName.getText().trim();
        }

        String tradeName() {
            return txtTradeName.getText().trim();
        }

        String tinNo() {
            return txtTinNo.getText().trim();
        }

        String headOfficeAddress() {
            return txtHeadOfficeAddress.getText().trim();
        }

        String vatType() {
            return (String) cboVatType.getSelectedItem();
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private final class StoreFiscalDialog extends JDialog {

        final JComboBox<LookupOption> cboStore = new JComboBox<>();
        final JComboBox<LookupOption> cboTaxpayer = new JComboBox<>();
        final JTextField txtBranchCode = new JTextField(10);
        final JTextArea txtRegisteredBusinessAddress = new JTextArea(3, 25);
        final JTextField txtPosVendorName = new JTextField(25);
        final JTextField txtPosVendorTinNo = new JTextField(20);
        final JTextArea txtPosVendorAddress = new JTextArea(3, 25);
        final JTextField txtSupplierAccreditationNo = new JTextField(20);
        final JDateChooser dcAccreditationIssuedAt = dateChooser();
        final JDateChooser dcAccreditationValidUntil = dateChooser();
        final JTextField txtBirPermitToUseNo = new JTextField(20);
        final JDateChooser dcPermitToUseIssuedAt = dateChooser();
        final JTextField txtAtpNo = new JTextField(20);
        final JDateChooser dcAtpIssuedAt = dateChooser();
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        StoreFiscalDialog(StoreFiscalProfileRow row) {
            super(SwingUtilities.getWindowAncestor(FiscalBirPanel.this), row == null ? "Add Store Fiscal Profile" : "Edit Store Fiscal Profile", ModalityType.APPLICATION_MODAL);
            txtRegisteredBusinessAddress.setLineWrap(true);
            txtRegisteredBusinessAddress.setWrapStyleWord(true);
            txtPosVendorAddress.setLineWrap(true);
            txtPosVendorAddress.setWrapStyleWord(true);
            for (LookupOption option : service.storeOptions()) {
                cboStore.addItem(option);
            }
            for (LookupOption option : service.taxpayerOptions()) {
                cboTaxpayer.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboStore);
            ComboBoxAutoSuggestSupport.install(cboTaxpayer);

            cboStore.setFont(font14Plain);
            cboStore.setPreferredSize(new Dimension(250, 30));

            cboTaxpayer.setFont(font14Plain);
            cboTaxpayer.setPreferredSize(new Dimension(250, 30));

            txtBranchCode.setFont(font14Plain);
            txtBranchCode.putClientProperty("JTextField.placeholderText", "Branch code");
            txtBranchCode.setPreferredSize(new Dimension(250, 30));

            txtRegisteredBusinessAddress.setFont(font14Plain);

            txtPosVendorName.setFont(font14Plain);
            txtPosVendorName.putClientProperty("JTextField.placeholderText", "Vendor name");
            txtPosVendorName.setPreferredSize(new Dimension(250, 30));

            txtPosVendorTinNo.setFont(font14Plain);
            txtPosVendorTinNo.putClientProperty("JTextField.placeholderText", "Vendor TIN");
            txtPosVendorTinNo.setPreferredSize(new Dimension(250, 30));

            txtPosVendorAddress.setFont(font14Plain);
            txtPosVendorAddress.putClientProperty("JTextField.placeholderText", "Vendor address");
            txtPosVendorAddress.setPreferredSize(new Dimension(250, 30));

            txtSupplierAccreditationNo.setFont(font14Plain);
            txtSupplierAccreditationNo.putClientProperty("JTextField.placeholderText", "Supplier accreditation no");
            txtSupplierAccreditationNo.setPreferredSize(new Dimension(250, 30));

            dcAccreditationIssuedAt.setFont(font14Plain);
            dcAccreditationIssuedAt.setPreferredSize(new Dimension(250, 30));

            dcAccreditationValidUntil.setFont(font14Plain);
            dcAccreditationValidUntil.setPreferredSize(new Dimension(250, 30));
            
            txtBirPermitToUseNo.setFont(font14Plain);
            txtBirPermitToUseNo.putClientProperty("JTextField.placeholderText", "Supplier accreditation no");
            txtBirPermitToUseNo.setPreferredSize(new Dimension(250, 30));

            txtAtpNo.setFont(font14Plain);
            txtAtpNo.putClientProperty("JTextField.placeholderText", "Vendor address");
            txtAtpNo.setPreferredSize(new Dimension(250, 30));

            dcAtpIssuedAt.setFont(font14Plain);
            dcAtpIssuedAt.setPreferredSize(new Dimension(250, 30));
            
            dcAccreditationValidUntil.setFont(font14Plain);
            dcAccreditationValidUntil.setPreferredSize(new Dimension(250, 30));
            
            dcPermitToUseIssuedAt.setFont(font14Plain);
            dcPermitToUseIssuedAt.setPreferredSize(new Dimension(250, 30));
            

            chkActive.setFont(font14Plain);

            if (row != null) {
                selectLookup(cboStore, row.storeId());
                selectLookup(cboTaxpayer, row.taxpayerProfileId());
                cboStore.setEnabled(false);
                txtBranchCode.setText(row.branchCode());
                txtRegisteredBusinessAddress.setText(row.registeredBusinessAddress());
                txtPosVendorName.setText(row.posVendorName());
                txtPosVendorTinNo.setText(row.posVendorTinNo());
                txtPosVendorAddress.setText(row.posVendorAddress());
                txtSupplierAccreditationNo.setText(row.supplierAccreditationNo());
                dcAccreditationIssuedAt.setDate(toUtilDate(row.accreditationIssuedAt()));
                dcAccreditationValidUntil.setDate(toUtilDate(row.accreditationValidUntil()));
                txtBirPermitToUseNo.setText(row.birPermitToUseNo());
                dcPermitToUseIssuedAt.setDate(toUtilDate(row.permitToUseIssuedAt()));
                txtAtpNo.setText(row.atpNo());
                dcAtpIssuedAt.setDate(toUtilDate(row.atpIssuedAt()));
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(new String[]{"Store", "Tax Payer Profile", "Branch Code", "Business Address", "POS Vendor Name", "POS Vendor TIN", "POS Vendor Address", "Supplier Accreditation No", "Accreditation Issued", "Accreditation Valid Until", "BIR Permit To Use No", "Permit Issued", "ATP No", "ATP Issued", "Status"},
                    new JComponent[]{cboStore, cboTaxpayer, txtBranchCode, new JScrollPane(txtRegisteredBusinessAddress), txtPosVendorName, txtPosVendorTinNo, new JScrollPane(txtPosVendorAddress), txtSupplierAccreditationNo, dcAccreditationIssuedAt, dcAccreditationValidUntil, txtBirPermitToUseNo, dcPermitToUseIssuedAt, txtAtpNo, dcAtpIssuedAt, chkActive});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setDanger();
            cancel.setIcon(iconClose);
            ok.addActionListener(e -> {
                if (storeId() == null || taxpayerProfileId() == null || branchCode().isBlank() || registeredBusinessAddress().isBlank()) {
                    SwingUtils.info(this, "Store, tax payer profile, branch code, and business address are required.");
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
            setLocationRelativeTo(FiscalBirPanel.this);
        }

        Integer storeId() {
            return selectedLookupId(cboStore);
        }

        Integer taxpayerProfileId() {
            return selectedLookupId(cboTaxpayer);
        }

        String branchCode() {
            return txtBranchCode.getText().trim();
        }

        String registeredBusinessAddress() {
            return txtRegisteredBusinessAddress.getText().trim();
        }

        String posVendorName() {
            return txtPosVendorName.getText().trim();
        }

        String posVendorTinNo() {
            return txtPosVendorTinNo.getText().trim();
        }

        String posVendorAddress() {
            return txtPosVendorAddress.getText().trim();
        }

        String supplierAccreditationNo() {
            return txtSupplierAccreditationNo.getText().trim();
        }

        Date accreditationIssuedAt() {
            return toSqlDate(dcAccreditationIssuedAt);
        }

        Date accreditationValidUntil() {
            return toSqlDate(dcAccreditationValidUntil);
        }

        String birPermitToUseNo() {
            return txtBirPermitToUseNo.getText().trim();
        }

        Date permitToUseIssuedAt() {
            return toSqlDate(dcPermitToUseIssuedAt);
        }

        String atpNo() {
            return txtAtpNo.getText().trim();
        }

        Date atpIssuedAt() {
            return toSqlDate(dcAtpIssuedAt);
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private final class ReceiptSeriesDialog extends JDialog {

        final JComboBox<LookupOption> cboTerminal = new JComboBox<>();
        final JComboBox<String> cboDocType = new JComboBox<>(new String[]{"INVOICE", "SUPP_RECEIPT"});
        final JTextField txtPrefix = new JTextField(20);
        final JSpinner spnSerialFrom = new JSpinner(new SpinnerNumberModel(1L, 1L, Long.MAX_VALUE, 1L));
        final JSpinner spnSerialTo = new JSpinner(new SpinnerNumberModel(100L, 1L, Long.MAX_VALUE, 1L));
        final JSpinner spnNextSerial = new JSpinner(new SpinnerNumberModel(1L, 1L, Long.MAX_VALUE, 1L));
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        ReceiptSeriesDialog(ReceiptSeriesRow row) {
            super(SwingUtilities.getWindowAncestor(FiscalBirPanel.this), row == null ? "Add Receipt Series" : "Edit Receipt Series", ModalityType.APPLICATION_MODAL);
            for (LookupOption option : service.terminalOptions()) {
                cboTerminal.addItem(option);
            }
            ComboBoxAutoSuggestSupport.install(cboTerminal);

            cboTerminal.setFont(font14Plain);
            cboTerminal.setPreferredSize(new Dimension(250, 30));

            cboDocType.setFont(font14Plain);
            cboDocType.setPreferredSize(new Dimension(250, 30));

            txtPrefix.setFont(font14Plain);
            txtPrefix.putClientProperty("JTextField.placeholderText", "Prefix");
            txtPrefix.setPreferredSize(new Dimension(250, 30));

            spnSerialFrom.setFont(font14Plain);
            spnSerialFrom.setPreferredSize(new Dimension(250, 30));

            spnSerialTo.setFont(font14Plain);
            spnSerialTo.setPreferredSize(new Dimension(250, 30));

            spnNextSerial.setFont(font14Plain);
            spnNextSerial.setPreferredSize(new Dimension(250, 30));

            chkActive.setFont(font14Plain);

            if (row != null) {
                selectLookup(cboTerminal, row.terminalId());
                cboDocType.setSelectedItem(row.docType());
                txtPrefix.setText(row.prefix());
                spnSerialFrom.setValue(row.serialFrom());
                spnSerialTo.setValue(row.serialTo());
                spnNextSerial.setValue(row.nextSerial());
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(new String[]{"Terminal", "Doc Type", "Prefix", "Serial From", "Serial To", "Next Serial", "Status"},
                    new JComponent[]{cboTerminal, cboDocType, txtPrefix, spnSerialFrom, spnSerialTo, spnNextSerial, chkActive});
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> {
                if (terminalId() == null || serialFrom() > serialTo() || nextSerial() < serialFrom() || nextSerial() > serialTo() + 1) {
                    SwingUtils.info(this, "Enter a valid terminal and serial range.");
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
            setLocationRelativeTo(FiscalBirPanel.this);
        }

        Integer terminalId() {
            return selectedLookupId(cboTerminal);
        }

        String docType() {
            return (String) cboDocType.getSelectedItem();
        }

        String prefix() {
            return txtPrefix.getText().trim();
        }

        long serialFrom() {
            return ((Number) spnSerialFrom.getValue()).longValue();
        }

        long serialTo() {
            return ((Number) spnSerialTo.getValue()).longValue();
        }

        long nextSerial() {
            return ((Number) spnNextSerial.getValue()).longValue();
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

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

    private static JPanel buttonBar(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        for (JButton b : buttons) {
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

    private static String fmtDate(java.util.Date value) {
        return value == null ? "" : DATE_FORMAT.format(value);
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

    private static JDateChooser dateChooser() {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MMM-dd");
        chooser.setPreferredSize(new Dimension(180, chooser.getPreferredSize().height));
        return chooser;
    }

    private static java.util.Date toUtilDate(Date value) {
        return value == null ? null : new java.util.Date(value.getTime());
    }

    private static Date toSqlDate(JDateChooser chooser) {
        java.util.Date value = chooser.getDate();
        return value == null ? null : new Date(value.getTime());
    }
}
