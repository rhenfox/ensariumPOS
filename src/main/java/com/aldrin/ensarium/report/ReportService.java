package com.aldrin.ensarium.report;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.awt.Component;
import java.awt.Window;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class ReportService {

    public static final String SQL_BIR_INVOICE_LIST =
            "SELECT "
            + "sale_id AS \"__SALE_ID\", "
            + "invoice_no AS \"Invoice No\", "
            + "invoice_date AS \"Date\", "
            + "store_code AS \"Store\", "
            + "terminal_code AS \"Terminal\", "
            + "seller_tin_no || '-' || seller_branch_code AS \"Seller TIN/Branch\", "
            + "buyer_registered_name AS \"Buyer\", "
            + "buyer_tin_no AS \"Buyer TIN\", "
            + "bir_permit_to_use_no AS \"PTU No\", "
            + "atp_no AS \"ATP No\", "
            + "gross_sales AS \"Gross Sales\", "
            + "vatable_sales AS \"Vatable Sales\", "
            + "vat_amount AS \"VAT Amount\", "
            + "vat_exempt_sales AS \"VAT Exempt\", "
            + "zero_rated_sales AS \"Zero Rated\", "
            + "discount_total AS \"Discount\", "
            + "total_amount_due AS \"Total Due\" "
            + "FROM v_bir_invoice_list "
            + "WHERE invoice_date BETWEEN ? AND ? "
            + "ORDER BY invoice_date, invoice_no";

    public static final String SQL_TAX_SUMMARY =
            "SELECT "
            + "business_date AS \"Date\", "
            + "store_code AS \"Store\", "
            + "terminal_code AS \"Terminal\", "
            + "invoice_count AS \"Invoice Count\", "
            + "void_invoice_count AS \"Void Count\", "
            + "gross_sales AS \"Gross Sales\", "
            + "vatable_sales AS \"Vatable Sales\", "
            + "vat_amount AS \"VAT Amount\", "
            + "vat_exempt_sales AS \"VAT Exempt\", "
            + "zero_rated_sales AS \"Zero Rated\", "
            + "withholding_tax_amount AS \"Withholding Tax\", "
            + "discount_total AS \"Discount\", "
            + "total_amount_due AS \"Total Due\" "
            + "FROM v_bir_tax_summary "
            + "WHERE business_date BETWEEN ? AND ? "
            + "ORDER BY business_date, store_code, terminal_code";

    public static final String SQL_MONTHLY_MACHINE_SALES =
            "SELECT "
            + "year_no AS \"Year\", "
            + "month_no AS \"Month\", "
            + "store_code AS \"Store\", "
            + "machine_code AS \"Machine\", "
            + "seller_tin_no || '-' || seller_branch_code AS \"Seller TIN/Branch\", "
            + "invoice_count AS \"Invoice Count\", "
            + "first_serial_no AS \"First Serial\", "
            + "last_serial_no AS \"Last Serial\", "
            + "gross_sales AS \"Gross Sales\", "
            + "vatable_sales AS \"Vatable Sales\", "
            + "vat_amount AS \"VAT Amount\", "
            + "vat_exempt_sales AS \"VAT Exempt\", "
            + "zero_rated_sales AS \"Zero Rated\", "
            + "discount_total AS \"Discount\", "
            + "withholding_tax_amount AS \"Withholding Tax\", "
            + "total_amount_due AS \"Total Due\" "
            + "FROM v_bir_monthly_machine_sales "
            + "WHERE (year_no * 100 + month_no) BETWEEN ? AND ? "
            + "ORDER BY year_no, month_no, store_code, machine_code";

    public static final String SQL_POS_PROFIT =
            "SELECT "
            + "sale_id AS \"__SALE_ID\", "
            + "sale_no AS \"Sale No\", "
            + "business_date AS \"Date\", "
            + "store_code AS \"Store\", "
            + "terminal_code AS \"Terminal\", "
            + "invoice_no AS \"Invoice No\", "
            + "customer_name AS \"Customer\", "
            + "gross_amount AS \"Gross Amount\", "
            + "discount_total AS \"Discount\", "
            + "tax_total AS \"Tax Total\", "
            + "net_sales AS \"Net Sales\", "
            + "cost_of_goods_sold AS \"COGS\", "
            + "gross_profit AS \"Gross Profit\", "
            + "profit_margin_pct AS \"Profit Margin %\" "
            + "FROM v_pos_profit_report "
            + "WHERE business_date BETWEEN ? AND ? "
            + "ORDER BY business_date, sale_no";

    public static final String SQL_FINANCIAL_STATEMENT =
            "SELECT "
            + "business_date AS \"Date\", "
            + "gross_sales AS \"Gross Sales\", "
            + "sales_discount AS \"Sales Discount\", "
            + "sales_net AS \"Sales Net\", "
            + "output_vat AS \"Output VAT\", "
            + "sales_returns AS \"Sales Returns\", "
            + "net_sales_after_returns AS \"Net Sales After Returns\", "
            + "net_cost_of_goods_sold AS \"Net COGS\", "
            + "gross_profit AS \"Gross Profit\", "
            + "purchases AS \"Purchases\", "
            + "return_cost_total AS \"Return Cost\", "
            + "damage_total AS \"Damage\", "
            + "expire_total AS \"Expire\", "
            + "adjustment_total AS \"Adjustment\", "
            + "(COALESCE(gross_sales,0) + COALESCE(sales_discount,0) + COALESCE(sales_net,0) + COALESCE(output_vat,0) + COALESCE(sales_returns,0) + COALESCE(net_sales_after_returns,0) + COALESCE(net_cost_of_goods_sold,0) + COALESCE(gross_profit,0) + COALESCE(purchases,0) + COALESCE(return_cost_total,0) + COALESCE(damage_total,0) + COALESCE(expire_total,0) + COALESCE(adjustment_total,0)) AS \"Row Total\" "
            + "FROM v_financial_statement "
            + "WHERE business_date BETWEEN ? AND ? "
            + "ORDER BY business_date";

    public static final String SQL_SALE_HEADER =
            "SELECT "
            + "s.sale_no, "
            + "si.invoice_no, "
            + "CAST(s.sold_at AS DATE) AS sold_date, "
            + "st.name AS store_name, "
            + "t.code AS terminal_code, "
            + "COALESCE(c.full_name, 'WALK-IN CUSTOMER') AS customer_name "
            + "FROM sale s "
            + "JOIN store st ON st.id = s.store_id "
            + "JOIN terminal t ON t.id = s.terminal_id "
            + "LEFT JOIN customer c ON c.id = s.customer_id "
            + "LEFT JOIN sale_invoice si ON si.sale_id = s.id "
            + "WHERE s.id = ?";

    public static final String SQL_SALE_DETAILS =
            "SELECT "
            + "sl.line_no AS \"Line No\", "
            + "COALESCE(sil.item_description, p.name) AS \"Item Description\", "
            + "sl.qty_in_base AS \"Qty\", "
            + "sl.unit_price AS \"Unit Price\", "
            + "(sl.qty_in_base * sl.unit_price) AS \"Gross Amount\", "
            + "COALESCE(vld.discount_total, 0) AS \"Discount\", "
            + "COALESCE(vlt.tax_total, 0) AS \"VAT Amount\", "
            + "CASE WHEN sl.price_includes_tax = 1 "
            + "THEN (sl.qty_in_base * sl.unit_price) - COALESCE(vld.discount_total,0) "
            + "ELSE (sl.qty_in_base * sl.unit_price) - COALESCE(vld.discount_total,0) + COALESCE(vlt.tax_total,0) END AS \"Net Amount\", "
            + "sl.buying_price AS \"Buying Price\", "
            + "sl.cost_total AS \"Cost Total\", "
            + "(CASE WHEN sl.price_includes_tax = 1 "
            + "THEN (sl.qty_in_base * sl.unit_price) - COALESCE(vld.discount_total,0) "
            + "ELSE (sl.qty_in_base * sl.unit_price) - COALESCE(vld.discount_total,0) + COALESCE(vlt.tax_total,0) END "
            + "- COALESCE(vlt.tax_total,0) - sl.cost_total) AS \"Gross Profit\" "
            + "FROM sale_line sl "
            + "JOIN sale s ON s.id = sl.sale_id "
            + "LEFT JOIN product p ON p.id = sl.product_id "
            + "LEFT JOIN sale_invoice_line sil ON sil.sale_line_id = sl.id "
            + "LEFT JOIN v_sale_line_discount vld ON vld.sale_line_id = sl.id "
            + "LEFT JOIN v_sale_line_tax_total vlt ON vlt.sale_line_id = sl.id "
            + "WHERE s.id = ? "
            + "ORDER BY sl.line_no";

    public DefaultTableModel loadBirInvoiceList(Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            return TableLoader.load(con, SQL_BIR_INVOICE_LIST, from, to);
        }
    }

    public DefaultTableModel loadTaxSummary(Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            return TableLoader.load(con, SQL_TAX_SUMMARY, from, to);
        }
    }

    public DefaultTableModel loadMonthlyMachineSales(Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            return TableLoader.loadMonthly(con, SQL_MONTHLY_MACHINE_SALES, from, to);
        }
    }

    public DefaultTableModel loadPosProfit(Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            return TableLoader.load(con, SQL_POS_PROFIT, from, to);
        }
    }

    public DefaultTableModel loadFinancialStatement(Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            return TableLoader.load(con, SQL_FINANCIAL_STATEMENT, from, to);
        }
    }

    public DefaultTableModel loadSaleDetails(long saleId) throws Exception {
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_SALE_DETAILS)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                return TableLoader.buildTableModel(rs);
            }
        }
    }

    public String loadSaleHeader(long saleId) throws Exception {
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_SALE_HEADER)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String saleNo = rs.getString("sale_no");
                    String invoiceNo = rs.getString("invoice_no");
                    java.sql.Date soldDate = rs.getDate("sold_date");
                    String store = rs.getString("store_name");
                    String terminal = rs.getString("terminal_code");
                    String customer = rs.getString("customer_name");
                    return "Sale No: " + saleNo
                            + " | Invoice No: " + (invoiceNo == null ? "N/A" : invoiceNo)
                            + " | Date: " + soldDate
                            + " | Store: " + store
                            + " | Terminal: " + terminal
                            + " | Customer: " + customer;
                }
            }
        }
        return "Sale Details";
    }

    public void showSaleDetails(Component parent, long saleId) throws Exception {
        DefaultTableModel model = loadSaleDetails(saleId);
        String header = loadSaleHeader(saleId);
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        new SaleDetailsDialog(owner, "Sale Details", header, model).setVisible(true);
    }

    private Session session;
    
    public void previewReport(String jrxmlName, String reportTitle, Date from, Date to) throws Exception {
        try (Connection con = Db.getConnection()) {
            InputStream jrxml = getClass().getResourceAsStream("/reports/" + jrxmlName);
            if (jrxml == null) {
                throw new IllegalStateException("Report template not found: " + jrxmlName);
            }
            var jasperReport = JasperCompileManager.compileReport(jrxml);
            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_TITLE", reportTitle);
            params.put("DATE_FROM", new java.sql.Date(from.getTime()));
            params.put("DATE_TO", new java.sql.Date(to.getTime()));
            params.put("FROM_YM", yearMonth(from));
            params.put("TO_YM", yearMonth(to));
//            params.put("PRINTED_BY", System.getProperty("user.name", "Unknown User"));
            params.put("PRINTED_BY", session.session.getFullName());
            params.put("PRINTED_AT", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            ReportHeader header = loadReportHeader(con);
            params.put("HEADER_REGISTERED_NAME", header.registeredName());
            params.put("HEADER_TRADE_NAME", header.tradeName());
            params.put("HEADER_TIN", header.tinNo());
            params.put("HEADER_BRANCH_CODE", header.branchCode());
            params.put("HEADER_BUSINESS_ADDRESS", header.businessAddress());
            params.put("HEADER_PTU_NO", header.ptuNo());
            params.put("HEADER_ATP_NO", header.atpNo());
            params.put("HEADER_POS_VENDOR", header.posVendorName());

            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle(reportTitle);
            viewer.setVisible(true);
        }
    }

    private ReportHeader loadReportHeader(Connection con) {
        String sql = "SELECT tp.registered_name, tp.trade_name, tp.tin_no, sfp.branch_code, sfp.registered_business_address, sfp.bir_permit_to_use_no, sfp.atp_no, sfp.pos_vendor_name "
                + "FROM store_fiscal_profile sfp "
                + "JOIN taxpayer_profile tp ON tp.id = sfp.taxpayer_profile_id "
                + "WHERE sfp.active = 1 FETCH FIRST ROW ONLY";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new ReportHeader(
                        nvl(rs.getString(1)),
                        nvl(rs.getString(2)),
                        nvl(rs.getString(3)),
                        nvl(rs.getString(4)),
                        nvl(rs.getString(5)),
                        nvl(rs.getString(6)),
                        nvl(rs.getString(7)),
                        nvl(rs.getString(8)));
            }
        } catch (SQLException ignored) {
        }
        return new ReportHeader("", "", "", "", "", "", "", "");
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private record ReportHeader(
            String registeredName,
            String tradeName,
            String tinNo,
            String branchCode,
            String businessAddress,
            String ptuNo,
            String atpNo,
            String posVendorName) {
    }

    private int yearMonth(Date value) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(value);
        return cal.get(java.util.Calendar.YEAR) * 100 + (cal.get(java.util.Calendar.MONTH) + 1);
    }
}
