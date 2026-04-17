package com.aldrin.ensarium.ui.sidebar;


import com.aldrin.ensarium.icons.FaSwingIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class NavRow extends JPanel {

    private final NavNode node;
    private final boolean isSectionHeader;

    private final Color sidebarBg;
    private final Color hoverBg;
    private final Color activeBg;
    private final Color textColor;
    private final Color mutedColor;

    private final JLabel iconLabel = new JLabel();
    private final JLabel textLabel = new JLabel();
    private final JLabel arrowLabel = new JLabel();

    private boolean collapsed = false;
    private boolean expanded = true;
    private boolean selected = false;
    private boolean hover = false;
    private boolean clickEnabled = true;

    private int indent = 0;
    private int collapsedIndent = 0;

    // tree line support
    private boolean treeBranch = false;
    private boolean lastTreeBranch = false;

    private Runnable onClick;

    public NavRow(
            NavNode node,
            boolean isSectionHeader,
            Color sidebarBg,
            Color hoverBg,
            Color activeBg,
            Color textColor,
            Color mutedColor
    ) {
        this.node = node;
        this.isSectionHeader = isSectionHeader;

        this.sidebarBg = sidebarBg;
        this.hoverBg = hoverBg;
        this.activeBg = activeBg;
        this.textColor = textColor;
        this.mutedColor = mutedColor;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(sidebarBg);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setBorder(new EmptyBorder(0, 12, 0, 8));

        iconLabel.setIcon(node.icon);
        iconLabel.setOpaque(false);

        textLabel.setText(node.label);
        textLabel.setForeground(textColor);
        textLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        left.add(iconLabel);
        left.add(textLabel);

        add(left, BorderLayout.CENTER);

        Icon iconChevLeft = FaSwingIcons.icon(FontAwesomeIcon.CHEVRON_LEFT, 18, textColor);

        if (isSectionHeader) {
            arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
            arrowLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
            arrowLabel.setIcon(iconChevLeft);
            add(arrowLabel, BorderLayout.EAST);
        } else {
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            spacer.setPreferredSize(new Dimension(30, 1));
            add(spacer, BorderLayout.EAST);
        }

        setToolTipText(node.label);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaintBg();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaintBg();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!clickEnabled) {
                    return;
                }
                if (onClick != null) {
                    onClick.run();
                }
            }
        };

        addMouseListener(ma);
        left.addMouseListener(ma);
        iconLabel.addMouseListener(ma);
        textLabel.addMouseListener(ma);
        arrowLabel.addMouseListener(ma);

        repaintBg();
    }

    public void onClick(Runnable r) {
        this.onClick = r;
    }

    public void setClickEnabled(boolean enabled) {
        this.clickEnabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        textLabel.setForeground(enabled ? textColor : mutedColor);
        repaintBg();
    }

    public void setSelected(boolean sel) {
        this.selected = sel;
        repaintBg();
    }

    public void setCollapsed(boolean value) {
        this.collapsed = value;

        textLabel.setVisible(!collapsed);
        if (isSectionHeader) {
            arrowLabel.setVisible(!collapsed);
        }

        applyIndent();
        revalidate();
        repaint();
    }

    public void setIndent(int indent) {
        this.indent = Math.max(0, indent);
        applyIndent();
    }

    public void setCollapsedIndent(int collapsedIndent) {
        this.collapsedIndent = Math.max(0, collapsedIndent);
        applyIndent();
    }

    public void setTreeBranch(boolean treeBranch, boolean lastTreeBranch) {
        this.treeBranch = treeBranch;
        this.lastTreeBranch = lastTreeBranch;
        repaint();
    }

    private void applyIndent() {
        int leftPad = collapsed ? collapsedIndent : indent;
        setBorder(new EmptyBorder(0, leftPad, 0, 0));
    }

    public void setExpanded(boolean expanded) {
        Icon iconChevDown = FaSwingIcons.icon(FontAwesomeIcon.CHEVRON_DOWN, 18, textColor);
        Icon iconChevLeft = FaSwingIcons.icon(FontAwesomeIcon.CHEVRON_LEFT, 18, textColor);

        this.expanded = expanded;
        if (isSectionHeader) {
            arrowLabel.setIcon(expanded ? iconChevDown : iconChevLeft);
        }
        repaint();
    }

    private void repaintBg() {
        if (selected) {
            setBackground(activeBg);
        } else if (hover && clickEnabled) {
            setBackground(hoverBg);
        } else {
            setBackground(sidebarBg);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintTreeLine(g);
    }
    

    
    private void paintTreeLine(Graphics g) {
    if (!treeBranch || collapsed) {
        return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(mutedColor.getRed(), mutedColor.getGreen(), mutedColor.getBlue(), 140));
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int midY = getHeight() / 2;
        int x = Math.max(10, indent - 10);
        int endX = x + 16;

        if (lastTreeBranch) {
            g2.drawLine(x, 0, x, midY - 8);
        } else {
            g2.drawLine(x, 0, x, getHeight());
        }

        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
        path.moveTo(x, midY - 8);
        path.curveTo(
                x, midY - 2,
                x + 4, midY,
                endX, midY
        );
        g2.draw(path);

    } finally {
        g2.dispose();
    }
}
    
    
}