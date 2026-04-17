package com.aldrin.ensarium.dashboard;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.formdev.flatlaf.FlatClientProperties;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class DashboardPanel extends JPanel {

    private final DashboardRepository repository = new DashboardRepository();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd");

    private final JComboBox<StoreOption> storeCombo = new JComboBox<>();
    private final JComboBox<String> periodCombo = new JComboBox<>(new String[]{"7 Days", "14 Days", "30 Days"});
    private final StyledButton refreshButton = new StyledButton("Refresh");

    private final KpiCard salesCard = new KpiCard();
    private final KpiCard txCard = new KpiCard();
    private final KpiCard avgCard = new KpiCard();
    private final KpiCard profitCard = new KpiCard();
    private final KpiCard shiftCard = new KpiCard();
    private final KpiCard stockCard = new KpiCard();

    private final ChartCard lineCard = new ChartCard("Sales Trend");
    private final ChartCard topCard = new ChartCard("Top Products");
    private final ChartCard paymentCard = new ChartCard("Payment Mix");
    private final ChartCard inventoryCard = new ChartCard("Inventory Status");
    private final ChartCard hourlyCard = new ChartCard("Hourly Sales Today");
    private final ChartCard tableCard = new ChartCard("Monthly Category Qty Sales");

    private boolean loadingStores;
    private final Integer userId;

    Color color = Color.WHITE;

    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 24, Color.WHITE);
    Icon iconResetPassword = new Icons().key(18, Color.WHITE);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);

    public DashboardPanel() {
        this(null);
    }

    private Session session;

    public DashboardPanel(Session session) {
        this.session = session;
        this.userId = session.getUserId();
        setLayout(new BorderLayout());
        buildUi();
        loadStores();
        loadDashboard();
    }

    private void buildUi() {

        JPanel sidebar = createSidebar();
        JPanel content = createContent();

//        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 22, 28, 22));
        sidebar.setOpaque(true);
        sidebar.setBackground(UIManager.getColor("Panel.background"));

        JLabel logo = new JLabel("ENSARIUM");
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 24f));
        logo.setForeground(UIManager.getColor("Label.foreground"));

        JLabel subtitle = new JLabel("POS Analytics Workspace");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(30));

        sidebar.add(createNavButton("Dashboard", true));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(createNavButton("Sales Insights", false));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(createNavButton("Inventory Pulse", false));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(createNavButton("Cashier Activity", false));
        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("FlatLightLaf + Derby + JavaFX Charts");
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setForeground(UIManager.getColor("Label.disabledForeground"));
        sidebar.add(footer);
        return sidebar;
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(JButton.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; borderWidth:1; focusWidth:0; innerFocusWidth:0;"
                + (active
                        ? "background:$Button.default.background; foreground:$Button.default.foreground;"
                        : "background:$Panel.background; foreground:$Label.foreground;"));
        return btn;
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(22, 18, 12, 18));

        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("POS Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(UIManager.getColor("Label.foreground"));
        String scopeText = userId == null
                ? "All users"
                : "User ID: " + userId;
        JLabel desc = new JLabel("Modern Swing dashboard with JavaFX charts for revenue, payment mix, inventory visibility, and monthly category quantity sales. Scope: " + scopeText + ".");
        desc.setForeground(UIManager.getColor("Label.disabledForeground"));
        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(6));
        titleWrap.add(desc);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setOpaque(false);
        storeCombo.setPreferredSize(new Dimension(240, 30));
        periodCombo.setPreferredSize(new Dimension(120, 30));
        refreshButton.setIcon(iconRefresh);
