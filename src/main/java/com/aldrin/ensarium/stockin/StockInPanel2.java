/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.aldrin.ensarium.stockin;

import com.aldrin.ensarium.dispense.ButtonColumn;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import com.aldrin.ensarium.util.ComboAutoFillSupport;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class StockInPanel2 extends javax.swing.JPanel {

    private final StockInDao stockInDao = new StockInDao();

    private enum FormMode {
        NEW, EDIT, RETURN
    }

    private final StockInPanel2.LineTableModel lineModel = new StockInPanel2.LineTableModel();

    private FormMode formMode = FormMode.NEW;

    private Session session;

    public StockInPanel2(Session session) {
        initComponents();
        this.session = session;
//        table = new JTable(lineModel);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
        table.getColumnModel().getColumn(9).setPreferredWidth(70);

        Icon iconEdit1 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 120, 215));
        Icon iconEdit2 = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, new Color(0, 90, 180));
        Icon iconDelete1 = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 23, new Color(220, 53, 69));
        Icon iconDelete2 = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 23, new Color(176, 42, 55));

        ButtonColumn colEdit = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
//                editQty(modelRow);
                editLine(Integer.parseInt(e.getActionCommand()));
            }
        }, 8);
        colEdit.setIcons(iconEdit1, iconEdit2);

        ButtonColumn colDel = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
//                deleteRow(modelRow);
                deleteLine(Integer.parseInt(e.getActionCommand()));
            }
        }, 9);
        colDel.setIcons(iconDelete1, iconDelete2);

        panelTable.add(new RoundedScrollPane(table), BorderLayout.CENTER);
        loadLookups();
        applyComboBoxAutoFill(cboStore);
        applyComboBoxAutoFill(cboSupplier);
        clearForm();

        Icon iconNew = FaSwingIcons.icon(FontAwesomeIcon.FILE_ALT, 23, Color.WHITE);
        Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 23, Color.WHITE);
        Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 23, Color.WHITE);
        Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 23, Color.WHITE);
        Icon iconReturn = FaSwingIcons.icon(FontAwesomeIcon.REPLY, 23, Color.WHITE);

        btnNew.setIcon(iconNew);
        btnAddProduct.setIcon(iconAdd);
        btnSave.setIcon(iconSave);
        btnOpen.setIcon(iconEdit);
        btnReturn.setIcon(iconReturn);

//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

