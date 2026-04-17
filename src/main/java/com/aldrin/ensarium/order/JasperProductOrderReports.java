package com.aldrin.ensarium.order;

import com.aldrin.ensarium.security.Session;
import java.awt.Component;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public final class JasperProductOrderReports {
    private static final DateTimeFormatter PRINTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);

    private JasperProductOrderReports() {}

    public static void showOrderReport(Component parent, String orderNo, SupplierOption supplier, String notes, List<OrderDraftLine> rows, Session session) throws JRException {
        JasperPrint print = buildPrint(orderNo, supplier, notes, rows, session);
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("Product Order Report");
        viewer.setVisible(true);
    }

    public static void showOrderReportByOrderNo(Component parent, String orderNo, Session session) throws JRException {
        try {
            ProductOrderDao.LoadedOrder loaded = new ProductOrderDao().loadOrderForReportByOrderNo(orderNo);
            JasperPrint print = buildPrint(loaded.orderNo(), loaded.supplier(), loaded.notes(), loaded.lines(), session);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Product Order Report - " + loaded.orderNo());
            viewer.setVisible(true);
        } catch (JRException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JRException("Failed to load saved order by Order No.", ex);
        }
    }

    private static JasperPrint buildPrint(String orderNo, SupplierOption supplier, String notes, List<OrderDraftLine> rows, Session session) throws JRException {
        try (InputStream in = openReport("/reports/product_order_sheet.jrxml")) {
            if (in == null) throw new JRException("Report template not found on classpath: /reports/product_order_sheet.jrxml");
            JasperReport report = JasperCompileManager.compileReport(in);
            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_TITLE", "Product Order Report");
            String selectedOrderNo = orderNo == null || orderNo.isBlank() ? "UNSAVED DRAFT" : orderNo;
            params.put("ORDER_NO", selectedOrderNo);
            params.put("SELECTED_ORDER_NO", selectedOrderNo);
            params.put("SUPPLIER_NAME", supplier == null ? "" : supplier.getName());
            params.put("ORDER_NOTES", notes == null ? "" : notes);
            params.put("PRINTED_BY", session == null ? "" : session.fullName() + " (" + session.username() + ")");
            params.put("PRINTED_AT", PRINTED_AT.format(LocalDateTime.now()));
            List<OrderDraftLine> ordered = new ArrayList<>(rows == null ? List.of() : rows);
            ordered.sort(Comparator
                    .comparing((OrderDraftLine r) -> safe(r.getCategoryName()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(r -> safe(r.getProductName()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(r -> safe(r.getSku()), String.CASE_INSENSITIVE_ORDER));
            return JasperFillManager.fillReport(report, params, new JRBeanCollectionDataSource(ordered));
        } catch (JRException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JRException("Failed to build product order report.", ex);
        }
    }

    private static InputStream openReport(String resourcePath) {
        InputStream in = JasperProductOrderReports.class.getResourceAsStream(resourcePath);
        if (in != null) return in;
        String noSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
        if (in != null) return in;
        try {
            java.nio.file.Path devPath = java.nio.file.Paths.get("src", "main", "resources", noSlash);
            if (java.nio.file.Files.exists(devPath)) return java.nio.file.Files.newInputStream(devPath);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
