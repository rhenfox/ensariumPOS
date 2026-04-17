package com.aldrin.ensarium.txn;

import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

public final class SwingUtils {
    private static final DecimalFormat QTY = new DecimalFormat("#,##0.####");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.0000");
    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SwingUtils() {}
    public static String formatQty(BigDecimal value) { return value == null ? "0" : QTY.format(value.stripTrailingZeros()); }
    public static String formatMoney(BigDecimal value) { return value == null ? "0.0000" : MONEY.format(value.setScale(4, RoundingMode.HALF_UP)); }
    public static String formatDateTime(Timestamp value) { return value == null ? "" : DATE_TIME.format(value); }
    public static void showError(Component parent, String message, Exception ex) { JOptionPane.showMessageDialog(parent, ex == null ? message : message + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
}
