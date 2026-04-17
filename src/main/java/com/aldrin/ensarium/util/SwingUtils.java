package com.aldrin.ensarium.util;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public final class SwingUtils {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final DecimalFormat QTY = new DecimalFormat("#,##0.####");
    private static final DecimalFormat PERCENT = new DecimalFormat("#,##0.00");

    private SwingUtils() {
    }

    public static void error(Component parent, String message, Exception ex) {
        JOptionPane.showMessageDialog(parent,
                message + "\n\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Component parent, String message, Exception ex) {
        String detail = ex == null ? "" : "\n\n" + ex.getMessage();
        JOptionPane.showMessageDialog(parent, message + detail, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String formatDateTime(Timestamp ts) {
        return ts == null ? "" : ts.toLocalDateTime().format(DATETIME);
    }

    public static String formatMoney(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number n) {
            return MONEY.format(n.doubleValue());
        }
        return String.valueOf(value);
    }

    public static String formatQty(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number n) {
            return QTY.format(n.doubleValue());
        }
        return String.valueOf(value);
    }

    public static String formatPercent(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number n) {
            return PERCENT.format(n.doubleValue()) + "%";
        }
        return String.valueOf(value);
    }
}
