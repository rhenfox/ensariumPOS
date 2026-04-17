package com.aldrin.ensarium.dashboard;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class KpiCard extends JPanel {

    private final JLabel titleLabel = new JLabel("-");
    private final JLabel valueLabel = new JLabel("0");
    private final JLabel subtitleLabel = new JLabel("-");

    public KpiCard() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 14f));

        valueLabel.setForeground(UIManager.getColor("Label.foreground"));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));

        subtitleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(titleLabel);
        wrap.add(Box.createVerticalStrut(12));
        wrap.add(valueLabel);
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(subtitleLabel);

        add(wrap, BorderLayout.CENTER);
    }

    public void setContent(String title, String value, String subtitle) {
        titleLabel.setText(title);
        valueLabel.setText(value);
        subtitleLabel.setText(subtitle);
    }
}
