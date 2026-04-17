package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.model.RoleRow;
import com.aldrin.ensarium.model.UserRow;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.UserService;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;
//import com.aldrin.ensarium.util.TableStyleSupport;
//import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UsersPanel extends JPanel {

    private final Session session;
    private final UserService userService = new UserService();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Username", "Full name", "Active", "Roles"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10_000, 1));
    private final List<UserRow> allUsers = new ArrayList<>();

    Color color = new Color(0x6E6E6E);

    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 24, Color.WHITE);
    Icon iconResetPassword = new Icons().key(18, Color.WHITE);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);
    
    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);
    
    private final Icon iconLoadData = FaSwingIcons.icon(FontAwesomeIcon.DOWNLOAD, 24, Color.WHITE);


    public UsersPanel(Session session) {
        this.session = session;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Users");
        
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        StyledButton btnAdd = new StyledButton("Add");
        StyledButton btnEdit = new StyledButton("Edit");
        StyledButton btnDelete = new StyledButton("Delete");
        StyledButton btnReset = new StyledButton("Reset Password");
        StyledButton btnRefresh = new StyledButton("Refresh");

        btnAdd.setIcon(iconAdd);
        btnEdit.setIcon(iconEdit);
        btnDelete.setIcon(iconDelete);
        btnReset.setPreferredSize(new Dimension(120, 30));
        btnReset.setMargin(new Insets(2, 2, 2, 2));
        btnReset.setIcon(iconResetPassword);
        btnRefresh.setIcon(iconRefresh);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel lblSearch = new JLabel("Search");
        lblSearch.setFont(font14Bold);
        leftTools.add(lblSearch); 
        txtSearch.setPreferredSize(new Dimension(250, 30));
        leftTools.add(txtSearch);
        JLabel lblLimit = new JLabel("Limit");
        lblLimit.setFont(font14Bold);
        leftTools.add(lblLimit);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.add(btnAdd);
        rightTools.add(btnEdit);
//        rightTools.add(btnDelete);
        rightTools.add(btnReset);
        rightTools.add(btnRefresh);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);



        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(table);

        int[] widths = {55, 150, 250, 100, 200};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.hideColumns(table, 0);
        BootstrapTableStyle.setColumnRight(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);
        for (int i = 6; i <= 13; i++) {
            BootstrapTableStyle.setColumnRight(table, i);
        }
