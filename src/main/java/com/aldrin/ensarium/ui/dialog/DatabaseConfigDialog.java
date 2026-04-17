package com.aldrin.ensarium.ui.dialog;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.db.DbConfig;
import com.aldrin.ensarium.db.DbConfigStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DatabaseConfigDialog extends JDialog {
    private final JTextField txtHost = new JTextField(20);
    private final JSpinner spnPort = new JSpinner(new SpinnerNumberModel(1527, 1, 65535, 1));
    private final JTextField txtDatabase = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JCheckBox chkCreateIfMissing = new JCheckBox("Create database if missing", true);
    private final JLabel lblStatus = new JLabel(" ");
    private boolean connected;
    private boolean cancelled = true;

    public DatabaseConfigDialog(Window owner) {
        super(owner, "Database Configuration", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onCancel(); }
        });

        Db.loadSavedConfig().ifPresent(cfg -> {
            txtHost.setText(cfg.host());
            spnPort.setValue(cfg.port());
            txtDatabase.setText(cfg.databaseName());
            txtUsername.setText(cfg.username());
            txtPassword.setText(cfg.password());
            chkCreateIfMissing.setSelected(cfg.createIfMissing());
        });
        if (txtHost.getText().isBlank()) txtHost.setText("127.0.0.1");
        if (txtDatabase.getText().isBlank()) txtDatabase.setText("ensariumdb");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(14, 14, 14, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        JLabel info = new JLabel("Connect to the Apache Derby Network Server before showing the login form.");
        info.setFont(info.getFont().deriveFont(Font.PLAIN));
        form.add(info, g); y++;

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Host"), g);
        g.gridx = 1; form.add(txtHost, g); y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Port"), g);
        g.gridx = 1; form.add(spnPort, g); y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Database"), g);
        g.gridx = 1; form.add(txtDatabase, g); y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Username"), g);
        g.gridx = 1; form.add(txtUsername, g); y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("Password"), g);
        g.gridx = 1; form.add(txtPassword, g); y++;
        g.gridx = 1; g.gridy = y; form.add(chkCreateIfMissing, g); y++;
        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        lblStatus.setForeground(new Color(180, 60, 60));
        form.add(lblStatus, g);

        JButton btnCancel = new JButton("Cancel");
        JButton btnConnect = new JButton("Save & Connect");
        JButton btnTest = new JButton("Test Connection");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnTest);
        actions.add(btnCancel);
        actions.add(btnConnect);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        btnTest.addActionListener(e -> testOnly());
        btnConnect.addActionListener(e -> saveAndConnect());
        btnCancel.addActionListener(e -> onCancel());
        getRootPane().setDefaultButton(btnConnect);

        pack();
        setSize(Math.max(getWidth(), 520), getHeight());
        setLocationRelativeTo(owner);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private DbConfig readConfig() {
        return new DbConfig(
                txtHost.getText().trim(),
                ((Number) spnPort.getValue()).intValue(),
                txtDatabase.getText().trim(),
                txtUsername.getText().trim(),
                new String(txtPassword.getPassword()),
                chkCreateIfMissing.isSelected()
        );
    }

    private void testOnly() {
        lblStatus.setText("Testing connection...");
        DbConfig cfg = readConfig();
        if (Db.testConnection(cfg)) {
            lblStatus.setForeground(new Color(38, 125, 67));
            lblStatus.setText("Connection successful.");
        } else {
            lblStatus.setForeground(new Color(180, 60, 60));
            lblStatus.setText("Unable to connect. Check host, port, database, and server status.");
        }
    }

    private void saveAndConnect() {
        DbConfig cfg = readConfig();
        if (cfg.host().isBlank() || cfg.databaseName().isBlank()) {
            lblStatus.setForeground(new Color(180, 60, 60));
            lblStatus.setText("Host and database are required.");
            return;
        }
        lblStatus.setForeground(new Color(180, 60, 60));
        lblStatus.setText("Connecting...");
        if (!Db.testConnection(cfg)) {
            lblStatus.setText("Unable to connect. Check host, port, database, and server status.");
            return;
        }
        DbConfigStore.save(cfg);
        connected = true;
        cancelled = false;
        dispose();
    }

    private void onCancel() {
        cancelled = true;
        dispose();
    }

    public static boolean ensureConfigured(Component parent) {
        if (Db.hasWorkingSavedConfig()) {
            return true;
        }
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        DatabaseConfigDialog dialog = new DatabaseConfigDialog(owner);
        dialog.setVisible(true);
        return dialog.isConnected();
    }
}
