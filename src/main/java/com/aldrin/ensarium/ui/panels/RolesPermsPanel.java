package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.PermissionRow;
import com.aldrin.ensarium.model.RoleRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.RoleService;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;
import com.aldrin.ensarium.util.TableStyleSupport;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RolesPermsPanel extends JPanel {

    private final Session session;
    private final RoleService roleService = new RoleService();

    private final DefaultTableModel roleModel = new DefaultTableModel(new Object[]{"ID", "Role", "Description"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable roleTable = new JTable(roleModel);


    private final DefaultTableModel permModel = new DefaultTableModel(new Object[]{"Allow", "ID", "Code", "Description"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    };
    private final JTable permTable = new JTable(permModel);

    private final StyledButton btnSavePermissions = new StyledButton("Permissions");
    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10_000, 1));
    private final List<RoleRow> allRoles = new ArrayList<>();

    Color color = new Color(0x6E6E6E);
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, Color.WHITE);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public RolesPermsPanel(Session session) {
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Roles & Permissions");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        StyledButton btnAdd = new StyledButton("Add Role");
        StyledButton btnEdit = new StyledButton("Edit Role");
        StyledButton btnDelete = new StyledButton("Delete Role");
        StyledButton btnRefresh = new StyledButton("Refresh");

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel search = new JLabel("Search");
        search.setFont(font14Bold);
        leftTools.add(search);
        txtSearch.setPreferredSize(new Dimension(250, 30));
        txtSearch.setFont(font14Plain);
        leftTools.add(txtSearch);
        JLabel limit = new JLabel("Limit");
        limit.setFont(font14Bold);
        leftTools.add(limit);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        spnLimit.setFont(font14Plain);
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnAdd.setIcon(iconAdd);
        btnEdit.setIcon(iconEdit);
        btnDelete.setIcon(iconDelete);
        btnSavePermissions.setIcon(iconSave);
        btnRefresh.setIcon(iconRefresh);

        rightTools.add(btnAdd);
        rightTools.add(btnEdit);
//        rightTools.add(btnDelete);
        rightTools.add(btnSavePermissions);
        rightTools.add(btnRefresh);

        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.add(leftTools, BorderLayout.WEST);
        actions.add(rightTools, BorderLayout.EAST);

        boolean canWrite = session.has(PermissionCodes.ROLES_PERMS_PAGE) || session.has(PermissionCodes.ROLE_WRITE);
        btnAdd.setEnabled(canWrite);
        btnEdit.setEnabled(canWrite);
        btnDelete.setEnabled(canWrite);
        btnSavePermissions.setEnabled(canWrite);

        roleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        roleTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(roleTable);

        int[] widths = {55, 150, 400};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(roleTable, i, widths[i]);
        }
//        BootstrapTableStyle.hideColumns(roleTable, 0);
        BootstrapTableStyle.setColumnLeft(roleTable, 1);
        BootstrapTableStyle.setColumnLeft(roleTable, 2);
        BootstrapTableStyle.setHeaderLeft(roleTable, 1);
        BootstrapTableStyle.setHeaderLeft(roleTable, 2);
        for (int i = 6; i <= 13; i++) {
            BootstrapTableStyle.setColumnRight(roleTable, i);
        }

        permTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        permTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(permTable);

        permTable.getColumnModel().getColumn(0).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(new String[]{"Yes", "No"}))
        );

        int[] width = {100, 0, 280, 350};
        for (int i = 0; i < width.length; i++) {
            BootstrapTableStyle.setColumnWidth(permTable, i, width[i]);
        }
        BootstrapTableStyle.hideColumns(permTable, 1);
        BootstrapTableStyle.setColumnLeft(permTable, 0);
        BootstrapTableStyle.setColumnLeft(permTable, 2);
        BootstrapTableStyle.setColumnLeft(permTable, 3);
        BootstrapTableStyle.setHeaderLeft(permTable, 0);
        BootstrapTableStyle.setHeaderLeft(permTable, 2);
        BootstrapTableStyle.setHeaderLeft(permTable, 3);
        for (int i = 6; i <= 13; i++) {
            BootstrapTableStyle.setColumnRight(permTable, i);
        }

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(roleTable),
                new JScrollPane(permTable));
        split.setResizeWeight(0.45);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.add(actions, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> onAddRole());
        btnEdit.addActionListener(e -> onEditRole());
        btnDelete.addActionListener(e -> onDeleteRole());
        btnRefresh.addActionListener(e -> refreshRoles());
        btnSavePermissions.addActionListener(e -> onSavePermissions());
        roleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPermissionsForSelectedRole();
            }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyRoleFilter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyRoleFilter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyRoleFilter();
            }
        });
        spnLimit.addChangeListener(e -> applyRoleFilter());
        AutoSuggestSupport.install(txtSearch, this::roleSuggestions);

        refreshRoles();
    }

    public void refreshRoles() {
        allRoles.clear();
        allRoles.addAll(roleService.listRoles());
        applyRoleFilter();
    }

    private void applyRoleFilter() {
        Integer keepRoleId = selectedRoleId();
        roleModel.setRowCount(0);
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        int limit = (Integer) spnLimit.getValue();
        int count = 0;

        for (RoleRow role : allRoles) {
            String hay = (role.name() + " " + nv(role.description())).toLowerCase();
            if (!q.isEmpty() && !hay.contains(q)) {
                continue;
            }
            roleModel.addRow(new Object[]{role.id(), role.name(), role.description()});
            count++;
            if (count >= limit) {
                break;
            }
        }

        if (roleModel.getRowCount() == 0) {
            permModel.setRowCount(0);
            return;
        }

        int rowToSelect = 0;
        if (keepRoleId != null) {
            for (int i = 0; i < roleModel.getRowCount(); i++) {
                if (keepRoleId.equals(roleModel.getValueAt(i, 0))) {
                    rowToSelect = i;
                    break;
                }
            }
        }
        roleTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        loadPermissionsForSelectedRole();
    }

    private List<String> roleSuggestions() {
        Set<String> out = new LinkedHashSet<>();
        for (RoleRow role : allRoles) {
            out.add(role.name());
            out.add(role.description());
        }
        return new ArrayList<>(out);
    }

    private String nv(String s) {
        return s == null ? "" : s;
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

    private Integer selectedRoleId() {
        int row = roleTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = roleTable.convertRowIndexToModel(row);
        return (Integer) roleModel.getValueAt(modelRow, 0);
    }

    private String selectedRoleName() {
        int row = roleTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = roleTable.convertRowIndexToModel(row);
        return String.valueOf(roleModel.getValueAt(modelRow, 1));
    }

    private void loadPermissionsForSelectedRole() {
        Integer roleId = selectedRoleId();
        permModel.setRowCount(0);
        if (roleId == null) {
            return;
        }
        List<PermissionRow> permissions = roleService.listPermissionsForRole(roleId);
        for (PermissionRow p : permissions) {
//            permModel.addRow(new Object[]{p.assigned(), p.id(), p.code(), p.description()});

            permModel.addRow(new Object[]{yesNo(p.assigned()), p.id(), p.code(), p.description()});
        }
    }

    private void onAddRole() {
        RoleDialog dialog = new RoleDialog(SwingUtilities.getWindowAncestor(this), "Add Role", null, null);
        dialog.setVisible(true);
        if (!dialog.saved) {
            return;
        }
        try {
            int roleId = roleService.createRole(session.userId(), dialog.roleName(), dialog.description());
            roleService.saveRolePermissions(session.userId(), roleId, dialog.permissionIds());
            refreshRoles();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to create role.", (Exception) ex);
        }
    }

    private void onEditRole() {
        Integer roleId = selectedRoleId();
        if (roleId == null) {
            SwingUtils.info(this, "Select a role first.");
            return;
        }
        int modelRow = roleTable.convertRowIndexToModel(roleTable.getSelectedRow());
        RoleDialog dialog = new RoleDialog(
                SwingUtilities.getWindowAncestor(this),
                "Edit Role",
                new RoleRow(roleId, selectedRoleName(), (String) roleModel.getValueAt(modelRow, 2)),
                roleService.listPermissionsForRole(roleId)
        );
        dialog.setVisible(true);
        if (!dialog.saved) {
            return;
        }
        try {
            roleService.updateRole(session.userId(), roleId, dialog.roleName(), dialog.description());
            roleService.saveRolePermissions(session.userId(), roleId, dialog.permissionIds());
            refreshRoles();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to update role.", (Exception) ex);
        }
    }

    private void onDeleteRole() {
        Integer roleId = selectedRoleId();
        if (roleId == null) {
            SwingUtils.info(this, "Select a role first.");
            return;
        }
        if (!SwingUtils.confirm(this, "Delete selected role?")) {
            return;
        }
        try {
            roleService.deleteRole(session.userId(), roleId);
            refreshRoles();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to delete role. Delete or reassign users first if the role is in use.", (Exception) ex);
        }
    }

    private void onSavePermissions() {
        Integer roleId = selectedRoleId();
        if (roleId == null) {
            SwingUtils.info(this, "Select a role first.");
            return;
        }
        Set<Integer> permissionIds = new HashSet<>();
        for (int i = 0; i < permModel.getRowCount(); i++) {
//            boolean allowed = Boolean.TRUE.equals(permModel.getValueAt(i, 0));
            boolean allowed = isYes(permModel.getValueAt(i, 0));
            int permId = (Integer) permModel.getValueAt(i, 1);
            if (allowed) {
                permissionIds.add(permId);
            }
        }
        try {
            roleService.saveRolePermissions(session.userId(), roleId, permissionIds);
            SwingUtils.info(this, "Permissions updated.");
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to save permissions.", (Exception) ex);
        }
    }

    private static class RoleDialog extends JDialog {

        private final JTextField txtName = new JTextField(20);
        private final JTextField txtDescription = new JTextField(20);

        private final DefaultTableModel permissionsModel = new DefaultTableModel(new Object[]{"Allow", "ID", "Code", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        private final JTable permissionsTable = new JTable(permissionsModel);
        boolean saved;

        RoleDialog(Window owner, String title, RoleRow role, List<PermissionRow> permissions) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            if (role != null) {
                txtName.setText(role.name());
                txtDescription.setText(role.description());
            }
            if (permissions == null) {
                permissions = new RoleService().listPermissionsForRole(-1);
            }
            for (PermissionRow p : permissions) {
                permissionsModel.addRow(new Object[]{p.assigned(), p.id(), p.code(), p.description()});
            }
//            hideColumn(permissionsTable, 1);
            ////            permissionsTable.setRowHeight(28);
//            permissionsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
//            permissionsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
//            permissionsTable.getColumnModel().getColumn(3).setPreferredWidth(230);
//            TableStyleSupport.apply(permissionsTable);


            permissionsTable.getColumnModel().getColumn(0).setCellEditor(
                    new DefaultCellEditor(new JComboBox<>(new String[]{"Yes", "No"}))
            );

            
            

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(12, 12, 12, 12));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 4, 4, 4);
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.gridx = 0;
            g.gridy = 0;
            form.add(new JLabel("Role Name"), g);
            g.gridx = 1;
            form.add(txtName, g);
            g.gridx = 0;
            g.gridy = 1;
            form.add(new JLabel("Description"), g);
            g.gridx = 1;
            form.add(txtDescription, g);
            g.gridx = 0;
            g.gridy = 2;
            g.gridwidth = 2;
            form.add(new JLabel("Permissions"), g);
            g.gridy = 3;
            g.fill = GridBagConstraints.BOTH;
            g.weighty = 1;
            form.add(new JScrollPane(permissionsTable), g);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCancel = new JButton("Cancel");
            JButton btnSave = new JButton("Save");
            actions.add(btnCancel);
            actions.add(btnSave);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            setSize(520, 480);
            setLocationRelativeTo(owner);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> {
                if (roleName().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Role name is required.");
                    return;
                }
                saved = true;
                dispose();
            });
        }

        String roleName() {
            return txtName.getText().trim();
        }

        String description() {
            return txtDescription.getText().trim();
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

        Set<Integer> permissionIds() {
            Set<Integer> ids = new HashSet<>();
            for (int i = 0; i < permissionsModel.getRowCount(); i++) {
//                if (Boolean.TRUE.equals(permissionsModel.getValueAt(i, 0))) {
//                    ids.add((Integer) permissionsModel.getValueAt(i, 1));
//                }
                if ("Yes".equalsIgnoreCase(String.valueOf(permissionsModel.getValueAt(i, 0)).trim())) {
                    ids.add((Integer) permissionsModel.getValueAt(i, 1));
                }
            }
            return ids;
        }
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private boolean isYes(Object value) {
        return value != null && "Yes".equalsIgnoreCase(String.valueOf(value).trim());
    }
}
