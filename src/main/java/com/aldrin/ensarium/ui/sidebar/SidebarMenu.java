package com.aldrin.ensarium.ui.sidebar;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.ui.sidebar.NavNode;
import com.aldrin.ensarium.ui.sidebar.NavRow;
import com.aldrin.ensarium.util.TextIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SidebarMenu extends JPanel {

    private static final int EXPANDED_W = 230;
    private static final int COLLAPSED_W = 80;
    private static final int ROW_H = 36;

    private Color sidebarBg;
    private Color hoverBg;
    private Color activeBg;
    private Color text;
    private Color muted;

    private boolean collapsed = false;
    private boolean menuEnabled = true;

    private final JPanel header = new JPanel(new BorderLayout());
    private final JLabel brand = new JLabel("Ensarium");
    private final JButton toggleBtn = new JButton();

    private final JPanel list = new JPanel();
    private final JScrollPane scroll;

    private final Consumer<String> navigator;
    private final Predicate<String> allowed;

    private final Map<String, NavRow> rowByKey = new LinkedHashMap<>();
    private final java.util.List<NavRow> allRows = new ArrayList<>();
    private final java.util.List<Section> sections = new ArrayList<>();
    private String selectedKey;

    Color color = new Color(0x6E6E6E);

    Icon iconNavicon = FaSwingIcons.icon(FontAwesomeIcon.NAVICON, 24, color);
    Icon iconChevronLeft = FaSwingIcons.icon(FontAwesomeIcon.CHEVRON_LEFT, 18, color);

    Icon iconDashboard = FaSwingIcons.icon(FontAwesomeIcon.DASHBOARD, 18, color);
    Icon iconRegister = FaSwingIcons.icon(FontAwesomeIcon.DESKTOP, 18, color);
    Icon iconSales = FaSwingIcons.icon(FontAwesomeIcon.SHOPPING_CART, 18, color);
    Icon iconShift = FaSwingIcons.icon(FontAwesomeIcon.EXCHANGE, 18, color);
//    Icon iconShift = new Icons().retweet(18, color);

    Icon iconStocks = FaSwingIcons.icon(FontAwesomeIcon.PIE_CHART, 18, color);
    Icon iconStockin = FaSwingIcons.icon(FontAwesomeIcon.DOWNLOAD, 18, color);
    Icon iconOrder = new Icons().dolly(18, color);

    Icon iconAdministration = FaSwingIcons.icon(FontAwesomeIcon.GEARS, 18, color);
    Icon iconProducts = FaSwingIcons.icon(FontAwesomeIcon.CUBES, 18, color);
    Icon iconSupplier = FaSwingIcons.icon(FontAwesomeIcon.TRUCK, 18, color);
//    Icon iconStore = FaSwingIcons.icon(FontAwesomeIcon.HOME, 18, color);
    Icon iconFiscalBIR = FaSwingIcons.icon(FontAwesomeIcon.UNIVERSITY, 18, color);
//    Icon iconSale = FaSwingIcons.icon(FontAwesomeIcon.UNIVERSITY, 18, color);
    Icon iconInventory = FaSwingIcons.icon(FontAwesomeIcon.BAR_CHART, 18, color);
    Icon iconPayment = FaSwingIcons.icon(FontAwesomeIcon.CREDIT_CARD, 18, color);
    Icon iconUser = FaSwingIcons.icon(FontAwesomeIcon.USER_CIRCLE_ALT, 18, color);
//    Icon iconCustomer = FaSwingIcons.icon(FontAwesomeIcon.USER_ALT, 18, color);
    Icon iconSecurity = FaSwingIcons.icon(FontAwesomeIcon.SHIELD, 18, color);
    Icon iconAuditLog = FaSwingIcons.icon(FontAwesomeIcon.HISTORY, 18, color);
    Icon iconPermission = FaSwingIcons.icon(FontAwesomeIcon.USERS, 18, color);

    Icon iconStore = new Icons().store(18, color);
    Icon iconCustomer = new Icons().walking(18, color);
    Icon iconSale = new Icons().handHoldingUsd(18, color);
    
    Icon iconReport = FaSwingIcons.icon(FontAwesomeIcon.PRINT, 18, color);

    public SidebarMenu(Consumer<String> navigator, Predicate<String> allowed) {
        this.navigator = navigator;
        this.allowed = allowed != null ? allowed : k -> true;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(EXPANDED_W, 10));
        setMinimumSize(new Dimension(COLLAPSED_W, 10));

        buildHeader();
        add(header, BorderLayout.NORTH);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
        applyTheme();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(this::applyTheme);
    }

    private static boolean isDarkLaf() {
        Object v = UIManager.get("laf.dark");
        return v instanceof Boolean && (Boolean) v;
    }

    private static Color blend(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (a.getRed() * (1 - t) + b.getRed() * t);
        int g = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = (int) (a.getBlue() * (1 - t) + b.getBlue() * t);
        return new Color(r, g, bl);
    }

    public void setMenuEnabled(boolean enabled) {
        this.menuEnabled = enabled;
        for (NavRow row : allRows) {
            row.setClickEnabled(enabled);
        }
    }

    public void selectKey(String key) {
        if (key == null || Objects.equals(selectedKey, key)) {
            return;
        }
        if (selectedKey != null) {
            NavRow prev = rowByKey.get(selectedKey);
            if (prev != null) {
                prev.setSelected(false);
            }
        }
        selectedKey = key;
        NavRow now = rowByKey.get(selectedKey);
        if (now != null) {
            now.setSelected(true);
        }
    }

    public String firstSelectableKeyOrNull() {
        for (String key : rowByKey.keySet()) {
            if (!"LOGOUT".equals(key)) {
                return key;
            }
        }
        return null;
    }

    private void buildHeader() {
        header.setBorder(new EmptyBorder(14, 14, 14, 14));
        brand.setPreferredSize(new Dimension(160, 25));
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));

        toggleBtn.setFocusable(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setOpaque(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setText(null);
        toggleBtn.setIcon(collapsed ? iconNavicon : iconChevronLeft);
        toggleBtn.setHorizontalAlignment(SwingConstants.CENTER);
        toggleBtn.setVerticalAlignment(SwingConstants.CENTER);
        toggleBtn.addActionListener(e -> setCollapsed(!collapsed, true));

        rebuildHeaderLayout();
    }

    private void rebuildHeaderLayout() {
        header.removeAll();
        if (collapsed) {
            header.add(toggleBtn, BorderLayout.CENTER);
            brand.setVisible(false);
        } else {
            brand.setVisible(true);
            header.add(brand, BorderLayout.LINE_START);
            header.add(toggleBtn, BorderLayout.LINE_END);
        }
        header.revalidate();
        header.repaint();
    }

    private void buildMenuData() {
        list.removeAll();
        rowByKey.clear();
        allRows.clear();
        sections.clear();

        addItem(new NavNode("DASH", "Dashboard", iconDashboard));

        addSection("Register", iconRegister, Arrays.asList(
                new NavNode("SALES", "Sales", iconSales),
                new NavNode("SHIFT", "Shift", iconShift),
                new NavNode("CUSTOMER", "Customer", iconCustomer)
        ));

        addSection("Stocks", iconStocks, Arrays.asList(
                new NavNode("STOCKIN", "Stock-in", iconStockin),
                new NavNode("ORDER", "Order", iconOrder)
        ));
        addSection("Inventory", iconInventory, Arrays.asList(
                new NavNode("INVENTORY_TXN", "Transactions", iconStockin),
                new NavNode("INVENTORY_ONHAND", "Products", iconOrder)
        ));

        addSection("Administration", iconAdministration, Arrays.asList(
                new NavNode("PRODUCTS", "Products", iconProducts),
                new NavNode("SUPPLIER", "Supplier", iconSupplier),
                new NavNode("STORE", "Store", iconStore),
                new NavNode("FISCAL_BIR", "Fiscal (BIR)", iconFiscalBIR),
                new NavNode("SETUP_SALE", "Sale", iconSale),
                new NavNode("SETUP_INVENTORY", "Status", iconInventory),
                new NavNode("PAYMENT", "Payments", iconPayment)
        ));

        addSection("Security", iconSecurity, Arrays.asList(
                new NavNode(PermissionCodes.USERS_PAGE, "User", iconUser),
                new NavNode(PermissionCodes.ROLES_PERMS_PAGE, "Roles & Permissions", iconPermission),
                new NavNode(PermissionCodes.AUDIT_PAGE, "Audit Log", iconAuditLog)
        ));

        addSection("Reports", iconReport, Arrays.asList(
                new NavNode("BIR_TAX", "Invoice List", iconReport),
                new NavNode("TAX_SUMMARY", "Tax summary", iconReport),
                new NavNode("POS_PROFIT", "POS Profit", iconReport),
                new NavNode("FINANCIAL", "Financial Statement", iconReport)
        ));

        list.add(Box.createVerticalGlue());
//        addItemAlways(new NavNode("LOGOUT", "Logout", icon("⇦")));
        list.revalidate();
        list.repaint();
    }

    private Icon icon(String textSymbol) {
        return new TextIcon(textSymbol, 18, text);
    }

    private void applyRowSizing(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(0, ROW_H));
        c.setMinimumSize(new Dimension(0, ROW_H));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_H));
    }

    private void addItem(NavNode node) {
        if (node.key != null && !allowed.test(node.key)) {
            return;
        }
        addItemAlways(node);
    }

    private void addItemAlways(NavNode node) {
        NavRow row = new NavRow(node, false, sidebarBg, hoverBg, activeBg, text, muted);
        applyRowSizing(row);
        row.onClick(() -> {
            if (!menuEnabled) {
                return;
            }
            navigator.accept(node.key);
            if (!"LOGOUT".equals(node.key)) {
                selectKey(node.key);
            }
        });
        rowByKey.put(node.key, row);
        allRows.add(row);
        list.add(row);
    }

    private void addSection(String label, Icon icon, List<NavNode> children) {
        java.util.List<NavNode> visibleChildren = new ArrayList<>();
        for (NavNode child : children) {
            if (child.key != null && allowed.test(child.key)) {
                visibleChildren.add(child);
            }
        }
        if (visibleChildren.isEmpty()) {
            return;
        }

        NavRow headerRow = new NavRow(new NavNode(null, label, icon), true, sidebarBg, hoverBg, activeBg, text, muted);
        applyRowSizing(headerRow);
        headerRow.setExpanded(true);

        JPanel childPanel = new JPanel();
        childPanel.setLayout(new BoxLayout(childPanel, BoxLayout.Y_AXIS));
        childPanel.setOpaque(false);
        childPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        childPanel.setVisible(true);

        Section section = new Section(headerRow, childPanel);
        sections.add(section);
        headerRow.onClick(section::toggle);

        list.add(headerRow);
        list.add(childPanel);
        allRows.add(headerRow);

        for (int i = 0; i < visibleChildren.size(); i++) {
            NavNode child = visibleChildren.get(i);
            boolean lastChild = (i == visibleChildren.size() - 1);

            NavRow row = new NavRow(child, false, sidebarBg, hoverBg, activeBg, text, muted);
            applyRowSizing(row);

            row.setIndent(28);
            row.setCollapsedIndent(10);
            row.setTreeBranch(true, lastChild);

            row.onClick(() -> {
                if (!menuEnabled) {
                    return;
                }
                navigator.accept(child.key);
                selectKey(child.key);
            });

            rowByKey.put(child.key, row);
            allRows.add(row);
            childPanel.add(row);
        }
    }

    private void setCollapsed(boolean value, boolean animate) {
        collapsed = value;
        toggleBtn.setText(null);
        toggleBtn.setIcon(collapsed ? iconNavicon : iconChevronLeft);
        rebuildHeaderLayout();

        if (collapsed) {
            for (Section section : sections) {
                section.collapseForGlobal();
            }
        } else {
            for (Section section : sections) {
                section.restoreAfterGlobal();
            }
        }
        for (NavRow row : allRows) {
            row.setCollapsed(collapsed);
        }

        int targetW = collapsed ? COLLAPSED_W : EXPANDED_W;
        setPreferredSize(new Dimension(targetW, 10));
        revalidate();
        repaint();
    }

    private final class Section {

        final NavRow headerRow;
        final JPanel childPanel;
        boolean expanded = true;
        boolean prevExpanded = true;

        Section(NavRow headerRow, JPanel childPanel) {
            this.headerRow = headerRow;
            this.childPanel = childPanel;
        }

        void toggle() {
            expanded = !expanded;
            childPanel.setVisible(expanded);
            headerRow.setExpanded(expanded);
            revalidate();
            repaint();
        }

        void collapseForGlobal() {
            prevExpanded = expanded;
            expanded = false;
            childPanel.setVisible(false);
            headerRow.setExpanded(false);
        }

        void restoreAfterGlobal() {
            expanded = prevExpanded;
            childPanel.setVisible(expanded);
            headerRow.setExpanded(expanded);
        }
    }

    private void applyTheme() {
        boolean dark = isDarkLaf();
        sidebarBg = dark ? new Color(33, 37, 41) : new Color(248, 249, 250);
        text = new Color(0x6E6E6E);
        muted = dark ? new Color(170, 180, 190) : new Color(120, 130, 140);
        hoverBg = blend(sidebarBg, text, dark ? 0.10f : 0.06f);
        activeBg = blend(sidebarBg, text, dark ? 0.18f : 0.10f);

        setBackground(sidebarBg);
        header.setBackground(sidebarBg);
        list.setBackground(sidebarBg);
        scroll.getViewport().setBackground(sidebarBg);
        brand.setForeground(text);

        toggleBtn.setText(null);
        toggleBtn.setIcon(collapsed ? iconNavicon : iconChevronLeft);

        buildMenuData();
        setMenuEnabled(menuEnabled);
        setCollapsed(collapsed, false);
        if (selectedKey != null) {
            selectKey(selectedKey);
        }
    }
}
