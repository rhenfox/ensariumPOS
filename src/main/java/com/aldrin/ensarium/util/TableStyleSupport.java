package com.aldrin.ensarium.util;

//import javax.swing.*;
//import javax.swing.border.Border;
//import javax.swing.border.EmptyBorder;
//import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.JTableHeader;
//import javax.swing.table.TableCellRenderer;
//import javax.swing.table.TableModel;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.math.BigDecimal;
//
//public final class TableStyleSupport {
//
//    private static final String HOVER_ROW_KEY = "table.hoverRow";
//    private static final Border LEFT_PAD = new EmptyBorder(6, 0, 6, 0);
//    private static final Border RIGHT_PAD = new EmptyBorder(6, 0, 6, 0);
//    private static final Border CENTER_PAD = new EmptyBorder(6, 4, 6, 4);
//
//    private TableStyleSupport() {
//    }
//
//    public static void apply(JTable table) {
//        table.setFillsViewportHeight(true);
//        table.setShowHorizontalLines(false);
//        table.setShowVerticalLines(false);
//        table.setIntercellSpacing(new Dimension(0, 0));
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        table.putClientProperty(HOVER_ROW_KEY, -1);
//
//        installHover(table);
//        installHeader(table);
//        installBodyRenderers(table);
//    }
//
//    private static void installHover(JTable table) {
//        MouseAdapter adapter = new MouseAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                setHoverRow(table, table.rowAtPoint(e.getPoint()));
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                setHoverRow(table, -1);
//            }
//        };
//        table.addMouseMotionListener(adapter);
//        table.addMouseListener(adapter);
//    }
//
//    private static void setHoverRow(JTable table, int viewRow) {
//        Object old = table.getClientProperty(HOVER_ROW_KEY);
//        int previous = old instanceof Integer ? (Integer) old : -1;
//        if (previous == viewRow) {
//            return;
//        }
//        table.putClientProperty(HOVER_ROW_KEY, viewRow);
//        if (previous >= 0) {
//            Rectangle r = table.getCellRect(previous, 0, true);
//            if (r != null) {
//                table.repaint(0, r.y, table.getWidth(), r.height);
//            }
//        }
//        if (viewRow >= 0) {
//            Rectangle r = table.getCellRect(viewRow, 0, true);
//            if (r != null) {
//                table.repaint(0, r.y, table.getWidth(), r.height);
//            }
//        }
//    }
//
//    private static void installHeader(JTable table) {
//        JTableHeader header = table.getTableHeader();
//        if (header == null) {
//            return;
//        }
//        header.setReorderingAllowed(false);
//        header.setResizingAllowed(true);
//        header.setOpaque(true);
//        header.setBorder(BorderFactory.createEmptyBorder());
//        TableCellRenderer base = header.getDefaultRenderer();
//        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
//            Component c = base.getTableCellRendererComponent(tbl, value, false, false, row, column);
//            JLabel label = asLabel(c);
//            if (label != null) {
//                int alignment = resolveAlignment(table, column, true);
//                label.setHorizontalAlignment(alignment);
//                label.setBorder(borderForAlignment(alignment));
//                label.setOpaque(true);
//                Font baseFont = header.getFont() != null ? header.getFont() : label.getFont();
//                if (baseFont != null) {
//                    label.setFont(baseFont.deriveFont(Font.BOLD));
//
//                }
//                Color bg = UIManager.getColor("TableHeader.background");
//                Color fg = UIManager.getColor("TableHeader.foreground");
//                Color color = new Color(0x6E6E6E);
//                label.setBackground(bg != null ? bg : tbl.getBackground());
////                label.setForeground(fg != null ? fg : tbl.getForeground());
//                label.setForeground(color);
//            }
//            return c;
//        });
//    }
//
//    private static JLabel asLabel(Component c) {
//        if (c instanceof JLabel label) {
//            return label;
//        }
//        return null;
//    }
//
//    private static void installBodyRenderers(JTable table) {
//        TableCellRenderer defaultObject = table.getDefaultRenderer(Object.class);
//        TableCellRenderer defaultNumber = table.getDefaultRenderer(Number.class);
//
//        table.setDefaultRenderer(Object.class, new StyledRenderer(table, defaultObject));
//        table.setDefaultRenderer(Number.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(Integer.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(Long.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(BigDecimal.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(Double.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(Float.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(java.math.BigDecimal.class, new StyledRenderer(table, defaultNumber));
//        table.setDefaultRenderer(Boolean.class, new BooleanStyledRenderer(table));
//    }
//
//    public static int resolveAlignment(JTable table, int viewColumn, boolean header) {
//        int modelColumn = table.convertColumnIndexToModel(viewColumn);
//        if (modelColumn < 0) {
//            return SwingConstants.LEFT;
//        }
//        TableModel model = table.getModel();
//        Class<?> cls = model.getColumnClass(modelColumn);
//        if (cls != null && cls != Object.class) {
//            if (Number.class.isAssignableFrom(cls)
//                    || cls == Integer.TYPE || cls == Long.TYPE || cls == Double.TYPE
//                    || cls == Float.TYPE || cls == Short.TYPE || cls == Byte.TYPE) {
//                return SwingConstants.RIGHT;
//            }
//            if (cls == Boolean.class || cls == Boolean.TYPE) {
//                return SwingConstants.CENTER;
//            }
//        }
//
//        for (int row = 0; row < model.getRowCount(); row++) {
//            Object value = model.getValueAt(row, modelColumn);
//            if (value == null) {
//                continue;
//            }
//            if (value instanceof Number) {
//                return SwingConstants.RIGHT;
//            }
//            if (value instanceof BigDecimal) {
//                return SwingConstants.RIGHT;
//            }
//            if (value instanceof Boolean) {
//                return SwingConstants.CENTER;
//            }
//            break;
//        }
//        return SwingConstants.LEFT;
//    }
//
//    private static Color stripeColor(JTable table, int row) {
//        Color base = table.getBackground();
//        if (row % 2 == 0) {
//            return base;
//        }
//        return blend(base, table.getForeground(), 0.035f);
//    }
//
//    private static Color hoverColor(JTable table) {
//        Color configured = UIManager.getColor("Table.hoverBackground");
//        if (configured != null) {
//            return configured;
//        }
//        return blend(table.getBackground(), table.getForeground(), 0.08f);
//    }
//
//    private static Color blend(Color a, Color b, float t) {
//        t = Math.max(0f, Math.min(1f, t));
//        int r = Math.round(a.getRed() * (1f - t) + b.getRed() * t);
//        int g = Math.round(a.getGreen() * (1f - t) + b.getGreen() * t);
//        int bl = Math.round(a.getBlue() * (1f - t) + b.getBlue() * t);
//        return new Color(r, g, bl);
//    }
//
//    private static Border borderForAlignment(int alignment) {
//        if (alignment == SwingConstants.RIGHT) {
//            return RIGHT_PAD;
//        }
//        if (alignment == SwingConstants.CENTER) {
//            return CENTER_PAD;
//        }
//        return LEFT_PAD;
//    }
//
//    private static final class StyledRenderer extends DefaultTableCellRenderer {
//
//        private final JTable table;
//        private final TableCellRenderer fallback;
//
//        private StyledRenderer(JTable table, TableCellRenderer fallback) {
//            this.table = table;
//            this.fallback = fallback;
//            setOpaque(true);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (fallback instanceof DefaultTableCellRenderer dtr && fallback != this) {
//                dtr.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
//            }
//            super.getTableCellRendererComponent(tbl, value, isSelected, false, row, column);
//            int alignment = resolveAlignment(table, column, false);
//            setHorizontalAlignment(alignment);
//            setBorder(borderForAlignment(alignment));
//            applyColors(tbl, this, isSelected, row);
//            return this;
//        }
//    }
//
//    private static final class BooleanStyledRenderer extends JCheckBox implements TableCellRenderer {
//
//        private final JTable table;
//
//        private BooleanStyledRenderer(JTable table) {
//            this.table = table;
//            setHorizontalAlignment(SwingConstants.CENTER);
//            setBorderPainted(false);
//            setOpaque(true);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            setSelected(Boolean.TRUE.equals(value));
//            applyColors(tbl, this, isSelected, row);
//            return this;
//        }
//    }
//
//    public static void applyColors(JTable table, JComponent comp, boolean isSelected, int viewRow) {
//        if (isSelected) {
//            comp.setBackground(table.getSelectionBackground());
//            comp.setForeground(table.getSelectionForeground());
//            return;
//        }
//        Object hover = table.getClientProperty(HOVER_ROW_KEY);
//        int hoverRow = hover instanceof Integer ? (Integer) hover : -1;
//        Color bg = hoverRow == viewRow ? hoverColor(table) : stripeColor(table, viewRow);
//        comp.setBackground(bg);
//        comp.setForeground(table.getForeground());
//    }
//}



