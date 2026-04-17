package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.Db;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ReceiptService {
    private static final double MM_TO_POINTS = 72d / 25.4d;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Preferences PREFS = Preferences.userNodeForPackage(ReceiptService.class);
    private static final String PREF_PRINTER_NAME = "receipt.defaultPrinterName";
    private static final String PREF_PAPER_CODE = "receipt.defaultPaperCode";

    private static final PaperProfile PAPER_56 = new PaperProfile("56MM", "56mm", 56d, 32, 14, 4, 6, 8, 8, 9.5d, 11);
    private static final PaperProfile PAPER_80 = new PaperProfile("80MM", "80mm", 80d, 42, 22, 4, 7, 9, 10, 11.5d, 12);

    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    public void showSaleReceiptDialog(java.awt.Window owner, long saleId) throws Exception {
        showReceiptDialog(owner, profile -> buildSaleReceipt(saleId, profile), "Sales Invoice", true);
    }

    public void printSaleReceipt(long saleId) throws Exception {
        showSaleReceiptDialog(null, saleId);
    }

    public void printReturnReceipt(long returnId) throws Exception {
        showReceiptDialog(null, profile -> buildReturnReceipt(returnId, profile), "Return Receipt", true);
    }

    private ReceiptDocument buildSaleReceipt(long saleId, PaperProfile profile) throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean birReady;
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            InvoiceHeader h = loadInvoiceHeader(conn, saleId);
            PaymentInfo pay = loadPayment(conn, saleId);
            List<InvoiceLine> lines;
            BenefitInfo benefit = null;
            if (h != null) {
                birReady = true;
                lines = loadInvoiceLines(conn, h.invoiceId);
                benefit = loadBenefit(conn, h.invoiceId);
            } else {
                birReady = false;
                h = loadSaleHeaderFallback(conn, saleId);
                lines = loadSaleLinesFallback(conn, saleId);
            }
            conn.commit();

            if (birReady) {
                sb.append(center(profile, "INVOICE")).append('\n');
                if (!blank(h.sellerTradeName)) sb.append(center(profile, h.sellerTradeName)).append('\n');
                if (!blank(h.sellerRegisteredName)) {
                    for (String line : wrap(h.sellerRegisteredName, profile.columns)) sb.append(center(profile, line)).append('\n');
                }
                if (!blank(h.vatLine)) sb.append(center(profile, h.vatLine)).append('\n');
                for (String line : wrap(h.sellerBusinessAddress, profile.columns)) {
                    if (!line.isBlank()) sb.append(center(profile, line)).append('\n');
                }
                if (!blank(h.terminalLabel)) sb.append(center(profile, h.terminalLabel)).append('\n');
                sb.append(line(profile)).append('\n');

                sb.append(kv(profile, "INVOICE NO", h.invoiceNo)).append('\n');
                sb.append(kv(profile, "SERIAL NO", h.serialNo <= 0 ? "" : String.valueOf(h.serialNo))).append('\n');
                sb.append(kv(profile, "SALE REF", h.saleNo)).append('\n');
                sb.append(kv(profile, "DATE", h.soldAt)).append('\n');
                if (!blank(h.cashierUsername)) sb.append(kv(profile, "USER", h.cashierUsername)).append('\n');
                if (!blank(h.cashierName) && !h.cashierName.equalsIgnoreCase(defaultIfBlank(h.cashierUsername, ""))) {
                    sb.append(kv(profile, "CASHIER", h.cashierName)).append('\n');
                }
                sb.append(kv(profile, "TYPE", h.chargeSales ? "CHARGE" : "CASH")).append('\n');
                sb.append(line(profile)).append('\n');

                sb.append("SOLD TO:").append('\n');
                for (String line : wrap(defaultIfBlank(h.buyerName, "WALK-IN"), profile.columns)) sb.append(line).append('\n');
                if (!blank(h.buyerTin)) sb.append(kv(profile, "TIN", h.buyerTin)).append('\n');
                if (!blank(h.buyerAddress)) {
                    for (String line : wrap("ADDR: " + h.buyerAddress, profile.columns)) sb.append(line).append('\n');
                }
                if (benefit != null) {
                    sb.append(line(profile)).append('\n');
                    sb.append(kv(profile, benefit.benefitType + " ID", defaultIfBlank(benefit.govIdNo, "N/A"))).append('\n');
                    sb.append(kv(profile, "SIGNATURE", defaultIfBlank(benefit.signatureName, benefit.beneficiaryName))).append('\n');
                }
                sb.append(line(profile)).append('\n');
                sb.append(cols(profile, "ITEM", "QTY", "PRICE", "AMT")).append('\n');
                sb.append(line(profile)).append('\n');
                for (InvoiceLine row : lines) appendItem(sb, row.itemDescription, row.qty, row.unitPrice, row.amount, profile);

                sb.append(line(profile)).append('\n');
                sb.append(kv(profile, "NO. OF ITEMS", itemCount(lines))).append('\n');
                sb.append(kvMoney(profile, "GROSS SALES", h.grossSales)).append('\n');
                sb.append(kvMoney(profile, "LESS: DISCOUNT", h.discountTotal)).append('\n');
                sb.append(kvMoney(profile, "VATABLE SALES", h.vatableSales)).append('\n');
                sb.append(kvMoney(profile, "VAT-EXEMPT SALES", h.vatExemptSales)).append('\n');
                sb.append(kvMoney(profile, "ZERO-RATED SALES", h.zeroRatedSales)).append('\n');
                sb.append(kvMoney(profile, "VAT AMOUNT", h.vatAmount)).append('\n');
                if (h.withholdingTaxAmount > 0) sb.append(kvMoney(profile, "W/TAX", h.withholdingTaxAmount)).append('\n');
                sb.append(doubleLine(profile)).append('\n');
                sb.append(kvMoney(profile, "TOTAL DUE", h.totalAmountDue)).append('\n');
                sb.append(kvMoney(profile, "TENDERED", pay.amount)).append('\n');
                sb.append(kvMoney(profile, "CHANGE", Math.max(0d, pay.amount - h.totalAmountDue))).append('\n');
                if (!blank(pay.methodName)) sb.append(kv(profile, "PAYMENT", pay.methodName)).append('\n');
                if (!blank(pay.referenceNo)) sb.append(kv(profile, "REFERENCE", pay.referenceNo)).append('\n');
                sb.append(line(profile)).append('\n');

                if (!blank(h.posVendorName)) {
                    for (String line : wrap("POS SUPPLIER: " + h.posVendorName, profile.columns)) sb.append(line).append('\n');
                }
                if (!blank(h.posVendorTinNo)) sb.append(kv(profile, "SUPPLIER TIN", h.posVendorTinNo)).append('\n');
                if (!blank(h.posVendorAddress)) {
                    for (String line : wrap("SUPPLIER ADDR: " + h.posVendorAddress, profile.columns)) sb.append(line).append('\n');
                }
                if (!blank(h.supplierAccreditationNo)) sb.append(kv(profile, "ACCRED NO", h.supplierAccreditationNo)).append('\n');
                if (!blank(h.accreditationValidUntil)) sb.append(kv(profile, "VALID UNTIL", h.accreditationValidUntil)).append('\n');
                if (!blank(h.birPermitToUseNo)) sb.append(kv(profile, "BIR PTU NO", h.birPermitToUseNo)).append('\n');
                if (!blank(h.permitIssuedAt)) sb.append(kv(profile, "PTU DATE", h.permitIssuedAt)).append('\n');
                if (!blank(h.atpNo)) sb.append(kv(profile, "ATP NO", h.atpNo)).append('\n');
                if (!blank(h.atpIssuedAt)) sb.append(kv(profile, "ATP DATE", h.atpIssuedAt)).append('\n');
                if (!blank(h.approvedSeries)) {
                    for (String line : wrap("APPROVED SERIES: " + h.approvedSeries, profile.columns)) sb.append(line).append('\n');
                }
                if (h.nonVatSeller) {
                    sb.append(line(profile)).append('\n');
                    for (String line : wrap("THIS DOCUMENT IS NOT VALID FOR CLAIM OF INPUT TAX", profile.columns)) sb.append(line).append('\n');
                }
                sb.append(line(profile)).append('\n');
                sb.append(center(profile, "THANK YOU FOR SHOPPING")).append('\n');
            } else {
                sb.append(center(profile, "SALES RECEIPT")).append('\n');
                if (!blank(h.sellerTradeName)) sb.append(center(profile, h.sellerTradeName)).append('\n');
                if (!blank(h.sellerBusinessAddress)) {
                    for (String line : wrap(h.sellerBusinessAddress, profile.columns)) sb.append(center(profile, line)).append('\n');
                }
                if (!blank(h.terminalLabel)) sb.append(center(profile, h.terminalLabel)).append('\n');
                sb.append(line(profile)).append('\n');
                sb.append(kv(profile, "SALE NO", h.saleNo)).append('\n');
                sb.append(kv(profile, "DATE", h.soldAt)).append('\n');
                if (!blank(h.cashierUsername)) sb.append(kv(profile, "USER", h.cashierUsername)).append('\n');
                if (!blank(h.cashierName) && !h.cashierName.equalsIgnoreCase(defaultIfBlank(h.cashierUsername, ""))) {
                    sb.append(kv(profile, "CASHIER", h.cashierName)).append('\n');
                }
                if (!blank(h.buyerName)) sb.append(kv(profile, "CUSTOMER", h.buyerName)).append('\n');
                sb.append(line(profile)).append('\n');
                sb.append(cols(profile, "ITEM", "QTY", "PRICE", "AMT")).append('\n');
                sb.append(line(profile)).append('\n');
                for (InvoiceLine row : lines) appendItem(sb, row.itemDescription, row.qty, row.unitPrice, row.amount, profile);
                sb.append(line(profile)).append('\n');
                sb.append(kv(profile, "NO. OF ITEMS", itemCount(lines))).append('\n');
                sb.append(kvMoney(profile, "SUBTOTAL", h.vatableSales)).append('\n');
                sb.append(kvMoney(profile, "DISCOUNT", h.discountTotal)).append('\n');
                sb.append(kvMoney(profile, "VAT", h.vatAmount)).append('\n');
                sb.append(doubleLine(profile)).append('\n');
                sb.append(kvMoney(profile, "TOTAL DUE", h.totalAmountDue)).append('\n');
                sb.append(kvMoney(profile, "TENDERED", pay.amount)).append('\n');
                sb.append(kvMoney(profile, "CHANGE", Math.max(0d, pay.amount - h.totalAmountDue))).append('\n');
                if (!blank(pay.methodName)) sb.append(kv(profile, "PAYMENT", pay.methodName)).append('\n');
                if (!blank(pay.referenceNo)) sb.append(kv(profile, "REFERENCE", pay.referenceNo)).append('\n');
                sb.append(line(profile)).append('\n');
                for (String line : wrap("PRINTED WITHOUT BIR FISCAL DETAILS", profile.columns)) sb.append(center(profile, line)).append('\n');
                sb.append(center(profile, "THANK YOU FOR SHOPPING")).append('\n');
            }
        }
        String title = birReady ? "Sales Invoice" : "Sales Receipt";
        return new ReceiptDocument("sale-" + saleId + "-" + profile.code.toLowerCase() + ".txt", title, sb.toString(), profile);
    }

    private InvoiceHeader loadInvoiceHeader(Connection conn, long saleId) throws Exception {
        String sql = """
            SELECT si.id, s.sale_no, s.sold_at,
                   si.invoice_no, si.serial_no,
                   si.seller_registered_name, si.seller_trade_name, si.seller_tin_no, si.seller_branch_code,
                   si.seller_business_address, si.seller_vat_registration_type,
                   si.buyer_registered_name, si.buyer_tin_no, si.buyer_business_address,
                   si.gross_sales, si.vatable_sales, si.vat_amount, si.vat_exempt_sales, si.zero_rated_sales,
                   si.discount_total, si.withholding_tax_amount, si.total_amount_due,
                   si.pos_vendor_name, si.pos_vendor_tin_no, si.pos_vendor_address,
                   si.supplier_accreditation_no, si.accreditation_valid_until,
                   si.bir_permit_to_use_no, si.permit_to_use_issued_at, si.atp_no, si.atp_issued_at,
                   si.approved_series_from, si.approved_series_to,
                   si.cash_sales, si.charge_sales,
                   t.code AS terminal_code, t.name AS terminal_name,
                   u.username AS cashier_username, u.full_name AS cashier_name
              FROM sale_invoice si
              JOIN sale s ON s.id = si.sale_id
              JOIN terminal t ON t.id = si.terminal_id
              LEFT JOIN users u ON u.id = s.cashier_user_id
             WHERE si.sale_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                InvoiceHeader h = new InvoiceHeader();
                h.invoiceId = rs.getLong("id");
                h.saleNo = rs.getString("sale_no");
                Timestamp soldAt = rs.getTimestamp("sold_at");
                h.soldAt = soldAt == null ? "" : DT.format(soldAt.toLocalDateTime());
                h.invoiceNo = rs.getString("invoice_no");
                h.serialNo = rs.getLong("serial_no");
                h.sellerRegisteredName = rs.getString("seller_registered_name");
                h.sellerTradeName = rs.getString("seller_trade_name");
                h.sellerBusinessAddress = rs.getString("seller_business_address");
                h.buyerName = rs.getString("buyer_registered_name");
                h.cashierUsername = rs.getString("cashier_username");
                h.cashierName = rs.getString("cashier_name");
                h.buyerTin = rs.getString("buyer_tin_no");
                h.buyerAddress = rs.getString("buyer_business_address");
                h.grossSales = n(rs, "gross_sales");
                h.vatableSales = n(rs, "vatable_sales");
                h.vatAmount = n(rs, "vat_amount");
                h.vatExemptSales = n(rs, "vat_exempt_sales");
                h.zeroRatedSales = n(rs, "zero_rated_sales");
                h.discountTotal = n(rs, "discount_total");
                h.withholdingTaxAmount = n(rs, "withholding_tax_amount");
                h.totalAmountDue = n(rs, "total_amount_due");
                h.posVendorName = rs.getString("pos_vendor_name");
                h.posVendorTinNo = rs.getString("pos_vendor_tin_no");
                h.posVendorAddress = rs.getString("pos_vendor_address");
                h.supplierAccreditationNo = rs.getString("supplier_accreditation_no");
                h.accreditationValidUntil = dateStr(rs.getDate("accreditation_valid_until"));
                h.birPermitToUseNo = rs.getString("bir_permit_to_use_no");
                h.permitIssuedAt = dateStr(rs.getDate("permit_to_use_issued_at"));
                h.atpNo = rs.getString("atp_no");
                h.atpIssuedAt = dateStr(rs.getDate("atp_issued_at"));
                long seriesFrom = rs.getLong("approved_series_from");
                long seriesTo = rs.getLong("approved_series_to");
                h.approvedSeries = (seriesFrom > 0 && seriesTo > 0) ? (seriesFrom + " - " + seriesTo) : "";
                h.chargeSales = rs.getInt("charge_sales") == 1;
                h.nonVatSeller = "NONVAT".equalsIgnoreCase(rs.getString("seller_vat_registration_type"));
                String tin = defaultIfBlank(rs.getString("seller_tin_no"), "");
                String branch = defaultIfBlank(rs.getString("seller_branch_code"), "");
                String vatType = h.nonVatSeller ? "NON-VAT REG TIN" : "VAT REG TIN";
                h.vatLine = (vatType + " " + tin + (branch.isBlank() ? "" : "-" + branch)).trim();
                String terminalCode = defaultIfBlank(rs.getString("terminal_code"), "");
                String terminalName = defaultIfBlank(rs.getString("terminal_name"), "");
                h.terminalLabel = (terminalCode + (terminalName.isBlank() ? "" : " / " + terminalName)).trim();
                return h;
            }
        }
    }

    private List<InvoiceLine> loadInvoiceLines(Connection conn, long invoiceId) throws Exception {
        List<InvoiceLine> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT item_description, qty_in_base, unit_price, line_net_amount FROM sale_invoice_line WHERE sale_invoice_id = ? ORDER BY line_no")) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceLine line = new InvoiceLine();
                    line.itemDescription = rs.getString(1);
                    line.qty = fmt(rs.getBigDecimal(2));
                    line.unitPrice = money.format(rs.getBigDecimal(3));
                    line.amount = money.format(rs.getBigDecimal(4));
                    out.add(line);
                }
            }
        }
        return out;
    }

    private PaymentInfo loadPayment(Connection conn, long saleId) throws Exception {
        PaymentInfo p = new PaymentInfo();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT pm.name, sp.amount, sp.reference_no FROM sale_payment sp JOIN payment_method pm ON pm.id = sp.method_id WHERE sp.sale_id = ? FETCH FIRST ROW ONLY")) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.methodName = rs.getString(1);
                    p.amount = n(rs, 2);
                    p.referenceNo = rs.getString(3);
                }
            }
        }
        return p;
    }

    private BenefitInfo loadBenefit(Connection conn, long invoiceId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT benefit_type, beneficiary_name, gov_id_no, signature_name FROM sale_benefit_claim WHERE sale_invoice_id = ? FETCH FIRST ROW ONLY")) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                BenefitInfo b = new BenefitInfo();
                b.benefitType = rs.getString(1);
                b.beneficiaryName = rs.getString(2);
                b.govIdNo = rs.getString(3);
                b.signatureName = rs.getString(4);
                return b;
            }
        }
    }

    private InvoiceHeader loadSaleHeaderFallback(Connection conn, long saleId) throws Exception {
        String sql = """
            SELECT s.sale_no, s.sold_at, c.full_name, c.tin_no, c.address,
                   COALESCE(v.gross_amount, 0) AS gross_amount,
                   COALESCE(v.discount_total, 0) AS discount_total,
                   COALESCE(v.tax_total, 0) AS tax_total,
                   COALESCE(v.total_due, 0) AS total_due,
                   st.name AS store_name, st.address AS store_address,
                   t.code AS terminal_code, t.name AS terminal_name,
                   u.username AS cashier_username, u.full_name AS cashier_name
              FROM sale s
              JOIN store st ON st.id = s.store_id
              JOIN terminal t ON t.id = s.terminal_id
              LEFT JOIN customer c ON c.id = s.customer_id
              LEFT JOIN users u ON u.id = s.cashier_user_id
              LEFT JOIN v_sale_totals v ON v.sale_id = s.id
             WHERE s.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Sale not found for receipt id " + saleId);
                InvoiceHeader h = new InvoiceHeader();
                h.saleNo = rs.getString("sale_no");
                Timestamp soldAt = rs.getTimestamp("sold_at");
                h.soldAt = soldAt == null ? "" : DT.format(soldAt.toLocalDateTime());
                h.sellerTradeName = rs.getString("store_name");
                h.sellerRegisteredName = rs.getString("store_name");
                h.sellerBusinessAddress = rs.getString("store_address");
                h.buyerName = rs.getString("full_name");
                h.buyerTin = rs.getString("tin_no");
                h.buyerAddress = rs.getString("address");
                h.cashierUsername = rs.getString("cashier_username");
                h.cashierName = rs.getString("cashier_name");
                h.grossSales = n(rs, "gross_amount");
                h.discountTotal = n(rs, "discount_total");
                h.vatAmount = n(rs, "tax_total");
                h.totalAmountDue = n(rs, "total_due");
                h.vatableSales = Math.max(0d, h.totalAmountDue - h.vatAmount);
                String terminalCode = defaultIfBlank(rs.getString("terminal_code"), "");
                String terminalName = defaultIfBlank(rs.getString("terminal_name"), "");
                h.terminalLabel = (terminalCode + (terminalName.isBlank() ? "" : " / " + terminalName)).trim();
                return h;
            }
        }
    }

    private List<InvoiceLine> loadSaleLinesFallback(Connection conn, long saleId) throws Exception {
        List<InvoiceLine> out = new ArrayList<>();
        String sql = """
            SELECT p.name AS item_description,
                   sl.qty_in_base,
                   sl.unit_price,
                   ((sl.qty_in_base * sl.unit_price)
                     - COALESCE((SELECT SUM(sd.amount) FROM sale_discount sd WHERE sd.sale_line_id = sl.id), 0)
                     + CASE WHEN sl.price_includes_tax = 1 THEN 0 ELSE COALESCE((SELECT SUM(slt.tax_amount) FROM sale_line_tax slt WHERE slt.sale_line_id = sl.id), 0) END
                   ) AS line_amount
              FROM sale_line sl
              JOIN product p ON p.id = sl.product_id
             WHERE sl.sale_id = ?
             ORDER BY sl.line_no
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceLine line = new InvoiceLine();
                    line.itemDescription = rs.getString("item_description");
                    line.qty = fmt(rs.getBigDecimal("qty_in_base"));
                    line.unitPrice = money.format(rs.getBigDecimal("unit_price"));
                    line.amount = money.format(rs.getBigDecimal("line_amount"));
                    out.add(line);
                }
            }
        }
        return out;
    }

    private ReceiptDocument buildReturnReceipt(long returnId, PaperProfile profile) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            String returnNo = "";
            String returnedAt = "";
            double refund = 0;
            String method = "";
            String saleNo = "";
            String invoiceNo = "";

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT sr.return_no, sr.returned_at, s.sale_no, si.invoice_no FROM sales_return sr JOIN sale s ON s.id = sr.sale_id LEFT JOIN sale_invoice si ON si.sale_id = s.id WHERE sr.id = ?")) {
                ps.setLong(1, returnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        returnNo = rs.getString(1);
                        Timestamp ts = rs.getTimestamp(2);
                        returnedAt = ts == null ? "" : DT.format(ts.toLocalDateTime());
                        saleNo = rs.getString(3) == null ? "" : rs.getString(3);
                        invoiceNo = rs.getString(4) == null ? "" : rs.getString(4);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT pm.name, srr.amount FROM sales_return_refund srr JOIN payment_method pm ON pm.id = srr.method_id WHERE sales_return_id = ? FETCH FIRST ROW ONLY")) {
                ps.setLong(1, returnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        method = rs.getString(1);
                        refund = rs.getBigDecimal(2).doubleValue();
                    }
                }
            }

            sb.append(center(profile, "RETURN RECEIPT")).append('\n');
            sb.append(line(profile)).append('\n');
            sb.append(kv(profile, "RETURN #", returnNo)).append('\n');
            sb.append(kv(profile, "DATE", returnedAt)).append('\n');
            if (!blank(saleNo)) sb.append(kv(profile, "SALE #", saleNo)).append('\n');
            if (!blank(invoiceNo)) sb.append(kv(profile, "INVOICE #", invoiceNo)).append('\n');
            sb.append(line(profile)).append('\n');
            sb.append(cols(profile, "ITEM", "QTY", "PRICE", "AMT")).append('\n');
            sb.append(line(profile)).append('\n');

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.name, qty_in_base, unit_price_refund, tax_refund, discount_refund, ((qty_in_base * unit_price_refund) + tax_refund - discount_refund) AS amount FROM sales_return_line srl JOIN product p ON p.id = srl.product_id WHERE sales_return_id = ? ORDER BY line_no")) {
                ps.setLong(1, returnId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        appendItem(sb,
                                rs.getString(1),
                                fmt(rs.getBigDecimal(2)),
                                money.format(rs.getBigDecimal(3)),
                                money.format(rs.getBigDecimal(6)),
                                profile);
                    }
                }
            }

            sb.append(doubleLine(profile)).append('\n');
            sb.append(kvMoney(profile, "REFUND", refund)).append('\n');
            if (!blank(method)) sb.append(kv(profile, "METHOD", method)).append('\n');
            sb.append(line(profile)).append('\n');
            sb.append(center(profile, "RETURN COMPLETED")).append('\n');
            conn.commit();
        }
        return new ReceiptDocument("return-" + returnId + "-" + profile.code.toLowerCase() + ".txt", "Return Receipt", sb.toString(), profile);
    }

    private void showReceiptDialog(java.awt.Window owner, ReceiptBuilder builder, String dialogTitle, boolean allowPrint) throws Exception {
        JDialog dlg = new JDialog(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));

        JTextArea area = new JTextArea(34, 42);
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JLabel lblSaved = new JLabel();
        JComboBox<PrinterChoice> cboPrinters = new JComboBox<>(loadPrinters());
        cboPrinters.setPrototypeDisplayValue(new PrinterChoice("Xprinter XP-58", null));
        selectPreferredPrinter(cboPrinters);

        JComboBox<PaperProfile> cboPaper = new JComboBox<>(new PaperProfile[]{PAPER_56, PAPER_80});
        selectPreferredPaper(cboPaper);
        JCheckBox chkRemember = new JCheckBox("Remember as default", true);

        ReceiptDocument[] current = new ReceiptDocument[1];
        Runnable refresh = () -> {
            try {
                PaperProfile profile = (PaperProfile) cboPaper.getSelectedItem();
                if (profile == null) profile = PAPER_56;
                ReceiptDocument doc = builder.build(profile);
                current[0] = doc;
                Path saved = saveReceipt(doc.filename, doc.text);
                area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, profile.previewFontSize));
                area.setText(doc.text);
                area.setColumns(profile.columns + 2);
                area.setCaretPosition(0);
                lblSaved.setText("Saved: " + saved);
                dlg.setTitle(doc.title + " - " + profile.label);
            } catch (Exception ex) {
                area.setText(ex.getMessage());
                lblSaved.setText("Saved: -");
            }
        };
        refresh.run();

        cboPaper.addActionListener(e -> refresh.run());

        dlg.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        south.add(lblSaved, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("Print Receipt");
        JButton btnClose = new JButton("Close");
        if (!allowPrint || cboPrinters.getItemCount() == 0) btnPrint.setEnabled(false);

        controls.add(new JLabel("Printer"));
        controls.add(cboPrinters);
        controls.add(new JLabel("Paper"));
        controls.add(cboPaper);
        controls.add(chkRemember);
        controls.add(btnPrint);
        controls.add(btnClose);
        south.add(controls, BorderLayout.SOUTH);

        btnPrint.addActionListener(e -> {
            try {
                PaperProfile profile = (PaperProfile) cboPaper.getSelectedItem();
                if (profile == null) profile = PAPER_56;
                ReceiptDocument doc = builder.build(profile);
                current[0] = doc;
                saveReceipt(doc.filename, doc.text);
                PrinterChoice choice = (PrinterChoice) cboPrinters.getSelectedItem();
                printToThermal(doc.text, choice == null ? null : choice.service, profile);
                if (chkRemember.isSelected()) saveDefaults(choice, profile);
                JOptionPane.showMessageDialog(dlg, "Receipt sent to printer.", "Printed", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dlg.dispose());

        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private DefaultComboBoxModel<PrinterChoice> loadPrinters() {
        DefaultComboBoxModel<PrinterChoice> model = new DefaultComboBoxModel<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) model.addElement(new PrinterChoice(service.getName(), service));
        return model;
    }

    private void selectPreferredPrinter(JComboBox<PrinterChoice> combo) {
        String savedName = PREFS.get(PREF_PRINTER_NAME, "");
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        int savedIndex = -1;
        int defaultIndex = -1;
        int xprinterIndex = -1;
        for (int i = 0; i < combo.getItemCount(); i++) {
            PrinterChoice choice = combo.getItemAt(i);
            if (choice == null || choice.service == null) continue;
            String actualName = choice.service.getName();
            String name = actualName.toLowerCase();
            if (savedIndex < 0 && !savedName.isBlank() && actualName.equalsIgnoreCase(savedName)) savedIndex = i;
            if (xprinterIndex < 0 && name.contains("xprinter")) xprinterIndex = i;
            if (defaultService != null && actualName.equals(defaultService.getName())) defaultIndex = i;
        }
        if (savedIndex >= 0) combo.setSelectedIndex(savedIndex);
        else if (xprinterIndex >= 0) combo.setSelectedIndex(xprinterIndex);
        else if (defaultIndex >= 0) combo.setSelectedIndex(defaultIndex);
    }

    private void selectPreferredPaper(JComboBox<PaperProfile> combo) {
        String code = PREFS.get(PREF_PAPER_CODE, PAPER_56.code);
        for (int i = 0; i < combo.getItemCount(); i++) {
            PaperProfile profile = combo.getItemAt(i);
            if (profile != null && profile.code.equalsIgnoreCase(code)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedItem(PAPER_56);
    }

    private void saveDefaults(PrinterChoice choice, PaperProfile profile) {
        if (choice != null && choice.service != null) PREFS.put(PREF_PRINTER_NAME, choice.service.getName());
        if (profile != null) PREFS.put(PREF_PAPER_CODE, profile.code);
    }

    private void printToThermal(String text, PrintService service, PaperProfile profile) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        if (service != null) job.setPrintService(service);
        String[] lines = (text == null ? "" : text.replace("\r\n", "\n")).split("\n", -1);
        PageFormat page = buildPageFormat(job, profile, lines.length);
        job.setPrintable(new ThermalReceiptPrintable(lines, profile), page);
        job.print();
    }

    private PageFormat buildPageFormat(PrinterJob job, PaperProfile profile, int lineCount) {
        double paperWidth = profile.widthMm * MM_TO_POINTS;
        double margin = 1.6d * MM_TO_POINTS;
        double paperHeight = Math.max(80d * MM_TO_POINTS, (lineCount + 4) * profile.printLineHeight + (margin * 2));

        Paper paper = new Paper();
        paper.setSize(paperWidth, paperHeight);
        paper.setImageableArea(margin, margin, paperWidth - (margin * 2), paperHeight - (margin * 2));

        PageFormat pf = job.defaultPage();
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }

    private void appendItem(StringBuilder sb, String item, String qty, String price, String amount, PaperProfile profile) {
        String[] wrapped = wrap(item, profile.itemWidth);
        for (int i = 0; i < wrapped.length; i++) {
            if (i == 0) {
                sb.append(padRight(wrapped[i], profile.itemWidth))
                        .append(padLeft(qty, profile.qtyWidth))
                        .append(padLeft(price, profile.priceWidth))
                        .append(padLeft(amount, profile.amountWidth))
                        .append('\n');
            } else {
                sb.append(wrapped[i]).append('\n');
            }
        }
    }

    private String[] wrap(String text, int max) {
        if (text == null) return new String[]{""};
        List<String> out = new ArrayList<>();
        String remaining = text.trim();
        if (remaining.isEmpty()) return new String[]{""};
        while (remaining.length() > max) {
            int cut = remaining.lastIndexOf(' ', max);
            if (cut <= 0) cut = max;
            out.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }
        out.add(remaining);
        return out.toArray(new String[0]);
    }

    private String center(PaperProfile profile, String s) {
        String value = defaultIfBlank(s, "");
        if (value.length() >= profile.columns) return value.substring(0, profile.columns);
        int left = (profile.columns - value.length()) / 2;
        return " ".repeat(Math.max(0, left)) + value;
    }

    private String cols(PaperProfile profile, String a, String b, String c, String d) {
        return padRight(a, profile.itemWidth) + padLeft(b, profile.qtyWidth) + padLeft(c, profile.priceWidth) + padLeft(d, profile.amountWidth);
    }

    private String itemCount(List<InvoiceLine> lines) {
        java.math.BigDecimal totalQty = java.math.BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            try {
                totalQty = totalQty.add(new java.math.BigDecimal(defaultIfBlank(line.qty, "0")));
            } catch (Exception ignored) {
            }
        }
        if (totalQty.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return totalQty.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(lines == null ? 0 : lines.size());
    }

    private String kv(PaperProfile profile, String k, String v) {
        String left = k + ": ";
        String value = defaultIfBlank(v, "");
        if (left.length() + value.length() <= profile.columns) return left + value;
        return left + value.substring(0, Math.max(0, profile.columns - left.length()));
    }

    private String kvMoney(PaperProfile profile, String k, double v) {
        String value = money.format(v);
        return padRight(k, profile.columns - value.length()) + value;
    }

    private String line(PaperProfile profile) { return "-".repeat(profile.columns); }
    private String doubleLine(PaperProfile profile) { return "=".repeat(profile.columns); }

    private String padRight(String s, int len) {
        String value = defaultIfBlank(s, "");
        if (value.length() >= len) return value.substring(0, len);
        return value + " ".repeat(len - value.length());
    }

    private String padLeft(String s, int len) {
        String value = defaultIfBlank(s, "");
        if (value.length() >= len) return value.substring(0, len);
        return " ".repeat(len - value.length()) + value;
    }

    private Path saveReceipt(String filename, String text) throws Exception {
        Path dir = Path.of(System.getProperty("user.home"), ".ensarium", "receipts");
        Files.createDirectories(dir);
        Path path = dir.resolve(filename);
        Files.writeString(path, text, StandardCharsets.UTF_8);
        return path;
    }

    private double n(ResultSet rs, String col) throws Exception {
        return rs.getBigDecimal(col) == null ? 0d : rs.getBigDecimal(col).doubleValue();
    }

    private double n(ResultSet rs, int col) throws Exception {
        return rs.getBigDecimal(col) == null ? 0d : rs.getBigDecimal(col).doubleValue();
    }

    private String fmt(java.math.BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private String dateStr(java.sql.Date d) {
        return d == null ? "" : d.toLocalDate().toString();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private String defaultIfBlank(String s, String def) {
        return blank(s) ? def : s;
    }

    @FunctionalInterface
    private interface ReceiptBuilder {
        ReceiptDocument build(PaperProfile profile) throws Exception;
    }

    private static class ReceiptDocument {
        final String filename;
        final String title;
        final String text;
        final PaperProfile profile;
        ReceiptDocument(String filename, String title, String text, PaperProfile profile) {
            this.filename = filename;
            this.title = title;
            this.text = text;
            this.profile = profile;
        }
    }

    private static class PaperProfile {
        final String code;
        final String label;
        final double widthMm;
        final int columns;
        final int itemWidth;
        final int qtyWidth;
        final int priceWidth;
        final int amountWidth;
        final int previewFontSize;
        final double printLineHeight;
        final int printFontSize;

        PaperProfile(String code, String label, double widthMm, int columns, int itemWidth, int qtyWidth, int priceWidth,
                     int amountWidth, int previewFontSize, double printLineHeight, int printFontSize) {
            this.code = code;
            this.label = label;
            this.widthMm = widthMm;
            this.columns = columns;
            this.itemWidth = itemWidth;
            this.qtyWidth = qtyWidth;
            this.priceWidth = priceWidth;
            this.amountWidth = amountWidth;
            this.previewFontSize = previewFontSize;
            this.printLineHeight = printLineHeight;
            this.printFontSize = printFontSize;
        }

        @Override public String toString() { return label; }
    }

    private static class PrinterChoice {
        final String label;
        final PrintService service;
        PrinterChoice(String label, PrintService service) { this.label = label; this.service = service; }
        @Override public String toString() { return label; }
    }

    private static class ThermalReceiptPrintable implements Printable {
        private final String[] lines;
        private final Font font;

        ThermalReceiptPrintable(String[] lines, PaperProfile profile) {
            this.lines = lines;
            this.font = new Font(Font.MONOSPACED, Font.PLAIN, profile.printFontSize);
        }

        @Override public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setFont(font);
            int y = g2.getFontMetrics().getAscent();
            int lineHeight = g2.getFontMetrics().getHeight();
            for (String line : lines) {
                g2.drawString(line, 0, y);
                y += lineHeight;
            }
            g2.dispose();
            return PAGE_EXISTS;
        }
    }

    private static class InvoiceHeader {
        long invoiceId;
        String saleNo;
        String soldAt;
        String invoiceNo;
        long serialNo;
        String sellerRegisteredName;
        String sellerTradeName;
        String sellerBusinessAddress;
        String vatLine;
        String terminalLabel;
        String buyerName;
        String cashierUsername;
        String cashierName;
        String buyerTin;
        String buyerAddress;
        double grossSales;
        double vatableSales;
        double vatAmount;
        double vatExemptSales;
        double zeroRatedSales;
        double discountTotal;
        double withholdingTaxAmount;
        double totalAmountDue;
        String posVendorName;
        String posVendorTinNo;
        String posVendorAddress;
        String supplierAccreditationNo;
        String accreditationValidUntil;
        String birPermitToUseNo;
        String permitIssuedAt;
        String atpNo;
        String atpIssuedAt;
        String approvedSeries;
        boolean chargeSales;
        boolean nonVatSeller;
    }

    private static class InvoiceLine {
        String itemDescription;
        String qty;
        String unitPrice;
        String amount;
    }

    private static class PaymentInfo {
        String methodName;
        String referenceNo;
        double amount;
    }

    private static class BenefitInfo {
        String benefitType;
        String beneficiaryName;
        String govIdNo;
        String signatureName;
    }
}
