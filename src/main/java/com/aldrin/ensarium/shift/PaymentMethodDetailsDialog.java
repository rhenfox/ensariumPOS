package com.aldrin.ensarium.shift;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class PaymentMethodDetailsDialog extends JDialog {

    private final JLabel lblHeader = new JLabel();
    private final DetailsTableModel tableModel = new DetailsTableModel();
    private final NumberFormat money = NumberFormat.getNumberInstance();
    JTable table = new JTable(tableModel);

    public PaymentMethodDetailsDialog(java.awt.Window owner, SalesDao.MethodTotal methodTotal, List<SalesDao.PaymentDetail> details) {
        super(owner, "Payment Method Details", Dialog.ModalityType.APPLICATION_MODAL);
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);
        buildUi(methodTotal, details);
        pack();
        setSize(1060, 620);
        setLocationRelativeTo(owner);

    }

    private void buildUi(SalesDao.MethodTotal methodTotal, List<SalesDao.PaymentDetail> details) {
        lblHeader.setText("Payment Method: " + methodTotal.methodCode() + " - " + methodTotal.methodName()
                + " | Transactions: " + details.size()
                + " | Total: " + money.format(methodTotal.amount()));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

//        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
//        left.setHorizontalAlignment(SwingConstants.LEFT);
//        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
//        right.setHorizontalAlignment(SwingConstants.RIGHT);
//
        ////        table.setShowGrid(false);
//        table.setIntercellSpacing(new Dimension(0, 0));
//        table.setSelectionBackground(new Color(0xD6E9FF));
//        table.setSelectionForeground(Color.BLACK);
//
//        table.getColumnModel().getColumn(0).setCellRenderer(left);
//        table.getColumnModel().getColumn(1).setCellRenderer(left);
//        table.getColumnModel().getColumn(2).setCellRenderer(right);
//        table.getColumnModel().getColumn(3).setCellRenderer(left);
//        table.getColumnModel().getColumn(4).setCellRenderer(left);
////        table.setRowHeight(24);
//
//        final TableCellRenderer hdrRenderer = table.getTableHeader().getDefaultRenderer();
//        table.getTableHeader().setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
//            Component c = hdrRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
//
//            if (c instanceof JLabel l) {
//                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0D7DE)));
//                if (col == 0) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                }
//                if (col == 1) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                } else if (col == 2) {
//                    l.setHorizontalAlignment(SwingConstants.RIGHT);
//                } else if (col == 3) {
//                    l.setHorizontalAlignment(SwingConstants.CENTER);
//                } else if (col == 4) {
//                    l.setHorizontalAlignment(SwingConstants.LEFT);
//                }
//            }
//            return c;
//        });
//
//        table.getColumnModel().getColumn(0).setPreferredWidth(150);
//        table.getColumnModel().getColumn(1).setPreferredWidth(150);
//        table.getColumnModel().getColumn(2).setPreferredWidth(80);
//        table.getColumnModel().getColumn(3).setPreferredWidth(100);


        installTableHover();

        tableModel.setRows(details);

        Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);
        StyledButton btnClose = new StyledButton();
        btnClose.setText("Close");
        btnClose.setIcon(iconClose);
        btnClose.setDanger();

        btnClose.addActionListener(e -> dispose());

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        north.add(lblHeader);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        south.add(btnClose);
        JPanel east = new JPanel();
        JPanel weast = new JPanel();
        east.setPreferredSize(new Dimension(5, 10));
        weast.setPreferredSize(new Dimension(5, 10));

        setLayout(new BorderLayout(8, 8));
        add(north, BorderLayout.NORTH);
        add(new com.aldrin.ensarium.ui.widgets.RoundedScrollPane(table), BorderLayout.CENTER);
        add(east, BorderLayout.EAST);
        add(weast, BorderLayout.WEST);
        add(south, BorderLayout.SOUTH);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        BootstrapTableStyle.installAll(this);

        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnRight(table, 2);
        BootstrapTableStyle.setColumnCenter(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);

        BootstrapTableStyle.setHeaderLeft(table, 0);
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderRight(table, 2);
        BootstrapTableStyle.setHeaderCenter(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
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

    private class DetailsTableModel extends AbstractTableModel {

        private final String[] columns = {"Sale No", "Sold At", "Amount", "Reference No", "Notes"};
        private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        private List<SalesDao.PaymentDetail> rows = new ArrayList<>();

        public void setRows(List<SalesDao.PaymentDetail> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SalesDao.PaymentDetail row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    row.saleNo();
                case 1 ->
                    row.soldAt() != null ? row.soldAt().format(dtf) : "";
                case 2 ->
                    money.format(row.amount());
                case 3 ->
                    row.referenceNo();
                case 4 ->
                    row.notes();
                default ->
                    "";
            };
        }
    }
}