//        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xD6E9FF));
        table.setSelectionForeground(Color.BLACK);

        table.getColumnModel().getColumn(0).setCellRenderer(left);
        table.getColumnModel().getColumn(1).setCellRenderer(left);
        table.getColumnModel().getColumn(2).setCellRenderer(left);
        table.getColumnModel().getColumn(3).setCellRenderer(left);
        table.getColumnModel().getColumn(4).setCellRenderer(left);
        table.getColumnModel().getColumn(5).setCellRenderer(right);
        table.getColumnModel().getColumn(6).setCellRenderer(right);
        table.getColumnModel().getColumn(7).setCellRenderer(right);

        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE))
        );

        final TableCellRenderer hdrRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            Component c = hdrRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);

            if (c instanceof JLabel l) {
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE)));
                if (col == 0) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 1) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 2) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 3) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 4) {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (col == 5) {
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 6) {
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 7) {
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 8) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col == 9) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                }
            }
            return c;
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        btnNew.addActionListener(e -> clearForm());
        btnAddProduct.addActionListener(e -> addProduct());
        btnOpen.addActionListener(e -> openReceipt());
        btnReturn.addActionListener(e -> returnReceipt());
        btnSave.addActionListener(e -> saveReceipt());
        txtNotes.putClientProperty("JTextField.placeholderText", "Notes");

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        txtReceiptNo = new javax.swing.JTextField();
        cboSupplier = new javax.swing.JComboBox<>();
        jPanel25 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel27 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        txtReceiptId = new javax.swing.JTextField();
        cboStore = new javax.swing.JComboBox<>();
        jPanel20 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtNotes = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        btnNew = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel10 = new javax.swing.JPanel();
        btnAddProduct = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel11 = new javax.swing.JPanel();
        btnSave = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel12 = new javax.swing.JPanel();
        btnOpen = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel13 = new javax.swing.JPanel();
        btnReturn = new com.aldrin.ensarium.ui.widgets.StyledButton();
        panelTable = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.BorderLayout());
        add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel7.setPreferredSize(new java.awt.Dimension(1253, 140));
        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel8.setPreferredSize(new java.awt.Dimension(0, 90));
        jPanel8.setLayout(new java.awt.BorderLayout(0, 5));

        jPanel14.setLayout(new java.awt.BorderLayout());

        jPanel16.setLayout(new java.awt.BorderLayout());

        jPanel24.setLayout(new java.awt.GridLayout(0, 1, 0, 5));
        jPanel24.add(txtReceiptNo);
        jPanel24.add(cboSupplier);

        jPanel16.add(jPanel24, java.awt.BorderLayout.CENTER);

        jPanel25.setPreferredSize(new java.awt.Dimension(100, 110));
        jPanel25.setLayout(new java.awt.GridLayout(0, 1));

        jPanel26.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jLabel7.setText("Receipt No");
        jPanel26.add(jLabel7);

        jPanel25.add(jPanel26);

        jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jLabel8.setText("Supplier");
        jPanel27.add(jLabel8);

        jPanel25.add(jPanel27);

        jPanel16.add(jPanel25, java.awt.BorderLayout.WEST);

        jPanel14.add(jPanel16, java.awt.BorderLayout.CENTER);

        jPanel17.setPreferredSize(new java.awt.Dimension(40, 80));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel14.add(jPanel17, java.awt.BorderLayout.WEST);

        jPanel8.add(jPanel14, java.awt.BorderLayout.CENTER);

        jPanel15.setPreferredSize(new java.awt.Dimension(500, 120));
        jPanel15.setLayout(new java.awt.BorderLayout());

        jPanel19.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        txtReceiptId.setEditable(false);
        txtReceiptId.setEnabled(false);
        jPanel19.add(txtReceiptId);
        jPanel19.add(cboStore);

        jPanel15.add(jPanel19, java.awt.BorderLayout.CENTER);

        jPanel20.setPreferredSize(new java.awt.Dimension(100, 110));
        jPanel20.setLayout(new java.awt.GridLayout(0, 1));

        jPanel21.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jLabel4.setText("Receipt ID");
        jPanel21.add(jLabel4);

        jPanel20.add(jPanel21);

        jPanel22.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jLabel5.setText("Store");
        jPanel22.add(jLabel5);

        jPanel20.add(jPanel22);

        jPanel15.add(jPanel20, java.awt.BorderLayout.WEST);

        jPanel8.add(jPanel15, java.awt.BorderLayout.WEST);

        jPanel18.setPreferredSize(new java.awt.Dimension(1265, 23));
        jPanel18.setLayout(new java.awt.BorderLayout());

        jLabel9.setText("Notes");
        jLabel9.setPreferredSize(new java.awt.Dimension(100, 16));
        jPanel18.add(jLabel9, java.awt.BorderLayout.WEST);
        jPanel18.add(txtNotes, java.awt.BorderLayout.CENTER);

        jPanel8.add(jPanel18, java.awt.BorderLayout.SOUTH);

        jPanel7.add(jPanel8, java.awt.BorderLayout.CENTER);

        jPanel9.setPreferredSize(new java.awt.Dimension(1253, 60));
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 15));

        btnNew.setText("New");
        jPanel9.add(btnNew);

        jPanel10.setPreferredSize(new java.awt.Dimension(10, 5));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel10);

        btnAddProduct.setText("Add");
        jPanel9.add(btnAddProduct);

        jPanel11.setPreferredSize(new java.awt.Dimension(60, 5));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel11);

        btnSave.setText("Save");
        jPanel9.add(btnSave);

        jPanel12.setPreferredSize(new java.awt.Dimension(10, 5));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel12);

        btnOpen.setText("Edit");
        btnOpen.addActionListener(this::btnOpenActionPerformed);
        jPanel9.add(btnOpen);

        jPanel13.setPreferredSize(new java.awt.Dimension(10, 5));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel13);

        btnReturn.setText("Return");
        jPanel9.add(btnReturn);

        jPanel7.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jPanel1.add(jPanel7, java.awt.BorderLayout.NORTH);

        panelTable.setLayout(new java.awt.BorderLayout());
        jPanel1.add(panelTable, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(1273, 30));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("STOCK IN");
        jPanel2.add(jLabel1);

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setPreferredSize(new java.awt.Dimension(1273, 45));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 28)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("TOTAL");
        jLabel2.setPreferredSize(new java.awt.Dimension(130, 30));
        jPanel3.add(jLabel2);

        lblTotal.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setText("0.00");
        lblTotal.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel3.add(lblTotal);

        add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel4.setPreferredSize(new java.awt.Dimension(10, 465));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 565, Short.MAX_VALUE)
        );

        add(jPanel4, java.awt.BorderLayout.LINE_END);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 465));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 565, Short.MAX_VALUE)
        );

        add(jPanel5, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOpenActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnAddProduct;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnNew;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnOpen;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnReturn;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnSave;
    private javax.swing.JComboBox<LookupOption> cboStore;
    private javax.swing.JComboBox<LookupOption> cboSupplier;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
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
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelTable;
    private javax.swing.JTextField txtNotes;
    private javax.swing.JTextField txtReceiptId;
    private javax.swing.JTextField txtReceiptNo;
    // End of variables declaration//GEN-END:variables

    private void loadLookups() {
        try {
//            setComboItems(cboStore, stockInDao.listStores());
//            setComboItems(cboSupplier, stockInDao.listSuppliers());
            
            
            
            for(LookupOption lookUpOption:stockInDao.listStores()){
                cboStore.addItem(lookUpOption);
            }
             for(LookupOption lookUpOption:stockInDao.listSuppliers()){
                cboSupplier.addItem(lookUpOption);
            }
            applyComboBoxAutoFill(cboStore);
            applyComboBoxAutoFill(cboSupplier);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load lookup values from database.\n" + ex.getMessage(),
                    "Load Header Lookups",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct() {
        StockInLine line = new StockInLine();
        StockInLineDialog dialog = new StockInLineDialog(SwingUtilities.getWindowAncestor(this), line);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            lineModel.addLine(line);
            refreshTotals();
        }
    }

    private void editLine(int row) {
        StockInLine line = lineModel.getLine(row);
        StockInLineDialog dialog = new StockInLineDialog(SwingUtilities.getWindowAncestor(this), line);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            lineModel.fireTableRowsUpdated(row, row);
            refreshTotals();
        }
    }

    private void deleteLine(int row) {
        if (JOptionPane.showConfirmDialog(this, "Delete this line?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            lineModel.removeLine(row);
            refreshTotals();
        }
    }

    private void openReceipt() {
        Long receiptId = chooseReceiptId("Open Stock-In Receipt");
        if (receiptId == null) {
            return;
        }
        try {
            loadReceiptIntoForm(receiptId);
            setFormMode(StockInPanel2.FormMode.EDIT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Open Stock-In", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceipt() {
        try {
            LookupOption store = (LookupOption) cboStore.getSelectedItem();
            LookupOption supplier = (LookupOption) cboSupplier.getSelectedItem();
            if (store == null || store.getId() == null) {
                throw new IllegalArgumentException("Store ID is required.");
            }
            Integer receivedByUserId = resolveCurrentUserIdForSave();

            StockInHeader header = new StockInHeader();
            if (!txtReceiptId.getText().trim().isEmpty()) {
                header.setReceiptId(Long.valueOf(Long.parseLong(txtReceiptId.getText().trim())));
            }
            header.setReceiptNo(blankToNull(txtReceiptNo.getText()));
            header.setStoreId(store.getId().intValue());
            header.setSupplierId(supplier == null ? null : supplier.getId());
            header.setReceivedBy(receivedByUserId);
            header.setNotes(txtNotes.getText());

            long receiptId = stockInDao.saveReceipt(header, lineModel.getLines());
            JOptionPane.showMessageDialog(this, "Stock-in saved successfully. Receipt ID: " + receiptId);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Stock-In", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnReceipt() {
        try {
            Long receiptId = resolveReceiptIdForReturn();
            if (receiptId == null) {
                return;
            }
            setFormMode(StockInPanel2.FormMode.RETURN);
            clearGridForReturnSelection();
            LookupOption store = (LookupOption) cboStore.getSelectedItem();
            if (store == null || store.getId() == null) {
                throw new IllegalArgumentException("Store ID is required.");
            }
            int storeId = store.getId().intValue();
            List<ReturnableLine> lines = stockInDao.listReturnableLines(receiptId);
            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No remaining quantities available for return.");
                return;
            }
            StockInReturnDialog dialog = new StockInReturnDialog(SwingUtilities.getWindowAncestor(this), lines);
            dialog.setVisible(true);
            if (!dialog.isProcessed()) {
                return;
            }
            long returnId = stockInDao.createReturn(
                    receiptId,
                    storeId,
                    dialog.getCreatedBy() == null ? resolveCurrentUserIdForSave() : dialog.getCreatedBy(),
                    dialog.getReturnedBy(),
                    dialog.getReturnedByName(),
                    dialog.getReasonId(),
                    dialog.getReasonName(),
                    dialog.getNotes(),
                    dialog.getEntries());
            JOptionPane.showMessageDialog(this, "Stock-in return saved. Inventory Txn ID: " + returnId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Stock-In", JOptionPane.ERROR_MESSAGE);
        } finally {
            setFormMode(StockInPanel2.FormMode.NEW);
        }
    }

    private Long chooseReceiptId(String dialogTitle) {
        ReceiptChooserDialog dialog = new ReceiptChooserDialog(SwingUtilities.getWindowAncestor(this), dialogTitle);
        dialog.setVisible(true);
        return dialog.getSelectedReceiptId();
    }

    private Long resolveReceiptIdForReturn() throws Exception {
        if (!txtReceiptId.getText().trim().isEmpty()) {
            return Long.valueOf(Long.parseLong(txtReceiptId.getText().trim()));
        }
        Long receiptId = chooseReceiptId("Select Stock-In Receipt to Return");
        if (receiptId == null) {
            return null;
        }
        loadReceiptIntoForm(receiptId);
        return receiptId;
    }

    private void loadReceiptIntoForm(Long receiptId) throws Exception {
        LoadedStockIn loaded = stockInDao.loadReceipt(receiptId);
        if (loaded == null) {
            throw new IllegalArgumentException("Stock-in not found.");
        }
        txtReceiptId.setText(String.valueOf(loaded.getHeader().getReceiptId()));
        txtReceiptNo.setText(nvl(loaded.getHeader().getReceiptNo()));
        selectComboById(cboStore, Long.valueOf(loaded.getHeader().getStoreId()));
        selectComboById(cboSupplier, loaded.getHeader().getSupplierId());
        txtNotes.setText(nvl(loaded.getHeader().getNotes()));
        lineModel.setLines(loaded.getLines());
        refreshTotals();
    }

    private void clearGridForReturnSelection() {
        lineModel.setLines(new ArrayList<>());
        refreshTotals();
    }

    private void clearForm() {
        txtReceiptId.setText("");
        txtReceiptNo.setText("");
        if (cboStore.getItemCount() > 0) {
            cboStore.setSelectedIndex(0);
        }
        selectComboById(cboSupplier, null);
        txtNotes.setText("");
        lineModel.setLines(new ArrayList<>());
        refreshTotals();
        setFormMode(StockInPanel2.FormMode.NEW);
    }

    private Integer resolveCurrentUserIdForSave() {
        CurrentUser currentUser = SessionResolver.resolveCurrentUser();
        if (currentUser != null) {
            if (currentUser.getId() != null) {
                return Integer.valueOf(currentUser.getId().intValue());
            }
            try {
                for (LookupOption user : stockInDao.listUsers()) {
                    if (user == null || user.getId() == null) {
                        continue;
                    }
                    boolean usernameMatch = currentUser.getUsername() != null
                            && currentUser.getUsername().trim().equalsIgnoreCase(user.getCode() == null ? "" : user.getCode().trim());
                    boolean fullNameMatch = currentUser.getFullName() != null
                            && currentUser.getFullName().trim().equalsIgnoreCase(user.getName() == null ? "" : user.getName().trim());
                    if (usernameMatch || fullNameMatch) {
                        return Integer.valueOf(user.getId().intValue());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return Integer.valueOf(1);
    }

    private void setFormMode(StockInPanel2.FormMode mode) {
        formMode = mode == null ? StockInPanel2.FormMode.NEW : mode;
        updateActionStates();
    }

    private void updateActionStates() {
        boolean newMode = formMode == StockInPanel2.FormMode.NEW;
        boolean editMode = formMode == StockInPanel2.FormMode.EDIT;
        boolean returnMode = formMode == StockInPanel2.FormMode.RETURN;

        btnNew.setEnabled(true);
        btnOpen.setEnabled(!returnMode);
        btnReturn.setEnabled(!editMode);
        btnAddProduct.setEnabled(!returnMode);
        btnSave.setEnabled(!returnMode);
    }

    private void refreshTotals() {
        BigDecimal total = BigDecimal.ZERO;
        for (StockInLine line : lineModel.getLines()) {
            total = total.add(line.getTotal());
        }
        lblTotal.setText(formatMoney(total));
    }

    private String formatMoney(BigDecimal value) {
        return new DecimalFormat("#,##0.00").format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private String blankToNull(String value) {
        String v = value == null ? null : value.trim();
        return v == null || v.isEmpty() ? null : v;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void setComboItems(JComboBox<LookupOption> combo, List<LookupOption> items) {
        ComboAutoFillSupport.updateItems(combo, items);
    }

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }

    private void selectComboById(JComboBox<LookupOption> combo, Long id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            LookupOption item = combo.getItemAt(i);
            if (id == null) {
                if (item == null || item.getId() == null) {
                    combo.setSelectedIndex(i);
                    return;
                }
            } else if (item != null && id.equals(item.getId())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private static class LineTableModel extends AbstractTableModel {

        private final String[] cols = {"SKU", "Product", "Lot No", "Expiry Date", "Unit", "Qty Base", "Unit Cost", "Total", "    ", "    "};
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        private List<StockInLine> lines = new ArrayList<>();

        public void addLine(StockInLine line) {
            lines.add(line);
            fireTableDataChanged();
        }

        public void removeLine(int index) {
            lines.remove(index);
            fireTableDataChanged();
        }

        public StockInLine getLine(int index) {
            return lines.get(index);
        }

        public List<StockInLine> getLines() {
            return lines;
        }

        public void setLines(List<StockInLine> lines) {
            this.lines = lines == null ? new ArrayList<>() : lines;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return lines.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 8 || columnIndex == 9;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StockInLine line = lines.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    line.getProduct().getSku();
                case 1 ->
                    line.getProduct().getName();
                case 2 ->
                    line.getLotNo();
                case 3 ->
                    line.getExpiryDate() == null ? "" : sdf.format(line.getExpiryDate());
                case 4 ->
                    line.getUnit() == null ? "" : line.getUnit().getCode();
                case 5 ->
                    line.getQuantityInBase().setScale(1, RoundingMode.HALF_UP);   
                case 6 ->
                    line.getUnitCost().setScale(2, RoundingMode.HALF_UP);
                case 7 ->
                    line.getTotal().setScale(2, RoundingMode.HALF_UP);
                case 8 ->
                    "Edit";
                case 9 ->
                    "Delete";
                default ->
                    "";
            };
        }
    }
    
    
    
    private int hoveredRow = -1;
    private final JTable table = new JTable(lineModel) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            if (!isRowSelected(row)) {
                if (row == hoveredRow) {
                    c.setBackground(new Color(0xE8F2FF)); // hover color
                } else {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(0xF7F9FC)); // stripes
                }
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(getSelectionBackground());
                c.setForeground(getSelectionForeground());
            }

            if (c instanceof JComponent jc) {
                jc.setOpaque(true);
            }

            return c;
        }
    };

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
}
