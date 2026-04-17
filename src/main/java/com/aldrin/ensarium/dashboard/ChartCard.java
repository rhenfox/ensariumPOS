package com.aldrin.ensarium.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class ChartCard extends JPanel {

    private final JPanel contentPanel = new JPanel(new BorderLayout());

    public ChartCard(String title) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        contentPanel.setOpaque(false);

        add(titleLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setChart(Component component) {
        contentPanel.removeAll();
        if (component != null) {
            contentPanel.add(component, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
}
