package com.aldrin.ensarium.ui;


import com.aldrin.ensarium.dashboard.DashboardPanel;
import com.aldrin.ensarium.db.AppConfig;
import com.aldrin.ensarium.dispense.DispensePanel2;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.inventory.product.ProductInventoryPanel2;
import com.aldrin.ensarium.order.OrderDraftManager;
import com.aldrin.ensarium.order.ProductOrderPanel;
import com.aldrin.ensarium.report.BirInvoiceListTabPanel;
import com.aldrin.ensarium.report.FinancialStatementTabPanel;
import com.aldrin.ensarium.report.PosProfitTabPanel;
import com.aldrin.ensarium.report.ReportService;
import com.aldrin.ensarium.report.TaxSummaryTabPanel;
import com.aldrin.ensarium.security.AccessRules;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.AuthService;
import com.aldrin.ensarium.service.UserService;
import com.aldrin.ensarium.shift.ShiftPanel2;
import com.aldrin.ensarium.stockin.StockInPanel;
import com.aldrin.ensarium.txn.TxnPanel;
import com.aldrin.ensarium.ui.panels.AuditLogPanel;
import com.aldrin.ensarium.ui.panels.CustomersPanel;
//import com.aldrin.ensarium.ui.panels.DashboardPanel;
import com.aldrin.ensarium.ui.panels.FiscalBirPanel;
import com.aldrin.ensarium.ui.panels.InventorySetupPanel;
import com.aldrin.ensarium.ui.panels.PaymentMethodsPanel;
import com.aldrin.ensarium.ui.panels.PlaceholderPanel;
import com.aldrin.ensarium.ui.panels.ProductsPanel;
import com.aldrin.ensarium.ui.panels.RolesPermsPanel;
import com.aldrin.ensarium.ui.panels.SaleSetupPanel;
import com.aldrin.ensarium.ui.panels.StoresPanel;
import com.aldrin.ensarium.ui.panels.SuppliersPanel;
import com.aldrin.ensarium.ui.panels.UsersPanel;
//import com.aldrin.ensarium.ui.panels.*;
import com.aldrin.ensarium.ui.sidebar.SidebarMenu;
import com.aldrin.ensarium.ui.widgets.BackgroundImagePanel;
import com.aldrin.ensarium.ui.widgets.RoundedPanel;
import com.aldrin.ensarium.util.RememberMeStore;

import com.aldrin.ensarium.ui.widgets.Avatar;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle2;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class MainFrame extends JFrame {

    private final JComponent topBar;
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    private Session session;

    private final CardLayout centerCards = new CardLayout();
    private final JPanel center = new JPanel(centerCards);
    private final JPanel loginCard = new JPanel(new BorderLayout());
    private final JPanel appCard = new JPanel(new BorderLayout());

    private final JLabel lblLeft = new JLabel("Ensarium");
    private final JLabel lblStatus = new JLabel("Not logged in");

    private SidebarMenu sidebar;
    private final CardLayout pagesLayout = new CardLayout();
    private final JPanel pages = new JPanel(pagesLayout);
//    private DashboardPanel dashboardPanel;
    private UsersPanel usersPanel;
    private RolesPermsPanel rolesPanel;
    private AuditLogPanel auditPanel;
    private ProductsPanel productsPanel;
    private StoresPanel storesPanel;
    private CustomersPanel customersPanel;
    private FiscalBirPanel fiscalBirPanel;
    private PaymentMethodsPanel paymentMethodPanel;
    private SuppliersPanel supplierPanel;
    private InventorySetupPanel inventorySetupPanel;
    private SaleSetupPanel saleSetupPanel;
    private DispensePanel2 dispensePanel;
    private ShiftPanel2 shiftPanel;
    private TxnPanel txnPanel;
    private ProductInventoryPanel2 productInventoryPanel;
    private ProductOrderPanel productOrderPanel;
    private DashboardPanel dashboardPanel;

    private StockInPanel stockInPanel;
    private BirInvoiceListTabPanel invoiceListPanel;
    private TaxSummaryTabPanel taxSummaryPanel;
    private PosProfitTabPanel posProfitTabPanel;
    private FinancialStatementTabPanel financialStatementTabPanel;

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JCheckBox chkRememberMe;
    private JLabel lblLoginMsg;
    private JButton btnLogin;

//    private AvatarButton avatarButton;
    private Avatar avatar;
    private JPopupMenu profileMenu;
    private JMenuItem miChangePassword;
    private JMenuItem miLogout;

    public MainFrame() {
        super("Ensarium - RBAC Demo");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1500, 815);
        setMinimumSize(new Dimension(1080, 640));
        setLocationRelativeTo(null);
        setContentPane(new BackgroundImagePanel("/images/login-bg.png"));
        getContentPane().setLayout(new BorderLayout());

        center.setOpaque(false);
        loginCard.setOpaque(false);
        appCard.setOpaque(false);

        topBar = buildTopBar();
        topBar.setVisible(false);
        add(topBar, BorderLayout.NORTH);
        buildLoginCard();
        center.add(loginCard, "LOGIN");
        center.add(appCard, "APP");
        add(center, BorderLayout.CENTER);
        setLoggedOutUi();
        SwingUtilities.invokeLater(this::tryAutoLogin);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (session != null) {
                    authService.logout(session);
                }
            }
        });
        BootstrapTableStyle2.installAll(this);

    }

    Icon iconKey = Icons.key(14, new Color(120, 120, 120));
    Icon iconLogout = FaSwingIcons.icon(FontAwesomeIcon.SIGN_OUT, 16, new Color(120, 120, 120));

    private JComponent buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(8, 10, 8, 10));

        lblLeft.setFont(lblLeft.getFont().deriveFont(Font.BOLD));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(lblLeft);
        left.add(new JLabel("|"));
        left.add(lblStatus);
        top.add(left, BorderLayout.WEST);

