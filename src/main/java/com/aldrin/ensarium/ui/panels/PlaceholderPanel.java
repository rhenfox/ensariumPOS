package com.aldrin.ensarium.ui.panels;

import javax.swing.*;
import java.awt.*;

public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(String title, String note) {
        setLayout(new GridBagLayout());
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 24f));
        JTextArea area = new JTextArea(note);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        wrap.add(lblTitle, BorderLayout.NORTH);
        wrap.add(area, BorderLayout.CENTER);
        add(wrap);
    }
}