//        refreshButton.putClientProperty(FlatClientProperties.STYLE,
//                "arc:16; font:bold +1; background:$Button.default.background;");
        filters.add(storeCombo);
        filters.add(periodCombo);
        filters.add(refreshButton);

        refreshButton.addActionListener(e -> loadDashboard());
        storeCombo.addActionListener(e -> {
            if (!loadingStores && storeCombo.getSelectedItem() != null) {
                loadDashboard();
            }
        });
        periodCombo.addActionListener(e -> loadDashboard());

        top.add(titleWrap, BorderLayout.WEST);
        top.add(filters, BorderLayout.EAST);

        JPanel grid = new JPanel();
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 18, 24, 18));
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));

        JPanel kpiGrid = new JPanel(new GridLayout(2, 3, 18, 18));
        kpiGrid.setOpaque(false);
        kpiGrid.add(salesCard);
        kpiGrid.add(txCard);
        kpiGrid.add(avgCard);
        kpiGrid.add(profitCard);
        kpiGrid.add(shiftCard);
        kpiGrid.add(stockCard);

        JPanel chartsRow1 = new JPanel(new GridLayout(1, 3, 18, 18));
        chartsRow1.setOpaque(false);
        chartsRow1.setPreferredSize(new Dimension(0, 320));
        chartsRow1.add(lineCard);
        chartsRow1.add(topCard);
        chartsRow1.add(paymentCard);

        JPanel chartsRow2 = new JPanel(new GridLayout(1, 3, 18, 18));
        chartsRow2.setOpaque(false);
        chartsRow2.setPreferredSize(new Dimension(0, 320));
        chartsRow2.add(inventoryCard);
        chartsRow2.add(hourlyCard);
        chartsRow2.add(tableCard);

        grid.add(kpiGrid);
        grid.add(Box.createVerticalStrut(18));
        grid.add(chartsRow1);
        grid.add(Box.createVerticalStrut(18));
        grid.add(chartsRow2);

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        root.add(top, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        return root;
    }

    private void loadStores() {
        loadingStores = true;
        try {
            List<StoreOption> stores = repository.loadStores();
            DefaultComboBoxModel<StoreOption> model = new DefaultComboBoxModel<>();
            for (StoreOption store : stores) {
                model.addElement(store);
            }
            storeCombo.setModel(model);
        } finally {
            loadingStores = false;
        }
    }

    private void loadDashboard() {
        refreshButton.setEnabled(false);
        StoreOption selectedStore = (StoreOption) storeCombo.getSelectedItem();
        Integer storeId = selectedStore == null ? null : selectedStore.id();
        int days = parsePeriod();

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() {
                return repository.loadDashboardData(storeId, days, userId);
            }

            @Override
            protected void done() {
                try {
                    updateUi(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Failed to load dashboard data:\n" + ex.getMessage(),
                            "Dashboard Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private int parsePeriod() {
        String value = String.valueOf(periodCombo.getSelectedItem());
        if (value.startsWith("14")) {
            return 14;
        }
        if (value.startsWith("30")) {
            return 30;
        }
        return 7;
    }

    private void updateUi(DashboardData data) {
        salesCard.setContent("Today's Sales", currency.format(zero(data.todaySales)), "Live POS revenue snapshot");
        txCard.setContent("Transactions", String.valueOf(data.todayTransactions), "Posted receipts today");
        avgCard.setContent("Average Ticket", currency.format(zero(data.avgTicket)), "Average basket size today");
        profitCard.setContent("Gross Profit", currency.format(zero(data.todayProfit)), "Estimated based on cost snapshots");
        shiftCard.setContent("Open Shifts", String.valueOf(data.openShifts), "Active cashiers / terminals");
        stockCard.setContent("Low Stock Items", String.valueOf(data.lowStockCount), "On-hand quantity at or below 5");

        lineCard.setChart(JavaFxChartFactory.createSalesTrendChart(data.salesTrend, dateFormat));
        topCard.setChart(JavaFxChartFactory.createCurrencyVerticalBarChart(data.topProducts));
        paymentCard.setChart(JavaFxChartFactory.createCurrencyPieChart(data.paymentMix));
        inventoryCard.setChart(JavaFxChartFactory.createQuantityPieChart(data.inventoryMix));
        hourlyCard.setChart(JavaFxChartFactory.createCurrencyHorizontalBarChart(data.hourlySales));
        tableCard.setChart(JavaFxChartFactory.createQuantityVerticalBarChart(data.monthlyCategoryQtySales));
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
