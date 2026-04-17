package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.AuditService;
import com.aldrin.ensarium.service.RoleService;
import com.aldrin.ensarium.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();
    private final AuditService auditService = new AuditService();

    private final JLabel lblWelcome = new JLabel();
    private final JLabel lblUsers = cardValue();
    private final JLabel lblRoles = cardValue();
    private final JLabel lblAudit = cardValue();

    public DashboardPanel(Session session) {
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        lblWelcome.setText("Welcome, " + (session.fullName() == null || session.fullName().isBlank() ? session.username() : session.fullName()));
        lblWelcome.setFont(lblWelcome.getFont().deriveFont(Font.BOLD, 24f));

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 16));
        cards.add(card("Users", lblUsers));
        cards.add(card("Roles", lblRoles));
        cards.add(card("Audit Entries", lblAudit));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setOpaque(false);
        info.setText("This starter project shows Derby-backed login/logout, RBAC-based sidebar visibility, user CRUD, role CRUD, role-permission assignment, and audit logging. Use the admin account to test all modules.");

        add(lblWelcome, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        lblUsers.setText(String.valueOf(userService.countUsers()));
        lblRoles.setText(String.valueOf(roleService.countRoles()));
        lblAudit.setText(String.valueOf(auditService.listAudit().size()));
    }

    private JPanel card(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(16, 16, 16, 16)
        ));
        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel cardValue() {
        JLabel label = new JLabel("0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 32f));
        return label;
    }
}
