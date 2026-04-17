package com.aldrin.ensarium.ui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class LoginBackgroundPanel extends JPanel {
    public LoginBackgroundPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        GradientPaint gp = new GradientPaint(0, 0, new Color(14, 52, 108), w, h, new Color(25, 108, 155));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 24));
        g2.fill(new Ellipse2D.Double(-60, -80, 280, 280));
        g2.fill(new Ellipse2D.Double(w - 260, 40, 220, 220));
        g2.fill(new Ellipse2D.Double(w * 0.15, h * 0.55, 360, 360));
        g2.fill(new Ellipse2D.Double(w * 0.65, h * 0.70, 280, 280));

        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(40, 40, w - 80, h - 80, 36, 36));

        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 34f));
        g2.drawString("Ensarium", 54, 92);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
        g2.setColor(new Color(235, 244, 255, 220));
        drawWrapped(g2,
                "Point of Sale and Access-Controlled Desktop App",
                56, 128, Math.max(220, w / 2 - 80), 22);

        drawFeatureCard(g2, 56, h - 220, w / 2 - 80, 52, "Role-based navigation");
        drawFeatureCard(g2, 56, h - 154, w / 2 - 80, 52, "User, role, and permission management");
        drawFeatureCard(g2, 56, h - 88, w / 2 - 80, 52, "Audit-ready login and logout tracking");

        g2.dispose();
    }

    private void drawFeatureCard(Graphics2D g2, int x, int y, int width, int height, String text) {
        if (width < 180) {
            width = 180;
        }
        g2.setColor(new Color(255, 255, 255, 24));
        g2.fillRoundRect(x, y, width, height, 22, 22);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(x, y, width, height, 22, 22);
        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 15f));
        g2.drawString(text, x + 16, y + 31);
    }

    private void drawWrapped(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }
    }
}
