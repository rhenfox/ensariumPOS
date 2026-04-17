package com.aldrin.ensarium.order;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ProductOrderDao {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DecimalFormat SEQ = new DecimalFormat("000");

    public List<SupplierOption> findSuppliers() throws SQLException {
        List<SupplierOption> rows = new ArrayList<>();
        rows.add(new SupplierOption(null, ""));
        String sql = "SELECT id, name FROM supplier WHERE active = 1 ORDER BY name";
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new SupplierOption(rs.getLong(1), rs.getString(2)));
            }
        }
        return rows;
    }

    public SavedOrder saveFinalOrder(Long existingOrderId, String existingOrderNo, List<OrderDraftLine> lines, SupplierOption supplier, String notes, Session session) throws SQLException {
        if (lines == null || lines.isEmpty()) {
            throw new SQLException("No products in the partial order.");
        }
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                final boolean updateMode = existingOrderId != null && existingOrderId.longValue() > 0L;
                final long orderId;
                final String orderNo;

                if (updateMode) {
                    orderId = updateOrderHeader(con, existingOrderId.longValue(), supplier, notes);
                    orderNo = resolveOrderNo(con, existingOrderId.longValue(), existingOrderNo);
                    deleteOrderLines(con, orderId);
                    insertOrderLines(con, orderId, lines);
                    insertAuditLog(con, session, "UPDATE_PRODUCT_ORDER",
                            "order_no=" + orderNo
                            + ", supplier=" + supplierName(supplier)
                            + ", lines=" + lines.size()
                            + ", notes=" + safe(notes));
                } else {
                    orderNo = generateOrderNo(con);
                    orderId = insertOrderHeader(con, orderNo, supplier, notes, session);
                    insertOrderLines(con, orderId, lines);
                    insertAuditLog(con, session, "SAVE_PRODUCT_ORDER",
                            "order_no=" + orderNo
                            + ", supplier=" + supplierName(supplier)
                            + ", lines=" + lines.size()
                            + ", notes=" + safe(notes));
                }

                con.commit();
                return new SavedOrder(orderId, orderNo);
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<SavedOrderSummary> findRecentOrders(int daysBack) throws SQLException {
        int effectiveDays = Math.max(1, daysBack);
        Timestamp cutoff = Timestamp.valueOf(LocalDateTime.now().minusDays(effectiveDays));
        String sql = ""
                + "SELECT h.id, h.order_no, COALESCE(s.name, ''), h.ordered_at, h.status, COUNT(l.id), COALESCE(h.notes, '') "
                + "FROM product_order_header h "
                + "LEFT JOIN supplier s ON s.id = h.supplier_id "
                + "LEFT JOIN product_order_line l ON l.order_id = h.id "
                + "WHERE h.ordered_at >= ? "
                + "GROUP BY h.id, h.order_no, s.name, h.ordered_at, h.status, h.notes "
                + "ORDER BY h.ordered_at DESC, h.id DESC";
        List<SavedOrderSummary> rows = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, cutoff);
            ps.setMaxRows(200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new SavedOrderSummary(
                            rs.getLong(1),
                            safe(rs.getString(2)),
                            safe(rs.getString(3)),
                            rs.getTimestamp(4),
                            safe(rs.getString(5)),
                            rs.getInt(6),
                            safe(rs.getString(7))
                    ));
                }
            }
        }
        return rows;
    }

    public LoadedOrder loadOrderForEdit(long orderId) throws SQLException {
        try (Connection con = Db.getConnection()) {
            return loadOrder(con, "WHERE h.id = ?", ps -> ps.setLong(1, orderId), "Saved order not found.");
        }
    }

    public LoadedOrder loadOrderForReportByOrderNo(String orderNo) throws SQLException {
        if (orderNo == null || orderNo.isBlank()) {
            throw new SQLException("Order No is required.");
        }
        try (Connection con = Db.getConnection()) {
            return loadOrder(con, "WHERE h.order_no = ?", ps -> ps.setString(1, orderNo.trim()), "Selected order was not found.");
        }
    }

