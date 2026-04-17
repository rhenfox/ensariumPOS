package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.model.DiscountTypeRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.DiscountTypeAdminService;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.TableStyleSupport;
import com.aldrin.ensarium.util.UiSupport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DiscountTypesPanel extends JPanel {
    private final Session session;
    private final DiscountTypeAdminService service = new DiscountTypeAdminService();
    private final boolean canWrite;

    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Code", "Name", "Kind", "Applies To", "Active"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 5 ? Boolean.class : Object.class; }
    };
    private final JTable table = new JTable(model);
    private final List<DiscountTypeRow> allRows = new ArrayList<>();

    public DiscountTypesPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.DISCOUNT_TYPE);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Discount Types");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        btnAdd.setEnabled(canWrite);
        btnEdit.setEnabled(canWrite);
        btnDelete.setEnabled(canWrite);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        spnLimit.addChangeListener(e -> applyFilter());
        AutoSuggestSupport.install(txtSearch, this::suggestions);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.add(new JLabel("Search"));
        leftTools.add(txtSearch);
        leftTools.add(new JLabel("Limit"));
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.add(btnAdd);
        rightTools.add(btnEdit);
        rightTools.add(btnDelete);
        rightTools.add(btnRefresh);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.setAutoCreateRowSorter(true);
        TableStyleSupport.apply(table);
        UiSupport.hideColumn(table, 0);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> refreshAll());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.add(toolbar, BorderLayout.NORTH);
        body.add(new RoundedScrollPane(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        try {
            allRows.clear();
            allRows.addAll(service.listDiscountTypes());
            applyFilter();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        model.setRowCount(0);
        String q = UiSupport.nz(txtSearch.getText()).trim().toLowerCase();
        int limit = (Integer) spnLimit.getValue();
        int count = 0;
        for (DiscountTypeRow row : allRows) {
            String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name()) + " " + UiSupport.nz(row.kind()) + " " + UiSupport.nz(row.appliesTo())).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) continue;
            model.addRow(new Object[]{row.id(), row.code(), row.name(), row.kind(), row.appliesTo(), row.active()});
            if (++count >= limit) break;
        }
        UiSupport.hideColumn(table, 0);
    }

    private List<String> suggestions() {
        Set<String> values = new LinkedHashSet<>();
        for (DiscountTypeRow row : allRows) {
            if (row.code() != null && !row.code().isBlank()) values.add(row.code());
            if (row.name() != null && !row.name().isBlank()) values.add(row.name());
            if (row.kind() != null && !row.kind().isBlank()) values.add(row.kind());
            if (row.appliesTo() != null && !row.appliesTo().isBlank()) values.add(row.appliesTo());
        }
        return new ArrayList<>(values);
    }

    private void onAdd() {
        DiscountTypeForm form = new DiscountTypeForm(SwingUtilities.getWindowAncestor(this), null);
        form.setVisible(true);
        if (!form.saved) return;
        try {
            service.create(session.userId(), form.txtCode.getText(), form.txtName.getText(),
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
        if (current == null) return;
        DiscountTypeForm form = new DiscountTypeForm(SwingUtilities.getWindowAncestor(this), current);
        form.setVisible(true);
        if (!form.saved) return;
        try {
            service.update(session.userId(), id, form.txtCode.getText(), form.txtName.getText(),
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
            service.delete(session.userId(), id);
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

        DiscountTypeForm(Window owner, DiscountTypeRow row) {
            super(owner, row == null ? "Add Discount Type" : "Edit Discount Type", ModalityType.APPLICATION_MODAL);
            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(12, 12, 12, 12));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5, 5, 5, 5);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Code"), gc);
            gc.gridx = 1; gc.gridy = y++; form.add(txtCode, gc);
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Name"), gc);
            gc.gridx = 1; gc.gridy = y++; form.add(txtName, gc);
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Kind"), gc);
            gc.gridx = 1; gc.gridy = y++; form.add(cboKind, gc);
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Applies To"), gc);
            gc.gridx = 1; gc.gridy = y++; form.add(cboAppliesTo, gc);
            gc.gridx = 1; gc.gridy = y++; form.add(chkActive, gc);

            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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
            setSize(Math.max(getWidth(), 460), getHeight());
            setLocationRelativeTo(owner);
        }
    }
}