//        avatarButton = new AvatarButton(32, 3);
//        avatarButton.setInitials("U");
        avatar = new Avatar();
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setBorderSize(3);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.setToolTipText("Profile");

        profileMenu = new JPopupMenu();
        miChangePassword = new JMenuItem("Change Password");
        miLogout = new JMenuItem("Logout");

        miChangePassword.setIcon(iconKey);
        miLogout.setIcon(iconLogout);

        miChangePassword.addActionListener(e -> onChangePassword());
        miLogout.addActionListener(e -> doLogout());

        profileMenu.add(miChangePassword);
        profileMenu.addSeparator();
        profileMenu.add(miLogout);

        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    showProfileMenu();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showProfileMenu();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showProfileMenu();
                }
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
//        right.add(avatarButton);
        right.add(avatar);

        top.add(right, BorderLayout.EAST);
        return top;
    }

    private void buildLoginCard() {
        loginCard.removeAll();
        loginCard.setOpaque(false);

        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(36, 36, 36, 36));

        RoundedPanel formCard = new RoundedPanel(36, new Color(255, 255, 255, 242), new Color(221, 228, 236));
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(30, 28, 30, 28));
        formCard.setPreferredSize(new Dimension(460, 420));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(360, 340));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JLabel title = new JLabel("Welcome back", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));

        JLabel subtitle = new JLabel("Sign in to continue to Ensarium", SwingConstants.CENTER);
        subtitle.setForeground(new Color(105, 113, 123));

        txtUser = new JTextField(20);
        txtPass = new JPasswordField(20);
        chkRememberMe = new JCheckBox("Remember me");
        chkRememberMe.setOpaque(false);
        lblLoginMsg = new JLabel(" ", SwingConstants.CENTER);
        lblLoginMsg.setForeground(new Color(180, 60, 60));
        btnLogin = new JButton("Login");

        txtUser.putClientProperty("JTextField.placeholderText", "Enter username");
        txtPass.putClientProperty("JTextField.placeholderText", "Enter password");
        txtUser.putClientProperty("JComponent.roundRect", true);
        txtPass.putClientProperty("JComponent.roundRect", true);
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        txtUser.putClientProperty("JTextField.padding", new Insets(0, 10, 0, 0));
        txtPass.putClientProperty("JTextField.padding", new Insets(0, 10, 0, 0));

        Dimension fieldSize = new Dimension(260, 30);
        txtUser.setPreferredSize(fieldSize);
        txtPass.setPreferredSize(fieldSize);
        btnLogin.setPreferredSize(new Dimension(260, 32));

        int y = 0;
        g.gridx = 0;
        g.gridy = y++;
        g.gridwidth = 2;
        form.add(title, g);
        g.gridy = y++;
        form.add(subtitle, g);
        g.gridy = y++;
        form.add(lblLoginMsg, g);

        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Username"), g);
        g.gridx = 1;
        form.add(txtUser, g);
        y++;
        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Password"), g);
        g.gridx = 1;
        form.add(txtPass, g);
        y++;

        g.gridx = 1;
        g.gridy = y;
        g.gridwidth = 1;
        g.insets = new Insets(2, 8, 8, 8);
        form.add(chkRememberMe, g);
        y++;

        g.gridx = 0;
        g.gridy = y;
        g.gridwidth = 2;
        g.insets = new Insets(16, 8, 6, 8);
        form.add(btnLogin, g);
        y++;

        JLabel footer = new JLabel("Use admin/admin123 or cashier/cashier123", SwingConstants.CENTER);
        footer.setForeground(new Color(122, 129, 138));
        footer.setFont(footer.getFont().deriveFont(12f));
        g.gridy = y;
        g.insets = new Insets(10, 8, 0, 8);