//    private LoadedOrder loadOrder(Connection con, String whereClause, SqlBinder binder, String notFoundMessage) throws SQLException {
//        String headerSql = "SELECT h.id, h.order_no, h.supplier_id, COALESCE(s.name,''), COALESCE(h.notes,'') "
//                + "FROM product_order_header h LEFT JOIN supplier s ON s.id = h.supplier_id " + whereClause;
//        SupplierOption supplier;
//        String orderNo;
//        String notes;
//        long orderId;
//        try (PreparedStatement ps = con.prepareStatement(headerSql)) {
//            binder.bind(ps);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (!rs.next()) {
//                    throw new SQLException(notFoundMessage);
//                }
//                orderId = rs.getLong(1);
//                orderNo = safe(rs.getString(2));
//                Object supplierIdObj = rs.getObject(3);
//                supplier = supplierIdObj == null ? new SupplierOption(null, "") : new SupplierOption(((Number) supplierIdObj).longValue(), safe(rs.getString(4)));
//                notes = safe(rs.getString(5));
//            }
//        }
//
//        Timestamp salesCutoff = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
//        String lineSql = ""
//                + "SELECT l.product_id, "
//                + "       COALESCE(pc.name, ''), "
//                + "       COALESCE(p.sku, ''), "
//                + "       COALESCE(p.name, ''), "
//                + "       COALESCE(l.barcode, pb.barcode, ''), "
//                + "       COALESCE(u.code, ''), "
//                + "       COALESCE(sb.qty_in_base, l.onhand_qty_snapshot, 0), "
//                + "       COALESCE(s30.qty_sold_30d, 0), "
//                + "       COALESCE(l.qty_to_order, 1), "
//                + "       COALESCE(p.buying_price, l.buying_price_snapshot, 0), "
//                + "       COALESCE(p.selling_price, l.selling_price_snapshot, 0), "
//                + "       COALESCE(l.expiry_snapshot, ''), "
//                + "       COALESCE(l.notes, '') "
//                + "FROM product_order_line l "
//                + "LEFT JOIN product p ON p.id = l.product_id "
//                + "LEFT JOIN product_category pc ON pc.id = p.category_id "
//                + "LEFT JOIN uom u ON u.id = p.base_uom_id "
//                + "LEFT JOIN product_barcode pb ON pb.product_id = l.product_id AND pb.is_primary = 1 AND pb.active = 1 "
//                + "LEFT JOIN (SELECT product_id, SUM(qty_in_base) AS qty_in_base FROM stock_balance WHERE status_code = 'ONHAND' GROUP BY product_id) sb ON sb.product_id = l.product_id "
//                + "LEFT JOIN (SELECT sl.product_id, SUM(sl.qty_in_base) AS qty_sold_30d FROM sale s JOIN sale_line sl ON sl.sale_id = s.id WHERE s.status = 'POSTED' AND s.sold_at >= ? GROUP BY sl.product_id) s30 ON s30.product_id = l.product_id "
//                + "WHERE l.order_id = ? "
//                + "ORDER BY l.line_no";
//        List<OrderDraftLine> lines = new ArrayList<>();
//        try (PreparedStatement ps = con.prepareStatement(lineSql)) {
//            ps.setTimestamp(1, salesCutoff);
//            ps.setLong(2, orderId);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    OrderDraftLine line = new OrderDraftLine();
//                    line.setProductId(rs.getLong(1));
//                    line.setCategoryName(safe(rs.getString(2)));
//                    line.setSku(safe(rs.getString(3)));
//                    line.setProductName(safe(rs.getString(4)));
//                    line.setBarcode(safe(rs.getString(5)));
//                    line.setUomCode(safe(rs.getString(6)));
//                    line.setOnhandQty(defaultZero(rs.getBigDecimal(7)));
//                    line.setQtySold30Days(defaultZero(rs.getBigDecimal(8)));
//                    line.setQtyToOrder(defaultQty(rs.getBigDecimal(9)));
//                    line.setBuyingPrice(defaultZero(rs.getBigDecimal(10)));
//                    line.setSellingPrice(defaultZero(rs.getBigDecimal(11)));
//                    line.setExpirySummary(safe(rs.getString(12)));
//                    line.setNotes(safe(rs.getString(13)));
//                    lines.add(line);
//                }
//            }
//        }
//        return new LoadedOrder(orderId, orderNo, supplier, notes, lines);
//    }
    private LoadedOrder loadOrder(Connection con, String whereClause, SqlBinder binder, String notFoundMessage) throws SQLException {
        String headerSql = "SELECT h.id, h.order_no, h.supplier_id, COALESCE(s.name,''), COALESCE(h.notes,'') "
                + "FROM product_order_header h LEFT JOIN supplier s ON s.id = h.supplier_id " + whereClause;

        SupplierOption supplier;
        String orderNo;
        String notes;
        long orderId;

        try (PreparedStatement ps = con.prepareStatement(headerSql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(notFoundMessage);
                }
                orderId = rs.getLong(1);
                orderNo = safe(rs.getString(2));
                Object supplierIdObj = rs.getObject(3);
                supplier = supplierIdObj == null
                        ? new SupplierOption(null, "")
                        : new SupplierOption(((Number) supplierIdObj).longValue(), safe(rs.getString(4)));
                notes = safe(rs.getString(5));
            }
        }

        Timestamp salesCutoff = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        String lineSql = ""
                + "SELECT l.product_id, "
                + "       COALESCE(pc.name, ''), "
                + "       COALESCE(p.sku, ''), "
                + "       COALESCE(p.name, ''), "
                + "       COALESCE(l.barcode, pb.barcode, ''), "
                + "       COALESCE(u.code, ''), "
                + "       COALESCE(sb.qty_in_base, l.onhand_qty_snapshot, 0), "
                + "       COALESCE(s30.qty_sold_30d, 0), "
                + "       COALESCE(l.qty_to_order, 1), "
                + "       COALESCE(p.buying_price, l.buying_price_snapshot, 0), "
                + "       COALESCE(p.selling_price, l.selling_price_snapshot, 0), "
                + "       COALESCE(l.expiry_snapshot, ''), "
                + "       COALESCE(l.notes, '') "
                + "FROM product_order_line l "
                + "LEFT JOIN product p ON p.id = l.product_id "
                + "LEFT JOIN product_category pc ON pc.id = p.category_id "
                + "LEFT JOIN uom u ON u.id = p.base_uom_id "
                + "LEFT JOIN product_barcode pb ON pb.product_id = l.product_id AND pb.is_primary = 1 AND pb.active = 1 "
                + "LEFT JOIN (SELECT product_id, SUM(qty_in_base) AS qty_in_base FROM stock_balance WHERE status_code = 'ONHAND' GROUP BY product_id) sb ON sb.product_id = l.product_id "
                + "LEFT JOIN (SELECT sl.product_id, SUM(sl.qty_in_base) AS qty_sold_30d FROM sale s JOIN sale_line sl ON sl.sale_id = s.id WHERE s.status = 'POSTED' AND s.sold_at >= ? GROUP BY sl.product_id) s30 ON s30.product_id = l.product_id "
                + "WHERE l.order_id = ? "
                + "ORDER BY l.line_no";

        List<OrderDraftLine> lines = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(lineSql)) {
            ps.setTimestamp(1, salesCutoff);
            ps.setLong(2, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDraftLine line = new OrderDraftLine();
                    line.setProductId(rs.getLong(1));
                    line.setCategoryName(safe(rs.getString(2)));
                    line.setSku(safe(rs.getString(3)));
                    line.setProductName(safe(rs.getString(4)));
                    line.setBarcode(safe(rs.getString(5)));
                    line.setUomCode(safe(rs.getString(6)));
                    line.setOnhandQty(defaultZero(rs.getBigDecimal(7)));
                    line.setQtySold30Days(defaultZero(rs.getBigDecimal(8)));
                    line.setQtyToOrder(defaultQty(rs.getBigDecimal(9)));
                    line.setBuyingPrice(defaultZero(rs.getBigDecimal(10)));
                    line.setSellingPrice(defaultZero(rs.getBigDecimal(11)));
                    line.setExpirySummary(safe(rs.getString(12))); // fallback only
                    line.setNotes(safe(rs.getString(13)));
                    lines.add(line);
                    productIds.add(line.getProductId());
                }
            }
        }

        Map<Long, String> liveExpiryMap = loadLiveExpirySummaryMap(con, productIds);
        for (OrderDraftLine line : lines) {
            String liveExpiry = liveExpiryMap.get(line.getProductId());
            if (liveExpiry != null && !liveExpiry.isBlank()) {
                line.setExpirySummary(liveExpiry);
            }
        }

        return new LoadedOrder(orderId, orderNo, supplier, notes, lines);
    }

    private Map<Long, String> loadLiveExpirySummaryMap(Connection conn, List<Long> ids) throws SQLException {
        Map<Long, String> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }

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
                    BigDecimal qtyIn = defaultZero(rs.getBigDecimal("qty_in"));
                    BigDecimal qtyOutTagged = defaultZero(rs.getBigDecimal("qty_out_tagged"));
                    if (qtyIn.compareTo(BigDecimal.ZERO) <= 0 && qtyOutTagged.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    lotsByProduct.computeIfAbsent(rs.getLong("product_id"), k -> new ArrayList<>())
                            .add(new LotBalance(
                                    rs.getLong("lot_id"),
                                    rs.getDate("expiry_date") == null ? null : rs.getDate("expiry_date").toLocalDate(),
                                    qtyIn.subtract(qtyOutTagged)
                            ));
                }
            }
        }

        Map<Long, BigDecimal> untaggedOutMap = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(untaggedOutSql)) {
            bindIds(ps, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    untaggedOutMap.put(rs.getLong("product_id"), defaultZero(rs.getBigDecimal("qty_out_untagged")));
                }
            }
        }

        for (Map.Entry<Long, List<LotBalance>> e : lotsByProduct.entrySet()) {
            BigDecimal untaggedOut = untaggedOutMap.getOrDefault(e.getKey(), BigDecimal.ZERO);
            List<String> liveLots = new ArrayList<>();

            for (LotBalance balance : e.getValue()) {
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
                    String label = (balance.expiryDate() == null ? "No expiry" : balance.expiryDate().toString())
                            + " [" + remaining.stripTrailingZeros().toPlainString() + "]";
                    liveLots.add(label);
                }
            }

            result.put(e.getKey(), String.join(" | ", liveLots));
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

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record LotBalance(long lotId, java.time.LocalDate expiryDate, BigDecimal remainingQty) {

    }

    private long insertOrderHeader(Connection con, String orderNo, SupplierOption supplier, String notes, Session session) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO product_order_header (order_no, supplier_id, requested_by, ordered_at, status, notes) VALUES (?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, orderNo);
            setSupplier(ps, 2, supplier);
            setActor(ps, 3, session);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, "SUBMITTED");
            ps.setString(6, notes);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to create order header.");
                }
                return rs.getLong(1);
            }
        }
    }

    private long updateOrderHeader(Connection con, long orderId, SupplierOption supplier, String notes) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE product_order_header SET supplier_id = ?, status = ?, notes = ? WHERE id = ?")) {
            setSupplier(ps, 1, supplier);
            ps.setString(2, "SUBMITTED");
            ps.setString(3, notes);
            ps.setLong(4, orderId);
            int updated = ps.executeUpdate();
            if (updated <= 0) {
                throw new SQLException("Order not found for update.");
            }
            return orderId;
        }
    }

    private String resolveOrderNo(Connection con, long orderId, String fallback) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT order_no FROM product_order_header WHERE id = ?")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String orderNo = rs.getString(1);
                    if (orderNo != null && !orderNo.isBlank()) {
                        return orderNo;
                    }
                }
            }
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        throw new SQLException("Unable to resolve order number for update.");
    }

    private void deleteOrderLines(Connection con, long orderId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM product_order_line WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        }
    }

    private void insertOrderLines(Connection con, long orderId, List<OrderDraftLine> lines) throws SQLException {
        int lineNo = 1;
        for (OrderDraftLine line : lines) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO product_order_line (order_id, line_no, product_id, barcode, expiry_snapshot, onhand_qty_snapshot, buying_price_snapshot, selling_price_snapshot, qty_to_order, notes) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                ps.setLong(1, orderId);
                ps.setInt(2, lineNo++);
                ps.setLong(3, line.getProductId());
                ps.setString(4, line.getBarcode());
                ps.setString(5, line.getExpirySummary());
                ps.setBigDecimal(6, defaultZero(line.getOnhandQty()));
                ps.setBigDecimal(7, defaultZero(line.getBuyingPrice()));
                ps.setBigDecimal(8, defaultZero(line.getSellingPrice()));
                ps.setBigDecimal(9, defaultQty(line.getQtyToOrder()));
                ps.setString(10, line.getNotes());
                ps.executeUpdate();
            }
        }
    }

    private void insertAuditLog(Connection con, Session session, String actionCode, String details) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO audit_log (actor_user_id, action_code, details) VALUES (?, ?, ?)")) {
            setActor(ps, 1, session);
            ps.setString(2, actionCode);
            ps.setString(3, details);
            ps.executeUpdate();
        }
    }

    private void setSupplier(PreparedStatement ps, int index, SupplierOption supplier) throws SQLException {
        if (supplier == null || supplier.getId() == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, supplier.getId());
        }
    }

    private void setActor(PreparedStatement ps, int index, Session session) throws SQLException {
        if (session == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, session.userId());
        }
    }

    private String generateOrderNo(Connection con) throws SQLException {
        String prefix = "PO-" + ORDER_NO_TIME.format(LocalDateTime.now()) + "-";
        int count = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM product_order_header WHERE order_no LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return prefix + SEQ.format(count + 1);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String supplierName(SupplierOption supplier) {
        return supplier == null ? "" : safe(supplier.getName());
    }

//    private static BigDecimal defaultZero(BigDecimal value) {
//        return value == null ? BigDecimal.ZERO : value;
//    }

    private static BigDecimal defaultQty(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return value;
    }

    @FunctionalInterface
    private interface SqlBinder {

        void bind(PreparedStatement ps) throws SQLException;
    }

    public record SavedOrder(long orderId, String orderNo) {

    }

    public record SavedOrderSummary(long orderId, String orderNo, String supplierName, Timestamp orderedAt, String status, int lineCount, String notes) {

        @Override
        public String toString() {
            String when = orderedAt == null ? "" : orderedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return orderNo + " | " + when + " | " + safe(supplierName) + " | " + lineCount + " line(s)";
        }
    }

    public record LoadedOrder(long orderId, String orderNo, SupplierOption supplier, String notes, List<OrderDraftLine> lines) {

    }
}
