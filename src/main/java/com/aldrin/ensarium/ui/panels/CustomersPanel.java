package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.CustomerBenefitProfileRow;
import com.aldrin.ensarium.model.CustomerRow;
import com.aldrin.ensarium.model.LookupOption;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.CustomerAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.ComboBoxAutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;
import com.aldrin.ensarium.util.TableStyleSupport;

import com.toedter.calendar.JDateChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class CustomersPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

    private final Session session;
    private final CustomerAdminService service = new CustomerAdminService();
    private final boolean canWrite;

    private final CustomerTab customerTab;
    private final BenefitTab benefitTab;

    Color color = new Color(0x6E6E6E);
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, Color.WHITE);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);

    public CustomersPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.CUSTOMER);
        this.customerTab = new CustomerTab();
        this.benefitTab = new BenefitTab();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Customers");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer", customerTab);
        tabs.addTab("Benefit profile", benefitTab);
        add(tabs, BorderLayout.CENTER);

//        BootstrapTabbedPaneStyle.install(tabs);
// BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
//        .accent(new Color(0x0D6EFD));
//
//BootstrapTabbedPaneStyle.install(tabs, style);
//        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
//                .selectedLineColor(new Color(0x0D6EFD))
//                .selectedLineThickness(3)
//                .selectedLineInset(12);
//
//        BootstrapTabbedPaneStyle.install(tabs, style);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);

        refreshAll();
    }

    public void refreshAll() {
        customerTab.refresh();
        benefitTab.refresh();
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

    private JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, JButton... rightButtons) {
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        leftTools.add(new JLabel("Search"));
        leftTools.add(txtSearch);
        leftTools.add(new JLabel("Limit"));
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        for (JButton b : rightButtons) {
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

    final DefaultTableModel modelCustomer = new DefaultTableModel(new Object[]{
        "ID", "Customer no", "Full name", "TIN", "Phone", "Email", "Address", "Senior", "Senior ID", "VAT exempt", "Active", "Created at"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
//                case 7, 9, 10 ->
//                    Boolean.class;
                default ->
                    Object.class;
            };
        }
    };

    private final class CustomerTab extends BaseTab {

//        final JTable tableBenefit = new JTable(modelCustomer);
        final List<CustomerRow> allRows = new ArrayList<>();

        CustomerTab() {

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
            btnDelete.setVisible(false);

            txtSearch.putClientProperty("JTextField.placeholderText", "Search customer");
            txtSearch.setPreferredSize(new Dimension(300, 30));
            spnLimit.setPreferredSize(new Dimension(90, 30));

            installTableHover();

            table.setAutoCreateRowSorter(true);
//            TableStyleSupport.apply(tableBenefit);
            hideColumn(table, 0);

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnDelete, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());
            installSuggest();

            BootstrapTableStyle.installAll(this);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            BootstrapTableStyle.setFixedColumnWidth(table, 0, 70);
            BootstrapTableStyle.setColumnWidth(table, 1, 140);
            BootstrapTableStyle.setColumnWidth(table, 2, 280);
            BootstrapTableStyle.setColumnWidth(table, 3, 110);
            BootstrapTableStyle.setColumnWidth(table, 4, 120);
            BootstrapTableStyle.setColumnWidth(table, 5, 260);
            BootstrapTableStyle.setColumnWidth(table, 6, 280);
            BootstrapTableStyle.setColumnWidth(table, 7, 140);
            BootstrapTableStyle.setColumnWidth(table, 8, 140);
            BootstrapTableStyle.setColumnWidth(table, 9, 140);
            BootstrapTableStyle.setColumnWidth(table, 10, 140);
            BootstrapTableStyle.setColumnWidth(table, 11, 240);

            BootstrapTableStyle.setColumnLeft(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnCenter(table, 7);
            BootstrapTableStyle.setColumnLeft(table, 8);
            BootstrapTableStyle.setColumnCenter(table, 9);
            BootstrapTableStyle.setColumnCenter(table, 10);
            BootstrapTableStyle.setColumnLeft(table, 11);

            BootstrapTableStyle.setHeaderLeft(table, 0);
            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderLeft(table, 5);
            BootstrapTableStyle.setHeaderLeft(table, 6);
            BootstrapTableStyle.setHeaderCenter(table, 7);
            BootstrapTableStyle.setHeaderLeft(table, 8);
            BootstrapTableStyle.setHeaderCenter(table, 9);
            BootstrapTableStyle.setHeaderCenter(table, 10);
            BootstrapTableStyle.setHeaderLeft(table, 11);
            installTableHover();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listCustomers());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedId();
            modelCustomer.setRowCount(0);
            String q = query();
            int count = 0;
            for (CustomerRow row : allRows) {
                String hay = String.join(" ",
                        nv(row.customerNo()), nv(row.fullName()), nv(row.tinNo()), nv(row.phone()), nv(row.email()), nv(row.address()), nv(row.seniorIdNo())
                ).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                modelCustomer.addRow(new Object[]{
                    row.id(), row.customerNo(), row.fullName(), row.tinNo(), row.phone(), row.email(), row.address(),
                    row.senior() ? "Yes" : "No", row.seniorIdNo(), row.vatExempt() ? "Yes" : "No", row.active() ? "Yes" : "No", fmt(row.createdAt())
                });
                if (++count >= limit()) {
                    break;
                }
            }
            reselectLong(table, modelCustomer, keepId);
//            r.active() ? "Yes" : "No"
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (CustomerRow r : allRows) {
                out.add(r.customerNo());
                out.add(r.fullName());
                out.add(r.tinNo());
                out.add(r.phone());
                out.add(r.email());
                out.add(r.address());
                out.add(r.seniorIdNo());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Long selectedId() {
            return selectedLongId(table, modelCustomer);
        }

        CustomerRow selectedRow() {
            Long id = selectedId();
            if (id == null) {
                return null;
            }
            return allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        }

        void onAdd() {
            CustomerDialog d = new CustomerDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createCustomer(session.userId(), d.customerNo(), d.fullName(), d.tinNo(), d.phone(), d.email(), d.address(), d.senior(), d.seniorIdNo(), d.vatExempt(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to create customer.", (Exception) ex);
            }
        }

        void onEdit() {
            CustomerRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(CustomersPanel.this, "Select a customer first.");
                return;
            }
            CustomerDialog d = new CustomerDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateCustomer(session.userId(), row.id(), d.customerNo(), d.fullName(), d.tinNo(), d.phone(), d.email(), d.address(), d.senior(), d.seniorIdNo(), d.vatExempt(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to update customer.", (Exception) ex);
            }
        }

        void onDelete() {
            CustomerRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(CustomersPanel.this, "Select a customer first.");
                return;
            }
            if (!SwingUtils.confirm(CustomersPanel.this, "Delete selected customer?")) {
                return;
            }
            try {
                service.deleteCustomer(session.userId(), row.id());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to delete customer.", (Exception) ex);
            }
        }
    }

    final DefaultTableModel modelBenefit = new DefaultTableModel(new Object[]{
        "ID", "CUSTOMER ID", "Customer", "Benefit type", "GOV ID NO", "TIN", "Active", "Effective from", "Effective to", "Created at"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Object.class;
        }
    };

    private final class BenefitTab extends BaseTab {

//        final JTable tableBenefit = new JTable(modelBenefit);
        final List<CustomerBenefitProfileRow> allRows = new ArrayList<>();

        BenefitTab() {

            txtSearch.putClientProperty("JTextField.placeholderText", "Search benefit");
            txtSearch.setPreferredSize(new Dimension(300, 30));
            spnLimit.setPreferredSize(new Dimension(90, 30));

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
            btnDelete.setVisible(false);

            installTableHoverBenefit();

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnDelete, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(tableBenefit), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());
            installSuggest();

            BootstrapTableStyle.installAll(this);

            tableBenefit.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            BootstrapTableStyle.setFixedColumnWidth(tableBenefit, 0, 70);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 1, 200);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 2, 150);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 3, 150);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 4, 200);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 5, 150);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 6, 150);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 7, 150);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 8, 250);
            BootstrapTableStyle.setColumnWidth(tableBenefit, 9, 200);

            BootstrapTableStyle.setColumnLeft(tableBenefit, 0);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 1);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 2);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 3);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 4);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 5);
            BootstrapTableStyle.setColumnCenter(tableBenefit, 6);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 7);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 8);
            BootstrapTableStyle.setColumnLeft(tableBenefit, 9);

            BootstrapTableStyle.setHeaderLeft(tableBenefit, 0);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 1);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 2);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 3);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 4);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 5);
            BootstrapTableStyle.setHeaderCenter(tableBenefit, 6);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 7);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 8);
            BootstrapTableStyle.setHeaderLeft(tableBenefit, 9);

            hideColumn(tableBenefit, 0);
            hideColumn(tableBenefit, 1);
            installTableHover();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listBenefitProfiles());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedId();
            modelBenefit.setRowCount(0);
            String q = query();
            int count = 0;
            for (CustomerBenefitProfileRow row : allRows) {
                String hay = String.join(" ", nv(row.customerName()), nv(row.benefitType()), nv(row.govIdNo()), nv(row.tinNo())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                modelBenefit.addRow(new Object[]{
                    row.id(), row.customerId(), row.customerName(), row.benefitType(), row.govIdNo(), row.tinNo(), row.active() ? "Yes" : "No",
                    fmtDate(row.effectiveFrom()), fmtDate(row.effectiveTo()), fmt(row.createdAt())
                });
                if (++count >= limit()) {
                    break;
                }
            }
            reselectLong(tableBenefit, modelBenefit, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (CustomerBenefitProfileRow r : allRows) {
                out.add(r.customerName());
                out.add(r.benefitType());
                out.add(r.govIdNo());
                out.add(r.tinNo());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Long selectedId() {
            return selectedLongId(tableBenefit, modelBenefit);
        }

        CustomerBenefitProfileRow selectedRow() {
            Long id = selectedId();
            if (id == null) {
                return null;
            }
            return allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        }

        void onAdd() {
            BenefitDialog d = new BenefitDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createBenefitProfile(session.userId(), d.customerId(), d.benefitType(), d.govIdNo(), d.tinNo(), d.active(), d.effectiveFrom(), d.effectiveTo());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to create benefit profile.", (Exception) ex);
            }
        }

        void onEdit() {
            CustomerBenefitProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(CustomersPanel.this, "Select a benefit profile first.");
                return;
            }
            BenefitDialog d = new BenefitDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateBenefitProfile(session.userId(), row.id(), d.customerId(), d.benefitType(), d.govIdNo(), d.tinNo(), d.active(), d.effectiveFrom(), d.effectiveTo());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to update benefit profile.", (Exception) ex);
            }
        }

        void onDelete() {
            CustomerBenefitProfileRow row = selectedRow();
            if (row == null) {
                SwingUtils.info(CustomersPanel.this, "Select a benefit profile first.");
                return;
            }
            if (!SwingUtils.confirm(CustomersPanel.this, "Delete selected benefit profile?")) {
                return;
            }
            try {
                service.deleteBenefitProfile(session.userId(), row.id());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(CustomersPanel.this, "Failed to delete benefit profile.", (Exception) ex);
            }
        }
    }

    private final class CustomerDialog extends JDialog {

        final JTextField txtCustomerNo = new JTextField(20);
        final JTextField txtFullName = new JTextField(20);
        final JTextField txtTin = new JTextField(20);
        final JTextField txtPhone = new JTextField(20);
        final JTextField txtEmail = new JTextField(20);
        final JTextArea txtAddress = new JTextArea(4, 20);
        final JCheckBox chkSenior = new JCheckBox("Senior Citizen");
        final JTextField txtSeniorId = new JTextField(20);
        final JCheckBox chkVatExempt = new JCheckBox("VAT Exempt");
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        CustomerDialog(CustomerRow row) {
            super(SwingUtilities.getWindowAncestor(CustomersPanel.this), row == null ? "Add Customer" : "Edit Customer", ModalityType.APPLICATION_MODAL);
            txtAddress.setLineWrap(true);
            txtAddress.setWrapStyleWord(true);
            JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            flags.add(chkSenior);
            flags.add(chkVatExempt);
            flags.add(chkActive);
            if (row != null) {
                txtCustomerNo.setText(row.customerNo());
                txtFullName.setText(row.fullName());
                txtTin.setText(row.tinNo());
                txtPhone.setText(row.phone());
                txtEmail.setText(row.email());
                txtAddress.setText(row.address());
                chkSenior.setSelected(row.senior());
                txtSeniorId.setText(row.seniorIdNo());
                chkVatExempt.setSelected(row.vatExempt());
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(
                    new String[]{"Customer No", "Full Name", "TIN", "Phone", "Email", "Address", "Senior ID No", "Flags"},
                    new JComponent[]{txtCustomerNo, txtFullName, txtTin, txtPhone, txtEmail, new JScrollPane(txtAddress), txtSeniorId, flags}
            );
//            JButton ok = new JButton("Save");
//            JButton cancel = new JButton("Cancel");
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> {
                if (fullName().isBlank()) {
                    SwingUtils.info(this, "Full name is required.");
                    return;
                }
                saved = true;
                dispose();
            });

            installTableHover();
            cancel.addActionListener(e -> dispose());
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttonBar(ok, cancel), BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(CustomersPanel.this);
            getRootPane().setDefaultButton(ok);
        }

        String customerNo() {
            return txtCustomerNo.getText().trim();
        }

        String fullName() {
            return txtFullName.getText().trim();
        }

        String tinNo() {
            return txtTin.getText().trim();
        }

        String phone() {
            return txtPhone.getText().trim();
        }

        String email() {
            return txtEmail.getText().trim();
        }

        String address() {
            return txtAddress.getText().trim();
        }

        boolean senior() {
            return chkSenior.isSelected();
        }

        String seniorIdNo() {
            return txtSeniorId.getText().trim();
        }

        boolean vatExempt() {
            return chkVatExempt.isSelected();
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private final class BenefitDialog extends JDialog {

        final JComboBox<LookupOption> cboCustomer = new JComboBox<>();
        final JComboBox<String> cboType = new JComboBox<>(new String[]{"SENIOR", "PWD"});
        final JTextField txtGovId = new JTextField(20);
        final JTextField txtTin = new JTextField(20);
        final JDateChooser dcFrom = dateChooser();
        final JDateChooser dcTo = dateChooser();
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;

        BenefitDialog(CustomerBenefitProfileRow row) {
            super(SwingUtilities.getWindowAncestor(CustomersPanel.this), row == null ? "Add Benefit Profile" : "Edit Benefit Profile", ModalityType.APPLICATION_MODAL);
            loadCustomers();
            ComboBoxAutoSuggestSupport.install(cboCustomer);
            if (row != null) {
                selectLookup(cboCustomer, (int) row.customerId());
                cboType.setSelectedItem(row.benefitType());
                txtGovId.setText(row.govIdNo());
                txtTin.setText(row.tinNo());
                if (row.effectiveFrom() != null) {
                    dcFrom.setDate(new java.util.Date(row.effectiveFrom().getTime()));
                }
                if (row.effectiveTo() != null) {
                    dcTo.setDate(new java.util.Date(row.effectiveTo().getTime()));
                }
                chkActive.setSelected(row.active());
            }
            JPanel form = simpleForm(
                    new String[]{"Customer", "Benefit Type", "Government ID No", "TIN", "Effective From", "Effective To", "Status"},
                    new JComponent[]{cboCustomer, cboType, txtGovId, txtTin, dcFrom, dcTo, chkActive}
            );
//            JButton ok = new JButton("Save");
//            JButton cancel = new JButton("Cancel");
            StyledButton ok = new StyledButton("Save");
            ok.setIcon(iconSave);
            StyledButton cancel = new StyledButton("Cancel");
            cancel.setIcon(iconClose);
            cancel.setDanger();
            ok.addActionListener(e -> {
                if (customerId() <= 0) {
                    SwingUtils.info(this, "Customer is required.");
                    return;
                }
                if (govIdNo().isBlank()) {
                    SwingUtils.info(this, "Government ID No is required.");
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
            setLocationRelativeTo(CustomersPanel.this);
            getRootPane().setDefaultButton(ok);
        }

        void loadCustomers() {
            cboCustomer.removeAllItems();
            for (LookupOption option : service.customerOptions()) {
                cboCustomer.addItem(option);
            }
        }

        long customerId() {
            LookupOption option = (LookupOption) cboCustomer.getSelectedItem();
            return option == null ? 0 : option.id();
        }

        String benefitType() {
            return (String) cboType.getSelectedItem();
        }

        String govIdNo() {
            return txtGovId.getText().trim();
        }

        String tinNo() {
            return txtTin.getText().trim();
        }

        Date effectiveFrom() {
            return toDate(dcFrom.getDate());
        }

        Date effectiveTo() {
            return toDate(dcTo.getDate());
        }

        boolean active() {
            return chkActive.isSelected();
        }
    }

    private JPanel simpleForm(String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            form.add(new JLabel(labels[i]), g);
            g.gridx = 1;
            g.weightx = 1;
            form.add(fields[i], g);
        }
        return form;
    }

    private JPanel buttonBar(JButton ok, JButton cancel) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setBorder(new EmptyBorder(0, 12, 12, 12));
        bar.add(ok);
        bar.add(cancel);
        return bar;
    }

    private void selectLookup(JComboBox<LookupOption> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            LookupOption option = combo.getItemAt(i);
            if (option.id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private Long selectedLongId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Object value = model.getValueAt(modelRow, 0);
        if (value instanceof Number n) {
            return n.longValue();
        }
        return value == null ? null : Long.parseLong(value.toString());
    }

    private void reselectLong(JTable table, DefaultTableModel model, Long id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 0);
            long rowId = value instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(value));
            if (rowId == id) {
                int viewRow = table.convertRowIndexToView(i);
                table.setRowSelectionInterval(viewRow, viewRow);
                break;
            }
        }
    }

    private String fmt(Timestamp timestamp) {
        return timestamp == null ? "" : TS_FORMAT.format(timestamp);
    }

    private String fmtDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private String nv(String s) {
        return s == null ? "" : s;
    }

    private JDateChooser dateChooser() {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MMM-dd");
        chooser.setPreferredSize(new Dimension(180, chooser.getPreferredSize().height));
        return chooser;
    }

    private Date toDate(java.util.Date date) {
        return date == null ? null : new Date(date.getTime());
    }

   
    private final JTable table = new JTable(modelCustomer) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            if (!isRowSelected(row)) {
                if (row == hoveredRow) {
                    c.setBackground(new Color(0xE8F2FF)); // hover color
                } else {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(0xF7F9FC)); // stripes
                }
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(getSelectionBackground());
                c.setForeground(getSelectionForeground());
            }

            if (c instanceof JComponent jc) {
                jc.setOpaque(true);
            }

            return c;
        }
    };

    private final JTable tableBenefit = new JTable(modelBenefit) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            if (!isRowSelected(row)) {
                if (row == hoveredRow) {
                    c.setBackground(new Color(0xE8F2FF)); // hover color
                } else {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(0xF7F9FC)); // stripes
                }
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(getSelectionBackground());
                c.setForeground(getSelectionForeground());
            }

            if (c instanceof JComponent jc) {
                jc.setOpaque(true);
            }

            return c;
        }
    };

    
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

    private void installTableHoverBenefit() {
        tableBenefit.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = tableBenefit.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    tableBenefit.repaint();
                }
            }
        });

        tableBenefit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    tableBenefit.repaint();
                }
            }
        });
    }
}
