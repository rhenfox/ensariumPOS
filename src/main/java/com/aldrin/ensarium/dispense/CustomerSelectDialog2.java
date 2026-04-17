/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.RoundedScrollPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class CustomerSelectDialog2 extends javax.swing.JDialog {

    private final CustomerDao dao = new CustomerDao();
//    private final JTextField txtSearch = new JTextField();
    private final CustomerSelectDialog2.CustomerTableModel model = new CustomerSelectDialog2.CustomerTableModel();
//    private final JTable table = new JTable(model);
    private CustomerRef result;
    private boolean cleared;

    /**
     * Creates new form CustomerSelectDialog2
     */
    Icon iconSearch = FaSwingIcons.icon(FontAwesomeIcon.SEARCH, 24, Color.WHITE);
    Icon iconSelect = FaSwingIcons.icon(FontAwesomeIcon.MOUSE_POINTER, 24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);
    Icon iconWalkIn = Icons.walking(24, Color.WHITE);

    public CustomerSelectDialog2(java.awt.Window owner) {
        super(owner, "Select Customer", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();

        txtSearch.putClientProperty("JTextField.placeholderText", "Search customer");

        btnCancel.setDanger();
        btnWalkIn.setWarning();
        btnFind.setSecondary();

        btnFind.setIcon(iconSearch);
        btnCancel.setIcon(iconCancel);
        btnSelect.setIcon(iconSelect);
        btnWalkIn.setIcon(iconWalkIn);
        panelTablel.add(new RoundedScrollPane(table), BorderLayout.CENTER);
        btnFind.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        btnSelect.addActionListener(e -> choose());
        btnWalkIn.addActionListener(e -> {
            cleared = true;
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
        btnCancel.addActionListener(e -> dispose());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    choose();
                }
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

//        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
//        left.setHorizontalAlignment(SwingConstants.LEFT);
//        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
//        right.setHorizontalAlignment(SwingConstants.RIGHT);
//
        ////        table.setShowGrid(false);
//
//        table.setIntercellSpacing(new Dimension(0, 0));
//        table.setSelectionBackground(new Color(0xD6E9FF));
//        table.setSelectionForeground(Color.BLACK);
//
//        table.getColumnModel().getColumn(0).setCellRenderer(left);
//        table.getColumnModel().getColumn(1).setCellRenderer(left);
//        table.getColumnModel().getColumn(2).setCellRenderer(left);
//        table.getColumnModel().getColumn(3).setCellRenderer(left);
//        table.getColumnModel().getColumn(4).setCellRenderer(left);
//
//        table.getTableHeader().setBorder(
//                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE))
//        );
//
//        final TableCellRenderer hdrRenderer = table.getTableHeader().getDefaultRenderer();
//        table.getTableHeader().setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
//            Component c = hdrRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
//
//            if (c instanceof JLabel l) {
//                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE)));
//                if (col == 0) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                } else if (col == 1) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                } else if (col == 2) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                } else if (col == 3) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                } else if (col == 4) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                }
//            }
//            return c;
//        });
//
//        table.getColumnModel().getColumn(0).setPreferredWidth(120);
//        table.getColumnModel().getColumn(1).setPreferredWidth(120);
//        table.getColumnModel().getColumn(2).setPreferredWidth(120);
//        table.getColumnModel().getColumn(3).setPreferredWidth(120);
//        table.getColumnModel().getColumn(4).setPreferredWidth(200);
//
//        installTableHover();


//        hideColumn(0);
//        hideColumn(1);
//
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);

        BootstrapTableStyle.setHeaderLeft(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
        
        installTableHover();

        setLocationRelativeTo(owner);
        search();
    }

    private void hideColumn(int i) {
        table.getColumnModel().getColumn(i).setMinWidth(0);
        table.getColumnModel().getColumn(i).setMaxWidth(0);
        table.getColumnModel().getColumn(i).setPreferredWidth(0);
        table.getColumnModel().getColumn(i).setResizable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelTablel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        btnFind = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel4 = new javax.swing.JPanel();
        btnSelect = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnWalkIn = new com.aldrin.ensarium.ui.widgets.StyledButton();
        btnCancel = new com.aldrin.ensarium.ui.widgets.StyledButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Customer");
        setResizable(false);

        jPanel1.setLayout(new java.awt.BorderLayout());

        panelTablel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(panelTablel, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(915, 50));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        txtSearch.setPreferredSize(new java.awt.Dimension(735, 27));
        jPanel2.add(txtSearch, java.awt.BorderLayout.CENTER);

        jPanel8.setPreferredSize(new java.awt.Dimension(884, 10));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 884, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel8, java.awt.BorderLayout.NORTH);

        jPanel9.setPreferredSize(new java.awt.Dimension(884, 10));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 884, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jPanel10.setPreferredSize(new java.awt.Dimension(10, 25));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel10, java.awt.BorderLayout.WEST);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel7.setPreferredSize(new java.awt.Dimension(100, 0));
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));

        btnFind.setText("Find");
        jPanel7.add(btnFind);

        jPanel3.add(jPanel7, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setPreferredSize(new java.awt.Dimension(915, 50));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));

        btnSelect.setText("Select");
        jPanel4.add(btnSelect);

        btnWalkIn.setText("Walk-in");
        jPanel4.add(btnWalkIn);

        btnCancel.setText("Cancel");
        jPanel4.add(btnCancel);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jPanel5.setPreferredSize(new java.awt.Dimension(10, 307));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 409, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setPreferredSize(new java.awt.Dimension(10, 307));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 409, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel6, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(1000, 518));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.aldrin.ensarium.ui.widgets.StyledButton btnCancel;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnFind;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnSelect;
    private com.aldrin.ensarium.ui.widgets.StyledButton btnWalkIn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel panelTablel;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    private void search() {
        try (var conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            model.setRows(dao.search(conn, txtSearch.getText().trim()));
            conn.commit();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void choose() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        result = model.get(row);
        dispose();
    }

    public CustomerRef showDialog() {
        setVisible(true);
        return result;
    }

    public boolean isCleared() {
        return cleared;
    }

    private static class CustomerTableModel extends AbstractTableModel {

        private final String[] cols = {"No", "Name", "Type", "TIN", "Address"};
        private List<CustomerRef> rows = new ArrayList<>();

        public void setRows(List<CustomerRef> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public CustomerRef get(int i) {
            return rows.get(i);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            CustomerRef x = rows.get(r);
            return switch (c) {
                case 0 ->
                    x.customerNo;
                case 1 ->
                    x.fullName;
                case 2 ->
                    x.benefitLabel();
                case 3 ->
                    x.tinNo;
                default ->
                    x.address;
            };
        }
    }

    private int hoveredRow = -1;
    private final JTable table = new JTable(model) {
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
