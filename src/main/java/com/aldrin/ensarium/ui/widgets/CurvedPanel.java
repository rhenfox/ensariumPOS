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

public class CurvedPanel extends JPanel {

    private int cornerRadius = 10;
    private Color borderColor = new Color(200, 200, 200);
    private int borderWidth = 1;

//    private Color gradientStart = new Color(0X00bf8f);
//    private Color gradientEnd = new Color(0X001510);
//   public  Color start = new Color(220, 53, 69);
//   public  Color end = new Color(176, 42, 55);
    public Color start = new Color(40, 167, 69);
    public Color end = new Color(25, 135, 84);

    public CurvedPanel() {
        setOpaque(false);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    public void setGradient(Color start, Color end) {
        this.start = start;
        this.end = end;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, start,
                0, height, end);

        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth / 2));
        g2.drawRoundRect(borderWidth / 2, borderWidth / 2,
                width - borderWidth, height - borderWidth,
                cornerRadius, cornerRadius);

        g2.dispose();
    }
}