import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class TableStyleSupport {

    private static final Color HEADER_BG = new Color(0xF5F7FA);
    private static final Color HEADER_FG = new Color(0x2E2E2E);
    private static final Color HEADER_LINE = new Color(0xE5E7EB);

    private static final Color ROW_EVEN = Color.WHITE;
    private static final Color ROW_ODD = new Color(0xF8FAFC);
    private static final Color ROW_HOVER = new Color(0xEAF3FF);

    private static final Color SELECTION_BG = new Color(0xCFE8FF);
    private static final Color SELECTION_FG = new Color(0x111111);

    private static final int HEADER_PAD_X = 12;
    private static final int CELL_PAD_X = 12;

    private static final String KEY_HOVER_ROW = "table.hover.row";
    private static final String KEY_CENTER_COLS = "table.center.cols";
    private static final String KEY_HOVER_INSTALLED = "table.hover.installed";
    

    private TableStyleSupport() {
    }

    public static void apply(JTable table) {
        apply(table, new int[0]);
    }

    public static void apply(JTable table, int... centeredColumns) {
        if (table == null) {
            return;
        }

        Set<Integer> centered = new HashSet<>();
        if (centeredColumns != null) {
            for (int c : centeredColumns) {
                centered.add(c);
            }
        }
        table.putClientProperty(KEY_CENTER_COLS, centered);

        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(SELECTION_FG);

        installHover(table);
        styleHeader(table);
        styleColumns(table);
    }

    public static void centerColumn(JTable table, int modelColumnIndex) {
        Set<Integer> centered = getCenteredColumns(table);
        centered.add(modelColumnIndex);
        table.putClientProperty(KEY_CENTER_COLS, centered);
        styleHeader(table);
        styleColumns(table);
    }

    @SuppressWarnings("unchecked")
    private static Set<Integer> getCenteredColumns(JTable table) {
        Object value = table.getClientProperty(KEY_CENTER_COLS);
        if (value instanceof Set<?>) {
            return (Set<Integer>) value;
        }
        return new HashSet<>();
    }

    private static void styleHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header == null) {
            return;
        }

        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setOpaque(true);

        TableColumnModel columnModel = table.getColumnModel();
        for (int viewIndex = 0; viewIndex < columnModel.getColumnCount(); viewIndex++) {
            TableColumn column = columnModel.getColumn(viewIndex);
            column.setHeaderRenderer(new HeaderRenderer(alignmentFor(table, column)));
        }
    }

    private static void styleColumns(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int viewIndex = 0; viewIndex < columnModel.getColumnCount(); viewIndex++) {
            TableColumn column = columnModel.getColumn(viewIndex);
            column.setCellRenderer(new BodyRenderer(table, alignmentFor(table, column)));
        }
    }

    public static int alignmentFor(JTable table, TableColumn column) {
        int modelIndex = column.getModelIndex();
        Set<Integer> centered = getCenteredColumns(table);

        if (centered.contains(modelIndex)) {
            return SwingConstants.CENTER;
        }

        Class<?> type = table.getModel().getColumnClass(modelIndex);
        if (Number.class.isAssignableFrom(type)) {
            return SwingConstants.RIGHT;
        }

        return SwingConstants.LEFT;
    }

    private static void installHover(JTable table) {
        if (Boolean.TRUE.equals(table.getClientProperty(KEY_HOVER_INSTALLED))) {
            return;
        }

        table.putClientProperty(KEY_HOVER_ROW, -1);
        table.putClientProperty(KEY_HOVER_INSTALLED, true);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int hoverRow = table.rowAtPoint(e.getPoint());
                Integer oldRow = (Integer) table.getClientProperty(KEY_HOVER_ROW);
                if (oldRow == null || oldRow != hoverRow) {
                    table.putClientProperty(KEY_HOVER_ROW, hoverRow);
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                table.putClientProperty(KEY_HOVER_ROW, -1);
                table.repaint();
            }
        });
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {

        private final int alignment;

        private HeaderRenderer(int alignment) {
            this.alignment = alignment;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(alignment);
            setBackground(HEADER_BG);
            setForeground(HEADER_FG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, HEADER_LINE),
                    new EmptyBorder(0, HEADER_PAD_X, 0, HEADER_PAD_X)
            ));
            return this;
        }
    }

    private static final class BodyRenderer extends DefaultTableCellRenderer {

        private final JTable table;
        private final int alignment;

        private BodyRenderer(JTable table, int alignment) {
            this.table = table;
            this.alignment = alignment;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(alignment);
            setBorder(new EmptyBorder(0, CELL_PAD_X, 0, CELL_PAD_X));

            if (isSelected) {
                setBackground(SELECTION_BG);
                setForeground(SELECTION_FG);
            } else {
                Integer hoverRow = (Integer) table.getClientProperty(KEY_HOVER_ROW);
                if (hoverRow != null && hoverRow == row) {
                    setBackground(ROW_HOVER);
                } else {
                    setBackground((row % 2 == 0) ? ROW_EVEN : ROW_ODD);
                }
                setForeground(tbl.getForeground());
            }

            return this;
        }
    }
}