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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BootstrapPanel extends JPanel {

    public enum Variant {
        PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, INFO, LIGHT, DARK
    }

    public enum ContentLayout {
        BORDER, FLOW, GRID, BOX_Y, BOX_X
    }

    private final JPanel headerPanel;
    private final JLabel titleLabel;
    private final JPanel bodyPanel;

    private Color headerBg;
    private Color headerFg;
    private Color hoverHeaderBg;
    private final Color bodyBg = Color.WHITE;

    private boolean hover = false;
    private boolean hoverEnabled = true;

    private int radius = 8;
    private int headerHeight = 42;

    private String title = "Bootstrap Panel";
    private Variant variant = Variant.PRIMARY;
    private ContentLayout contentLayout = ContentLayout.BORDER;

    public BootstrapPanel() {
        super(new BorderLayout());
        setOpaque(false);

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(100, headerHeight));
        headerPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);

        setVariant(Variant.PRIMARY);
        setContentLayout(ContentLayout.BORDER);
        installHoverSupport(this);
    }

    public JPanel getHeaderPanel() {
        return headerPanel;
    }

    public JPanel getBodyPanel() {
        return bodyPanel;
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
        repaint();
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        if (variant == null) {
            variant = Variant.PRIMARY;
        }
        this.variant = variant;

        switch (variant) {
            case PRIMARY -> { headerBg = new Color(13, 110, 253); headerFg = Color.WHITE; }
            case SECONDARY -> { headerBg = new Color(108, 117, 125); headerFg = Color.WHITE; }
            case SUCCESS -> { headerBg = new Color(25, 135, 84); headerFg = Color.WHITE; }
            case DANGER -> { headerBg = new Color(220, 53, 69); headerFg = Color.WHITE; }
            case WARNING -> { headerBg = new Color(255, 193, 7); headerFg = Color.BLACK; }
            case INFO -> { headerBg = new Color(13, 202, 240); headerFg = Color.BLACK; }
            case LIGHT -> { headerBg = new Color(248, 249, 250); headerFg = Color.BLACK; }
            case DARK -> { headerBg = new Color(33, 37, 41); headerFg = Color.WHITE; }
        }

        hoverHeaderBg = brighten(headerBg, 18);
        titleLabel.setForeground(headerFg);
        repaint();
    }

    public String getVariantName() {
        return variant.name();
    }

    public void setVariantName(String variantName) {
        try {
            setVariant(Variant.valueOf(variantName.toUpperCase()));
        } catch (Exception ex) {
            setVariant(Variant.PRIMARY);
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(0, radius);
        repaint();
    }

    public int getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = Math.max(24, headerHeight);
        headerPanel.setPreferredSize(new Dimension(100, this.headerHeight));
        revalidate();
        repaint();
    }

    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    public void setHoverEnabled(boolean hoverEnabled) {
        this.hoverEnabled = hoverEnabled;
        repaint();
    }

    public ContentLayout getContentLayout() {
        return contentLayout;
    }

    public void setContentLayout(ContentLayout contentLayout) {
        if (contentLayout == null) {
            contentLayout = ContentLayout.BORDER;
        }
        this.contentLayout = contentLayout;

        switch (contentLayout) {
            case BORDER -> bodyPanel.setLayout(new BorderLayout());
            case FLOW -> bodyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            case GRID -> bodyPanel.setLayout(new GridLayout(1, 1, 5, 5));
            case BOX_Y -> bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
            case BOX_X -> bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.X_AXIS));
        }

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    public String getContentLayoutName() {
        return contentLayout.name();
    }

    public void setContentLayoutName(String layoutName) {
        try {
            setContentLayout(ContentLayout.valueOf(layoutName.toUpperCase()));
        } catch (Exception ex) {
            setContentLayout(ContentLayout.BORDER);
        }
    }

    private Color currentHeaderColor() {
        return (hover && hoverEnabled) ? hoverHeaderBg : headerBg;
    }

    private Color currentBorderColor() {
        return currentHeaderColor();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(bodyBg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

        Shape oldClip = g2.getClip();
        g2.setClip(0, 0, w, headerHeight);
        g2.setColor(currentHeaderColor());
        g2.fillRoundRect(0, 0, w - 1, headerHeight + radius, radius, radius);
        g2.setClip(oldClip);

        g2.setColor(currentBorderColor());
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.dispose();
    }

    private Color brighten(Color c, int amount) {
        int r = Math.min(255, c.getRed() + amount);
        int g = Math.min(255, c.getGreen() + amount);
        int b = Math.min(255, c.getBlue() + amount);
        return new Color(r, g, b);
    }

    private void installHoverSupport(Component comp) {
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hoverEnabled) {
                    hover = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                PointerInfo pi = MouseInfo.getPointerInfo();
                if (pi == null) {
                    hover = false;
                    repaint();
                    return;
                }

                Point p = pi.getLocation();
                SwingUtilities.convertPointFromScreen(p, BootstrapPanel.this);

                if (!contains(p)) {
                    hover = false;
                    repaint();
                }
            }
        };

        addMouseRecursively(comp, hoverAdapter);
    }

    private void addMouseRecursively(Component comp, MouseAdapter adapter) {
        comp.addMouseListener(adapter);

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                addMouseRecursively(child, adapter);
            }

            container.addContainerListener(new ContainerAdapter() {
                @Override
                public void componentAdded(ContainerEvent e) {
                    addMouseRecursively(e.getChild(), adapter);
                }
            });
        }
    }
}