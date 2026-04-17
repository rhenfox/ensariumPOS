package com.aldrin.ensarium.inventory.product;

import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.util.SwingUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ProductInventoryReportExporter {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter PRINT_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    private ProductInventoryReportExporter() {
    }

    public static String buildSuggestedFileName(boolean stockOnHand, String keyword) {
        String base = stockOnHand ? "stock_on_hand_report" : "physical_count_report";
        String suffix = sanitizeFilePart(keyword);
        if (!suffix.isBlank()) {
            base += "_" + suffix;
        }
        return base + "_" + LocalDateTime.now().format(FILE_STAMP) + ".pdf";
    }

    public static void exportStockOnHandPdf(Path file, List<ProductInventoryItem> rows, String keyword, Session session) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(buildMetaLine(keyword, rows, session));
        lines.add(
                fit("SKU", 10) + " " +
                fit("Product", 28) + " " +
                fit("Barcode", 18) + " " +
                fit("Onhand", 10) + " " +
                fit("Sold30", 10) + " " +
                fit("Buy", 10) + " " +
                fit("Sell", 10) + " " +
                fit("Profit NT/ND", 12) + " " +
                fit("Profit W/TD", 12) + " " +
                fit("Markup% W/TD", 12) + " " +
                fit("Expiry / Remaining", 34) + " " +
                fit("Days to Expire", 16)
        );
        lines.add(repeat('-', 184));
        for (ProductInventoryItem row : rows) {
            lines.add(
                    fit(row.getSku(), 10) + " " +
                    fit(row.getProductName(), 28) + " " +
                    fit(row.getBarcodes(), 18) + " " +
                    fitRight(SwingUtils.formatQty(row.getOnhandQty()), 10) + " " +
                    fitRight(SwingUtils.formatQty(row.getQtySold30Days()), 10) + " " +
                    fitRight(SwingUtils.formatMoney(row.getBuyingPrice()), 10) + " " +
                    fitRight(SwingUtils.formatMoney(row.getSellingPrice()), 10) + " " +
                    fitRight(SwingUtils.formatMoney(row.getProfitWithoutTaxWithoutDiscount()), 12) + " " +
                    fitRight(SwingUtils.formatMoney(row.getProfitWithTaxAndDiscount()), 12) + " " +
                    fitRight(SwingUtils.formatPercent(row.getMarkupWithTaxAndDiscount()), 12) + " " +
                    fit(row.getExpirySummary(), 34) + " " +
                    fit(row.getDaysToExpireDisplay(), 16)
            );
        }
        lines.add(repeat('-', 184));
        lines.add("Prepared by: " + userText(session));
        SimplePdfWriter.writeMonospaceReport(file, "Stock on Hand Report", lines);
    }

    public static void exportPhysicalCountPdf(Path file, List<ProductInventoryItem> rows, String keyword, Session session) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(buildMetaLine(keyword, rows, session));
        lines.add(
                fit("SKU", 10) + " " +
                fit("Product", 28) + " " +
                fit("Barcode", 18) + " " +
                fit("System Qty", 11) + " " +
                fit("Counted Qty", 11) + " " +
                fit("Variance", 11) + " " +
                fit("Expiry / Remaining", 34) + " " +
                fit("Days", 12) + " " +
                fit("Remarks", 45)
        );
        lines.add(repeat('-', 188));
        for (ProductInventoryItem row : rows) {
            lines.add(
                    fit(row.getSku(), 10) + " " +
                    fit(row.getProductName(), 28) + " " +
                    fit(row.getBarcodes(), 18) + " " +
                    fitRight(SwingUtils.formatQty(row.getOnhandQty()), 11) + " " +
                    fit("________", 11) + " " +
                    fit("________", 11) + " " +
                    fit(row.getExpirySummary(), 34) + " " +
                    fit(row.getDaysToExpireDisplay(), 12) + " " +
                    fit("______________________________", 45)
            );
        }
        lines.add(repeat('-', 188));
        lines.add("Counted by: ______________________________    Checked by: ______________________________");
        lines.add("Prepared by: " + userText(session));
        SimplePdfWriter.writeMonospaceReport(file, "Physical Count Report (Stock Count Sheet)", lines);
    }

    private static String buildMetaLine(String keyword, List<ProductInventoryItem> rows, Session session) {
        String q = keyword == null || keyword.isBlank() ? "(all)" : keyword.trim();
        return "Generated: " + LocalDateTime.now().format(PRINT_STAMP)
                + " | Filter: " + q
                + " | Rows: " + rows.size()
                + " | User: " + userText(session);
    }

    private static String userText(Session session) {
        if (session == null) {
            return "";
        }
        String fullName = session.fullName() == null ? "" : session.fullName().trim();
        String username = session.username() == null ? "" : session.username().trim();
        if (!fullName.isBlank() && !username.isBlank()) {
            return fullName + " (" + username + ")";
        }
        return !fullName.isBlank() ? fullName : username;
    }

    private static String sanitizeFilePart(String text) {
        if (text == null) {
            return "";
        }
        String v = text.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return v.length() > 24 ? v.substring(0, 24) : v;
    }

    private static String fit(String text, int width) {
        String v = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ');
        if (v.length() > width) {
            return width <= 1 ? v.substring(0, width) : v.substring(0, width - 1) + "…";
        }
        return String.format("%-" + width + "s", v);
    }

    private static String fitRight(String text, int width) {
        String v = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ');
        if (v.length() > width) {
            return width <= 1 ? v.substring(0, width) : "…" + v.substring(v.length() - width + 1);
        }
        return String.format("%" + width + "s", v);
    }

    private static String repeat(char ch, int count) {
        return String.valueOf(ch).repeat(Math.max(0, count));
    }
}
