/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {

    private Color color1 = new Color(0, 120, 215);
    private Color color2 = new Color(0, 90, 180);

    private Color hover1 = new Color(0, 150, 255);
    private Color hover2 = new Color(0, 110, 210);

    private boolean hover = false;
    private int radius = 10;

    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO,
        DARK,
        DEFAULT
    }

    public StyledButton() {
        super();
        initButton();
        setPrimary();
    }

    public StyledButton(String text) {
        super(text);
        initButton();
        setPrimary();
    }

    private void initButton() {
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("Segoe UI", Font.PLAIN, 12));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setRadius(int r) {
        radius = r;
        repaint();
    }

    public void setGradient(Color c1, Color c2) {
        color1 = c1;
        color2 = c2;
        repaint();
    }

    public void setHoverGradient(Color h1, Color h2) {
        hover1 = h1;
        hover2 = h2;
        repaint();
    }

    public void setButtonStyle(ButtonStyle style) {
        switch (style) {
            case PRIMARY:
                setForeground(Color.WHITE);
                setGradient(new Color(13, 110, 253), new Color(10, 88, 202));
                setHoverGradient(new Color(60, 139, 253), new Color(13, 110, 253));
                break;

            case SECONDARY:
                setForeground(Color.WHITE);
                setGradient(new Color(108, 117, 125), new Color(84, 91, 98));
                setHoverGradient(new Color(130, 138, 145), new Color(108, 117, 125));
                break;

            case SUCCESS:
                setForeground(Color.WHITE);
                setGradient(new Color(25, 135, 84), new Color(20, 108, 67));
                setHoverGradient(new Color(46, 160, 103), new Color(25, 135, 84));
                break;

            case DANGER:
                setForeground(Color.WHITE);
                setGradient(new Color(220, 53, 69), new Color(176, 42, 55));
                setHoverGradient(new Color(230, 80, 95), new Color(220, 53, 69));
                break;

            case WARNING:
                setForeground(Color.BLACK);
                setGradient(new Color(255, 193, 7), new Color(255, 179, 0));
                setHoverGradient(new Color(255, 205, 57), new Color(255, 193, 7));
                break;

            case INFO:
                setForeground(Color.BLACK);
                setGradient(new Color(13, 202, 240), new Color(49, 210, 242));
                setHoverGradient(new Color(80, 220, 245), new Color(13, 202, 240));
                break;

            case DARK:
                setForeground(Color.WHITE);
                setGradient(new Color(33, 37, 41), new Color(20, 23, 26));
                setHoverGradient(new Color(52, 58, 64), new Color(33, 37, 41));
                break;

            case DEFAULT:
                setForeground(Color.BLACK);
                setGradient(new Color(248, 249, 250), new Color(222, 226, 230));
                setHoverGradient(new Color(255, 255, 255), new Color(248, 249, 250));
                break;
        }
        repaint();
    }

    public void setPrimary() {
        setButtonStyle(ButtonStyle.PRIMARY);
    }

    public void setSecondary() {
        setButtonStyle(ButtonStyle.SECONDARY);
    }

    public void setSuccess() {
        setButtonStyle(ButtonStyle.SUCCESS);
    }

    public void setDanger() {
        setButtonStyle(ButtonStyle.DANGER);
    }

    public void setWarning() {
        setButtonStyle(ButtonStyle.WARNING);
    }

    public void setInfo() {
        setButtonStyle(ButtonStyle.INFO);
    }

    public void setDark() {
        setButtonStyle(ButtonStyle.DARK);
    }

    public void setDefaultStyle() {
        setButtonStyle(ButtonStyle.DEFAULT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color start = hover ? hover1 : color1;
        Color end = hover ? hover2 : color2;

        GradientPaint gp = new GradientPaint(0, 0, start, 0, getHeight(), end);

        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}