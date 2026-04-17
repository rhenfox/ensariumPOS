/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;

/**
 *
 * @author ALDRIN CABUSOG
 */

import javax.swing.*;
import java.awt.*;

public class EnsariumLabel extends JLabel {

    private Color c1 = new Color(0X858585);
    private Color c2 = new Color(0X858585);
    private Color c3 = new Color(0X858585);

    public EnsariumLabel() {
        super();
        setOpaque(false);
    }

    public void setGradientColors(Color c1, Color c2, Color c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics();
        int x = 0;
        int y = fm.getAscent();

        float[] dist = {0f, 0.5f, 1f};
        Color[] colors = {c1, c2, c3};

        LinearGradientPaint lgp = new LinearGradientPaint(
                0, 0, getWidth(), 0,
                dist,
                colors
        );

        g2.setPaint(lgp);
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}