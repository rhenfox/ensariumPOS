/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.InventoryStatusRow;
import com.aldrin.ensarium.model.ReturnReasonRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.InventoryStatusAdminService;
import com.aldrin.ensarium.service.ReturnReasonAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.TableStyleSupport;
import com.aldrin.ensarium.util.UiSupport;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class InventorySetupPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");

    private final Session session;
//    private final InventoryStatusAdminService service = new InventoryStatusAdminService();
    private final InventoryStatusAdminService inventoryService = new InventoryStatusAdminService();
    private final ReturnReasonAdminService retutnReasonervice = new ReturnReasonAdminService();
    private final boolean canWrite;

    private final InventoryStatusTab inventoryStatusTab;
    private final ReturnReasonsTab returnReasonTab;

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public InventorySetupPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.INVENTORY);
        this.inventoryStatusTab = new InventoryStatusTab();
        this.returnReasonTab = new ReturnReasonsTab();
//        this.storeFiscalTab = new StoreFiscalTab();
//        this.receiptSeriesTab = new ReceiptSeriesTab();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Inventory setup");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Product status", inventoryStatusTab);
        tabs.addTab("Return reason", returnReasonTab);
//        tabs.addTab("Receipt Series", receiptSeriesTab);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);  
        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        inventoryStatusTab.refresh();
        returnReasonTab.refresh();
