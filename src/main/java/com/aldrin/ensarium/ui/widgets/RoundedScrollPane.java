package com.aldrin.ensarium.ui.widgets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedScrollPane extends JScrollPane {

    private int arc = 18; // corner radius

    public RoundedScrollPane(Component view) {
        super(view);

        setOpaque(false);
//        getViewport().setOpaque(false);

        // remove default border and use padding
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // make scrollbars optional style-friendly
        getVerticalScrollBar().setUnitIncrement(14);
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background
            Color bg = UIManager.getColor("Table.background");
            if (bg == null) bg = UIManager.getColor("Table.background");

            // A slightly “card” look
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Border
            Color border = UIManager.getColor("Table.background");
            if (border == null) border = UIManager.getColor("Table.background");

            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        } finally {
            g2.dispose();
        }

        super.paintComponent(g);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        // Clip the viewport to rounded rect so table rows don't paint outside corners
        getViewport().setBorder(null);
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(),
                    arc, arc
            );
            g2.clip(clip);
            super.paintChildren(g2);
        } finally {
            g2.dispose();
        }
    }
}
