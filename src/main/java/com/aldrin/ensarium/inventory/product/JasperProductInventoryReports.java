package com.aldrin.ensarium.inventory.product;

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

public final class JasperProductInventoryReports {

    private static final DateTimeFormatter PRINTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);

    private JasperProductInventoryReports() {}

    public static void showStockOnHandReport(Component parent, List<ProductInventoryItem> rows, String keyword, Session session) throws JRException {
        JasperPrint print = buildPrint("/reports/stock_on_hand.jrxml", rows, keyword, session, "Stock On Hand Report");
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("Stock On Hand Report");
        viewer.setVisible(true);
    }

    public static void showPhysicalCountReport(Component parent, List<ProductInventoryItem> rows, String keyword, Session session) throws JRException {
        JasperPrint print = buildPrint("/reports/physical_count_sheet.jrxml", rows, keyword, session, "Physical Count Report (Stock Count Sheet)");
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle("Physical Count Report (Stock Count Sheet)");
        viewer.setVisible(true);
    }

    private static JasperPrint buildPrint(String resourcePath, List<ProductInventoryItem> rows, String keyword, Session session, String title) throws JRException {
        try (InputStream in = openReport(resourcePath)) {
            if (in == null) {
                throw new JRException("Report template not found on classpath: " + resourcePath);
            }
            JasperReport report = JasperCompileManager.compileReport(in);
            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_TITLE", title);
            params.put("FILTER_TEXT", (keyword == null || keyword.trim().isBlank()) ? "All active products" : "Filter: " + keyword.trim());
            params.put("PRINTED_BY", session == null ? "" : session.fullName() + " (" + session.username() + ")");
            params.put("PRINTED_AT", PRINTED_AT.format(LocalDateTime.now()));
            List<ProductInventoryItem> ordered = new ArrayList<>(rows == null ? List.of() : rows);
            ordered.sort(Comparator
                    .comparing((ProductInventoryItem r) -> safe(r.getCategoryName()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(r -> safe(r.getProductName()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(r -> safe(r.getSku()), String.CASE_INSENSITIVE_ORDER));
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ordered);
            return JasperFillManager.fillReport(report, params, dataSource);
        } catch (JRException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JRException("Failed to build Jasper report.", ex);
        }
    }


    private static InputStream openReport(String resourcePath) {
        InputStream in = JasperProductInventoryReports.class.getResourceAsStream(resourcePath);
        if (in != null) {
            return in;
        }
        String noSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
        if (in != null) {
            return in;
        }
        try {
            java.nio.file.Path devPath = java.nio.file.Paths.get("src", "main", "resources", noSlash);
            if (java.nio.file.Files.exists(devPath)) {
                return java.nio.file.Files.newInputStream(devPath);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