//        installCustomRenderers();

        JPanel centerWrap = new JPanel(new BorderLayout(0, 10));
        centerWrap.add(toolbar, BorderLayout.NORTH);
        centerWrap.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        boolean canWrite = session.has(PermissionCodes.USERS_PAGE) || session.has(PermissionCodes.USER_WRITE);
        btnAdd.setEnabled(canWrite);
        btnEdit.setEnabled(canWrite);
        btnDelete.setEnabled(canWrite);
        btnReset.setEnabled(canWrite);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnReset.addActionListener(e -> onResetPassword());
        btnRefresh.addActionListener(e -> refreshTable());

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

        AutoSuggestSupport.install(txtSearch, this::userSuggestions);
        refreshTable();
    }

    public void refreshTable() {
        allUsers.clear();
        allUsers.addAll(userService.listUsers());
        applyFilter();
    }

    private void applyFilter() {
        model.setRowCount(0);
        txtSearch.setFont(font14Plain);
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        int limit = (Integer) spnLimit.getValue();
        int count = 0;

        for (UserRow row : allUsers) {
            String hay = (row.username() + " " + nv(row.fullName()) + " " + nv(row.roles()) + " " + (row.active() ? "yes active" : "no inactive")).toLowerCase();
            if (!q.isEmpty() && !hay.contains(q)) {
                continue;
            }
            model.addRow(new Object[]{row.id(), row.username(), row.fullName(), row.active() ? "Yes" : "No", row.roles()});
            count++;
            if (count >= limit) {
                break;
            }
        }
    }

    private List<String> userSuggestions() {
        Set<String> out = new LinkedHashSet<>();
        for (UserRow row : allUsers) {
            out.add(row.username());
            out.add(row.fullName());
            if (row.roles() != null) {
                for (String role : row.roles().split(",")) {
                    out.add(role.trim());
                }
            }
            out.add(row.active() ? "Active" : "Inactive");
        }
        return new ArrayList<>(out);
    }

    private String nv(String s) {
        return s == null ? "" : s;
    }



    private Integer selectedUserId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (Integer) model.getValueAt(modelRow, 0);
    }

    private String selectedUsername() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return String.valueOf(model.getValueAt(modelRow, 1));
    }

    private void onAdd() {
        UserDialog dialog = new UserDialog(
                SwingUtilities.getWindowAncestor(this),
                "Add User",
                userService.listRoles(),
                null,
                true
        );
        dialog.setVisible(true);
        if (!dialog.saved) {
            return;
        }
        try {
            userService.createUser(
                    session.userId(),
                    dialog.username(),
                    dialog.password(),
                    dialog.fullName(),
                    dialog.active(),
                    dialog.roleIds(),
                    dialog.photoBytes()
            );
            refreshTable();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to create user.", (Exception) ex);
        }
    }

    private void onEdit() {
        Integer userId = selectedUserId();
        if (userId == null) {
            SwingUtils.info(this, "Select a user first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());

        try {
            UserDialog.Data data = new UserDialog.Data(
                    (String) model.getValueAt(modelRow, 1),
                    (String) model.getValueAt(modelRow, 2),
                    "Yes".equals(model.getValueAt(modelRow, 3)),
                    userService.roleIdsForUser(userId),
                    userService.getUserPhoto(userId)
            );

            UserDialog dialog = new UserDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Edit User",
                    userService.listRoles(),
                    data,
                    false
            );
            dialog.setVisible(true);
            if (!dialog.saved) {
                return;
            }

            userService.updateUser(
                    session.userId(),
                    userId,
                    dialog.username(),
                    dialog.fullName(),
                    dialog.active(),
                    dialog.roleIds(),
                    dialog.photoBytes()
            );
            refreshTable();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to update user.", (Exception) ex);
        }
    }

    private void onDelete() {
        Integer userId = selectedUserId();
        if (userId == null) {
            SwingUtils.info(this, "Select a user first.");
            return;
        }
        if (userId == session.userId()) {
            SwingUtils.info(this, "You cannot delete the current logged-in account.");
            return;
        }
        if (!SwingUtils.confirm(this, "Delete selected user?")) {
            return;
        }
        try {
            userService.deleteUser(session.userId(), userId);
            refreshTable();
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to delete user.", (Exception) ex);
        }
    }

    private void onResetPassword() {
        Integer userId = selectedUserId();
        if (userId == null) {
            SwingUtils.info(this, "Select a user first.");
            return;
        }
        PasswordDialog dialog = new PasswordDialog(SwingUtilities.getWindowAncestor(this), selectedUsername());
        dialog.setVisible(true);
        if (!dialog.saved) {
            return;
        }
        try {
            userService.resetPassword(session.userId(), userId, dialog.password());
            SwingUtils.info(this, "Password reset completed.");
        } catch (Exception ex) {
            SwingUtils.error(this, "Failed to reset password.", (Exception) ex);
        }
    }

    private static class PasswordDialog extends JDialog {

        private final JPasswordField txtPassword = new JPasswordField(20);
        boolean saved;

        PasswordDialog(Window owner, String username) {
            super(owner, "Reset Password - " + username, ModalityType.APPLICATION_MODAL);
            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(new EmptyBorder(12, 12, 12, 12));
            JPanel form = new JPanel(new GridLayout(2, 1, 0, 8));
            form.add(new JLabel("New Password"));
            form.add(txtPassword);
            root.add(form, BorderLayout.CENTER);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            actions.add(btnCancel);
            actions.add(btnSave);
            root.add(actions, BorderLayout.SOUTH);
            setContentPane(root);
            pack();
            setLocationRelativeTo(owner);
            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> {
                if (password().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Password is required.");
                    return;
                }
                saved = true;
                dispose();
            });
        }

        String password() {
            return new String(txtPassword.getPassword());
        }
    }


    private static class UserDialog extends JDialog {

        private final JTextField txtUsername = new JTextField(20);
        private final JTextField txtFullName = new JTextField(20);
        private final JCheckBox chkActive = new JCheckBox("Active", true);
        private final JPasswordField txtPassword = new JPasswordField(20);
        private final JList<RoleRow> roleList;

        private final JLabel lblPhotoPreview = new JLabel("None", SwingConstants.CENTER);
        private byte[] photoBytes;

        boolean saved;
        private final boolean createMode;

        record Data(String username, String fullName, boolean active, Set<Integer> roleIds, byte[] photoBytes) {

        }

        UserDialog(Window owner, String title, List<RoleRow> roles, Data data, boolean createMode) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            this.createMode = createMode;
            this.roleList = new JList<>(roles.toArray(RoleRow[]::new));

            roleList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            roleList.setVisibleRowCount(8);
            roleList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof RoleRow r) {
                        setText(r.name());
                    }
                    return c;
                }
            });

            lblPhotoPreview.setPreferredSize(new Dimension(100, 100));
            lblPhotoPreview.setMinimumSize(new Dimension(100, 100));
            lblPhotoPreview.setMaximumSize(new Dimension(100, 100));
            lblPhotoPreview.setOpaque(true);
            lblPhotoPreview.setBackground(Color.WHITE);
            lblPhotoPreview.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
            lblPhotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblPhotoPreview.setVerticalAlignment(SwingConstants.CENTER);

            JButton btnBrowsePhoto = new JButton("Browse...");
            JButton btnClearPhoto = new JButton("Clear");

            btnBrowsePhoto.addActionListener(e -> browsePhoto());
            btnClearPhoto.addActionListener(e -> {
                photoBytes = null;
                updatePhotoPreview();
            });

            JPanel photoButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            photoButtonPanel.add(btnBrowsePhoto);
            photoButtonPanel.add(btnClearPhoto);

            JPanel previewWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            previewWrap.setOpaque(false);
            previewWrap.add(lblPhotoPreview);

            JPanel photoPanel = new JPanel(new BorderLayout(0, 6));
            photoPanel.setOpaque(false);
            photoPanel.add(previewWrap, BorderLayout.NORTH);
            photoPanel.add(photoButtonPanel, BorderLayout.SOUTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(12, 12, 12, 12));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 4, 4, 4);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;

            int y = 0;
            addRow(form, g, y++, "Username", txtUsername);
            if (createMode) {
                addRow(form, g, y++, "Password", txtPassword);
            }
            addRow(form, g, y++, "Full Name", txtFullName);
            addRow(form, g, y++, "Photo", photoPanel);

            g.gridx = 0;
            g.gridy = y;
            g.gridwidth = 2;
            form.add(chkActive, g);
            y++;

            g.gridx = 0;
            g.gridy = y;
            g.gridwidth = 2;
            form.add(new JLabel("Roles"), g);
            y++;

            g.gridx = 0;
            g.gridy = y;
            g.gridwidth = 2;
            g.weighty = 1;
            g.fill = GridBagConstraints.BOTH;
            form.add(new JScrollPane(roleList), g);

            if (data != null) {
                txtUsername.setText(data.username());
                txtFullName.setText(data.fullName());
                chkActive.setSelected(data.active());

                this.photoBytes = data.photoBytes() != null ? data.photoBytes().clone() : null;
                updatePhotoPreview();

                java.util.List<Integer> ids = roles.stream().map(RoleRow::id).toList();
                java.util.List<Integer> selectedIndexes = new java.util.ArrayList<>();
                for (Integer rid : data.roleIds()) {
                    int idx = ids.indexOf(rid);
                    if (idx >= 0) {
                        selectedIndexes.add(idx);
                    }
                }
                roleList.setSelectedIndices(selectedIndexes.stream().mapToInt(Integer::intValue).toArray());
            } else {
                updatePhotoPreview();
            }
            Color color = new Color(0x6E6E6E);
            Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 18, color);
            Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 18, color);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCancel = new JButton("Cancel");
            JButton btnSave = new JButton("Save");
            btnSave.setIcon(iconSave);
            btnCancel.setIcon(iconClose);
            actions.add(btnCancel);
            actions.add(btnSave);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            setSize(460, 560);
            setLocationRelativeTo(owner);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> {
                if (username().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Username is required.");
                    return;
                }
                if (createMode && password().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Password is required.");
                    return;
                }
                saved = true;
                dispose();
            });
        }

        private void browsePhoto() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select User Photo");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Image Files (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                    "png", "jpg", "jpeg", "gif", "bmp"
            ));

            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) {
                    JOptionPane.showMessageDialog(this, "Selected file is not a valid image.");
                    return;
                }

                photoBytes = Files.readAllBytes(file.toPath());
                updatePhotoPreview();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
            }
        }

        private void updatePhotoPreview() {
            if (photoBytes == null || photoBytes.length == 0) {
                lblPhotoPreview.setIcon(null);
                lblPhotoPreview.setText("None");
                return;
            }

            try {
                BufferedImage src = ImageIO.read(new ByteArrayInputStream(photoBytes));
                if (src == null) {
                    lblPhotoPreview.setIcon(null);
                    lblPhotoPreview.setText("None");
                    return;
                }

                BufferedImage scaled = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, 100, 100);

                    double scale = Math.min(100.0 / src.getWidth(), 100.0 / src.getHeight());
                    int drawW = (int) Math.round(src.getWidth() * scale);
                    int drawH = (int) Math.round(src.getHeight() * scale);
                    int x = (100 - drawW) / 2;
                    int y = (100 - drawH) / 2;

                    g2.drawImage(src, x, y, drawW, drawH, null);
                } finally {
                    g2.dispose();
                }

                lblPhotoPreview.setText(null);
                lblPhotoPreview.setIcon(new ImageIcon(scaled));
            } catch (IOException ex) {
                lblPhotoPreview.setIcon(null);
                lblPhotoPreview.setText("None");
            }
        }

        private void addRow(JPanel form, GridBagConstraints g, int y, String label, JComponent input) {
            g.gridx = 0;
            g.gridy = y;
            g.gridwidth = 1;
            g.weighty = 0;
            g.fill = GridBagConstraints.HORIZONTAL;
            form.add(new JLabel(label), g);

            g.gridx = 1;
            form.add(input, g);
        }

        String username() {
            return txtUsername.getText().trim();
        }

        String password() {
            return new String(txtPassword.getPassword());
        }

        String fullName() {
            return txtFullName.getText().trim();
        }

        boolean active() {
            return chkActive.isSelected();
        }

        Set<Integer> roleIds() {
            Set<Integer> ids = new LinkedHashSet<>();
            for (RoleRow role : roleList.getSelectedValuesList()) {
                ids.add(role.id());
            }
            return ids;
        }

        byte[] photoBytes() {
            return photoBytes;
        }
    }

    public byte[] getUserPhoto(int userId) throws Exception {
        String sql = "SELECT photo FROM users WHERE id = ?";

        try (java.sql.Connection con = com.aldrin.ensarium.db.Db.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("photo");
                }
            }
        }
        return null;
    }

}
