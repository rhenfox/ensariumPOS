/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.icons;

/**
 *
 * @author ALDRIN CABUSOG
 */
import javax.swing.*;
import java.awt.*;

public final class FaGlyphIcon implements Icon {
    private final Font baseFont;
    private final int codePoint;   // e.g. 0xF788
    private final int size;        // icon size in px
    private final Color color;

    public FaGlyphIcon(Font baseFont, int codePoint, int size, Color color) {
        this.baseFont = baseFont;
        this.codePoint = codePoint;
        this.size = size;
        this.color = color;
    }

    @Override public int getIconWidth()  { return size; }
    @Override public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);

            // derive font to roughly fit the icon box
            Font f = baseFont.deriveFont(Font.PLAIN, (float) size);
            g2.setFont(f);

            String s = new String(Character.toChars(codePoint));
            FontMetrics fm = g2.getFontMetrics();

            // center glyph in the icon box
            int textW = fm.stringWidth(s);
            int ascent = fm.getAscent();
            int descent = fm.getDescent();

            int gx = x + (size - textW) / 2;
            int gy = y + (size + ascent - descent) / 2;

            g2.drawString(s, gx, gy);
        } finally {
            g2.dispose();
        }
    }
}