//        form.add(footer, g);

        formCard.add(form);
        shell.add(formCard);
        loginCard.add(shell, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void doLogin() {
        lblLoginMsg.setText(" ");
        btnLogin.setEnabled(false);

        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty()) {
            lblLoginMsg.setText("Enter username.");
            txtUser.requestFocusInWindow();
            btnLogin.setEnabled(true);
            return;
        }

        if (password.isEmpty()) {
            lblLoginMsg.setText("Enter password.");
            txtPass.requestFocusInWindow();
            btnLogin.setEnabled(true);
            return;
        }

        try {
            Session newSession = authService.login(username, password);
            if (newSession == null) {
                lblLoginMsg.setText("Invalid credentials or inactive account.");
                return;
            }
            this.session = newSession;
            Session.session = newSession;
            populateSessionContext(this.session);

//            this.session = newSession;
            if (chkRememberMe.isSelected()) {
                try {
                    String storedHash = authService.getStoredPasswordHash(username);
                    if (storedHash != null && !storedHash.isBlank()) {
                        RememberMeStore.save(username, storedHash);
                    } else {
                        RememberMeStore.clear();
                    }
                } catch (Exception ex) {
                    RememberMeStore.clear();
                }
            } else {
                RememberMeStore.clear();
            }

            try {
                finishLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
                lblLoginMsg.setText("Login succeeded, but main screen failed to load.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            lblLoginMsg.setText("Login failed: " + ex.getMessage());
        } finally {
            btnLogin.setEnabled(true);
        }
    }

    private void finishLogin() {
        setLoggedInUi();
        buildAppUiForSession();
        if (AccessRules.canAccess(session, "DASH")) {
            sidebar.selectKey("DASH");
            if (dashboardPanel != null) {
//                dashboardPanel.refreshData();
            }
            pagesLayout.show(pages, "DASH");
        }
        centerCards.show(center, "APP");
    }

    private void tryAutoLogin() {
        try {
            var saved = RememberMeStore.load();
            if (saved.isEmpty()) {
                return;
            }
            chkRememberMe.setSelected(true);
            txtUser.setText(saved.get().username());
            Session savedSession = authService.autoLogin(saved.get().username(), saved.get().passwordHash());
            if (savedSession == null) {
                RememberMeStore.clear();
                chkRememberMe.setSelected(false);
                return;
            }
//            this.session = savedSession;
            this.session = savedSession;
            Session.session = savedSession;
            populateSessionContext(this.session);
            finishLogin();
        } catch (Exception ex) {
            RememberMeStore.clear();
        }
    }

    ReportService service = new ReportService();
    
    private void buildAppUiForSession() {
        appCard.removeAll();
        pages.removeAll();
        
        OrderDraftManager draftManager = new OrderDraftManager();
//        tabs.addTab("Product Inventory", new ProductInventoryPanel(session, draftManager));

        dashboardPanel = new DashboardPanel(session);
        usersPanel = new UsersPanel(session);
        rolesPanel = new RolesPermsPanel(session);
        auditPanel = new AuditLogPanel(session);
        productsPanel = new ProductsPanel(session);
        storesPanel = new StoresPanel(session);
        customersPanel = new CustomersPanel(session);
        fiscalBirPanel = new FiscalBirPanel(session);
        supplierPanel = new SuppliersPanel(session);
        paymentMethodPanel = new PaymentMethodsPanel(session);
        inventorySetupPanel = new InventorySetupPanel(session);
        saleSetupPanel = new SaleSetupPanel(session);
        stockInPanel = new StockInPanel(session);
        dispensePanel = new DispensePanel2(session);
        txnPanel = new TxnPanel(session);
        productInventoryPanel = new ProductInventoryPanel2(session, draftManager);
        productOrderPanel = new ProductOrderPanel(session, draftManager);
        dashboardPanel = new DashboardPanel(session);
        invoiceListPanel = new BirInvoiceListTabPanel(service);
        taxSummaryPanel = new TaxSummaryTabPanel(service);
        posProfitTabPanel = new PosProfitTabPanel(service);
        financialStatementTabPanel = new FinancialStatementTabPanel(service);

        try {
            shiftPanel = new ShiftPanel2(session);
        } catch (SQLException ex) {
            shiftPanel = null;
            System.getLogger(MainFrame.class.getName())
                    .log(System.Logger.Level.ERROR, "Unable to initialize Shift panel", ex);
        }

        pages.add(dashboardPanel, "DASH");
        pages.add(dispensePanel, "SALES");
        pages.add(customersPanel, "CUSTOMER");
        pages.add(stockInPanel, "STOCKIN");

        // Do not add the same panel twice under different keys
        pages.add(productOrderPanel, "ORDER");

        pages.add(new PlaceholderPanel("Inventory", "This is a placeholder inventory page."), "INVENTORY");
        pages.add(txnPanel, "INVENTORY_TXN");
        pages.add(productInventoryPanel, "INVENTORY_ONHAND");

        pages.add(productsPanel, "PRODUCTS");
        pages.add(supplierPanel, "SUPPLIER");
        pages.add(storesPanel, "STORE");
        pages.add(fiscalBirPanel, "FISCAL_BIR");
        pages.add(paymentMethodPanel, "PAYMENT");
        pages.add(saleSetupPanel, "SETUP_SALE");
        pages.add(inventorySetupPanel, "SETUP_INVENTORY");
        pages.add(usersPanel, "USERS");
        pages.add(rolesPanel, "ROLES_PERMS");
        pages.add(auditPanel, "AUDIT");
        pages.add(invoiceListPanel,"BIR_TAX");
        pages.add(taxSummaryPanel,"TAX_SUMMARY");
        pages.add(posProfitTabPanel,"POS_PROFIT");
        pages.add(financialStatementTabPanel,"FINANCIAL");

        pages.add(
                shiftPanel != null
                        ? shiftPanel
                        : new PlaceholderPanel("Shift", "Shift panel could not be loaded. Add setup for store and terminal."),
                "SHIFT"
        );

        pages.add(new PlaceholderPanel("Not Authorized", "The current user does not have access to this page."), "NO_ACCESS");

        sidebar = new SidebarMenu(this::navigate, key -> AccessRules.canAccess(session, key));
        String initial = AccessRules.canAccess(session, "DASH") ? "DASH" : sidebar.firstSelectableKeyOrNull();
        if (initial == null) {
            initial = "NO_ACCESS";
        }

        sidebar.selectKey(initial);
        pagesLayout.show(pages, initial);

        appCard.add(sidebar, BorderLayout.WEST);
        appCard.add(pages, BorderLayout.CENTER);
        appCard.revalidate();
        appCard.repaint();
    }

    private void navigate(String key) {
        if ("LOGOUT".equals(key)) {
            doLogout();
            return;
        }
        if (!AccessRules.canAccess(session, key)) {
            pagesLayout.show(pages, "NO_ACCESS");
            return;
        }
        if ("DASH".equals(key) && dashboardPanel != null) {
//            dashboardPanel.refreshData();
        }
        if ("USERS".equals(key) && usersPanel != null) {
            usersPanel.refreshTable();
        }
        if ("ROLES_PERMS".equals(key) && rolesPanel != null) {
            rolesPanel.refreshRoles();
        }
        if ("AUDIT".equals(key) && auditPanel != null) {
            auditPanel.refresh();
        }
        if ("CUSTOMER".equals(key) && customersPanel != null) {
            customersPanel.refreshAll();
        }
        if ("PRODUCTS".equals(key) && productsPanel != null) {
            productsPanel.refreshAll();
        }
        if ("STORE".equals(key) && storesPanel != null) {
            storesPanel.refreshAll();
        }
        if ("FISCAL_BIR".equals(key) && fiscalBirPanel != null) {
            fiscalBirPanel.refreshAll();
        }
        if ("SALES".equals(key) && dispensePanel != null) {
            javax.swing.SwingUtilities.invokeLater(dispensePanel::requestBarcodeFocus);
        }
        if ("SHIFT".equals(key) && shiftPanel != null) {
            shiftPanel.refreshData();
        }
        pagesLayout.show(pages, key);
    }

    private void onChangePassword() {
        if (session == null) {
            return;
        }

        ChangePasswordDialog chd = new ChangePasswordDialog(this, session, true);
        chd.setVisible(true);
    }


    private void doLogout() {
        if (session != null) {
            authService.logout(session);
            session = null;
        }
        Session.session = null;
        RememberMeStore.clear();
        setLoggedOutUi();
        centerCards.show(center, "LOGIN");
        txtUser.requestFocusInWindow();
    }

    private void setLoggedInUi() {
        String displayName = (session.fullName() == null || session.fullName().isBlank()) ? session.username() : session.fullName();
        lblStatus.setText("Logged in as: " + displayName + " [" + session.username() + "]");
        topBar.setVisible(true);

        if (miChangePassword != null) {
            miChangePassword.setEnabled(true);
        }
        if (miLogout != null) {
            miLogout.setEnabled(true);
        }

        txtPass.setText("");
        lblLoginMsg.setText(" ");
        btnLogin.setEnabled(true);

        refreshProfileAvatar();
    }

    private void setLoggedOutUi() {
        lblStatus.setText("Not logged in");
        topBar.setVisible(false);

        if (miChangePassword != null) {
            miChangePassword.setEnabled(false);
        }
        if (miLogout != null) {
            miLogout.setEnabled(false);
        }

        if (avatar != null) {
            avatar.setIcon(null);
        }

        if (txtUser != null) {
            txtUser.setText("");
        }
        if (txtPass != null) {
            txtPass.setText("");
        }
        if (chkRememberMe != null) {
            chkRememberMe.setSelected(false);
        }
        if (lblLoginMsg != null) {
            lblLoginMsg.setText(" ");
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(true);
        }

        appCard.removeAll();
        appCard.revalidate();
        appCard.repaint();
    }

    private void showProfileMenu() {
        if (profileMenu != null && avatar != null) {
            profileMenu.show(avatar, Math.max(0, avatar.getWidth() - 140), avatar.getHeight() + 4);
        }
    }

    private void refreshProfileAvatar() {
        if (avatar == null) {
            return;
        }

        if (session == null) {
            avatar.setIcon(null);
            return;
        }

        try {
            byte[] photoBytes = userService.getUserPhoto(session.userId());
            if (photoBytes != null && photoBytes.length > 0) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(photoBytes));
                if (img != null) {
                    avatar.setIcon(new ImageIcon(img));
                } else {
                    avatar.setIcon(null);
                }
            } else {
                avatar.setIcon(null);
            }
        } catch (Exception ex) {
            avatar.setIcon(null);
        }
    }
    
    
    private void populateSessionContext(Session s) {
    if (s == null) {
        return;
    }

    int storeId = AppConfig.getInt("app.storeId", 1);
    int terminalId = AppConfig.getInt("app.terminalId", 1);

    if (s.getStoreId() <= 0) {
        s.setStoreId(storeId);
    }
    if (s.getTerminalId() <= 0) {
        s.setTerminalId(terminalId);
    }

    if (s.getStoreCode() == null || s.getStoreCode().isBlank()) {
        s.setStoreCode("STORE-" + s.getStoreId());
    }
    if (s.getStoreName() == null || s.getStoreName().isBlank()) {
        s.setStoreName("Store " + s.getStoreId());
    }
    if (s.getTerminalCode() == null || s.getTerminalCode().isBlank()) {
        s.setTerminalCode("TERM-" + s.getTerminalId());
    }
    if (s.getTerminalName() == null || s.getTerminalName().isBlank()) {
        s.setTerminalName("Terminal " + s.getTerminalId());
    }
}

}
