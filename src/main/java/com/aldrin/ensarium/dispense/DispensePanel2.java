/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class DispensePanel2 extends javax.swing.JPanel {

    /**
     * Creates new form DispensePanel2
     */
//    private final DispenseService service = new DispenseService();
//    private final ReceiptService receiptService = new ReceiptService();
//    private final HotkeyService hotkeyService = new HotkeyService();
//    private final CustomerDao customerDao = new CustomerDao();
//
//    private final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
//
//    private final DispenseTableModel tableModel = new DispenseTableModel(service.lines());
//    private final JTable table = new JTable(tableModel);
//    private final Session session;
    private final Session session;
    private final DispenseService service;
    private final ReceiptService receiptService = new ReceiptService();
    private final HotkeyService hotkeyService = new HotkeyService();
    private final CustomerDao customerDao = new CustomerDao();

    private final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private final DispenseTableModel tableModel;
    private final JTable table;

    Color color = new Color(0x6E6E6E);
    private boolean showingLastPayment = false;

    Icon iconSearch = FaSwingIcons.icon(FontAwesomeIcon.SEARCH, 24, Color.WHITE);
    Icon iconCustomer = FaSwingIcons.icon(FontAwesomeIcon.USER, 24, Color.WHITE);
    Icon iconDiscount = FaSwingIcons.icon(FontAwesomeIcon.TAGS, 24, Color.WHITE);
    Icon iconHold = Icons.handHolding(18, Color.WHITE);
    Icon iconUnHold = Icons.handUnHolding(18, Color.WHITE);
    Icon iconNew = FaSwingIcons.icon(FontAwesomeIcon.FILE_ALT, 24, Color.WHITE);
    Icon iconPay = Icons.moneyCheckAlt(18, Color.WHITE);
    Icon iconHistory = FaSwingIcons.icon(FontAwesomeIcon.HISTORY, 24, Color.WHITE);
    Icon iconReturn = FaSwingIcons.icon(FontAwesomeIcon.MAIL_REPLY, 24, Color.WHITE);
    Icon iconHotkeys = FaSwingIcons.icon(FontAwesomeIcon.KEYBOARD_ALT, 24, Color.WHITE);

    private final Timer sessionTimer = new Timer(2000, e -> refreshSessionInfo());

    public DispensePanel2(Session session) {
        this.session = session;
        this.service = new DispenseService(session);
        this.tableModel = new DispenseTableModel(service.lines());
        this.table = new JTable(tableModel);

        initComponents();

//        service.initShift();
        inputCards.add(txtBarcode, "BARCODE");
        inputCards.add(txtSearch, "SEARCH");

//        txtSearch.putClientProperty("JComponent.arc", 10);
//        txtBarcode.putClientProperty("JComponent.arc", 10);
//        margin
        lSubtotal.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        lDiscount.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        lTax.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        lTotal.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
//        lPayTotal.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
//        lRendered.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
//        lChange.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        lblSubtotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblDiscount.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblTax.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblPayTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblTendered.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        lblChange.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        btnSearch.setIcon(iconSearch);
        btnCustomer.setIcon(iconCustomer);
        btnDiscount.setIcon(iconDiscount);
        btnHold.setIcon(iconHold);
        btnLoadHold.setIcon(iconUnHold);
        btnNew.setIcon(iconNew);
        btnPay.setIcon(iconPay);
        btnHistory.setIcon(iconHistory);
        btnReturn.setIcon(iconReturn);
        btnHotkeys.setIcon(iconHotkeys);

        btnSearch.setFocusable(false);
        btnCustomer.setFocusable(false);
        btnDiscount.setFocusable(false);
        btnHold.setFocusable(false);
        btnLoadHold.setFocusable(false);
        btnNew.setFocusable(false);
        btnPay.setFocusable(false);
        btnHistory.setFocusable(false);
        btnReturn.setFocusable(false);
        btnHotkeys.setFocusable(false);
        txtSearch.setFocusable(false);
        tglInput.setFocusable(false);

        txtBarcode.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (showingLastPayment) {
                        clearPaymentSummary();
                    }

                    String bc = txtBarcode.getText().trim();
                    txtBarcode.setText("");

                    if (bc.isEmpty()) {
                        return;
                    }

                    try {
                        service.addByBarcode(bc);
                        tableModel.fireAll();
                        refreshTotals();
                        requestBarcodeFocus();
                    } catch (Exception ex) {
                        showErr(ex);
                        requestBarcodeFocus();
                    }
                }
            }
        });

        lblGrandTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        txtBarcode.putClientProperty("JTextField.placeholderText", "Enter barcode");
//        txtBarcode.putClientProperty("JComponent.roundRect", true);

        txtSearch.putClientProperty("JTextField.placeholderText", "Search product");
//        txtSearch.putClientProperty("JComponent.roundRect", true);

        tglInput.setSelected(true);
        tglInput.addActionListener(e -> setInputMode(tglInput.isSelected()));

