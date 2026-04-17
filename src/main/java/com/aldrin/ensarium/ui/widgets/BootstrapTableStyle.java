/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;

/**
 *
 * @author ALDRIN CABUSOG
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class BootstrapTableStyle {

    private static final String KEY_STYLE = "bootstrap.table.style";
    private static final String KEY_HOVER_ROW = "bootstrap.table.hoverRow";
    private static final String KEY_HOVER_COLUMN = "bootstrap.table.hoverColumn";

    private static final String KEY_CELL_INDENT = "bootstrap.table.cellIndent";
    private static final String KEY_HEADER_INDENT = "bootstrap.table.headerIndent";

    private static final String KEY_CELL_ALIGNMENT = "bootstrap.table.cellAlignment";
    private static final String KEY_HEADER_ALIGNMENT = "bootstrap.table.headerAlignment";

    private static final String KEY_MOUSE_INSTALLED = "bootstrap.table.mouseInstalled";

    private static final String KEY_ALL_COLUMNS = "bootstrap.table.allColumns";
    private static final String KEY_COLUMN_POPUP_INSTALLED = "bootstrap.table.columnPopupInstalled";

    private BootstrapTableStyle() {
    }

    public static void install(JTable table) {
        install(table, Style.defaultStyle());
    }

    public static void install(JTable table, Style style) {
        if (table == null) {
            return;
        }

        table.putClientProperty(KEY_STYLE, style);
        table.putClientProperty(KEY_HOVER_ROW, -1);
        table.putClientProperty(KEY_HOVER_COLUMN, -1);

        getIndentMap(table, KEY_CELL_INDENT);
        getIndentMap(table, KEY_HEADER_INDENT);
        getAlignmentMap(table, KEY_CELL_ALIGNMENT);
        getAlignmentMap(table, KEY_HEADER_ALIGNMENT);

        table.setFont(style.font);
        table.setForeground(style.textColor);
        table.setBackground(style.rowEvenColor);
        table.setRowHeight(style.rowHeight);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(style.selectionBackground);
        table.setSelectionForeground(style.selectionForeground);
        table.setFillsViewportHeight(true);
        table.setOpaque(true);

        table.setDefaultRenderer(Object.class, new BootstrapCellRenderer(null));
        table.setDefaultRenderer(Number.class, new BootstrapCellRenderer(SwingConstants.RIGHT));
        table.setDefaultRenderer(Boolean.class, new BootstrapBooleanRenderer());
        table.setDefaultRenderer(Icon.class, new BootstrapIconRenderer());

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setDefaultRenderer(new BootstrapHeaderRenderer());
            header.setReorderingAllowed(false);
            header.setFont(style.headerFont);
            header.setForeground(style.headerForeground);
            header.setBackground(style.headerBackground);
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, style.headerHeight));
        }

        rememberAllColumns(table);
        installHoverSupport(table);

        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(style.rowEvenColor);
            scrollPane.setBorder(BorderFactory.createLineBorder(style.borderColor));
        }

        table.repaint();
    }

    public static void installAll(Container root) {
        installAll(root, Style.defaultStyle());
    }

    public static void installAll(Container root, Style style) {
        if (root == null) {
            return;
        }

        for (Component comp : root.getComponents()) {
            if (comp instanceof JTable table) {
                install(table, style);
            } else if (comp instanceof Container container) {
                installAll(container, style);
            }
        }
    }

    public static void enableColumnHiding(JTable table) {
        if (table == null) {
            return;
        }

        rememberAllColumns(table);

        if (Boolean.TRUE.equals(table.getClientProperty(KEY_COLUMN_POPUP_INSTALLED))) {
            return;
        }

        JTableHeader header = table.getTableHeader();
        if (header == null) {
            return;
        }

        MouseAdapter popupHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                JPopupMenu popup = buildColumnPopup(table);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        };

        header.addMouseListener(popupHandler);
        table.putClientProperty(KEY_COLUMN_POPUP_INSTALLED, true);
    }

    public static void hideColumn(JTable table, int modelColumn) {
        setColumnVisible(table, modelColumn, false);
    }

    public static void showColumn(JTable table, int modelColumn) {
        setColumnVisible(table, modelColumn, true);
    }

    public static void hideColumns(JTable table, int... modelColumns) {
        if (table == null || modelColumns == null) {
            return;
        }
        for (int modelColumn : modelColumns) {
            setColumnVisible(table, modelColumn, false);
        }
    }

    public static void showColumns(JTable table, int... modelColumns) {
        if (table == null || modelColumns == null) {
            return;
        }
        for (int modelColumn : modelColumns) {
            setColumnVisible(table, modelColumn, true);
        }
    }

    public static boolean isColumnVisible(JTable table, int modelColumn) {
        if (table == null) {
            return false;
        }

        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            if (cm.getColumn(i).getModelIndex() == modelColumn) {
                return true;
            }
        }
        return false;
    }

    public static void setColumnVisible(JTable table, int modelColumn, boolean visible) {
        if (table == null) {
            return;
        }

        rememberAllColumns(table);

        if (visible) {
            doShowColumn(table, modelColumn);
        } else {
            doHideColumn(table, modelColumn);
        }

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
        table.repaint();
    }

    public static void showAllColumns(JTable table) {
        if (table == null) {
            return;
        }

        rememberAllColumns(table);

        Map<Integer, TableColumn> allColumns = getAllColumns(table);
        for (Integer modelColumn : allColumns.keySet()) {
            if (!isColumnVisible(table, modelColumn)) {
                doShowColumn(table, modelColumn);
            }
        }

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
        table.repaint();
    }

    public static void setColumnIndent(JTable table, int modelColumn, int left, int right) {
        getIndentMap(table, KEY_CELL_INDENT).put(modelColumn, new Insets(0, left, 0, right));
        table.repaint();
    }

    public static void setHeaderIndent(JTable table, int modelColumn, int left, int right) {
        getIndentMap(table, KEY_HEADER_INDENT).put(modelColumn, new Insets(0, left, 0, right));
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    public static void clearColumnIndent(JTable table, int modelColumn) {
        getIndentMap(table, KEY_CELL_INDENT).remove(modelColumn);
        table.repaint();
    }

    public static void clearHeaderIndent(JTable table, int modelColumn) {
        getIndentMap(table, KEY_HEADER_INDENT).remove(modelColumn);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    public static void setColumnAlignment(JTable table, int modelColumn, int alignment) {
        getAlignmentMap(table, KEY_CELL_ALIGNMENT).put(modelColumn, alignment);
        table.repaint();
    }

    public static void setHeaderAlignment(JTable table, int modelColumn, int alignment) {
        getAlignmentMap(table, KEY_HEADER_ALIGNMENT).put(modelColumn, alignment);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    public static void clearColumnAlignment(JTable table, int modelColumn) {
        getAlignmentMap(table, KEY_CELL_ALIGNMENT).remove(modelColumn);
        table.repaint();
    }

    public static void clearHeaderAlignment(JTable table, int modelColumn) {
        getAlignmentMap(table, KEY_HEADER_ALIGNMENT).remove(modelColumn);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    public static void setColumnLeft(JTable table, int modelColumn) {
        setColumnAlignment(table, modelColumn, SwingConstants.LEFT);
    }

    public static void setColumnCenter(JTable table, int modelColumn) {
        setColumnAlignment(table, modelColumn, SwingConstants.CENTER);
    }

    public static void setColumnRight(JTable table, int modelColumn) {
        setColumnAlignment(table, modelColumn, SwingConstants.RIGHT);
    }

    public static void setHeaderLeft(JTable table, int modelColumn) {
        setHeaderAlignment(table, modelColumn, SwingConstants.LEFT);
    }

    public static void setHeaderCenter(JTable table, int modelColumn) {
        setHeaderAlignment(table, modelColumn, SwingConstants.CENTER);
    }

    public static void setHeaderRight(JTable table, int modelColumn) {
        setHeaderAlignment(table, modelColumn, SwingConstants.RIGHT);
    }

    public static void setColumnWidth(JTable table, int viewColumn, int width) {
        if (table == null || viewColumn < 0 || viewColumn >= table.getColumnModel().getColumnCount()) {
            return;
        }

        TableColumn col = table.getColumnModel().getColumn(viewColumn);
        col.setPreferredWidth(width);
    }

    public static void setColumnWidth(JTable table, int viewColumn, int min, int preferred, int max) {
        if (table == null || viewColumn < 0 || viewColumn >= table.getColumnModel().getColumnCount()) {
            return;
        }

        TableColumn col = table.getColumnModel().getColumn(viewColumn);
        col.setMinWidth(min);
        col.setPreferredWidth(preferred);
        col.setMaxWidth(max);
    }

    public static void setFixedColumnWidth(JTable table, int viewColumn, int width) {
        if (table == null || viewColumn < 0 || viewColumn >= table.getColumnModel().getColumnCount()) {
            return;
        }

        TableColumn col = table.getColumnModel().getColumn(viewColumn);
        col.setMinWidth(width);
        col.setPreferredWidth(width);
        col.setMaxWidth(width);
    }

    private static void installHoverSupport(JTable table) {
        if (Boolean.TRUE.equals(table.getClientProperty(KEY_MOUSE_INSTALLED))) {
            return;
        }

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(table, e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHover(table, e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                table.putClientProperty(KEY_HOVER_ROW, -1);
                table.putClientProperty(KEY_HOVER_COLUMN, -1);
                table.repaint();
            }

            private void updateHover(JTable table, MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                Integer oldRow = (Integer) table.getClientProperty(KEY_HOVER_ROW);
                Integer oldCol = (Integer) table.getClientProperty(KEY_HOVER_COLUMN);

                if (oldRow == null || oldCol == null || oldRow != row || oldCol != col) {
                    table.putClientProperty(KEY_HOVER_ROW, row);
                    table.putClientProperty(KEY_HOVER_COLUMN, col);
                    table.repaint();
                }
            }
        };

        table.addMouseMotionListener(adapter);
        table.addMouseListener(adapter);
        table.putClientProperty(KEY_MOUSE_INSTALLED, true);
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Insets> getIndentMap(JTable table, String key) {
        Map<Integer, Insets> map = (Map<Integer, Insets>) table.getClientProperty(key);
        if (map == null) {
            map = new HashMap<>();
            table.putClientProperty(key, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Integer> getAlignmentMap(JTable table, String key) {
        Map<Integer, Integer> map = (Map<Integer, Integer>) table.getClientProperty(key);
        if (map == null) {
            map = new HashMap<>();
            table.putClientProperty(key, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, TableColumn> getAllColumns(JTable table) {
        Map<Integer, TableColumn> map = (Map<Integer, TableColumn>) table.getClientProperty(KEY_ALL_COLUMNS);
        if (map == null) {
            map = new LinkedHashMap<>();
            TableColumnModel cm = table.getColumnModel();
            Enumeration<TableColumn> en = cm.getColumns();
            while (en.hasMoreElements()) {
                TableColumn tc = en.nextElement();
                map.put(tc.getModelIndex(), tc);
            }
            table.putClientProperty(KEY_ALL_COLUMNS, map);
        }
        return map;
    }

    private static Insets getColumnInsets(JTable table, String key, int modelColumn, int defaultLeft, int defaultRight) {
        Map<Integer, Insets> map = getIndentMap(table, key);
        Insets insets = map.get(modelColumn);
        return insets != null ? insets : new Insets(0, defaultLeft, 0, defaultRight);
    }

    private static int getAlignment(JTable table, String key, int modelColumn, int defaultAlignment) {
        Map<Integer, Integer> map = getAlignmentMap(table, key);
        Integer alignment = map.get(modelColumn);
        return alignment != null ? alignment : defaultAlignment;
    }

    private static Style getStyle(JTable table) {
        Style style = (Style) table.getClientProperty(KEY_STYLE);
        return style != null ? style : Style.defaultStyle();
    }

    private static int getHoverRow(JTable table) {
        Object value = table.getClientProperty(KEY_HOVER_ROW);
        return value instanceof Integer ? (Integer) value : -1;
    }

    private static Color resolveRowBackground(JTable table, int row, boolean isSelected) {
        Style style = getStyle(table);

        if (isSelected) {
            return style.selectionBackground;
        }

        int hoverRow = getHoverRow(table);
        if (row == hoverRow) {
            return style.hoverRowColor;
        }

        return (row % 2 == 0) ? style.rowEvenColor : style.rowOddColor;
    }

    private static Color resolveForeground(JTable table, boolean isSelected) {
        Style style = getStyle(table);
        return isSelected ? style.selectionForeground : style.textColor;
    }

    private static Border createCellBorder(JTable table, int modelColumn) {
        Style style = getStyle(table);
        Insets pad = getColumnInsets(
                table,
                KEY_CELL_INDENT,
                modelColumn,
                style.cellLeftPadding,
                style.cellRightPadding
        );

        Border line = BorderFactory.createMatteBorder(0, 0, 1, 0, style.borderColor);
        Border empty = BorderFactory.createEmptyBorder(0, pad.left, 0, pad.right);
        return BorderFactory.createCompoundBorder(line, empty);
    }

    private static Border createHeaderBorder(JTable table, int modelColumn) {
        Style style = getStyle(table);
        Insets pad = getColumnInsets(
                table,
                KEY_HEADER_INDENT,
                modelColumn,
                style.headerLeftPadding,
                style.headerRightPadding
        );

        Border line = BorderFactory.createMatteBorder(0, 0, 1, 0, style.borderColor);
        Border empty = BorderFactory.createEmptyBorder(0, pad.left, 0, pad.right);
        return BorderFactory.createCompoundBorder(line, empty);
    }

    private static void rememberAllColumns(JTable table) {
        Map<Integer, TableColumn> map = getAllColumns(table);
        TableColumnModel cm = table.getColumnModel();
        Enumeration<TableColumn> en = cm.getColumns();
        while (en.hasMoreElements()) {
            TableColumn tc = en.nextElement();
            if (!map.containsKey(tc.getModelIndex())) {
                map.put(tc.getModelIndex(), tc);
            }
        }
    }

    private static JPopupMenu buildColumnPopup(JTable table) {
        JPopupMenu popup = new JPopupMenu();

        Map<Integer, TableColumn> allColumns = getAllColumns(table);
        int visibleCount = table.getColumnModel().getColumnCount();

        for (Map.Entry<Integer, TableColumn> entry : allColumns.entrySet()) {
            int modelColumn = entry.getKey();
            TableColumn tc = entry.getValue();

            String title = tc.getHeaderValue() == null
                    ? ("Column " + modelColumn)
                    : tc.getHeaderValue().toString();

            boolean visible = isColumnVisible(table, modelColumn);

            JCheckBoxMenuItem item = new JCheckBoxMenuItem(title, visible);

            if (visible && visibleCount <= 1) {
                item.setEnabled(false);
            }

            item.addActionListener(ae -> setColumnVisible(table, modelColumn, item.isSelected()));
            popup.add(item);
        }

        popup.addSeparator();

        JMenuItem showAll = new JMenuItem("Show All Columns");
        showAll.addActionListener(ae -> showAllColumns(table));
        popup.add(showAll);

        return popup;
    }

    private static void doHideColumn(JTable table, int modelColumn) {
        TableColumnModel cm = table.getColumnModel();
        TableColumn tc = findVisibleColumn(table, modelColumn);
        if (tc != null) {
            cm.removeColumn(tc);
        }
    }

    private static void doShowColumn(JTable table, int modelColumn) {
        if (isColumnVisible(table, modelColumn)) {
            return;
        }

        Map<Integer, TableColumn> allColumns = getAllColumns(table);
        TableColumn tc = allColumns.get(modelColumn);
        if (tc == null) {
            return;
        }

        TableColumnModel cm = table.getColumnModel();
        cm.addColumn(tc);

        int currentIndex = cm.getColumnCount() - 1;
        int targetIndex = getTargetViewIndex(table, modelColumn);

        if (targetIndex >= 0 && targetIndex < cm.getColumnCount()) {
            cm.moveColumn(currentIndex, targetIndex);
        }
    }

    private static TableColumn findVisibleColumn(JTable table, int modelColumn) {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            TableColumn tc = cm.getColumn(i);
            if (tc.getModelIndex() == modelColumn) {
                return tc;
            }
        }
        return null;
    }

    private static int getTargetViewIndex(JTable table, int modelColumnToShow) {
        List<Integer> originalOrder = new ArrayList<>(getAllColumns(table).keySet());

        int target = 0;
        for (Integer modelColumn : originalOrder) {
            if (modelColumn == modelColumnToShow) {
                break;
            }
            if (isColumnVisible(table, modelColumn)) {
                target++;
            }
        }
        return target;
    }

    private static final class BootstrapCellRenderer extends DefaultTableCellRenderer {

        private final Integer fixedAlignment;

        BootstrapCellRenderer(Integer fixedAlignment) {
            this.fixedAlignment = fixedAlignment;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            int modelColumn = table.convertColumnIndexToModel(column);

            setBorder(createCellBorder(table, modelColumn));
            setBackground(resolveRowBackground(table, row, isSelected));
            setForeground(resolveForeground(table, isSelected));
            setFont(getStyle(table).font);

            int defaultAlignment;
            if (fixedAlignment != null) {
                defaultAlignment = fixedAlignment;
            } else if (value instanceof Number) {
                defaultAlignment = SwingConstants.RIGHT;
            } else {
                defaultAlignment = SwingConstants.LEFT;
            }

            setHorizontalAlignment(getAlignment(
                    table,
                    KEY_CELL_ALIGNMENT,
                    modelColumn,
                    defaultAlignment
            ));

            return this;
        }
    }

    private static final class BootstrapBooleanRenderer extends JCheckBox implements TableCellRenderer {

        BootstrapBooleanRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            int modelColumn = table.convertColumnIndexToModel(column);

            setSelected(Boolean.TRUE.equals(value));
            setBackground(resolveRowBackground(table, row, isSelected));
            setForeground(resolveForeground(table, isSelected));
            setBorder(createCellBorder(table, modelColumn));
            setHorizontalAlignment(getAlignment(
                    table,
                    KEY_CELL_ALIGNMENT,
                    modelColumn,
                    SwingConstants.CENTER
            ));

            return this;
        }
    }

    private static final class BootstrapIconRenderer extends JLabel implements TableCellRenderer {

        BootstrapIconRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            int modelColumn = table.convertColumnIndexToModel(column);

            setIcon(value instanceof Icon ? (Icon) value : null);
            setText("");
            setBackground(resolveRowBackground(table, row, isSelected));
            setForeground(resolveForeground(table, isSelected));
            setBorder(createCellBorder(table, modelColumn));
            setHorizontalAlignment(getAlignment(
                    table,
                    KEY_CELL_ALIGNMENT,
                    modelColumn,
                    SwingConstants.CENTER
            ));

            return this;
        }
    }

    private static final class BootstrapHeaderRenderer extends DefaultTableCellRenderer {

        BootstrapHeaderRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);

            Style style = getStyle(table);
            int modelColumn = table.convertColumnIndexToModel(column);

            setText(value == null ? "" : value.toString());
            setFont(style.headerFont);
            setBackground(style.headerBackground);
            setForeground(style.headerForeground);
            setHorizontalAlignment(getAlignment(
                    table,
                    KEY_HEADER_ALIGNMENT,
                    modelColumn,
                    SwingConstants.LEFT
            ));
            setBorder(createHeaderBorder(table, modelColumn));

            return this;
        }
    }

    public static final class Style {

        private Font font = new Font("Segoe UI", Font.PLAIN, 13);
        private Font headerFont = new Font("Segoe UI", Font.BOLD, 13);

        private Color textColor = new Color(33, 37, 41);
        private Color headerForeground = new Color(33, 37, 41);
        private Color headerBackground = new Color(248, 249, 250);

        private Color rowEvenColor = Color.WHITE;
        private Color rowOddColor = new Color(248, 249, 250);
        private Color hoverRowColor = new Color(233, 245, 255);

        private Color selectionBackground = new Color(13, 110, 253);
        private Color selectionForeground = Color.WHITE;

        private Color borderColor = new Color(222, 226, 230);

        private int rowHeight = 36;
        private int headerHeight = 38;

        private int cellLeftPadding = 14;
        private int cellRightPadding = 14;

        private int headerLeftPadding = 14;
        private int headerRightPadding = 14;

        public static Style defaultStyle() {
            return new Style();
        }

        public Style font(Font font) {
            this.font = font;
            return this;
        }

        public Style headerFont(Font headerFont) {
            this.headerFont = headerFont;
            return this;
        }

        public Style rowHeight(int rowHeight) {
            this.rowHeight = rowHeight;
            return this;
        }

        public Style headerHeight(int headerHeight) {
            this.headerHeight = headerHeight;
            return this;
        }

        public Style stripedRows(Color even, Color odd) {
            this.rowEvenColor = even;
            this.rowOddColor = odd;
            return this;
        }

        public Style hoverColor(Color hover) {
            this.hoverRowColor = hover;
            return this;
        }

        public Style selection(Color bg, Color fg) {
            this.selectionBackground = bg;
            this.selectionForeground = fg;
            return this;
        }

        public Style headerColors(Color bg, Color fg) {
            this.headerBackground = bg;
            this.headerForeground = fg;
            return this;
        }

        public Style borderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Style textColor(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public Style cellPadding(int left, int right) {
            this.cellLeftPadding = left;
            this.cellRightPadding = right;
            return this;
        }

        public Style headerPadding(int left, int right) {
            this.headerLeftPadding = left;
            this.headerRightPadding = right;
            return this;
        }
    }

    public static void applyCellStyle(
            JTable table,
            JComponent component,
            boolean isSelected,
            int row,
            int column,
            int defaultAlignment
    ) {
        if (table == null || component == null) {
            return;
        }

        int modelColumn = table.convertColumnIndexToModel(column);

        component.setOpaque(true);
        component.setBackground(resolveRowBackground(table, row, isSelected));
        component.setForeground(resolveForeground(table, isSelected));
        component.setFont(getStyle(table).font);
        component.setBorder(createCellBorder(table, modelColumn));

        int alignment = getAlignment(
                table,
                KEY_CELL_ALIGNMENT,
                modelColumn,
                defaultAlignment
        );

        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(alignment);
        } else if (component instanceof JCheckBox checkBox) {
            checkBox.setHorizontalAlignment(alignment);
        }
    }
}

//BootstrapTableStyle.install(table);
//BootstrapTableStyle.enableColumnHiding(table);
//
//// cell/header indent
//BootstrapTableStyle.setColumnIndent(table, 1, 20, 10);
//BootstrapTableStyle.setHeaderIndent(table, 1, 20, 10);
//
//// alignment
//BootstrapTableStyle.setColumnLeft(table, 1);
//BootstrapTableStyle.setColumnCenter(table, 2);
//BootstrapTableStyle.setColumnRight(table, 3);
//
//BootstrapTableStyle.setHeaderLeft(table, 1);
//BootstrapTableStyle.setHeaderCenter(table, 2);
//BootstrapTableStyle.setHeaderRight(table, 3);
//
//// hide columns by model index
//BootstrapTableStyle.hideColumn(table, 0);
//BootstrapTableStyle.hideColumns(table, 7, 8);
//
//// width by view index
//BootstrapTableStyle.setFixedColumnWidth(table, 7, 42);
//BootstrapTableStyle.setFixedColumnWidth(table, 8, 42);
