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
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class BootstrapTabbedPaneStyle {

    private BootstrapTabbedPaneStyle() {
    }

    public static void install(JTabbedPane tabbedPane) {
        install(tabbedPane, Style.bootstrapDefault());
    }

    public static void install(JTabbedPane tabbedPane, Style style) {
        if (tabbedPane == null || style == null) {
            return;
        }

        tabbedPane.setUI(new BootstrapTabsUI(style));
        tabbedPane.setFont(style.font);
        tabbedPane.setOpaque(false);
        tabbedPane.setFocusable(false);
        tabbedPane.setBackground(style.contentBackground);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.repaint();
        tabbedPane.revalidate();
    }

    public static final class Style {

//        private Color accent = new Color(0x0D6EFD);
//        private Color borderColor = new Color(0xDEE2E6);
//        private Color hoverBackground = new Color(0xE9ECEF);
        ////        private Color selectedBackground = Color.WHITE;
//        private Color normalBackground = new Color(0, 0, 0, 0);
//
////        private Color selectedText = new Color(0x495057);
//        private Color normalText = new Color(0x0D6EFD);
//        private Color hoverText = new Color(0x0A58CA);
//        private Color disabledText = new Color(0xADB5BD);
//
//        private Color selectedBackground = Color.WHITE;
//        private Color selectedText = new Color(0x495057);
////        private Color selectedLineColor = new Color(0x0D6EFD);
//
        private Color contentBackground = Color.WHITE;
        private Color accent = new Color(0x0D6EFD);
        private Color borderColor = new Color(0xDEE2E6);
        private Color hoverBackground = new Color(0xE9ECEF);
        private Color selectedBackground = new Color(0x0D6EFD);
        private Color normalBackground = new Color(0, 0, 0, 0);

        private Color selectedText = Color.WHITE;
        private Color normalText = new Color(0x0D6EFD);
        private Color hoverText = new Color(0x0A58CA);
        private Color disabledText = new Color(0xADB5BD);

        private Color selectedLineColor = Color.WHITE;

        private Font font = new Font("Segoe UI", Font.PLAIN, 13);

        private int tabHeight = 38;
        private int hPad = 18;
        private int vPad = 8;
        private int topArc = 5;

//        private Color selectedLineColor = new Color(0x0D6EFD);
        private int selectedLineThickness = 3;
        private int selectedLineInset = 10;

        public Style selectedLineColor(Color value) {
//            this.selectedLineColor = value;
            return this;
        }

        public Style selectedLineThickness(int value) {
            this.selectedLineThickness = value;
            return this;
        }

        public Style selectedLineInset(int value) {
            this.selectedLineInset = value;
            return this;
        }

        public static Style bootstrapDefault() {
            return new Style();
        }

//        public Style accent(Color value) {
//            this.accent = value;
//            this.normalText = value;
//            return this;
//        }
        public Style accent(Color value) {
            this.accent = value;
            this.normalText = value;
            this.selectedBackground = value;
            return this;
        }

        public Style borderColor(Color value) {
            this.borderColor = value;
            return this;
        }

        public Style hoverBackground(Color value) {
            this.hoverBackground = value;
            return this;
        }

        public Style selectedBackground(Color value) {
            this.selectedBackground = value;
            return this;
        }

        public Style normalBackground(Color value) {
            this.normalBackground = value;
            return this;
        }

        public Style selectedText(Color value) {
            this.selectedText = value;
            return this;
        }

        public Style normalText(Color value) {
            this.normalText = value;
            return this;
        }

        public Style hoverText(Color value) {
            this.hoverText = value;
            return this;
        }

        public Style disabledText(Color value) {
            this.disabledText = value;
            return this;
        }

        public Style contentBackground(Color value) {
            this.contentBackground = value;
            return this;
        }

        public Style font(Font value) {
            this.font = value;
            return this;
        }

        public Style tabHeight(int value) {
            this.tabHeight = value;
            return this;
        }

        public Style horizontalPadding(int value) {
            this.hPad = value;
            return this;
        }

        public Style verticalPadding(int value) {
            this.vPad = value;
            return this;
        }

        public Style topArc(int value) {
            this.topArc = value;
            return this;
        }
    }

    private static final class BootstrapTabsUI extends BasicTabbedPaneUI {

        private final Style style;
        private int hoverTab = -1;
        private MouseAdapter hoverHandler;

        private BootstrapTabsUI(Style style) {
            this.style = style;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(style.vPad, style.hPad, style.vPad, style.hPad);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(4, 6, 0, 6);
            contentBorderInsets = new Insets(0, 6, 6, 6);
            lightHighlight = style.borderColor;
            shadow = style.borderColor;
            darkShadow = style.borderColor;
            focus = new Color(0, 0, 0, 0);
        }

        @Override
        protected void installListeners() {
            super.installListeners();

            hoverHandler = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int old = hoverTab;
                    hoverTab = tabForCoordinate(tabPane, e.getX(), e.getY());
                    if (old != hoverTab) {
                        tabPane.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoverTab != -1) {
                        hoverTab = -1;
                        tabPane.repaint();
                    }
                }
            };

            tabPane.addMouseMotionListener(hoverHandler);
            tabPane.addMouseListener(hoverHandler);
        }

        @Override
        protected void uninstallListeners() {
            if (hoverHandler != null) {
                tabPane.removeMouseMotionListener(hoverHandler);
                tabPane.removeMouseListener(hoverHandler);
            }
            super.uninstallListeners();
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return Math.max(style.tabHeight, super.calculateTabHeight(tabPlacement, tabIndex, fontHeight));
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            return Math.max(90, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
        }

        @Override
        protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
            return 0;
        }

        @Override
        protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
            return 0;
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected) {
            if (tabPlacement != TOP) {
                super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Shape shape = new RoundedTopTabShape(x, y + 2, w, h - 2, style.topArc);

                if (isSelected) {
                    g2.setColor(style.selectedBackground);
                    g2.fill(shape);
                } else if (tabIndex == hoverTab && tabPane.isEnabledAt(tabIndex)) {
                    g2.setColor(style.hoverBackground);
                    g2.fill(shape);
                } else {
                    g2.setColor(style.normalBackground);
                    g2.fill(shape);
                }
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                int x, int y, int w, int h, boolean isSelected) {
            if (tabPlacement != TOP) {
                super.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(style.borderColor);

                int arc = style.topArc;
                int yy = y + 2;
                int hh = h - 2;

                if (isSelected) {
                    g2.drawRoundRect(x, yy, w - 1, hh, arc, arc);

                    // hide bottom border so selected tab connects to content
                    g2.setColor(style.selectedBackground);
                    g2.drawLine(x + 1, y + h - 1, x + w - 2, y + h - 1);
                } else if (tabIndex == hoverTab && tabPane.isEnabledAt(tabIndex)) {
                    g2.drawRoundRect(x, yy, w - 1, hh, arc, arc);

                    // keep bottom line flat for unselected tab
                    g2.setColor(style.borderColor);
                    g2.drawLine(x + 1, y + h - 1, x + w - 2, y + h - 1);
                }
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(style.font);

            if (!tabPane.isEnabledAt(tabIndex)) {
                g.setColor(style.disabledText);
            } else if (isSelected) {
                g.setColor(style.selectedText);
            } else if (tabIndex == hoverTab) {
                g.setColor(style.hoverText);
            } else {
                g.setColor(style.normalText);
            }

            int mnemonicIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            drawStringUnderlineCharAt(g, title, mnemonicIndex, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // no focus box
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            if (tabPlacement != TOP) {
                super.paintContentBorder(g, tabPlacement, selectedIndex);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setColor(style.contentBackground);

                Insets insets = tabPane.getInsets();
                int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

                int x = insets.left + 6;
                int y = insets.top + tabAreaHeight - 1;
                int w = tabPane.getWidth() - insets.left - insets.right - 12;
                int h = tabPane.getHeight() - y - insets.bottom - 6;

                g2.fillRect(x, y, w, h);

                g2.setColor(style.borderColor);
                g2.drawRect(x, y, w - 1, h - 1);
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex,
                int x, int y, int w, int h) {
        }

        @Override
        protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex,
                int x, int y, int w, int h) {
        }

        @Override
        protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex,
                int x, int y, int w, int h) {
        }

        @Override
        protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex,
                int x, int y, int w, int h) {
        }

        private void drawStringUnderlineCharAt(Graphics g, String text, int underlinedIndex, int x, int y) {
            if (text == null || text.isEmpty()) {
                return;
            }

            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, x, y);

            if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
                int underlineX = x + fm.stringWidth(text.substring(0, underlinedIndex));
                int underlineWidth = fm.charWidth(text.charAt(underlinedIndex));
                g.drawLine(underlineX, y + 1, underlineX + underlineWidth - 1, y + 1);
            }
        }
    }

    private static final class RoundedTopTabShape extends Polygon {

        RoundedTopTabShape(int x, int y, int w, int h, int arc) {
            super(
                    new int[]{
                        x,
                        x,
                        x + arc / 2,
                        x + arc,
                        x + w - arc,
                        x + w - arc / 2,
                        x + w,
                        x + w,
                        x
                    },
                    new int[]{
                        y + h,
                        y + arc,
                        y + arc / 2,
                        y,
                        y,
                        y + arc / 2,
                        y + arc,
                        y + h,
                        y + h
                    },
                    9
            );
        }
    }

}
