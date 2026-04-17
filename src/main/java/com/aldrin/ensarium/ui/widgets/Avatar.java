/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class Avatar extends JComponent {

    private Icon icon;
    private int borderSize;

    private boolean hovered = false;

    private Color borderColor1 = new Color(225, 61, 64);
    private Color borderColor2 = new Color(237, 247, 38);

    private Color hoverBorderColor1 = new Color(0, 120, 215);
    private Color hoverBorderColor2 = new Color(0, 170, 255);

    public Avatar() {
        setPreferredSize(new Dimension(50, 50));
        setOpaque(false);
        setBorderSize(3);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            float stroke = Math.max(1, borderSize);

            Ellipse2D outer = new Ellipse2D.Float(
                    x + stroke / 2f,
                    y + stroke / 2f,
                    size - stroke,
                    size - stroke
            );

            int innerX = Math.round(x + stroke);
            int innerY = Math.round(y + stroke);
            int innerSize = Math.round(size - (stroke * 2));

            if (innerSize > 0) {
                Ellipse2D inner = new Ellipse2D.Float(innerX, innerY, innerSize, innerSize);

                Shape oldClip = g2d.getClip();
                g2d.setClip(inner);

                if (icon instanceof ImageIcon imageIcon) {
                    Image img = imageIcon.getImage();
                    g2d.drawImage(img, innerX, innerY, innerSize, innerSize, this);
                } else {
                    g2d.setColor(new Color(230, 230, 230));
                    g2d.fill(inner);
                }

                if (hovered) {
                    g2d.setColor(new Color(255, 255, 255, 28));
                    g2d.fill(inner);
                }

                g2d.setClip(oldClip);
            }

            GradientPaint gp;
            if (hovered) {
                gp = new GradientPaint(
                        x, y, hoverBorderColor1,
                        x + size, y + size, hoverBorderColor2
                );
            } else {
                gp = new GradientPaint(
                        x, y, borderColor1,
                        x + size, y + size, borderColor2
                );
            }

            g2d.setPaint(gp);
            g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(outer);

        } finally {
            g2d.dispose();
        }
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        repaint();
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = Math.max(1, borderSize);
        repaint();
    }

    public Color getBorderColor1() {
        return borderColor1;
    }

    public void setBorderColor1(Color borderColor1) {
        this.borderColor1 = borderColor1;
        repaint();
    }

    public Color getBorderColor2() {
        return borderColor2;
    }

    public void setBorderColor2(Color borderColor2) {
        this.borderColor2 = borderColor2;
        repaint();
    }

    public Color getHoverBorderColor1() {
        return hoverBorderColor1;
    }

    public void setHoverBorderColor1(Color hoverBorderColor1) {
        this.hoverBorderColor1 = hoverBorderColor1;
        repaint();
    }

    public Color getHoverBorderColor2() {
        return hoverBorderColor2;
    }

    public void setHoverBorderColor2(Color hoverBorderColor2) {
        this.hoverBorderColor2 = hoverBorderColor2;
        repaint();
    }
}