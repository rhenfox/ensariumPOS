package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.model.BenefitPolicyRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.BenefitPolicyAdminService;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.TableStyleSupport;
import com.aldrin.ensarium.util.UiSupport;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BenefitPoliciesPanel extends JPanel {

    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a");

    private final Session session;
    private final BenefitPolicyAdminService service = new BenefitPolicyAdminService();
    private final boolean canWrite;

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
                case 5, 6, 7 ->
                    BigDecimal.class;   // numeric columns -> right aligned
                default ->
                    Object.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final List<BenefitPolicyRow> allRows = new ArrayList<>();

    public BenefitPoliciesPanel(Session session) {
        this.session = session;
        this.canWrite = session != null && session.has(PermissionCodes.BENEFIT_POLICY);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Benefit Policies");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
//        btnAdd.setEnabled(canWrite);
//        btnEdit.setEnabled(canWrite);
//        btnDelete.setEnabled(canWrite);
        btnAdd.setEnabled(true);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);

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
        leftTools.add(new JLabel("Searchs"));
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

//        table.setAutoCreateRowSorter(true);
//        TableStyleSupport.apply(table);
//        table.getColumnModel().getColumn(1).setPreferredWidth(90);
//        table.getColumnModel().getColumn(2).setPreferredWidth(220);
//        table.getColumnModel().getColumn(3).setPreferredWidth(100);
//        table.getColumnModel().getColumn(4).setPreferredWidth(90);
//        table.getColumnModel().getColumn(10).setPreferredWidth(180);
//        table.getColumnModel().getColumn(14).setPreferredWidth(160);
//        UiSupport.hideColumn(table, 0);
        table.setAutoCreateRowSorter(true);
        TableStyleSupport.apply(table, 3, 4, 8, 9, 11, 12, 13); // centered columns

        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // Code
        table.getColumnModel().getColumn(2).setPreferredWidth(220);  // Name
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Benefit Type
        table.getColumnModel().getColumn(4).setPreferredWidth(90);   // Kind
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Default Rate
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Min Rate
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Max Rate
        table.getColumnModel().getColumn(8).setPreferredWidth(90);   // VAT Exempt
        table.getColumnModel().getColumn(9).setPreferredWidth(120);  // Manual Override
        table.getColumnModel().getColumn(10).setPreferredWidth(220); // Legal Basis
        table.getColumnModel().getColumn(11).setPreferredWidth(110); // Effective From
        table.getColumnModel().getColumn(12).setPreferredWidth(110); // Effective To
        table.getColumnModel().getColumn(13).setPreferredWidth(80);  // Active
        table.getColumnModel().getColumn(14).setPreferredWidth(160); // Created At

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
            allRows.addAll(service.listBenefitPolicies());
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
        for (BenefitPolicyRow row : allRows) {
            String haystack = String.join(" ",
                    UiSupport.nz(row.code()), UiSupport.nz(row.name()), UiSupport.nz(row.benefitType()),
                    UiSupport.nz(row.kind()), UiSupport.nz(row.legalBasis())).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }
//            model.addRow(new Object[]{
//                row.id(), row.code(), row.name(), row.benefitType(), row.kind(),
//                row.defaultRate(), row.minRate(), row.maxRate(),
//                row.vatExempt(), row.allowManualOverride(), row.legalBasis(),
//                row.effectiveFrom(), row.effectiveTo(), row.active(),
//                row.createdAt() == null ? null : TS_FORMAT.format(row.createdAt())
//            });
            model.addRow(new Object[]{
                row.id(),
                row.code(),
                row.name(),
                row.benefitType(),
                row.kind(),
                row.defaultRate(),
                row.minRate(),
                row.maxRate(),
                row.vatExempt() ? "Yes" : "No",
                row.allowManualOverride() ? "Yes" : "No",
                row.legalBasis(),
                row.effectiveFrom(),
                row.effectiveTo(),
                row.active() ? "Yes" : "No",
                row.createdAt() == null ? null : TS_FORMAT.format(row.createdAt())
            });
            if (++count >= limit) {
                break;
            }
        }
        UiSupport.hideColumn(table, 0);
    }

    private List<String> suggestions() {
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
            service.create(session.userId(),
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
            service.update(session.userId(), id,
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
            service.delete(session.userId(), id);
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

        BenefitPolicyForm(Window owner, BenefitPolicyRow row) {
            super(owner, row == null ? "Add Benefit Policy" : "Edit Benefit Policy", ModalityType.APPLICATION_MODAL);
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
            form.add(new JLabel("Code"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtCode, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Name"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtName, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Benefit Type"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(cboBenefitType, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Kind"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(cboKind, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Default Rate"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtDefaultRate, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Min Rate"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtMinRate, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Max Rate"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtMaxRate, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Legal Basis"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(txtLegalBasis, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Effective From"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(dcEffectiveFrom, gc);
            gc.gridx = 0;
            gc.gridy = y;
            form.add(new JLabel("Effective To"), gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(dcEffectiveTo, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(chkVatExempt, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(chkAllowManualOverride, gc);
            gc.gridx = 1;
            gc.gridy = y++;
            form.add(chkActive, gc);

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
            setSize(Math.max(getWidth(), 520), 520);
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
