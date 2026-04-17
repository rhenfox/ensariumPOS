package com.aldrin.ensarium.inventory.product;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ProductInventoryDao {

    public List<ProductInventoryItem> findInventory(String keyword, int limit) throws SQLException {
        String sql = """
                select p.id, coalesce(p.sku, '') sku, p.name, coalesce(pc.name, 'Uncategorized') category_name,
                       coalesce(u.code, '') uom_code,
                       p.buying_price, p.selling_price, p.price_includes_tax, coalesce(t.rate, 0) tax_rate
                from product p
                left join product_category pc on pc.id = p.category_id
                left join uom u on u.id = p.base_uom_id
                left join tax t on t.id = p.default_tax_id
                where p.active = 1
                  and (? is null or ? = ''
                       or upper(coalesce(p.sku, '')) like ?
                       or upper(p.name) like ?
                       or upper(coalesce(pc.name, '')) like ?)
                order by coalesce(pc.name, 'Uncategorized'), p.name
                fetch first ? rows only
                """;

        String trimmed = keyword == null ? "" : keyword.trim();
        String like = "%" + trimmed.toUpperCase() + "%";
        List<ProductInventoryItem> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, trimmed);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setInt(6, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductInventoryItem row = new ProductInventoryItem();
                    row.setProductId(rs.getLong("id"));
                    row.setSku(rs.getString("sku"));
                    row.setProductName(rs.getString("name"));
                    row.setCategoryName(rs.getString("category_name"));
                    row.setUomCode(rs.getString("uom_code"));
                    row.setBuyingPrice(nvl(rs.getBigDecimal("buying_price")));
                    row.setSellingPrice(nvl(rs.getBigDecimal("selling_price")));
                    row.setPriceIncludesTax(rs.getInt("price_includes_tax") == 1);
                    row.setTaxRate(nvl(rs.getBigDecimal("tax_rate")));
                    row.setOnhandQty(BigDecimal.ZERO);
                    row.setQtySoldAllTime(BigDecimal.ZERO);
                    row.setQtySold30Days(BigDecimal.ZERO);
                    row.setDiscountPerUnit(BigDecimal.ZERO);
                    rows.add(row);
                }
            }
            enrichInventory(conn, rows);
        }
        return rows;
    }

    public void updateSellingPrice(long productId, BigDecimal newSellingPrice, Session session) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                ProductSnapshot before = findProductForUpdate(con, productId);
                if (before == null) {
                    throw new SQLException("Product not found.");
                }
                if (newSellingPrice == null || newSellingPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Selling price must be zero or greater.");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE product SET selling_price = ? WHERE id = ?")) {
                    ps.setBigDecimal(1, newSellingPrice);
                    ps.setLong(2, productId);
                    ps.executeUpdate();
                }
                String details = "product_id=" + before.productId
                        + ", sku=" + nullSafe(before.sku)
                        + ", name=" + nullSafe(before.name)
                        + ", old_selling_price=" + before.sellingPrice
                        + ", new_selling_price=" + newSellingPrice
                        + ", updated_by=" + (session == null ? "" : session.username());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO audit_log (actor_user_id, action_code, details) VALUES (?, ?, ?)")) {
                    if (session == null) {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(1, session.userId());
                    }
                    ps.setString(2, "UPDATE_SELLING_PRICE");
                    ps.setString(3, details);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }


    public void recordInventoryCondition(long productId, BigDecimal qty, String targetStatus, Session session, String notes) throws SQLException {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Qty must be greater than zero.");
        }
        String normalizedStatus = targetStatus == null ? "" : targetStatus.trim().toUpperCase();
        String txnType = switch (normalizedStatus) {
            case "DAMAGED" -> "DAMAGE";
            case "EXPIRED" -> "EXPIRE";
            case "RETURNED" -> "ADJUSTMENT";
            default -> throw new SQLException("Unsupported inventory status: " + targetStatus);
        };
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                ProductSnapshot product = findProductForUpdate(con, productId);
                if (product == null) {
                    throw new SQLException("Product not found.");
                }
                BigDecimal onhandQty = findStatusQty(con, productId, "ONHAND");
                if (qty.compareTo(onhandQty) > 0) {
                    throw new SQLException("Qty exceeds available ONHAND stock. Available: " + onhandQty.stripTrailingZeros().toPlainString());
                }

                long txnId;
                String refNo = buildInventoryRefNo(txnType);
                String finalNotes = buildInventoryNotes(normalizedStatus, notes);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inventory_txn (store_id, txn_type, ref_no, notes, created_by) VALUES (?,?,?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, 1);
                    ps.setString(2, txnType);
                    ps.setString(3, refNo);
                    ps.setString(4, finalNotes);
                    if (session == null) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(5, session.userId());
                    }
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Failed to create inventory transaction.");
                        }
                        txnId = rs.getLong(1);
                    }
                }

                BigDecimal unitCost = nvl(product.buyingPrice);
                BigDecimal totalCost = unitCost.multiply(qty).setScale(4, RoundingMode.HALF_UP);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inventory_txn_line (txn_id, product_id, lot_id, from_status, to_status, qty_in_base, unit_cost, total_cost) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setLong(1, txnId);
                    ps.setLong(2, productId);
                    ps.setNull(3, java.sql.Types.BIGINT);
                    ps.setString(4, "ONHAND");
                    ps.setString(5, normalizedStatus);
                    ps.setBigDecimal(6, qty);
                    ps.setBigDecimal(7, unitCost);
                    ps.setBigDecimal(8, totalCost);
                    ps.executeUpdate();
                }

                applyStockDelta(con, 1, productId, "ONHAND", qty.negate());
                applyStockDelta(con, 1, productId, normalizedStatus, qty);

                String details = "product_id=" + product.productId
                        + ", sku=" + nullSafe(product.sku)
                        + ", name=" + nullSafe(product.name)
                        + ", movement=ONHAND->" + normalizedStatus
                        + ", qty=" + qty.stripTrailingZeros().toPlainString()
                        + ", ref_no=" + refNo
                        + ", txn_type=" + txnType
                        + ", notes=" + nullSafe(finalNotes)
                        + ", created_by=" + (session == null ? "" : session.username());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO audit_log (actor_user_id, action_code, details) VALUES (?, ?, ?)")) {
                    if (session == null) {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(1, session.userId());
                    }
                    ps.setString(2, "RECORD_" + normalizedStatus + "_QTY");
                    ps.setString(3, details);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }


    public void recordInventoryShrinkage(long productId, BigDecimal qty, String reasonCode, Session session, String notes) throws SQLException {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Qty must be greater than zero.");
        }
        String normalizedReason = normalizeShrinkageReason(reasonCode);
        String txnType = "ADJUSTMENT";
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                ProductSnapshot product = findProductForUpdate(con, productId);
                if (product == null) {
                    throw new SQLException("Product not found.");
                }
                BigDecimal onhandQty = findStatusQty(con, productId, "ONHAND");
                if (qty.compareTo(onhandQty) > 0) {
                    throw new SQLException("Qty exceeds available ONHAND stock. Available: " + onhandQty.stripTrailingZeros().toPlainString());
                }

                long txnId;
                String refNo = buildInventoryRefNo(txnType + "-" + normalizedReason);
                String finalNotes = buildShrinkageNotes(normalizedReason, notes);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inventory_txn (store_id, txn_type, ref_no, notes, created_by) VALUES (?,?,?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, 1);
                    ps.setString(2, txnType);
                    ps.setString(3, refNo);
                    ps.setString(4, finalNotes);
                    if (session == null) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(5, session.userId());
                    }
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Failed to create inventory shrinkage transaction.");
                        }
                        txnId = rs.getLong(1);
                    }
                }

                BigDecimal unitCost = nvl(product.buyingPrice);
                BigDecimal totalCost = unitCost.multiply(qty).setScale(4, RoundingMode.HALF_UP);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inventory_txn_line (txn_id, product_id, lot_id, from_status, to_status, qty_in_base, unit_cost, total_cost) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setLong(1, txnId);
                    ps.setLong(2, productId);
                    ps.setNull(3, java.sql.Types.BIGINT);
                    ps.setString(4, "ONHAND");
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.setBigDecimal(6, qty);
                    ps.setBigDecimal(7, unitCost);
                    ps.setBigDecimal(8, totalCost);
                    ps.executeUpdate();
                }

                applyStockDelta(con, 1, productId, "ONHAND", qty.negate());

                String details = "product_id=" + product.productId
                        + ", sku=" + nullSafe(product.sku)
                        + ", name=" + nullSafe(product.name)
                        + ", movement=ONHAND->SHRINKAGE"
                        + ", shrinkage_reason=" + normalizedReason
                        + ", qty=" + qty.stripTrailingZeros().toPlainString()
                        + ", ref_no=" + refNo
                        + ", txn_type=" + txnType
                        + ", notes=" + nullSafe(finalNotes)
                        + ", created_by=" + (session == null ? "" : session.username());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO audit_log (actor_user_id, action_code, details) VALUES (?, ?, ?)")) {
                    if (session == null) {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(1, session.userId());
                    }
                    ps.setString(2, "RECORD_SHRINKAGE_" + normalizedReason + "_QTY");
                    ps.setString(3, details);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<String> findRecentSellingPriceLogs(long productId, int limit) throws SQLException {
        String sql = """
            SELECT details || ' @ ' || VARCHAR(created_at) AS entry
            FROM audit_log
            WHERE action_code = 'UPDATE_SELLING_PRICE'
              AND details LIKE ?
            ORDER BY created_at DESC, id DESC
            FETCH FIRST ? ROWS ONLY
        """;
        List<String> rows = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "product_id=" + productId + ",%");
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(rs.getString(1));
                }
            }
        }
        return rows;
    }

    public ProductMonthlySummary findMonthlySummary(long productId, YearMonth month) throws SQLException {
        ProductMonthlySummary summary = new ProductMonthlySummary();
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);

        try (Connection conn = Db.getConnection()) {
            summary.setOnhandQty(findOnhandQty(conn, productId));

            String salesSql = """
                    select count(distinct s.id) sales_count,
                           coalesce(sum(sl.qty_in_base),0) qty_sold,
                           coalesce(sum(sl.qty_in_base * sl.unit_price),0) gross_sales,
                           coalesce(sum(coalesce(vld.discount_total,0)),0) discount_total,
                           coalesce(sum(coalesce(vlt.tax_total,0)),0) tax_total,
                           coalesce(sum((sl.qty_in_base * sl.unit_price)
                               - coalesce(vld.discount_total,0)
                               + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end),0) net_sales_with_tax,
                           coalesce(sum(((sl.qty_in_base * sl.unit_price)
                               - coalesce(vld.discount_total,0)
                               + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end)
                               - coalesce(vlt.tax_total,0)),0) net_sales_without_tax,
                           coalesce(sum(sl.cost_total),0) cost_total,
                           coalesce(sum(((sl.qty_in_base * sl.unit_price)
                               - coalesce(vld.discount_total,0)
                               + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end)
                               - coalesce(vlt.tax_total,0) - sl.cost_total),0) profit_without_tax,
                           coalesce(sum(((sl.qty_in_base * sl.unit_price)
                               - coalesce(vld.discount_total,0)
                               + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end)
                               - sl.cost_total),0) profit_with_tax,
                           coalesce(avg(sl.unit_price),0) avg_unit_price
                    from sale_line sl
                    join sale s on s.id = sl.sale_id and s.status = 'POSTED'
                    left join v_sale_line_discount vld on vld.sale_line_id = sl.id
                    left join v_sale_line_tax_total vlt on vlt.sale_line_id = sl.id
                    where sl.product_id = ?
                      and s.sold_at >= ?
                      and s.sold_at < ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(salesSql)) {
                ps.setLong(1, productId);
                ps.setTimestamp(2, Timestamp.valueOf(start.atStartOfDay()));
                ps.setTimestamp(3, Timestamp.valueOf(end.atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setSalesCount(rs.getInt("sales_count"));
                        summary.setQtySold(nvl(rs.getBigDecimal("qty_sold")));
                        summary.setGrossSales(nvl(rs.getBigDecimal("gross_sales")));
                        summary.setDiscountTotal(nvl(rs.getBigDecimal("discount_total")));
                        summary.setTaxTotal(nvl(rs.getBigDecimal("tax_total")));
                        summary.setNetSalesWithTax(nvl(rs.getBigDecimal("net_sales_with_tax")));
                        summary.setNetSalesWithoutTax(nvl(rs.getBigDecimal("net_sales_without_tax")));
                        summary.setCostTotal(nvl(rs.getBigDecimal("cost_total")));
                        summary.setProfitWithoutTax(nvl(rs.getBigDecimal("profit_without_tax")));
                        summary.setProfitWithTax(nvl(rs.getBigDecimal("profit_with_tax")));
                        summary.setAverageUnitPrice(nvl(rs.getBigDecimal("avg_unit_price")));
                    }
                }
            }

            String returnSql = """
                    select coalesce(sum(srl.qty_in_base),0) qty_returned
                    from sales_return_line srl
                    join sales_return sr on sr.id = srl.sales_return_id and sr.status = 'POSTED'
                    where srl.product_id = ?
                      and sr.returned_at >= ?
                      and sr.returned_at < ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(returnSql)) {
                ps.setLong(1, productId);
                ps.setTimestamp(2, Timestamp.valueOf(start.atStartOfDay()));
                ps.setTimestamp(3, Timestamp.valueOf(end.atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setQtyReturned(nvl(rs.getBigDecimal("qty_returned")));
                    }
                }
            }
        }
        return summary;
    }

    public List<ProductMonthlySalesRow> findMonthlySaleRows(long productId, YearMonth month) throws SQLException {
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);
        String sql = """
                select s.sale_no, coalesce(si.invoice_no, '') invoice_no, coalesce(c.full_name, '') customer_name,
                       s.sold_at, sl.qty_in_base, sl.unit_price,
                       (sl.qty_in_base * sl.unit_price) gross_amount,
                       coalesce(vld.discount_total,0) discount_amount,
                       coalesce(vlt.tax_total,0) tax_amount,
                       ((sl.qty_in_base * sl.unit_price)
                           - coalesce(vld.discount_total,0)
                           + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end) net_amount,
                       sl.cost_total,
                       (((sl.qty_in_base * sl.unit_price)
                           - coalesce(vld.discount_total,0)
                           + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end)
                           - coalesce(vlt.tax_total,0) - sl.cost_total) profit_without_tax,
                       (((sl.qty_in_base * sl.unit_price)
                           - coalesce(vld.discount_total,0)
                           + case when sl.price_includes_tax = 1 then 0 else coalesce(vlt.tax_total,0) end)
                           - sl.cost_total) profit_with_tax
                from sale_line sl
                join sale s on s.id = sl.sale_id and s.status = 'POSTED'
                left join customer c on c.id = s.customer_id
                left join sale_invoice si on si.sale_id = s.id
                left join v_sale_line_discount vld on vld.sale_line_id = sl.id
                left join v_sale_line_tax_total vlt on vlt.sale_line_id = sl.id
                where sl.product_id = ?
                  and s.sold_at >= ?
                  and s.sold_at < ?
                order by s.sold_at desc, s.id desc, sl.id desc
                """;
        List<ProductMonthlySalesRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setTimestamp(2, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(end.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductMonthlySalesRow row = new ProductMonthlySalesRow();
                    row.setSaleNo(rs.getString("sale_no"));
                    row.setInvoiceNo(rs.getString("invoice_no"));
                    row.setCustomerName(rs.getString("customer_name"));
                    row.setSoldAt(rs.getTimestamp("sold_at"));
                    row.setQtySold(nvl(rs.getBigDecimal("qty_in_base")));
                    row.setUnitPrice(nvl(rs.getBigDecimal("unit_price")));
                    row.setGrossAmount(nvl(rs.getBigDecimal("gross_amount")));
                    row.setDiscountAmount(nvl(rs.getBigDecimal("discount_amount")));
                    row.setTaxAmount(nvl(rs.getBigDecimal("tax_amount")));
                    row.setNetAmountWithTax(nvl(rs.getBigDecimal("net_amount")));
                    row.setCostAmount(nvl(rs.getBigDecimal("cost_total")));
                    row.setProfitWithoutTax(nvl(rs.getBigDecimal("profit_without_tax")));
                    row.setProfitWithTax(nvl(rs.getBigDecimal("profit_with_tax")));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private ProductSnapshot findProductForUpdate(Connection con, long productId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id, sku, name, buying_price, selling_price FROM product WHERE id = ? FOR UPDATE")) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProductSnapshot s = new ProductSnapshot();
                s.productId = rs.getLong("id");
                s.sku = rs.getString("sku");
                s.name = rs.getString("name");
                s.buyingPrice = rs.getBigDecimal("buying_price");
                s.sellingPrice = rs.getBigDecimal("selling_price");
                return s;
            }
        }
    }

    private void enrichInventory(Connection conn, List<ProductInventoryItem> rows) throws SQLException {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(ProductInventoryItem::getProductId).toList();
        Map<Long, String> barcodeMap = loadBarcodeMap(conn, ids);
        Map<Long, BigDecimal> onhandMap = loadOnhandMap(conn, ids);
        Map<Long, ExpiryInfo> expiryMap = loadExpiryMap(conn, ids);
        Map<Long, SalesStats> salesMap = loadSalesStats(conn, ids);

        for (ProductInventoryItem row : rows) {
            row.setBarcodes(barcodeMap.getOrDefault(row.getProductId(), ""));
            row.setOnhandQty(onhandMap.getOrDefault(row.getProductId(), BigDecimal.ZERO));
            ExpiryInfo expiryInfo = expiryMap.getOrDefault(row.getProductId(), new ExpiryInfo("", "", null, "", ""));
            row.setExpirySummary(expiryInfo.summary());
            row.setExpiryDisplayHtml(expiryInfo.html());
            row.setDaysToExpire(expiryInfo.daysToExpire());
            row.setDaysToExpireDisplay(expiryInfo.daysDisplay());
            row.setDaysToExpireDisplayHtml(expiryInfo.daysDisplayHtml());
            SalesStats stats = salesMap.getOrDefault(row.getProductId(), new SalesStats(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            row.setQtySoldAllTime(stats.qtySold());
            row.setQtySold30Days(stats.qtySold30Days());
            BigDecimal discountPerUnit = BigDecimal.ZERO;
            if (stats.qtySold().compareTo(BigDecimal.ZERO) > 0) {
                discountPerUnit = stats.discountTotal().divide(stats.qtySold(), 4, RoundingMode.HALF_UP);
            }
            row.setDiscountPerUnit(discountPerUnit);

            BigDecimal taxRate = nvl(row.getTaxRate());
            BigDecimal sellingNoTax = row.isPriceIncludesTax() && taxRate.compareTo(BigDecimal.ZERO) > 0
                    ? nvl(row.getSellingPrice()).divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP)
                    : nvl(row.getSellingPrice());
            BigDecimal profitNoTaxNoDiscount = sellingNoTax.subtract(nvl(row.getBuyingPrice()));
            BigDecimal profitWithTaxDiscount = nvl(row.getSellingPrice()).subtract(discountPerUnit).subtract(nvl(row.getBuyingPrice()));
            row.setProfitWithoutTaxWithoutDiscount(profitNoTaxNoDiscount);
            row.setProfitWithTaxAndDiscount(profitWithTaxDiscount);
            row.setMarkupWithoutTaxWithoutDiscount(percentOf(profitNoTaxNoDiscount, row.getBuyingPrice()));
            row.setMarkupWithTaxAndDiscount(percentOf(profitWithTaxDiscount, row.getBuyingPrice()));
        }
    }

    private Map<Long, String> loadBarcodeMap(Connection conn, List<Long> ids) throws SQLException {
        String sql = "select product_id, barcode, is_primary from product_barcode where active = 1 and product_id in (" + placeholders(ids.size()) + ") order by product_id, is_primary desc, barcode";
        Map<Long, List<String>> temp = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    temp.computeIfAbsent(rs.getLong("product_id"), k -> new ArrayList<>()).add(rs.getString("barcode"));
                }
            }
        }
        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, List<String>> e : temp.entrySet()) {
            result.put(e.getKey(), String.join(", ", e.getValue()));
        }
        return result;
    }

    private Map<Long, BigDecimal> loadOnhandMap(Connection conn, List<Long> ids) throws SQLException {
        String sql = "select product_id, coalesce(sum(qty_in_base),0) qty from stock_balance where status_code = 'ONHAND' and product_id in (" + placeholders(ids.size()) + ") group by product_id";
        Map<Long, BigDecimal> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong("product_id"), nvl(rs.getBigDecimal("qty")));
                }
            }
        }
        return result;
    }

    private BigDecimal findOnhandQty(Connection conn, long productId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("select coalesce(sum(qty_in_base),0) qty from stock_balance where product_id = ? and status_code = 'ONHAND'")) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return nvl(rs.getBigDecimal("qty"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private Map<Long, ExpiryInfo> loadExpiryMap(Connection conn, List<Long> ids) throws SQLException {
        String lotSql = """
                select l.product_id, l.id lot_id, l.expiry_date,
                       coalesce(sum(case when itl.to_status = 'ONHAND' then itl.qty_in_base else 0 end),0) as qty_in,
                       coalesce(sum(case when itl.from_status = 'ONHAND' then itl.qty_in_base else 0 end),0) as qty_out_tagged
                from inventory_lot l
                left join inventory_txn_line itl on itl.lot_id = l.id
                where l.product_id in (%s)
                group by l.product_id, l.id, l.expiry_date
                order by l.product_id,
                         case when l.expiry_date is null then 1 else 0 end,
                         l.expiry_date,
                         l.id
                """.formatted(placeholders(ids.size()));
        String untaggedOutSql = """
                select product_id, coalesce(sum(qty_in_base),0) qty_out_untagged
                from inventory_txn_line
                where from_status = 'ONHAND'
                  and lot_id is null
                  and product_id in (%s)
                group by product_id
                """.formatted(placeholders(ids.size()));

        Map<Long, List<LotBalance>> lotsByProduct = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(lotSql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal qtyIn = nvl(rs.getBigDecimal("qty_in"));
                    BigDecimal qtyOutTagged = nvl(rs.getBigDecimal("qty_out_tagged"));
                    if (qtyIn.compareTo(BigDecimal.ZERO) <= 0 && qtyOutTagged.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    lotsByProduct.computeIfAbsent(rs.getLong("product_id"), k -> new ArrayList<>())
                            .add(new LotBalance(
                                    rs.getLong("lot_id"),
                                    rs.getDate("expiry_date") == null ? null : rs.getDate("expiry_date").toLocalDate(),
                                    qtyIn.subtract(qtyOutTagged)));
                }
            }
        }

        Map<Long, BigDecimal> untaggedOutMap = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(untaggedOutSql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    untaggedOutMap.put(rs.getLong("product_id"), nvl(rs.getBigDecimal("qty_out_untagged")));
                }
            }
        }

        Map<Long, ExpiryInfo> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (Map.Entry<Long, List<LotBalance>> e : lotsByProduct.entrySet()) {
            List<LotBalance> balances = e.getValue();
            BigDecimal untaggedOut = untaggedOutMap.getOrDefault(e.getKey(), BigDecimal.ZERO);

            List<ExpiryLotRow> liveLots = new ArrayList<>();
            for (LotBalance balance : balances) {
                BigDecimal remaining = balance.remainingQty();
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                if (untaggedOut.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal deduct = remaining.min(untaggedOut);
                    remaining = remaining.subtract(deduct);
                    untaggedOut = untaggedOut.subtract(deduct);
                }
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    liveLots.add(new ExpiryLotRow(balance.expiryDate(), remaining));
                }
            }

            if (liveLots.isEmpty()) {
                continue;
            }

            String summary = liveLots.stream()
                    .map(this::toExpirySummary)
                    .reduce((a, b) -> a + " | " + b)
                    .orElse("");
            String html = liveLots.stream()
                    .map(lot -> toExpiryHtml(lot, today))
                    .reduce((a, b) -> a + " <span style='color:#6c757d;'>|</span> " + b)
                    .orElse("");
            String daysDisplay = liveLots.stream()
                    .map(lot -> toDaysToExpireSummary(lot, today))
                    .reduce((a, b) -> a + " | " + b)
                    .orElse("");
            String daysHtml = liveLots.stream()
                    .map(lot -> toDaysToExpireHtml(lot, today))
                    .reduce((a, b) -> a + " <span style='color:#6c757d;'>|</span> " + b)
                    .orElse("");
            Integer daysToExpire = liveLots.stream()
                    .filter(lot -> lot.expiryDate() != null)
                    .map(lot -> (int) java.time.temporal.ChronoUnit.DAYS.between(today, lot.expiryDate()))
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            result.put(e.getKey(), new ExpiryInfo(
                    summary,
                    html.isBlank() ? "" : "<html>" + html + "</html>",
                    daysToExpire,
                    daysDisplay,
                    daysHtml.isBlank() ? "" : "<html>" + daysHtml + "</html>"));
        }
        return result;
    }

    private Map<Long, SalesStats> loadSalesStats(Connection conn, List<Long> ids) throws SQLException {
        String sql = """
                select sl.product_id,
                       coalesce(sum(sl.qty_in_base),0) qty_sold,
                       coalesce(sum(case when s.sold_at >= ? then sl.qty_in_base else 0 end),0) qty_sold_30_days,
                       coalesce(sum(coalesce(vld.discount_total,0)),0) discount_total
                from sale_line sl
                join sale s on s.id = sl.sale_id and s.status = 'POSTED'
                left join v_sale_line_discount vld on vld.sale_line_id = sl.id
                where sl.product_id in (%s)
                group by sl.product_id
                """.formatted(placeholders(ids.size()));
        Map<Long, SalesStats> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay()));
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 2, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong("product_id"), new SalesStats(
                            nvl(rs.getBigDecimal("qty_sold")),
                            nvl(rs.getBigDecimal("qty_sold_30_days")),
                            nvl(rs.getBigDecimal("discount_total"))));
                }
            }
        }
        return result;
    }

    private String placeholders(int count) {
        StringJoiner joiner = new StringJoiner(",");
        for (int i = 0; i < count; i++) {
            joiner.add("?");
        }
        return joiner.toString();
    }

    private void bindIds(PreparedStatement ps, List<Long> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            ps.setLong(i + 1, ids.get(i));
        }
    }


    private BigDecimal findStatusQty(Connection conn, long productId, String statusCode) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select coalesce(sum(qty_in_base),0) qty from stock_balance where store_id = 1 and product_id = ? and status_code = ?")) {
            ps.setLong(1, productId);
            ps.setString(2, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return nvl(rs.getBigDecimal("qty"));
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private void applyStockDelta(Connection conn, int storeId, long productId, String statusCode, BigDecimal delta) throws SQLException {
        BigDecimal current = BigDecimal.ZERO;
        boolean exists = false;
        try (PreparedStatement ps = conn.prepareStatement(
                "select qty_in_base from stock_balance where store_id = ? and product_id = ? and status_code = ?")) {
            ps.setInt(1, storeId);
            ps.setLong(2, productId);
            ps.setString(3, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    current = nvl(rs.getBigDecimal(1));
                    exists = true;
                }
            }
        }
        BigDecimal updated = current.add(nvl(delta));
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Stock balance cannot go below zero for status " + statusCode + ".");
        }
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "update stock_balance set qty_in_base = ? where store_id = ? and product_id = ? and status_code = ?")) {
                ps.setBigDecimal(1, updated);
                ps.setInt(2, storeId);
                ps.setLong(3, productId);
                ps.setString(4, statusCode);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "insert into stock_balance (store_id, product_id, status_code, qty_in_base) values (?,?,?,?)")) {
                ps.setInt(1, storeId);
                ps.setLong(2, productId);
                ps.setString(3, statusCode);
                ps.setBigDecimal(4, updated);
                ps.executeUpdate();
            }
        }
    }

    private String buildInventoryRefNo(String txnType) {
        return txnType + "-" + System.currentTimeMillis();
    }

    private String buildInventoryNotes(String statusCode, String notes) {
        String base = switch (statusCode) {
            case "DAMAGED" -> "Recorded damaged qty";
            case "EXPIRED" -> "Recorded expired qty";
            case "RETURNED" -> "Recorded returned qty";
            default -> "Recorded inventory qty";
        };
        String trimmed = notes == null ? "" : notes.trim();
        return trimmed.isEmpty() ? base : base + ": " + trimmed;
    }


    private String normalizeShrinkageReason(String reasonCode) throws SQLException {
        String normalized = reasonCode == null ? "" : reasonCode.trim().toUpperCase();
        return switch (normalized) {
            case "THEFT", "ERROR", "MISCOUNT" -> normalized;
            default -> throw new SQLException("Unsupported shrinkage reason: " + reasonCode);
        };
    }

    private String buildShrinkageNotes(String reasonCode, String notes) {
        String base = "Recorded shrinkage [" + normalizeShrinkageReasonQuiet(reasonCode) + "]";
        String trimmed = notes == null ? "" : notes.trim();
        return trimmed.isEmpty() ? base : base + ": " + trimmed;
    }

    private String normalizeShrinkageReasonQuiet(String reasonCode) {
        String normalized = reasonCode == null ? "" : reasonCode.trim().toUpperCase();
        if ("THEFT".equals(normalized) || "ERROR".equals(normalized) || "MISCOUNT".equals(normalized)) {
            return normalized;
        }
        return "UNKNOWN";
    }

    private BigDecimal percentOf(BigDecimal profit, BigDecimal buyingPrice) {
        if (profit == null || buyingPrice == null || buyingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(new BigDecimal("100")).divide(buyingPrice, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullSafe(String v) { return v == null ? "" : v; }

    private String toExpirySummary(ExpiryLotRow lot) {
        return (lot.expiryDate() == null ? "No expiry" : lot.expiryDate().toString()) + " [" + lot.remainingQty().stripTrailingZeros().toPlainString() + "]";
    }

    private String toExpiryHtml(ExpiryLotRow lot, LocalDate today) {
        String text = escapeHtml(toExpirySummary(lot));
        if (lot.expiryDate() == null) {
            return "<span style='color:#495057;'>" + text + "</span>";
        }
        return "<span style='color:" + expiryColor(lot.expiryDate(), today) + ";'>" + text + "</span>";
    }

    private String toDaysToExpireSummary(ExpiryLotRow lot, LocalDate today) {
        if (lot.expiryDate() == null) {
            return "No expiry";
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, lot.expiryDate());
        return days < 0 ? "Expired " + Math.abs(days) + "d" : days + "d";
    }

    private String toDaysToExpireHtml(ExpiryLotRow lot, LocalDate today) {
        String text = escapeHtml(toDaysToExpireSummary(lot, today));
        if (lot.expiryDate() == null) {
            return "<span style='color:#495057;'>" + text + "</span>";
        }
        return "<span style='color:" + expiryColor(lot.expiryDate(), today) + ";'>" + text + "</span>";
    }

    private String expiryColor(LocalDate expiryDate, LocalDate today) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
        return days < 0 ? "#dc3545" : (days <= 30 ? "#fd7e14" : "#198754");
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record ExpiryLotRow(LocalDate expiryDate, BigDecimal remainingQty) {}
    private record LotBalance(long lotId, LocalDate expiryDate, BigDecimal remainingQty) {}
    private record ExpiryInfo(String summary, String html, Integer daysToExpire, String daysDisplay, String daysDisplayHtml) {}
    private record SalesStats(BigDecimal qtySold, BigDecimal qtySold30Days, BigDecimal discountTotal) {}

    private static final class ProductSnapshot {
        long productId;
        String sku;
        String name;
        BigDecimal buyingPrice;
        BigDecimal sellingPrice;
    }
}
