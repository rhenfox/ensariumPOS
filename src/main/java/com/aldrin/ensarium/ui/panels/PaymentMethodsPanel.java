package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.PaymentMethodRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.PaymentMethodAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.UiSupport;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PaymentMethodsPanel extends JPanel {

    private final Session session;
    private final PaymentMethodAdminService service = new PaymentMethodAdminService();
    private final boolean canWrite;

    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
        "ID", "Code", "Name", "Active"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final List<PaymentMethodRow> allRows = new ArrayList<>();

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public PaymentMethodsPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.PAYMENT_METHOD);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        txtSearch.putClientProperty("JTextField.placeholderText", "Enter barcode");

        JLabel title = new JLabel("Payment Methods");
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
        txtSearch.setPreferredSize(new Dimension(250, 30));
        txtSearch.setFont(font14Plain);
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
//        rightTools.add(btnDelete);
        rightTools.add(btnRefresh);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(table);

        int[] widths = {55, 150, 150, 100};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.hideColumns(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);

        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
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

        refreshAll();
    }

    public void refreshAll() {
        try {
            allRows.clear();
            allRows.addAll(service.listPaymentMethods());
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
        for (PaymentMethodRow row : allRows) {
            String haystack = (UiSupport.nz(row.code()) + " " + UiSupport.nz(row.name())).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }
            model.addRow(new Object[]{row.id(), row.code(), row.name(), row.active() ? "Yes" : "No"});
            if (++count >= limit) {
                break;
            }
        }
    }

    private List<String> suggestions() {
        Set<String> values = new LinkedHashSet<>();
        for (PaymentMethodRow row : allRows) {
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
        PaymentMethodForm form = new PaymentMethodForm(SwingUtilities.getWindowAncestor(this), null);
        form.setVisible(true);
        if (!form.saved) {
            return;
        }
        try {
            service.create(session.userId(), form.txtCode.getText(), form.txtName.getText(), form.chkActive.isSelected());
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a payment method first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        PaymentMethodRow current = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
        if (current == null) {
            return;
        }
        PaymentMethodForm form = new PaymentMethodForm(SwingUtilities.getWindowAncestor(this), current);
        form.setVisible(true);
        if (!form.saved) {
            return;
        }
        try {
            service.update(session.userId(), id, form.txtCode.getText(), form.txtName.getText(), form.chkActive.isSelected());
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a payment method first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        if (JOptionPane.showConfirmDialog(this, "Delete selected payment method?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.delete(session.userId(), id);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class PaymentMethodForm extends JDialog {

        final JTextField txtCode = new JTextField(20);
        final JTextField txtName = new JTextField(20);
        final JCheckBox chkActive = new JCheckBox("Active", true);
        boolean saved;
        Color color = Color.WHITE;
        Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
        Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
        Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
        Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
        Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, color);
        Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, color);

         final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
         final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

        PaymentMethodForm(Window owner, PaymentMethodRow row) {
            super(owner, row == null ? "Add Payment Method" : "Edit Payment Method", ModalityType.APPLICATION_MODAL);
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
            gc.gridx = 1;
            gc.gridy = y++;
            chkActive.setFont(font14Plain);
            form.add(chkActive, gc);

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
                chkActive.setSelected(row.active());
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
