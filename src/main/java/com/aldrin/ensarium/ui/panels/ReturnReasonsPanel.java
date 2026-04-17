package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.model.ReturnReasonRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.ReturnReasonAdminService;
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

public class ReturnReasonsPanel extends JPanel {
    private final Session session;
    private final ReturnReasonAdminService service = new ReturnReasonAdminService();
    private final boolean canWrite;

    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Code", "Name"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final List<ReturnReasonRow> allRows = new ArrayList<>();

    public ReturnReasonsPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.RETURN_REASON);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Return Reasons");
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

        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
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
            allRows.addAll(service.listReturnReasons());
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
        for (ReturnReasonRow row : allRows) {
            String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name())).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) continue;
            model.addRow(new Object[]{row.id(), row.code(), row.name()});
            if (++count >= limit) break;
        }
        UiSupport.hideColumn(table, 0);
    }

    private List<String> suggestions() {
        Set<String> values = new LinkedHashSet<>();
        for (ReturnReasonRow row : allRows) {
            if (row.code() != null && !row.code().isBlank()) values.add(row.code());
            if (row.name() != null && !row.name().isBlank()) values.add(row.name());
        }
        return new ArrayList<>(values);
    }

    private void onAdd() {
        ReturnReasonForm form = new ReturnReasonForm(SwingUtilities.getWindowAncestor(this), null);
        form.setVisible(true);
        if (!form.saved) return;
        try {
            service.create(session.userId(), form.txtCode.getText(), form.txtName.getText());
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
        if (current == null) return;
        ReturnReasonForm form = new ReturnReasonForm(SwingUtilities.getWindowAncestor(this), current);
        form.setVisible(true);
        if (!form.saved) return;
        try {
            service.update(session.userId(), id, form.txtCode.getText(), form.txtName.getText());
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
            service.delete(session.userId(), id);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class ReturnReasonForm extends JDialog {
        final JTextField txtCode = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        boolean saved;

        ReturnReasonForm(Window owner, ReturnReasonRow row) {
            super(owner, row == null ? "Add Return Reason" : "Edit Return Reason", ModalityType.APPLICATION_MODAL);
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
            }

            setLayout(new BorderLayout(0, 10));
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setSize(Math.max(getWidth(), 420), getHeight());
            setLocationRelativeTo(owner);
        }
    }
}
