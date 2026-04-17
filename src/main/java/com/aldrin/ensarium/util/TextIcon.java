package com.aldrin.ensarium.util;

import javax.swing.*;
import java.awt.*;

public final class TextIcon implements Icon {
    private final String text;
    private final int size;
    private final Color color;

    public TextIcon(String text, int size, Color color) {
        this.text = text;
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setFont(new Font("Dialog", Font.BOLD, size));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x;
        int ty = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