//        Icon iconEdit1 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 120, 215));
//        Icon iconEdit2 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 90, 180));
        Icon iconEdit1 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(33, 37, 41));
        Icon iconEdit2 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(20, 24, 28)); // darker shade
        Icon iconDelete1 = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 23, new Color(220, 53, 69));
        Icon iconDelete2 = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 23, new Color(176, 42, 55));

        ButtonColumn colEdit = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                editQty(modelRow);
            }
        }, 7);
        colEdit.setIcons(iconEdit1, iconEdit2);

        ButtonColumn colDel = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                deleteRow(modelRow);
            }
        }, 8);
        colDel.setIcons(iconDelete1, iconDelete2);

        tablePanel.add(new RoundedScrollPane(table), BorderLayout.CENTER);

        wireEvents();
        reloadHotkeys();
        refreshSessionInfo();
        refreshCustomerLabel();
        refreshTotals();
        focusBarcode();
        sessionTimer.setRepeats(true);
        sessionTimer.start();

        hideColumn(0);
        hideColumn(1);

        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(420);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setPreferredWidth(60);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnRight(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnRight(table, 4);
        BootstrapTableStyle.setColumnRight(table, 5);
        BootstrapTableStyle.setColumnRight(table, 6);
        BootstrapTableStyle.setColumnCenter(table, 7);
        BootstrapTableStyle.setColumnCenter(table, 8);

        BootstrapTableStyle.setHeaderRight(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderRight(table, 4);
        BootstrapTableStyle.setHeaderRight(table, 5);
        BootstrapTableStyle.setHeaderRight(table, 6);
        BootstrapTableStyle.setHeaderCenter(table, 7);
        BootstrapTableStyle.setHeaderCenter(table, 8);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        bootstrapPanel2 = new com.aldrin.ensarium.ui.widgets.BootstrapPanel();
        jPanel41 = new javax.swing.JPanel();
        lblPayTotal = new javax.swing.JLabel();
        jPanel42 = new javax.swing.JPanel();
        lblTendered = new javax.swing.JLabel();
        jPanel43 = new javax.swing.JPanel();
        lblChange = new javax.swing.JLabel();
        jPanel39 = new javax.swing.JPanel();
        jPanel37 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        jPanelAmountDetails = new javax.swing.JPanel();
        bootstrapPanel1 = new com.aldrin.ensarium.ui.widgets.BootstrapPanel();
        jPanel23 = new javax.swing.JPanel();
        lblSubtotal = new javax.swing.JLabel();
        lSubtotal = new javax.swing.JLabel();
        jPanel34 = new javax.swing.JPanel();
        lblDiscount = new javax.swing.JLabel();
        lDiscount = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        lblTax = new javax.swing.JLabel();
        lTax = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lTotal = new javax.swing.JLabel();
        curvedPanel1 = new com.aldrin.ensarium.ui.widgets.CurvedPanel();
        lblGrandTotal = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jPanel22 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        tglInput = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        inputCards = new javax.swing.JPanel();
        txtBarcode = new javax.swing.JTextField();
        txtSearch = new javax.swing.JTextField();
        jPanel31 = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblCustomer = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        lblCustomerType = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        lblCurrentUser = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        lblShift = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        btnSearch = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnCustomer = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnDiscount = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnHold = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnLoadHold = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel13 = new javax.swing.JPanel();
        btnNew = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnPay = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnHistory = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnReturn = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnHotkeys = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel8.setPreferredSize(new java.awt.Dimension(10, 650));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 631, Short.MAX_VALUE)
        );

        add(jPanel8, java.awt.BorderLayout.WEST);

        jPanel9.setPreferredSize(new java.awt.Dimension(350, 650));
        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel18.setPreferredSize(new java.awt.Dimension(330, 300));
        jPanel18.setLayout(new java.awt.BorderLayout());

        jPanel24.setPreferredSize(new java.awt.Dimension(330, 350));
        jPanel24.setLayout(new java.awt.BorderLayout());

        jPanel17.setPreferredSize(new java.awt.Dimension(330, 320));
        jPanel17.setLayout(new java.awt.BorderLayout());

        bootstrapPanel2.setTitle("Amount Summary");
        bootstrapPanel2.setVariantName("SUCCESS");
        bootstrapPanel2.getBodyPanel().setLayout(new java.awt.GridLayout(0, 1));

        jPanel41.setBackground(new java.awt.Color(102, 102, 102));
        jPanel41.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18), new java.awt.Color(102, 102, 102))); // NOI18N
        jPanel41.setOpaque(false);
        jPanel41.setLayout(new java.awt.BorderLayout());

        lblPayTotal.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblPayTotal.setForeground(new java.awt.Color(99, 99, 99));
        lblPayTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPayTotal.setText("0.00");
        jPanel41.add(lblPayTotal, java.awt.BorderLayout.CENTER);

        bootstrapPanel2.getBodyPanel().add(jPanel41);

        jPanel42.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Rendered", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18), new java.awt.Color(102, 102, 102))); // NOI18N
        jPanel42.setForeground(new java.awt.Color(102, 102, 102));
        jPanel42.setOpaque(false);
        jPanel42.setLayout(new java.awt.BorderLayout());

        lblTendered.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblTendered.setForeground(new java.awt.Color(99, 99, 99));
        lblTendered.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTendered.setText("0.00");
        jPanel42.add(lblTendered, java.awt.BorderLayout.CENTER);

        bootstrapPanel2.getBodyPanel().add(jPanel42);

        jPanel43.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Change", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18), new java.awt.Color(102, 102, 102))); // NOI18N
        jPanel43.setOpaque(false);
        jPanel43.setLayout(new java.awt.BorderLayout(10, 0));

        lblChange.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblChange.setForeground(new java.awt.Color(99, 99, 99));
        lblChange.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblChange.setText("0.00");
        jPanel43.add(lblChange, java.awt.BorderLayout.CENTER);

        bootstrapPanel2.getBodyPanel().add(jPanel43);

        jPanel17.add(bootstrapPanel2, java.awt.BorderLayout.CENTER);

        jPanel24.add(jPanel17, java.awt.BorderLayout.CENTER);

        jPanel18.add(jPanel24, java.awt.BorderLayout.SOUTH);

        jPanel39.setPreferredSize(new java.awt.Dimension(330, 20));

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jPanel18.add(jPanel39, java.awt.BorderLayout.NORTH);

        jPanel20.add(jPanel18, java.awt.BorderLayout.CENTER);

        jPanel37.setOpaque(false);
        jPanel37.setLayout(new java.awt.BorderLayout());

        jPanel16.setOpaque(false);
        jPanel16.setLayout(new java.awt.BorderLayout());

        jPanel38.setOpaque(false);
        jPanel38.setPreferredSize(new java.awt.Dimension(330, 10));

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 330, Short.MAX_VALUE)
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel16.add(jPanel38, java.awt.BorderLayout.NORTH);

        jPanelAmountDetails.setPreferredSize(new java.awt.Dimension(330, 200));
        jPanelAmountDetails.setLayout(new java.awt.BorderLayout());

        bootstrapPanel1.setTitle("Amount Details");
        bootstrapPanel1.setVariantName("INFO");
        bootstrapPanel1.getBodyPanel().setLayout(new java.awt.GridLayout(0, 1));

        jPanel23.setOpaque(false);
        jPanel23.setLayout(new java.awt.BorderLayout());

        lblSubtotal.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblSubtotal.setForeground(new java.awt.Color(99, 99, 99));
        lblSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSubtotal.setText("0.00");
        jPanel23.add(lblSubtotal, java.awt.BorderLayout.CENTER);

        lSubtotal.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lSubtotal.setForeground(new java.awt.Color(99, 99, 99));
        lSubtotal.setText("Subtotal");
        jPanel23.add(lSubtotal, java.awt.BorderLayout.WEST);

        bootstrapPanel1.getBodyPanel().add(jPanel23);

        jPanel34.setOpaque(false);
        jPanel34.setLayout(new java.awt.BorderLayout());

        lblDiscount.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblDiscount.setForeground(new java.awt.Color(99, 99, 99));
        lblDiscount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDiscount.setText("0.00");
        jPanel34.add(lblDiscount, java.awt.BorderLayout.CENTER);

        lDiscount.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lDiscount.setForeground(new java.awt.Color(99, 99, 99));
        lDiscount.setText("Discount");
        jPanel34.add(lDiscount, java.awt.BorderLayout.WEST);

        bootstrapPanel1.getBodyPanel().add(jPanel34);

        jPanel35.setOpaque(false);
        jPanel35.setLayout(new java.awt.BorderLayout());

        lblTax.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblTax.setForeground(new java.awt.Color(99, 99, 99));
        lblTax.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTax.setText("0.00");
        jPanel35.add(lblTax, java.awt.BorderLayout.CENTER);

        lTax.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lTax.setForeground(new java.awt.Color(99, 99, 99));
        lTax.setText("Tax");
        jPanel35.add(lTax, java.awt.BorderLayout.WEST);

        bootstrapPanel1.getBodyPanel().add(jPanel35);

        jPanel36.setOpaque(false);
        jPanel36.setLayout(new java.awt.BorderLayout());

        lblTotal.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(99, 99, 99));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setText("0.00");
        jPanel36.add(lblTotal, java.awt.BorderLayout.CENTER);

        lTotal.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lTotal.setForeground(new java.awt.Color(99, 99, 99));
        lTotal.setText("Total");
        jPanel36.add(lTotal, java.awt.BorderLayout.WEST);

        bootstrapPanel1.getBodyPanel().add(jPanel36);

        jPanelAmountDetails.add(bootstrapPanel1, java.awt.BorderLayout.CENTER);

        jPanel16.add(jPanelAmountDetails, java.awt.BorderLayout.SOUTH);

        jPanel37.add(jPanel16, java.awt.BorderLayout.CENTER);

        curvedPanel1.setLayout(new java.awt.BorderLayout());

        lblGrandTotal.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblGrandTotal.setForeground(new java.awt.Color(255, 255, 255));
        lblGrandTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblGrandTotal.setText("0.00");
        curvedPanel1.add(lblGrandTotal, java.awt.BorderLayout.CENTER);

        jPanel37.add(curvedPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel20.add(jPanel37, java.awt.BorderLayout.NORTH);

        jPanel9.add(jPanel20, java.awt.BorderLayout.CENTER);

        jPanel21.setPreferredSize(new java.awt.Dimension(10, 574));

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 631, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel21, java.awt.BorderLayout.WEST);

        jPanel22.setPreferredSize(new java.awt.Dimension(10, 574));

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 631, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel22, java.awt.BorderLayout.EAST);

        add(jPanel9, java.awt.BorderLayout.EAST);

        jPanel14.setPreferredSize(new java.awt.Dimension(1235, 10));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1561, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        add(jPanel14, java.awt.BorderLayout.PAGE_START);

        jPanel25.setLayout(new java.awt.BorderLayout());

        tablePanel.setLayout(new java.awt.BorderLayout());
        jPanel25.add(tablePanel, java.awt.BorderLayout.CENTER);

        jPanel28.setPreferredSize(new java.awt.Dimension(1515, 120));
        jPanel28.setLayout(new java.awt.BorderLayout());

        jPanel30.setMinimumSize(new java.awt.Dimension(40, 7));
        jPanel30.setPreferredSize(new java.awt.Dimension(1515, 50));
        jPanel30.setLayout(new java.awt.BorderLayout(5, 0));

        tglInput.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tglInput.setPreferredSize(new java.awt.Dimension(35, 35));
        jPanel30.add(tglInput, java.awt.BorderLayout.WEST);

        jPanel2.setPreferredSize(new java.awt.Dimension(1515, 5));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1201, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        jPanel30.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel6.setPreferredSize(new java.awt.Dimension(1075, 10));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1201, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel30.add(jPanel6, java.awt.BorderLayout.SOUTH);

        inputCards.setLayout(new java.awt.CardLayout());

        txtBarcode.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        inputCards.add(txtBarcode, "card2");
        inputCards.add(txtSearch, "card3");

        jPanel30.add(inputCards, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel30, java.awt.BorderLayout.SOUTH);

        jPanel31.setPreferredSize(new java.awt.Dimension(1515, 80));
        jPanel31.setRequestFocusEnabled(false);
        jPanel31.setLayout(new java.awt.BorderLayout());

        jPanel32.setPreferredSize(new java.awt.Dimension(0, 30));
        jPanel32.setLayout(new java.awt.GridLayout(1, 0, 20, 0));

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("CUSTOMER");
        jLabel2.setPreferredSize(new java.awt.Dimension(62, 14));
        jPanel1.add(jLabel2, java.awt.BorderLayout.NORTH);

        lblCustomer.setText("Walk-in");
        jPanel1.add(lblCustomer, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel1);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jLabel4.setText("CUSTOMER TYPE");
        jLabel4.setPreferredSize(new java.awt.Dimension(62, 14));
        jPanel3.add(jLabel4, java.awt.BorderLayout.NORTH);

        lblCustomerType.setText("Regular");
        jPanel3.add(lblCustomerType, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel6.setText("USER");
        jLabel6.setPreferredSize(new java.awt.Dimension(62, 14));
        jPanel4.add(jLabel6, java.awt.BorderLayout.NORTH);

        lblCurrentUser.setText("--");
        jPanel4.add(lblCurrentUser, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jLabel8.setText("SHIFT");
        jLabel8.setPreferredSize(new java.awt.Dimension(62, 14));
        jPanel5.add(jLabel8, java.awt.BorderLayout.NORTH);

        lblShift.setText("--");
        jPanel5.add(lblShift, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel5);

        jPanel31.add(jPanel32, java.awt.BorderLayout.CENTER);

        jPanel33.setMinimumSize(new java.awt.Dimension(533, 40));
        jPanel33.setPreferredSize(new java.awt.Dimension(1515, 40));
        jPanel33.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 40)); // NOI18N
        jLabel1.setText("ENSARIUM SUPERMARKET POS");
        jPanel33.add(jLabel1, java.awt.BorderLayout.CENTER);

        jPanel31.add(jPanel33, java.awt.BorderLayout.NORTH);

        jPanel28.add(jPanel31, java.awt.BorderLayout.CENTER);

        jPanel25.add(jPanel28, java.awt.BorderLayout.NORTH);

        add(jPanel25, java.awt.BorderLayout.CENTER);

        jPanel26.setPreferredSize(new java.awt.Dimension(1535, 80));
        jPanel26.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel11.setLayout(new java.awt.GridLayout(1, 0));

        jPanel12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        btnSearch.setText("Search (F2)");
        btnSearch.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnSearch.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel12.add(btnSearch);

        btnCustomer.setText("Customer (F3)");
        btnCustomer.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnCustomer.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel12.add(btnCustomer);

        btnDiscount.setText("Discount (F4)");
        btnDiscount.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnDiscount.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel12.add(btnDiscount);

        btnHold.setText("Hold (F6)");
        btnHold.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel12.add(btnHold);

        btnLoadHold.setText("Unhold (F7)");
        btnLoadHold.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnLoadHold.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel12.add(btnLoadHold);

        jPanel11.add(jPanel12);

        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));

        btnNew.setText("New (Ctrl+N)");
        btnNew.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnNew.setPreferredSize(new java.awt.Dimension(116, 36));
        jPanel13.add(btnNew);

        btnPay.setText("Pay (F8)");
        btnPay.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnPay.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel13.add(btnPay);

        btnHistory.setText("History (F9)");
        btnHistory.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnHistory.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel13.add(btnHistory);

        btnReturn.setText("Return (F10)");
        btnReturn.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnReturn.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel13.add(btnReturn);

        btnHotkeys.setText("Hotkeys (F12)");
        btnHotkeys.setMargin(new java.awt.Insets(2, 1, 3, 1));
        btnHotkeys.setPreferredSize(new java.awt.Dimension(110, 36));
        jPanel13.add(btnHotkeys);

        jPanel11.add(jPanel13);

        jPanel7.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel26.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel10.setPreferredSize(new java.awt.Dimension(1535, 25));
        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        jLabel3.setText("F1 focus barcode. Enter adds item. Search and barcode modes work like a supermarket POS.");
        jPanel10.add(jLabel3);

        jPanel26.add(jPanel10, java.awt.BorderLayout.SOUTH);

        jPanel15.setPreferredSize(new java.awt.Dimension(1535, 10));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1561, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel26.add(jPanel15, java.awt.BorderLayout.NORTH);

        add(jPanel26, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.BootstrapPanel bootstrapPanel1;
    private com.aldrin.ensarium.ui.widgets.BootstrapPanel bootstrapPanel2;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCustomer;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnDiscount;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnHistory;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnHold;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnHotkeys;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnLoadHold;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnNew;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnPay;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnReturn;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnSearch;
    private com.aldrin.ensarium.ui.widgets.CurvedPanel curvedPanel1;
    private javax.swing.JPanel inputCards;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelAmountDetails;
    private javax.swing.JLabel lDiscount;
    private javax.swing.JLabel lSubtotal;
    private javax.swing.JLabel lTax;
    private javax.swing.JLabel lTotal;
    private javax.swing.JLabel lblChange;
    private javax.swing.JLabel lblCurrentUser;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblCustomerType;
    private javax.swing.JLabel lblDiscount;
    private javax.swing.JLabel lblGrandTotal;
    private javax.swing.JLabel lblPayTotal;
    private javax.swing.JLabel lblShift;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTax;
    private javax.swing.JLabel lblTendered;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JToggleButton tglInput;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    public void requestBarcodeFocus() {
        setInputMode(true); // ensures BARCODE card is visible
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (txtBarcode.isShowing() && txtBarcode.isEnabled()) {
                txtBarcode.requestFocusInWindow();
                txtBarcode.selectAll(); // optional
            }
        });
    }

    private void setInputMode(boolean barcodeMode) {
        CardLayout cl = (CardLayout) inputCards.getLayout();

        if (barcodeMode) {
//            tglInput.setText("Barcode Mode");
//            tglInput.setIcon(barcodeModeIcon);
            Icon iconUser = FaSwingIcons.icon(FontAwesomeIcon.BARCODE, 26, color);
            tglInput.setIcon(iconUser);
            cl.show(inputCards, "BARCODE");
            SwingUtilities.invokeLater(() -> txtBarcode.requestFocusInWindow());
            System.out.println("barcode:" + barcodeMode);
        } else {
//            tglInput.setText("Search Mode");
//            tglInput.setIcon(productModeIcon);
            Icon iconUser = FaSwingIcons.icon(FontAwesomeIcon.CUBES, 26, color);
            tglInput.setIcon(iconUser);

            cl.show(inputCards, "SEARCH");
            SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
            System.out.println("product:" + barcodeMode);
        }
    }

    private int hoveredRow = -1;

    private void installTableHover() {
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    table.repaint();
                }
            }
        });
    }

    private void wireEvents() {
        txtBarcode.addActionListener(e -> {
            if (!ensureDispenseShift()) {
                return;
            }
            if (showingLastPayment) {
                clearPaymentSummary();
            }
            String bc = txtBarcode.getText().trim();
            txtBarcode.setText("");
            if (bc.isEmpty()) {
                return;
            }
            try {
                service.addByBarcode(bc);
                tableModel.fireAll();
                refreshTotals();
            } catch (Exception ex) {
                showErr(ex);
            }
        });

        txtSearch.addActionListener(e -> openSearchDialog());
        btnSearch.addActionListener(e -> openSearchDialog());
        btnCustomer.addActionListener(e -> openCustomerDialog());
        btnDiscount.addActionListener(e -> openDiscountDialog());
        btnHold.addActionListener(e -> doHold());
        btnLoadHold.addActionListener(e -> openHoldDialog());
        btnPay.addActionListener(e -> openPayDialog());
        btnHistory.addActionListener(e -> openHistoryDialog());
        btnReturn.addActionListener(e -> openReturnDialog());
        btnNew.addActionListener(e -> newSale());
        btnHotkeys.addActionListener(e -> openHotkeySettings());
    }

    private void refreshTotals() {
        CartTotals t = service.computeTotals();
        lblSubtotal.setText(MONEY.format(t.subtotal));
        lblDiscount.setText(MONEY.format(t.lineDiscounts.add(t.saleDiscount)));
        lblTax.setText(MONEY.format(t.tax));
        lblTotal.setText(MONEY.format(t.grandTotal));
        if (!showingLastPayment) {
            lblPayTotal.setText(MONEY.format(t.grandTotal));
            lblGrandTotal.setText(MONEY.format(t.grandTotal));
        }
    }

    private void editQty(int modelRow) {
        if (!ensureDispenseShift()) {
            return;
        }
        if (modelRow < 0 || modelRow >= service.lines().size()) {
            return;
        }
        if (showingLastPayment) {
            clearPaymentSummary();
        }
        CartLine l = service.lines().get(modelRow);
        String s = JOptionPane.showInputDialog(this,
                "Enter quantity for " + l.productName + " (" + l.uomCode + "):",
                l.qtyUom.toPlainString());
        if (s == null) {
            return;
        }
        try {
            BigDecimal q = new BigDecimal(s.trim());
            if (q.signum() <= 0) {
                throw new IllegalArgumentException("Qty must be > 0");
            }
            service.setQtyAt(modelRow, q);
            tableModel.fireAll();
            refreshTotals();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void deleteRow(int modelRow) {
        if (!ensureDispenseShift()) {
            return;
        }
        if (modelRow < 0 || modelRow >= service.lines().size()) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Remove this item from cart?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        if (showingLastPayment) {
            clearPaymentSummary();
        }
        service.deleteAt(modelRow);
        tableModel.fireAll();
        refreshTotals();
    }

    private void openSearchDialog() {
        if (!ensureDispenseShift()) {
            return;
        }
        if (showingLastPayment) {
            clearPaymentSummary();
        }
        String q = txtSearch.getText().trim();
        SearchProductDialog2 dlg = new SearchProductDialog2(SwingUtilities.getWindowAncestor(this), q);
        SearchProductDialog2.Result chosen = dlg.showDialog();
        if (chosen != null && chosen.product != null) {
            try {
                service.addByProductUom(chosen.product, chosen.qty);
                tableModel.fireAll();
            } catch (Exception ex) {
                showErr(ex);
                return;
            }
            refreshTotals();
            focusBarcode();
        }
    }

    private void openCustomerDialog() {
        if (showingLastPayment) {
            clearPaymentSummary();
        }
        CustomerSelectDialog2 dlg = new CustomerSelectDialog2(SwingUtilities.getWindowAncestor(this));
        CustomerRef c = dlg.showDialog();
        try {
            if (dlg.isCleared()) {
                service.setCustomerId(null);
            } else if (c != null) {
                service.setCustomerId(c.customerId);
            }
            refreshCustomerLabel();
            refreshTotals();
            focusBarcode();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void openHistoryDialog() {
        if (!ensureDispenseShift()) {
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        DonutLoadingDialog loading = new DonutLoadingDialog(owner, "Loading sale history...");
        loading.setVisible(true);

        SwingWorker<SaleHistoryDialog2, Void> worker = new SwingWorker<>() {
            @Override
            protected SaleHistoryDialog2 doInBackground() throws Exception {
                SaleHistoryDialog2 dlg = new SaleHistoryDialog2(owner, service); // only if constructor is light
//            SaleHistoryDialog2 dlg = get();
                dlg.loadHistoryData();
                return dlg;
            }

            @Override
            protected void done() {
                try {
                    SaleHistoryDialog2 dlg = get();
                    loading.dispose();
                    dlg.showDialog();
                    refreshSessionInfo();
                    focusBarcode();
                } catch (Exception ex) {
                    loading.dispose();
                    showErr(ex);
                }
            }
        };

        worker.execute();
    }

    private boolean ensureDispenseShift() {
        try {
            service.assertCurrentUserOpenShift();
            refreshSessionInfo();
            return true;
        } catch (Exception ex) {
            refreshSessionInfo();
            showErr(ex);
            return false;
        }
    }

    public void applyShiftAccess(boolean shiftOpen) {
        txtBarcode.setEnabled(shiftOpen);
        txtSearch.setEnabled(shiftOpen);
        btnSearch.setEnabled(shiftOpen);
        btnDiscount.setEnabled(shiftOpen);
        btnHold.setEnabled(shiftOpen);
        btnLoadHold.setEnabled(shiftOpen);
        btnPay.setEnabled(shiftOpen);
        btnReturn.setEnabled(shiftOpen);
    }

    public void refreshSessionInfo() {
        try {
            DispenseService.CurrentSession session = service.loadCurrentSession();
            lblCurrentUser.setText(session.username == null || session.username.isBlank() ? "-" : session.username);
            lblShift.setText(session.shiftLabel == null || session.shiftLabel.isBlank() ? "No open shift for current user" : session.shiftLabel);
            applyShiftAccess(session.shiftId != null);
        } catch (Exception ex) {
            lblCurrentUser.setText("User error");
            lblShift.setText("Shift unavailable");
            applyShiftAccess(false);
        }
    }

    private void refreshCustomerLabel() {
        Long cid = service.customerId();
        CustomerRef selected = service.selectedCustomer();
        if (cid == null || selected == null) {
            String s = "WALK-IN CUSTOMER";
            if (service.isEditingSale() && service.editingSaleNo() != null) {
                s += " | Editing: " + service.editingSaleNo();
            }
            lblCustomer.setText(s);
            lblCustomerType.setText("Regular");
            return;
        }
        try (java.sql.Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            CustomerRef cr = customerDao.findById(conn, cid);
            conn.commit();
            if (cr == null) {
                lblCustomer.setText("UNKNOWN CUSTOMER");
                lblCustomerType.setText("Regular");
            } else {
                lblCustomer.setText(cr.fullName);
                lblCustomerType.setText(cr.benefitLabel() + (cr.vatExempt ? " / VAT Exempt" : ""));
            }
        } catch (Exception ex) {
            lblCustomer.setText("CUSTOMER ERROR");
            lblCustomerType.setText("Unknown");
        }
    }

    private void openDiscountDialog() {
        if (!ensureDispenseShift()) {
            return;
        }
        if (showingLastPayment) {
            clearPaymentSummary();
        }
        DiscountDialog2 dlg = new DiscountDialog2(SwingUtilities.getWindowAncestor(this), service);
        SaleDiscountInfo di = dlg.showDialog(service.saleDiscount());
        if (di != null) {
            service.setSaleDiscount(di);
        } else {
            service.clearSaleDiscount();
        }
        refreshTotals();
        focusBarcode();
    }

    private void doHold() {
        if (!ensureDispenseShift()) {
            return;
        }
        try {
            clearPaymentSummary();
            long saleId = service.holdCurrent();
            JOptionPane.showMessageDialog(this, "Held as ticket ID " + saleId, "Hold", JOptionPane.INFORMATION_MESSAGE);
            tableModel.fireAll();
            refreshSessionInfo();
            refreshCustomerLabel();
            refreshTotals();
            focusBarcode();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void openHoldDialog() {
        if (!ensureDispenseShift()) {
            return;
        }
        try {
            HoldDialog2 dlg = new HoldDialog2(SwingUtilities.getWindowAncestor(this), service);
            Long saleId = dlg.showDialog();
            if (saleId != null) {
                clearPaymentSummary();
                service.loadHold(saleId);
                tableModel.fireAll();
                refreshCustomerLabel();
                refreshTotals();
                focusBarcode();
            }
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void openPayDialog() {
        if (!ensureDispenseShift()) {
            return;
        }
        try {
            CartTotals t = service.computeTotals();
            PayDialog2 dlg = new PayDialog2(SwingUtilities.getWindowAncestor(this), t.grandTotal);
            PayDialog2.Result pay = dlg.showDialog();
            if (pay == null) {
                return;
            }

            DispenseService.PayResult pr = service.pay(pay.amountRendered, pay.methodId, pay.referenceNo);
            tableModel.fireAll();
            refreshSessionInfo();
            refreshCustomerLabel();
            refreshTotals();

            showingLastPayment = true;
            lblPayTotal.setText(MONEY.format(pr.total));
            lblGrandTotal.setText(MONEY.format(pr.total));
            lblTendered.setText(MONEY.format(pr.cash));
            lblChange.setText(MONEY.format(pr.change));

            receiptService.showSaleReceiptDialog(SwingUtilities.getWindowAncestor(this), pr.saleId);
            focusBarcode();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void openReturnDialog() {
        if (!ensureDispenseShift()) {
            return;
        }
        try {
            ReturnDialog2 dlg = new ReturnDialog2(SwingUtilities.getWindowAncestor(this), service);
            ReturnDialog2.Result r = dlg.showDialog();
            if (r == null) {
                return;
            }

            DispenseService.ReturnResult rr = service.processReturnBySaleNumber(
                    r.saleNumber, r.selectedLines, r.reasonId, r.restockStatus, r.refundMethodId, r.refundReferenceNo);
            JOptionPane.showMessageDialog(this,
                    "Refund: " + MONEY.format(rr.totalRefund) + "\nReturn #: " + rr.returnNumber,
                    "Return Posted", JOptionPane.INFORMATION_MESSAGE);

            receiptService.printReturnReceipt(rr.returnId);
            focusBarcode();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void newSale() {
        clearPaymentSummary();
        service.resetNewSale();
        tableModel.fireAll();
        refreshSessionInfo();
        refreshCustomerLabel();
        refreshTotals();
        focusBarcode();
    }

    private void focusBarcode() {
        setInputMode(true);
    }

    private void openHotkeySettings() {
        try {
            HotkeySettingsDialog2 dlg = new HotkeySettingsDialog2(SwingUtilities.getWindowAncestor(this), hotkeyService);
            boolean changed = dlg.showDialog();
            if (changed) {
                reloadHotkeys();
            }
            focusBarcode();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    private void reloadHotkeys() {
        try {
            Map<String, javax.swing.KeyStroke> hk = hotkeyService.loadEffectiveHotkeys();

            Map<String, Action> actions = new HashMap<>();
            actions.put(HotkeyService.ACT_FOCUS_BARCODE, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    focusBarcode();
                }
            });
            actions.put(HotkeyService.ACT_SEARCH, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openSearchDialog();
                }
            });
            actions.put(HotkeyService.ACT_CUSTOMER, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openCustomerDialog();
                }
            });
            actions.put(HotkeyService.ACT_DISCOUNT, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openDiscountDialog();
                }
            });
            actions.put(HotkeyService.ACT_HOLD, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doHold();
                }
            });
            actions.put(HotkeyService.ACT_LOAD_HOLD, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openHoldDialog();
                }
            });
            actions.put(HotkeyService.ACT_PAY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openPayDialog();
                }
            });
            actions.put(HotkeyService.ACT_RETURN, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openReturnDialog();
                }
            });
            actions.put(HotkeyService.ACT_HISTORY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openHistoryDialog();
                }
            });
            actions.put(HotkeyService.ACT_NEW_SALE, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newSale();
                }
            });
            actions.put(HotkeyService.ACT_HOTKEYS, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openHotkeySettings();
                }
            });

            HotkeyService.bind(this, hk, actions);

            btnSearch.setText("Search (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_SEARCH)) + ")");
            btnCustomer.setText("Customer (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_CUSTOMER)) + ")");
            btnDiscount.setText("Discount (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_DISCOUNT)) + ")");
            btnHold.setText("Hold (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_HOLD)) + ")");
            btnLoadHold.setText("Unhold (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_LOAD_HOLD)) + ")");
            btnPay.setText("Pay (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_PAY)) + ")");
            btnReturn.setText("Return (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_RETURN)) + ")");
            btnHistory.setText("History (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_HISTORY)) + ")");
            btnNew.setText("New (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_NEW_SALE)) + ")");
            btnHotkeys.setText("Hotkeys (" + HotkeyService.toHuman(hk.get(HotkeyService.ACT_HOTKEYS)) + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearPaymentSummary() {
        showingLastPayment = false;
        lblTendered.setText("0.00");
        lblChange.setText("0.00");
    }

    private JPanel labeled(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(6, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x6B7280));
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static JLabel moneyLabel(int size, int align) {
        JLabel lbl = new JLabel("0.00", align);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
        return lbl;
    }

    private JLabel summaryLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(0x4B5563));
        return lbl;
    }

    private JLabel paymentCaption(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private JLabel lightLabel(JLabel lbl) {
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(12, 12, 12, 12)));
        return p;
    }

    private JButton actionButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(0, 46));
        return b;
    }

    private void hideColumn(int i) {
        table.getColumnModel().getColumn(i).setMinWidth(0);
        table.getColumnModel().getColumn(i).setMaxWidth(0);
        table.getColumnModel().getColumn(i).setPreferredWidth(0);
        table.getColumnModel().getColumn(i).setResizable(false);
    }

    private static void installActionColumnHover(JTable table, DispenseTableModel model) {
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int vr = table.rowAtPoint(e.getPoint());
                int vc = table.columnAtPoint(e.getPoint());
                boolean overAction = (vr >= 0 && (vc == 7 || vc == 8));
                if (overAction) {
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    table.putClientProperty("hoverCell", new Point(vr, vc));
                    model.setHoverCell(vr, vc);
                } else {
                    table.setCursor(Cursor.getDefaultCursor());
                    table.putClientProperty("hoverCell", null);
                    model.setHoverCell(-1, -1);
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
                table.putClientProperty("hoverCell", null);
                model.setHoverCell(-1, -1);
            }
        });
    }

    @Override
    public void removeNotify() {
        sessionTimer.stop();
        super.removeNotify();
    }

    private static ImageIcon loadIcon(String path) {
        java.net.URL url = DispensePanel2.class.getResource(path);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }

    private void showErr(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    static class DonutLoadingDialog extends JDialog {

        private final DonutSpinner spinner;

        public DonutLoadingDialog(Window owner, String message) {
            super(owner, "Loading", Dialog.ModalityType.MODELESS);
            setUndecorated(true);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD0D7DE)),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            panel.setBackground(Color.WHITE);

            spinner = new DonutSpinner();

            JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
            lblMessage.setForeground(new Color(60, 60, 60));

            panel.add(spinner, BorderLayout.CENTER);
            panel.add(lblMessage, BorderLayout.SOUTH);

            setContentPane(panel);
            setSize(220, 180);
            setLocationRelativeTo(owner);

            spinner.start();
        }

        @Override
        public void dispose() {
            spinner.stop();
            super.dispose();
        }
    }

    static class DonutSpinner extends JPanel {

        private int angle = 0;
        private final Timer timer;

        public DonutSpinner() {
            setOpaque(false);
            setPreferredSize(new Dimension(90, 90));

            timer = new Timer(20, e -> {
                angle += 8;
                if (angle >= 360) {
                    angle = 0;
                }
                repaint();
            });
        }

        public void start() {
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        public void stop() {
            if (timer.isRunning()) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 14;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // background ring
            g2.setColor(new Color(230, 235, 240));
            g2.drawArc(x, y, size, size, 0, 360);

            // animated arc
            g2.setColor(new Color(0, 120, 215));
            g2.drawArc(x, y, size, size, angle, 110);

            g2.dispose();
        }
    }

}
