package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.model.SupplierRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.SupplierAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.TableStyleSupport;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SuppliersPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a");

    private final Session session;
    private final SupplierAdminService service = new SupplierAdminService();
    private final boolean canWrite;

    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
        "ID", "Supplier no", "Name", "Phone", "Email", "Address", "Active", "Created at"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 ->
                    Long.class;
                default ->
                    Object.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final List<SupplierRow> allRows = new ArrayList<>();

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    private final List<SupplierRow> visibleRows = new ArrayList<>();

    public SuppliersPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.SUPPLIER);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Suppliers");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

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
        spnLimit.setFont(font14Plain);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.add(btnAdd);
        rightTools.add(btnEdit);
//        rightTools.add(btnDelete);
        rightTools.add(btnRefresh);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(table);

        int[] widths = {55, 150, 200, 150, 200, 200, 100, 180};
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

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> refreshAll());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.add(toolbar, BorderLayout.NORTH);
        body.add(new JScrollPane(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        try {
            allRows.clear();
            allRows.addAll(service.listSuppliers());
            applyFilter();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        model.setRowCount(0);
        visibleRows.clear();

        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        int limit = (Integer) spnLimit.getValue();
        int count = 0;

        for (SupplierRow row : allRows) {
            String haystack = String.join(" ",
                    nz(row.supplierNo()), nz(row.name()), nz(row.phone()), nz(row.email()), nz(row.address()))
                    .toLowerCase();

            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }

            visibleRows.add(row);

            model.addRow(new Object[]{
                row.id(),
                row.supplierNo(),
                row.name(),
                row.phone(),
                row.email(),
                row.address(),
                row.active() ? "Yes" : "No",
                row.createdAt() == null ? null : TS_FORMAT.format(row.createdAt())
            });

            if (++count >= limit) {
                break;
            }
        }
    }

    private List<String> suggestions() {
        Set<String> values = new LinkedHashSet<>();
        for (SupplierRow row : allRows) {
            add(values, row.supplierNo());
            add(values, row.name());
            add(values, row.phone());
            add(values, row.email());
            add(values, row.address());
        }
        return new ArrayList<>(values);
    }

    private void onAdd() {
        SupplierForm form = new SupplierForm(SwingUtilities.getWindowAncestor(this), null);
        form.setVisible(true);
        if (!form.saved) {
            return;
        }
        try {
            service.createSupplier(session.userId(), form.txtSupplierNo.getText(), form.txtName.getText(), form.txtPhone.getText(), form.txtEmail.getText(), form.txtAddress.getText(), form.chkActive.isSelected());
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id;
        try {
            id = selectedSupplierId();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to read selected supplier ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SupplierRow current = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Selected supplier not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SupplierForm form = new SupplierForm(SwingUtilities.getWindowAncestor(this), current);
        form.setVisible(true);
        if (!form.saved) {
            return;
        }

        try {
            service.updateSupplier(
                    session.userId(),
                    id,
                    form.txtSupplierNo.getText(),
                    form.txtName.getText(),
                    form.txtPhone.getText(),
                    form.txtEmail.getText(),
                    form.txtAddress.getText(),
                    form.chkActive.isSelected()
            );
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    private void onDelete() {
//        int row = table.getSelectedRow();
//        if (row < 0) {
//            JOptionPane.showMessageDialog(this, "Select a supplier first.", "Warning", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//        long id = ((Number) table.getValueAt(row, 0)).longValue();
//        if (JOptionPane.showConfirmDialog(this, "Delete selected supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
//            return;
//        }
//        try {
//            service.deleteSupplier(session.userId(), id);
//            refreshAll();
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id;
        try {
            id = selectedSupplierId();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to read selected supplier ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Delete selected supplier?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteSupplier(session.userId(), id);
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

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private final class SupplierForm extends JDialog {

        final JTextField txtSupplierNo = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        final JTextField txtPhone = new JTextField(20);
        final JTextField txtEmail = new JTextField(20);
        final JTextField txtAddress = new JTextField(20);
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;
        Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
        Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

        SupplierForm(Window owner, SupplierRow row) {
            super(owner, row == null ? "Add Supplier" : "Edit Supplier", ModalityType.APPLICATION_MODAL);
            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(12, 12, 12, 12));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5, 5, 5, 5);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            gc.gridx = 0;
            gc.gridy = y;
            JLabel supplierNol = new JLabel("Supplier No");
            supplierNol.setFont(font14Bold);
            form.add(supplierNol, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            txtSupplierNo.setFont(font14Plain);
            txtSupplierNo.putClientProperty("JTextField.placeholderText", "Supplier No");
            txtSupplierNo.setPreferredSize(new Dimension(250, 30));
            form.add(txtSupplierNo, gc);
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
            JLabel phonel = new JLabel("Phone");
            phonel.setFont(font14Bold);
            form.add(phonel, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            txtPhone.setFont(font14Plain);
            txtPhone.putClientProperty("JTextField.placeholderText", "Phone");
            txtPhone.setPreferredSize(new Dimension(250, 30));
            form.add(txtPhone, gc);
            gc.gridx = 0;
            gc.gridy = y;
            JLabel emaill = new JLabel("Email");
            emaill.setFont(font14Bold);
            form.add(emaill, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            txtEmail.setFont(font14Plain);
            txtEmail.putClientProperty("JTextField.placeholderText", "Email");
            txtEmail.setPreferredSize(new Dimension(250, 30));
            form.add(txtEmail, gc);
            gc.gridx = 0;
            gc.gridy = y;
            JLabel addressl = new JLabel("Address");
            addressl.setFont(font14Bold);
            form.add(addressl, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            txtAddress.setFont(font14Plain);
            txtAddress.putClientProperty("JTextField.placeholderText", "Address");
            txtAddress.setPreferredSize(new Dimension(250, 30));
            form.add(txtAddress, gc);
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
                if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name is required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                saved = true;
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());

            if (row != null) {
                txtSupplierNo.setText(row.supplierNo());
                txtName.setText(row.name());
                txtPhone.setText(row.phone());
                txtEmail.setText(row.email());
                txtAddress.setText(row.address());
                chkActive.setSelected(row.active());
            }

            setLayout(new BorderLayout(0, 10));
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setSize(Math.max(getWidth(), 360), getHeight());
            setLocationRelativeTo(owner);
        }
    }

    private long selectedSupplierId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            throw new IllegalStateException("Select a supplier first.");
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = model.getValueAt(modelRow, 0);

        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

}
