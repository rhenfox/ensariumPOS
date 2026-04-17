/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.BenefitPolicyRow;
import com.aldrin.ensarium.model.DiscountTypeRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.BenefitPolicyAdminService;
import com.aldrin.ensarium.service.DiscountTypeAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.TableStyleSupport;
import com.aldrin.ensarium.util.UiSupport;
import com.toedter.calendar.JDateChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class SaleSetupPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

    private final Session session;
    private final boolean canWrite;

    private final DiscountTypeAdminService discountTypesService = new DiscountTypeAdminService();
    private final BenefitPolicyAdminService benefitPoliciesService = new BenefitPolicyAdminService();

    private final DiscountTypesPanel discountTypesTab;
    private final BenefitPoliciesPanel benefitPolicyTab;
//    private final Be

    static Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public SaleSetupPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.SETUP_INVENTORY);
        this.discountTypesTab = new DiscountTypesPanel();
        this.benefitPolicyTab = new BenefitPoliciesPanel();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Sale setup");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Discount types", discountTypesTab);
        tabs.addTab("Benefit policies", benefitPolicyTab);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);

        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        discountTypesTab.refresh();
        benefitPolicyTab.refresh();
//        receiptSeriesTab.refresh();
    }

    private JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, JButton... rightButtons) {
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        leftTools.add(searchl);
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("");
        limitl.setFont(font14Bold);
        leftTools.add(limitl);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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

    private final class DiscountTypesPanel extends BaseTab {

        private final JTextField txtSearch = new JTextField(22);
        private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Code", "Name", "Kind", "Applies To", "Active"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                return columnIndex == 5 ? Boolean.class : Object.class;
//            }
        };
        private final JTable table = new JTable(model);
        private final List<DiscountTypeRow> allRows = new ArrayList<>();

        DiscountTypesPanel() {

            setLayout(new BorderLayout(0, 0));
//            setBorder(new EmptyBorder(16, 16, 16, 16));

//            JLabel title = new JLabel("Inventory Statuses");
//            title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
//            add(title, BorderLayout.NORTH);
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
            AutoSuggestSupport.install(txtSearch, this::suggestions);

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
            spnLimit.setFont(font14Plain);
            leftTools.add(spnLimit);

            JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightTools.add(btnAdd);
            rightTools.add(btnEdit);
//            rightTools.add(btnDelete);
            rightTools.add(btnRefresh);

            JPanel toolbar = new JPanel(new BorderLayout(8, 0));
            toolbar.add(leftTools, BorderLayout.WEST);
            toolbar.add(rightTools, BorderLayout.EAST);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {100, 250, 200, 200, 100};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderLeft(table, 5);
            for (int i = 6; i <= 13; i++) {
                BootstrapTableStyle.setColumnRight(table, i);
            }

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());

            JPanel body = new JPanel(new BorderLayout(0, 10));
            body.add(toolbar, BorderLayout.NORTH);
            body.add(new JScrollPane(table), BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);

            refresh();
        }

        @Override
        void refresh() {
            try {
                allRows.clear();
                allRows.addAll(discountTypesService.listDiscountTypes());
                applyFilter();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        void applyFilter() {
            model.setRowCount(0);
            String q = UiSupport.nz(txtSearch.getText()).trim().toLowerCase();
            int limit = (Integer) spnLimit.getValue();
            int count = 0;
            for (DiscountTypeRow row : allRows) {
                String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name()) + " " + UiSupport.nz(row.kind()) + " " + UiSupport.nz(row.appliesTo())).toLowerCase();
                if (!q.isEmpty() && !haystack.contains(q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.code(), row.name(), row.kind(), row.appliesTo(), row.active() ? "Yes" : "No"});
                if (++count >= limit) {
                    break;
                }
            }
            UiSupport.hideColumn(table, 0);
        }

        @Override
        List<String> suggestions() {
            Set<String> values = new LinkedHashSet<>();
            for (DiscountTypeRow row : allRows) {
                if (row.code() != null && !row.code().isBlank()) {
                    values.add(row.code());
                }
                if (row.name() != null && !row.name().isBlank()) {
                    values.add(row.name());
                }
                if (row.kind() != null && !row.kind().isBlank()) {
                    values.add(row.kind());
                }
                if (row.appliesTo() != null && !row.appliesTo().isBlank()) {
                    values.add(row.appliesTo());
                }
            }
            return new ArrayList<>(values);
        }

        private void onAdd() {
            DiscountTypeForm form = new DiscountTypeForm(SwingUtilities.getWindowAncestor(this), null);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                discountTypesService.create(session.userId(), form.txtCode.getText(), form.txtName.getText(),
                        (String) form.cboKind.getSelectedItem(), (String) form.cboAppliesTo.getSelectedItem(),
                        form.chkActive.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onEdit() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a discount type first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            DiscountTypeRow current = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (current == null) {
                return;
            }
            DiscountTypeForm form = new DiscountTypeForm(SwingUtilities.getWindowAncestor(this), current);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                discountTypesService.update(session.userId(), id, form.txtCode.getText(), form.txtName.getText(),
                        (String) form.cboKind.getSelectedItem(), (String) form.cboAppliesTo.getSelectedItem(),
                        form.chkActive.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onDelete() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a discount type first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            if (JOptionPane.showConfirmDialog(this, "Delete selected discount type?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                discountTypesService.delete(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static final class DiscountTypeForm extends JDialog {

            final JTextField txtCode = new JTextField(20);
            final JTextField txtName = new JTextField(20);
            final JComboBox<String> cboKind = new JComboBox<>(new String[]{"PERCENT", "AMOUNT"});
            final JComboBox<String> cboAppliesTo = new JComboBox<>(new String[]{"LINE", "SUBTOTAL", "TOTAL"});
            final JCheckBox chkActive = new JCheckBox("Active", true);
            boolean saved;

            Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
            Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

            DiscountTypeForm(Window owner, DiscountTypeRow row) {
                super(owner, row == null ? "Add discount type" : "Edit discount type", Dialog.ModalityType.APPLICATION_MODAL);
                JPanel form = new JPanel(new GridBagLayout());
                form.setBorder(new EmptyBorder(12, 12, 12, 12));
                GridBagConstraints gc = new GridBagConstraints();
                gc.insets = new Insets(5, 5, 5, 5);
                gc.anchor = GridBagConstraints.WEST;
                gc.fill = GridBagConstraints.HORIZONTAL;

                int y = 0;
                gc.gridx = 0;
                gc.gridy = y;
                JLabel codel = new JLabel("Code");
                codel.setFont(font14Bold);
                form.add(codel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtCode.setFont(font14Plain);
                txtCode.putClientProperty("JTextField.placeholderText", "Code");
                form.add(txtCode, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel namel = new JLabel("Name");
                namel.setFont(font14Bold);
                form.add(namel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                form.add(txtName, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel kindl = new JLabel("Kind");
                kindl.setFont(font14Bold);
                form.add(kindl, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                cboKind.setFont(font14Plain);
                form.add(cboKind, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel appliesTol = new JLabel("Applies To");
                appliesTol.setFont(font14Bold);
                form.add(appliesTol, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                cboAppliesTo.setFont(font14Plain);
                form.add(cboAppliesTo, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                chkActive.setFont(font14Plain);
                form.add(chkActive, gc);

                Color color = Color.WHITE;
                Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
                Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
                Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
                Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
                Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
                Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

                StyledButton btnSave = new StyledButton("Save");
                StyledButton btnCancel = new StyledButton("Cancel");
                btnSave.setIcon(iconSave);
                btnCancel.setIcon(iconClose);
                btnCancel.setDanger();
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
                actions.add(btnSave);
                actions.add(btnCancel);

                btnSave.addActionListener(e -> {
                    if (txtCode.getText() == null || txtCode.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Code is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Name is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    saved = true;
                    dispose();
                });
                btnCancel.addActionListener(e -> dispose());

                if (row != null) {
                    txtCode.setText(row.code());
                    txtName.setText(row.name());
                    cboKind.setSelectedItem(row.kind());
                    cboAppliesTo.setSelectedItem(row.appliesTo());
                    chkActive.setSelected(row.active());
                }

                setLayout(new BorderLayout(0, 10));
                add(form, BorderLayout.CENTER);
                add(actions, BorderLayout.SOUTH);
                pack();
//                setSize(Math.max(getWidth(), 460), getHeight());
                setSize(getWidth(), getHeight());
                setLocationRelativeTo(owner);
            }
        }
    }

    public final class BenefitPoliciesPanel extends BaseTab {

        private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a");

        private final JTextField txtSearch = new JTextField(22);
        private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Code", "Name", "Benefit Type", "Kind", "Default Rate", "Min Rate", "Max Rate",
            "VAT Exempt", "Manual Override", "Legal Basis", "Effective From", "Effective To", "Active", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    default ->
                        Object.class;
                };
            }
        };
        private final JTable table = new JTable(model);
        private final List<BenefitPolicyRow> allRows = new ArrayList<>();

        BenefitPoliciesPanel() {

            setLayout(new BorderLayout(0, 0));
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);

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
            AutoSuggestSupport.install(txtSearch, this::suggestions);

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
            spnLimit.setFont(font14Plain);
            spnLimit.setPreferredSize(new Dimension(80, 30));
            leftTools.add(spnLimit);

            JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightTools.add(btnAdd);
            rightTools.add(btnEdit);
//            rightTools.add(btnDelete);
            rightTools.add(btnRefresh);

            JPanel toolbar = new JPanel(new BorderLayout(8, 0));
            toolbar.add(leftTools, BorderLayout.WEST);
            toolbar.add(rightTools, BorderLayout.EAST);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 150, 250, 120, 120, 120, 120, 120, 150, 150, 180, 150, 150, 150, 180};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnRight(table, 5);
            BootstrapTableStyle.setColumnRight(table, 6);
            BootstrapTableStyle.setColumnRight(table, 7);
            BootstrapTableStyle.setColumnLeft(table, 8);
            BootstrapTableStyle.setColumnLeft(table, 9);
            BootstrapTableStyle.setColumnLeft(table, 10);
            BootstrapTableStyle.setColumnLeft(table, 11);
            BootstrapTableStyle.setColumnLeft(table, 12);
            BootstrapTableStyle.setColumnLeft(table, 13);
            BootstrapTableStyle.setColumnLeft(table, 14);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderRight(table, 5);
            BootstrapTableStyle.setHeaderRight(table, 6);
            BootstrapTableStyle.setHeaderRight(table, 7);
            BootstrapTableStyle.setHeaderLeft(table, 8);
            BootstrapTableStyle.setHeaderLeft(table, 9);
            BootstrapTableStyle.setHeaderLeft(table, 10);
            BootstrapTableStyle.setHeaderLeft(table, 11);
            BootstrapTableStyle.setHeaderLeft(table, 12);
            BootstrapTableStyle.setHeaderLeft(table, 13);
            BootstrapTableStyle.setHeaderLeft(table, 14);
//            for (int i = 6; i <= 13; i++) {
//                BootstrapTableStyle.setColumnRight(table, i);
//            }

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refreshAll());

            JPanel body = new JPanel(new BorderLayout(15, 15));
            body.add(toolbar, BorderLayout.NORTH);
            body.add(new JScrollPane(table), BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);

            refresh();
        }

        @Override
        void refresh() {
            try {
                allRows.clear();
                allRows.addAll(benefitPoliciesService.listBenefitPolicies());
                applyFilter();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        void applyFilter() {
            model.setRowCount(0);
            String q = UiSupport.nz(txtSearch.getText()).trim().toLowerCase();
            int limit = (Integer) spnLimit.getValue();
            int count = 0;
            for (BenefitPolicyRow row : allRows) {
                String haystack = String.join(" ",
                        UiSupport.nz(row.code()), UiSupport.nz(row.name()), UiSupport.nz(row.benefitType()),
                        UiSupport.nz(row.kind()), UiSupport.nz(row.legalBasis())).toLowerCase();
                if (!q.isEmpty() && !haystack.contains(q)) {
                    continue;
                }
                model.addRow(new Object[]{
                    row.id(), row.code(), row.name(), row.benefitType(), row.kind(),
                    row.defaultRate(), row.minRate(), row.maxRate(),
                    row.vatExempt() ? "Yes" : "No", row.allowManualOverride() ? "Yes" : "No", row.legalBasis(),
                    row.effectiveFrom(), row.effectiveTo(), row.active() ? "Yes" : "No",
                    row.createdAt() == null ? null : TS_FORMAT.format(row.createdAt())
                });
                if (++count >= limit) {
                    break;
                }
            }
//            UiSupport.hideColumn(table, 0);
        }

        @Override
        List<String> suggestions() {
            Set<String> values = new LinkedHashSet<>();
            for (BenefitPolicyRow row : allRows) {
                add(values, row.code());
                add(values, row.name());
                add(values, row.benefitType());
                add(values, row.kind());
                add(values, row.legalBasis());
            }
            return new ArrayList<>(values);
        }

        private void onAdd() {
            BenefitPolicyForm form = new BenefitPolicyForm(SwingUtilities.getWindowAncestor(this), null);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                benefitPoliciesService.create(session.userId(),
                        form.txtCode.getText(), form.txtName.getText(),
                        (String) form.cboBenefitType.getSelectedItem(),
                        (String) form.cboKind.getSelectedItem(),
                        form.defaultRate(), form.minRate(), form.maxRate(),
                        form.chkVatExempt.isSelected(), form.chkAllowManualOverride.isSelected(),
                        form.txtLegalBasis.getText(), UiSupport.sqlDateOrNull(form.dcEffectiveFrom), UiSupport.sqlDateOrNull(form.dcEffectiveTo),
                        form.chkActive.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onEdit() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a benefit policy first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            BenefitPolicyRow current = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (current == null) {
                return;
            }
            BenefitPolicyForm form = new BenefitPolicyForm(SwingUtilities.getWindowAncestor(this), current);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                benefitPoliciesService.update(session.userId(), id,
                        form.txtCode.getText(), form.txtName.getText(),
                        (String) form.cboBenefitType.getSelectedItem(),
                        (String) form.cboKind.getSelectedItem(),
                        form.defaultRate(), form.minRate(), form.maxRate(),
                        form.chkVatExempt.isSelected(), form.chkAllowManualOverride.isSelected(),
                        form.txtLegalBasis.getText(), UiSupport.sqlDateOrNull(form.dcEffectiveFrom), UiSupport.sqlDateOrNull(form.dcEffectiveTo),
                        form.chkActive.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onDelete() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a benefit policy first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            if (JOptionPane.showConfirmDialog(this, "Delete selected benefit policy?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                benefitPoliciesService.delete(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void add(Set<String> values, String value) {
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }

        private static final class BenefitPolicyForm extends JDialog {

            final JTextField txtCode = new JTextField(20);
            final JTextField txtName = new JTextField(20);
            final JComboBox<String> cboBenefitType = new JComboBox<>(new String[]{"SENIOR", "PWD"});
            final JComboBox<String> cboKind = new JComboBox<>(new String[]{"PERCENT"});
            final JTextField txtDefaultRate = new JTextField(20);
            final JTextField txtMinRate = new JTextField(20);
            final JTextField txtMaxRate = new JTextField(20);
            final JCheckBox chkVatExempt = new JCheckBox("VAT Exempt", true);
            final JCheckBox chkAllowManualOverride = new JCheckBox("Allow Manual Override", false);
            final JTextField txtLegalBasis = new JTextField(20);
            final JDateChooser dcEffectiveFrom = new JDateChooser();
            final JDateChooser dcEffectiveTo = new JDateChooser();
            final JCheckBox chkActive = new JCheckBox("Active", true);
            boolean saved;

            Color color = Color.WHITE;
            Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
            Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
            Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
            Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
            Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
            Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);
            private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
            private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

            BenefitPolicyForm(Window owner, BenefitPolicyRow row) {
                super(owner, row == null ? "Add Benefit Policy" : "Edit Benefit Policy", Dialog.ModalityType.APPLICATION_MODAL);
                JPanel form = new JPanel(new GridBagLayout());
                form.setBorder(new EmptyBorder(12, 12, 12, 12));
                GridBagConstraints gc = new GridBagConstraints();
                gc.insets = new Insets(5, 5, 5, 5);
                gc.anchor = GridBagConstraints.WEST;
                gc.fill = GridBagConstraints.HORIZONTAL;
                gc.weightx = 1;

                dcEffectiveFrom.setDateFormatString("yyyy-MM-dd");
                dcEffectiveTo.setDateFormatString("yyyy-MM-dd");
                txtDefaultRate.setText("0.200000");
                txtMinRate.setText("0.200000");
                txtMaxRate.setText("0.200000");

                int y = 0;
                gc.gridx = 0;
                gc.gridy = y;
                JLabel codel = new JLabel("Code");
                codel.setFont(font14Bold);
                form.add(codel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtCode.setFont(font14Plain);
                txtCode.putClientProperty("JTextField.placeholderText", "Code");
                txtCode.setPreferredSize(new Dimension(250, 30));
                form.add(txtCode, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel namel = new JLabel("Name");
                namel.setFont(font14Bold);
                form.add(namel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));
                form.add(txtName, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel benefitTypel = new JLabel("Benefit Type");
                benefitTypel.setFont(font14Bold);
                form.add(benefitTypel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                cboBenefitType.setFont(font14Plain);
                cboBenefitType.setPreferredSize(new Dimension(250, 30));
                form.add(cboBenefitType, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel kindl = new JLabel("Kind");
                kindl.setFont(font14Bold);
                form.add(kindl, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                cboKind.setFont(font14Plain);
                cboKind.setPreferredSize(new Dimension(250, 30));
                form.add(cboKind, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel defaultRatel = new JLabel("Default Rate");
                defaultRatel.setFont(font14Bold);
                form.add(defaultRatel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtDefaultRate.setFont(font14Plain);
                txtDefaultRate.setPreferredSize(new Dimension(250, 30));
                form.add(txtDefaultRate, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel minRatel = new JLabel("Min Rate");
                minRatel.setFont(font14Bold);
                form.add(minRatel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtMinRate.setFont(font14Plain);
                txtMinRate.setPreferredSize(new Dimension(250, 30));
                form.add(txtMinRate, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel maxRatel = new JLabel("Max Rate");
                maxRatel.setFont(font14Bold);
                form.add(maxRatel, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtMaxRate.setFont(font14Plain);
                txtMaxRate.setPreferredSize(new Dimension(250, 30));
                form.add(txtMaxRate, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel legalBasisl = new JLabel("Legal Basis");
                legalBasisl.setFont(font14Bold);
                form.add(legalBasisl, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                txtLegalBasis.setFont(font14Plain);
                txtLegalBasis.setPreferredSize(new Dimension(250, 30));
                txtLegalBasis.putClientProperty("JTextField.placeholderText", "Legal Basis");
                form.add(txtLegalBasis, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel effectiveFroml = new JLabel("Effective From");
                effectiveFroml.setFont(font14Bold);
                form.add(effectiveFroml, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                dcEffectiveFrom.setFont(font14Plain);
                dcEffectiveFrom.setPreferredSize(new Dimension(250, 30));
                form.add(dcEffectiveFrom, gc);
                gc.gridx = 0;
                gc.gridy = y;
                JLabel effectiveTol = new JLabel("Effective To");
                effectiveTol.setFont(font14Bold);
                form.add(effectiveTol, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                dcEffectiveTo.setFont(font14Plain);
                dcEffectiveTo.setPreferredSize(new Dimension(250, 30));
                form.add(dcEffectiveTo, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                chkVatExempt.setFont(font14Plain);
                chkVatExempt.setPreferredSize(new Dimension(250, 30));
                form.add(chkVatExempt, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                chkAllowManualOverride.setFont(font14Plain);
                form.add(chkAllowManualOverride, gc);
                gc.gridx = 1;
                gc.gridy = y++;
                chkActive.setFont(font14Plain);
                form.add(chkActive, gc);

                StyledButton btnSave = new StyledButton("Save");
                btnSave.setIcon(iconSave);
                StyledButton btnCancel = new StyledButton("Cancel");
                btnCancel.setDanger();
                btnCancel.setIcon(iconClose);
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
                actions.add(btnSave);
                actions.add(btnCancel);

                btnSave.addActionListener(e -> {
                    if (txtCode.getText() == null || txtCode.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Code is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Name is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (dcEffectiveFrom.getDate() == null) {
                        JOptionPane.showMessageDialog(this, "Effective From is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    try {
                        BigDecimal defaultRate = defaultRate();
                        BigDecimal minRate = minRate();
                        BigDecimal maxRate = maxRate();
                        if (defaultRate.compareTo(BigDecimal.ZERO) < 0 || minRate.compareTo(BigDecimal.ZERO) < 0 || maxRate.compareTo(minRate) < 0) {
                            JOptionPane.showMessageDialog(this, "Check the rate values.", "Warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (defaultRate.compareTo(minRate) < 0 || defaultRate.compareTo(maxRate) > 0) {
                            JOptionPane.showMessageDialog(this, "Default rate must be between min rate and max rate.", "Warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Rate values must be valid decimals.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    saved = true;
                    dispose();
                });
                btnCancel.addActionListener(e -> dispose());

                if (row != null) {
                    txtCode.setText(row.code());
                    txtName.setText(row.name());
                    cboBenefitType.setSelectedItem(row.benefitType());
                    cboKind.setSelectedItem(row.kind());
                    txtDefaultRate.setText(row.defaultRate().toPlainString());
                    txtMinRate.setText(row.minRate().toPlainString());
                    txtMaxRate.setText(row.maxRate().toPlainString());
                    chkVatExempt.setSelected(row.vatExempt());
                    chkAllowManualOverride.setSelected(row.allowManualOverride());
                    txtLegalBasis.setText(row.legalBasis());
                    UiSupport.setDate(dcEffectiveFrom, row.effectiveFrom());
                    UiSupport.setDate(dcEffectiveTo, row.effectiveTo());
                    chkActive.setSelected(row.active());
                }

                setLayout(new BorderLayout(0, 10));
                add(form, BorderLayout.CENTER);
                add(actions, BorderLayout.SOUTH);
                pack();
                setSize(520, 600);
                setLocationRelativeTo(owner);
            }

            BigDecimal defaultRate() {
                return new BigDecimal(txtDefaultRate.getText().trim());
            }

            BigDecimal minRate() {
                return new BigDecimal(txtMinRate.getText().trim());
            }

            BigDecimal maxRate() {
                return new BigDecimal(txtMaxRate.getText().trim());
            }
        }
    }

}
