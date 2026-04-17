package com.aldrin.ensarium.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public final class TableLoader {

    private TableLoader() {
    }

    public static DefaultTableModel load(Connection con, String sql, java.util.Date fromDate, java.util.Date toDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(fromDate.getTime()));
            ps.setDate(2, new java.sql.Date(toDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                return buildTableModel(rs);
            }
        }
    }

    public static DefaultTableModel loadMonthly(Connection con, String sql, java.util.Date fromDate, java.util.Date toDate) throws SQLException {
        Calendar from = Calendar.getInstance();
        from.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);
        int fromYearMonth = from.get(Calendar.YEAR) * 100 + (from.get(Calendar.MONTH) + 1);
        int toYearMonth = to.get(Calendar.YEAR) * 100 + (to.get(Calendar.MONTH) + 1);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, fromYearMonth);
            ps.setInt(2, toYearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                return buildTableModel(rs);
            }
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        Vector<String> cols = new Vector<>();
        Vector<Class<?>> colClasses = new Vector<>();
        for (int i = 1; i <= colCount; i++) {
            cols.add(meta.getColumnLabel(i));
            colClasses.add(resolveColumnClass(meta, i));
        }
        Vector<Vector<Object>> rows = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
        return new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 0 && columnIndex < colClasses.size()) {
                    return colClasses.get(columnIndex);
                }
                return Object.class;
            }
        };
    }

    private static Class<?> resolveColumnClass(ResultSetMetaData meta, int index) {
        try {
            String className = meta.getColumnClassName(index);
            if (className != null && !className.isBlank()) {
                return Class.forName(className);
            }
        } catch (Exception ignored) {
        }
        return Object.class;
    }
}
