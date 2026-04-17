package com.aldrin.ensarium.ui.widgets;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BackgroundImagePanel extends JPanel {
    private final BufferedImage image;

    public BackgroundImagePanel(String resourcePath) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(BackgroundImagePanel.class.getResource(resourcePath));
        } catch (IOException | IllegalArgumentException ex) {
            img = null;
        }
        this.image = img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (image != null) {
            double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
            int drawW = (int) Math.ceil(image.getWidth() * scale);
            int drawH = (int) Math.ceil(image.getHeight() * scale);
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;
            g2.drawImage(image, x, y, drawW, drawH, null);
            g2.setColor(new Color(255, 255, 255, 52));
            g2.fillRect(0, 0, w, h);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, new Color(226, 235, 247), w, h, new Color(200, 217, 238));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