//        receiptSeriesTab.refresh();
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
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        leftTools.add(searchl);
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
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

    private final class InventoryStatusTab extends BaseTab {

        private final JTextField txtSearch = new JTextField(22);
        private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Code", "Name", "Sellable"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);
        private final List<InventoryStatusRow> allRows = new ArrayList<>();

        InventoryStatusTab() {

            setLayout(new BorderLayout(0, 0));
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
            txtSearch.setPreferredSize(new Dimension(250, 30));
            txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
            leftTools.add(txtSearch);
            leftTools.add(new JLabel("Limit"));
            ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
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

//            table.getColumnModel().getColumn(0).setPreferredWidth(140);
//            table.getColumnModel().getColumn(1).setPreferredWidth(280);
//            table.getColumnModel().getColumn(2).setPreferredWidth(80);
//            table.setAutoCreateRowSorter(true);
//            TableStyleSupport.apply(table);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {150, 150, 150};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
//            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);

            BootstrapTableStyle.setHeaderLeft(table, 0);
            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
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
                allRows.addAll(inventoryService.listInventoryStatuses());
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
            for (InventoryStatusRow row : allRows) {
                String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name())).toLowerCase();
                if (!q.isEmpty() && !haystack.contains(q)) {
                    continue;
                }
                model.addRow(new Object[]{row.code(), row.name(), row.sellable() ? "Yes" : "No"});
                if (++count >= limit) {
                    break;
                }
            }
        }

        @Override
        List<String> suggestions() {
            Set<String> values = new LinkedHashSet<>();
            for (InventoryStatusRow row : allRows) {
                if (row.code() != null && !row.code().isBlank()) {
                    values.add(row.code());
                }
                if (row.name() != null && !row.name().isBlank()) {
                    values.add(row.name());
                }
            }
            return new ArrayList<>(values);
        }

        void onAdd() {
            InventoryStatusForm form = new InventoryStatusForm(SwingUtilities.getWindowAncestor(this), null);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                inventoryService.create(session.userId(), form.txtCode.getText(), form.txtName.getText(), form.chkSellable.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        void onEdit() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select an inventory status first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            String code = String.valueOf(model.getValueAt(row, 0));
            InventoryStatusRow current = allRows.stream().filter(r -> r.code().equals(code)).findFirst().orElse(null);
            if (current == null) {
                return;
            }
            InventorySetupPanel.InventoryStatusForm form = new InventorySetupPanel.InventoryStatusForm(SwingUtilities.getWindowAncestor(this), current);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                inventoryService.update(session.userId(), current.code(), form.txtCode.getText(), form.txtName.getText(), form.chkSellable.isSelected());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        void onDelete() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select an inventory status first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            String code = String.valueOf(model.getValueAt(row, 0));
            if (JOptionPane.showConfirmDialog(this, "Delete selected inventory status?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                inventoryService.delete(session.userId(), code);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static final class InventoryStatusForm extends JDialog {

        final JTextField txtCode = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        final JCheckBox chkSellable = new JCheckBox("Sellable", false);
        boolean saved;

        Color color = Color.WHITE;
        Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
        Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
        Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
        Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
        Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
        Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

        Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
        Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

        InventoryStatusForm(Window owner, InventoryStatusRow row) {
            super(owner, row == null ? "Add Inventory Status" : "Edit Inventory Status", Dialog.ModalityType.APPLICATION_MODAL);
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
            gc.gridx = 1;
            gc.gridy = y++;
            chkSellable.setFont(font14Plain);
            form.add(chkSellable, gc);

            StyledButton btnSave = new StyledButton("Save");
            btnSave.setIcon(iconSave);
            StyledButton btnCancel = new StyledButton("Cancel");
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
                chkSellable.setSelected(row.sellable());
            }

            setLayout(new BorderLayout(0, 10));
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setSize(Math.max(getWidth(), 320), getHeight());
            setLocationRelativeTo(owner);
        }
    }

    private final class ReturnReasonsTab extends BaseTab {

//        private final Session session;
//        private final boolean canWrite;
        private final JTextField txtSearch = new JTextField(22);
        private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Code", "Name"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);
        private final List<ReturnReasonRow> allRows = new ArrayList<>();

        ReturnReasonsTab() {
            setLayout(new BorderLayout(0, 0));
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
            txtSearch.setPreferredSize(new Dimension(250, 30));
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

            int[] widths = {150, 150, 250};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
//            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);

            BootstrapTableStyle.setHeaderLeft(table, 0);
            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
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

//            refreshAll();
            refresh();
        }

        @Override
        void refresh() {
            try {
                allRows.clear();
                allRows.addAll(retutnReasonervice.listReturnReasons());
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
            for (ReturnReasonRow row : allRows) {
                String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name())).toLowerCase();
                if (!q.isEmpty() && !haystack.contains(q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.code(), row.name()});
                if (++count >= limit) {
                    break;
                }
            }
            UiSupport.hideColumn(table, 0);
        }

        @Override
        List<String> suggestions() {
            Set<String> values = new LinkedHashSet<>();
            for (ReturnReasonRow row : allRows) {
                if (row.code() != null && !row.code().isBlank()) {
                    values.add(row.code());
                }
                if (row.name() != null && !row.name().isBlank()) {
                    values.add(row.name());
                }
            }
            return new ArrayList<>(values);
        }

        private void onAdd() {
            ReturnReasonForm form = new ReturnReasonForm(SwingUtilities.getWindowAncestor(this), null);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                retutnReasonervice.create(session.userId(), form.txtCode.getText(), form.txtName.getText());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onEdit() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a return reason first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            ReturnReasonRow current = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (current == null) {
                return;
            }
            ReturnReasonForm form = new ReturnReasonForm(SwingUtilities.getWindowAncestor(this), current);
            form.setVisible(true);
            if (!form.saved) {
                return;
            }
            try {
                retutnReasonervice.update(session.userId(), id, form.txtCode.getText(), form.txtName.getText());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void onDelete() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a return reason first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(row, 0)).intValue();
            if (JOptionPane.showConfirmDialog(this, "Delete selected return reason?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                retutnReasonervice.delete(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static final class ReturnReasonForm extends JDialog {

            final JTextField txtCode = new JTextField(20);
            final JTextField txtName = new JTextField(20);
            boolean saved;

            Color color = Color.WHITE;
            Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
            Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
            Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
            Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
            Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
            Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

            Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
            Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

            ReturnReasonForm(Window owner, ReturnReasonRow row) {
                super(owner, row == null ? "Add Return Reason" : "Edit Return Reason", ModalityType.APPLICATION_MODAL);
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
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));
                form.add(txtName, gc);

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
                    saved = true;
                    dispose();
                });
                btnCancel.addActionListener(e -> dispose());

                if (row != null) {
                    txtCode.setText(row.code());
                    txtName.setText(row.name());
                }

                setLayout(new BorderLayout(0, 10));
                add(form, BorderLayout.CENTER);
                add(actions, BorderLayout.SOUTH);
                pack();
                setSize(Math.max(getWidth(), 320), getHeight());
                setLocationRelativeTo(owner);
            }
        }
    }

}
