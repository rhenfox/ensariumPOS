package com.aldrin.ensarium.ui.widgets;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color fill;
    private final Color border;

    public RoundedPanel() {
        this.arc = 15;
        this.fill = Color.WHITE;
        this.border = Color.BLACK;
    }
    
    
    
    
    public RoundedPanel(JPanel panel,int arc, Color fill, Color border) {
        this.arc = arc;
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    public RoundedPanel(int arc, Color fill, Color border) {
        this.arc = arc;
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
