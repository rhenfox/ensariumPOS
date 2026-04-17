package com.aldrin.ensarium.util;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

public final class UiSupport {
    private UiSupport() {
    }

    public static void hideColumn(JTable table, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= table.getColumnModel().getColumnCount()) {
            return;
        }
        var column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setWidth(0);
    }

    public static String nz(String value) {
        return value == null ? "" : value;
    }

    public static String trimmedOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static BigDecimal decimalOrZero(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    public static Date sqlDateOrNull(JDateChooser chooser) {
        return chooser.getDate() == null ? null : new Date(chooser.getDate().getTime());
    }

    public static void setDate(JDateChooser chooser, Date value) {
        chooser.setDate(value);
    }

    public static boolean same(String a, String b) {
        return Objects.equals(trimmedOrNull(a), trimmedOrNull(b));
    }
}
