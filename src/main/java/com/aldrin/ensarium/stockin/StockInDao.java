package com.aldrin.ensarium.stockin;

import com.aldrin.ensarium.db.Db;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StockInDao {

    private static final DateTimeFormatter REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String STATUS_ONHAND = "ONHAND";
    private static final String STATUS_RETURNED = "RETURNED";

    public List<LookupOption> listStores() throws SQLException {
        String sql = "SELECT id, code, name FROM store WHERE active = 1 ORDER BY name";
        List<LookupOption> items = new ArrayList<>();
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new LookupOption(rs.getLong("id"), rs.getString("code"), rs.getString("name")));
            }
        }
        return items;
    }

    public List<LookupOption> listSuppliers() throws SQLException {
        String sql = "SELECT id, supplier_no, name FROM supplier WHERE active = 1 ORDER BY name";
        List<LookupOption> items = new ArrayList<>();
        items.add(new LookupOption(null, "", ""));
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new LookupOption(rs.getLong("id"), rs.getString("supplier_no"), rs.getString("name")));
            }
        }
        return items;
    }

    public List<LookupOption> listUsers() throws SQLException {
        String sql = "SELECT id, username, full_name FROM users WHERE active = 1 ORDER BY full_name, username";
        List<LookupOption> items = new ArrayList<>();
        items.add(new LookupOption(null, "", ""));
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new LookupOption(rs.getLong("id"), rs.getString("username"), rs.getString("full_name")));
            }
        }
        return items;
    }

    public List<LookupOption> listReturnReasons() throws SQLException {
        String sql = "SELECT id, code, name FROM return_reason ORDER BY name";
        List<LookupOption> items = new ArrayList<>();
        items.add(new LookupOption(null, "", ""));
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new LookupOption(rs.getLong("id"), rs.getString("code"), rs.getString("name")));
            }
        }
        return items;
    }

    public List<ProductOption> listProducts() throws SQLException {
        String sql = """
                SELECT p.id, p.sku, p.name, u.id AS uom_id, u.code AS uom_code
                FROM product p
                JOIN uom u ON u.id = p.base_uom_id
                WHERE p.active = 1
                ORDER BY p.name
                """;
        List<ProductOption> items = new ArrayList<>();
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new ProductOption(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getLong("uom_id"),
                        rs.getString("uom_code")));
            }
        }
        return items;
    }


    public BigDecimal findLatestUnitCost(Long productId) throws SQLException {
        if (productId == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        String sql = """
                SELECT prl.unit_cost
                FROM purchase_receipt_line prl
                JOIN purchase_receipt pr ON pr.id = prl.purchase_receipt_id
                WHERE prl.product_id = ?
                ORDER BY pr.received_at DESC, prl.id DESC
                FETCH FIRST 1 ROW ONLY
                """;
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                            : value.setScale(4, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    public List<ReceiptLookup> listReceipts() throws SQLException {
        return listReceipts(null, 100);
    }

    public List<ReceiptLookup> listReceipts(String searchText, int limit) throws SQLException {
        int safeLimit = limit <= 0 ? 100 : limit;
        String sql = """
                SELECT pr.id,
                       pr.receipt_no,
                       s.name AS store_name,
                       sp.name AS supplier_name,
                       COALESCE(u.full_name, u.username, '') AS received_by_name,
                       pr.received_at
                FROM purchase_receipt pr
                JOIN store s ON s.id = pr.store_id
                LEFT JOIN supplier sp ON sp.id = pr.supplier_id
                LEFT JOIN users u ON u.id = pr.received_by
                WHERE (
                       ? IS NULL
                    OR UPPER(COALESCE(pr.receipt_no, '')) LIKE ?
                    OR UPPER(COALESCE(s.name, '')) LIKE ?
                    OR UPPER(COALESCE(sp.name, '')) LIKE ?
                    OR UPPER(COALESCE(u.full_name, u.username, '')) LIKE ?
                )
                ORDER BY pr.id DESC
                FETCH FIRST %d ROWS ONLY
                """.formatted(safeLimit);
        String normalized = searchText == null ? null : searchText.trim().toUpperCase();
        if (normalized != null && normalized.isEmpty()) {
            normalized = null;
        }
        String pattern = normalized == null ? null : "%" + normalized + "%";
        List<ReceiptLookup> items = new ArrayList<>();
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReceiptLookup item = new ReceiptLookup();
                    item.setId(rs.getLong("id"));
                    item.setReceiptNo(rs.getString("receipt_no"));
                    item.setStoreName(rs.getString("store_name"));
                    item.setSupplierName(rs.getString("supplier_name"));
                    item.setReceivedByName(rs.getString("received_by_name"));
                    item.setReceivedAt(rs.getTimestamp("received_at"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public LoadedStockIn loadReceipt(long receiptId) throws SQLException {
        try (Connection conn = Db.getConnection()) {
            return loadReceipt(conn, receiptId);
        }
    }

    public long saveReceipt(StockInHeader header, List<StockInLine> rawLines) throws SQLException {
        List<StockInLine> lines = normalizeLines(rawLines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Add at least one stock-in line.");
        }
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            if (header.getReceiptId() == null) {
                if (header.getReceiptNo() == null || header.getReceiptNo().isBlank()) {
                    header.setReceiptNo(generateReceiptNo());
                }
                long receiptId = insertReceipt(conn, header);
                insertReceiptLines(conn, receiptId, lines);
                long txnId = insertInventoryTxn(conn, header.getStoreId(), "PURCHASE_RECEIPT", header.getReceiptNo(), null,
                        receiptId, "Stock-in saved", header.getReceivedBy());
                insertInventoryTxnLinesForStockIn(conn, txnId, lines, null, STATUS_ONHAND);
                applyStockInBalance(conn, header.getStoreId(), lines, null, STATUS_ONHAND);
                conn.commit();
                return receiptId;
            }

            StockInHeader existingHeader = loadReceiptHeader(conn, header.getReceiptId());
            if (existingHeader == null) {
                throw new SQLException("Receipt not found: " + header.getReceiptId());
            }
            if (existingHeader.getStoreId() != header.getStoreId()) {
                throw new IllegalArgumentException("Changing store on an existing stock-in is not allowed.");
            }
            List<StockInLine> oldLines = loadReceiptLines(conn, header.getReceiptId());
            ensureEnoughOnHandForReverse(conn, header.getStoreId(), oldLines);

            updateReceipt(conn, header);

            long reverseTxnId = insertInventoryTxn(conn, header.getStoreId(), "ADJUSTMENT",
                    valueOrFallback(header.getReceiptNo(), existingHeader.getReceiptNo()), null, header.getReceiptId(),
                    "Reverse old stock-in lines due to edit", header.getReceivedBy());
            insertInventoryTxnLinesForStockIn(conn, reverseTxnId, oldLines, STATUS_ONHAND, null);
            applyStockInBalance(conn, header.getStoreId(), oldLines, STATUS_ONHAND, null);

            deleteReceiptLines(conn, header.getReceiptId());
            insertReceiptLines(conn, header.getReceiptId(), lines);

            long applyTxnId = insertInventoryTxn(conn, header.getStoreId(), "PURCHASE_RECEIPT",
                    valueOrFallback(header.getReceiptNo(), existingHeader.getReceiptNo()), null, header.getReceiptId(),
                    "Apply edited stock-in lines", header.getReceivedBy());
            insertInventoryTxnLinesForStockIn(conn, applyTxnId, lines, null, STATUS_ONHAND);
            applyStockInBalance(conn, header.getStoreId(), lines, null, STATUS_ONHAND);

            conn.commit();
            return header.getReceiptId();
        } catch (SQLException | RuntimeException ex) {
            Db.rollbackQuietly(conn);
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<ReturnableLine> listReturnableLines(long receiptId) throws SQLException {
        try (Connection conn = Db.getConnection()) {
            LoadedStockIn loaded = loadReceipt(conn, receiptId);
            if (loaded == null) {
                return List.of();
            }
            String sql = """
                    SELECT itl.product_id, itl.lot_id, itl.unit_cost, COALESCE(SUM(itl.qty_in_base), 0) AS returned_qty
                    FROM inventory_txn it
                    JOIN inventory_txn_line itl ON itl.txn_id = it.id
                    WHERE it.purchase_receipt_id = ?
                      AND it.txn_type = 'ADJUSTMENT'
                      AND itl.from_status = 'ONHAND'
                      AND itl.to_status = 'RETURNED'
                    GROUP BY itl.product_id, itl.lot_id, itl.unit_cost
                    """;
            Map<String, BigDecimal> returnedMap = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, receiptId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        returnedMap.put(returnKey(rs.getLong("product_id"), (Long) rs.getObject("lot_id"), rs.getBigDecimal("unit_cost")),
                                rs.getBigDecimal("returned_qty"));
                    }
                }
            }

            List<ReturnableLine> result = new ArrayList<>();
            for (StockInLine line : loaded.getLines()) {
                BigDecimal returnedQty = returnedMap.getOrDefault(returnKey(line.getProduct().getId(), line.getLotId(), line.getUnitCost()), BigDecimal.ZERO);
                BigDecimal available = line.getQuantityInBase().subtract(returnedQty).setScale(4, RoundingMode.HALF_UP);
                if (available.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                ReturnableLine item = new ReturnableLine();
                item.setReceiptLineId(line.getLineId());
                item.setProductId(line.getProduct().getId());
                item.setLotId(line.getLotId());
                item.setSku(line.getProduct().getSku());
                item.setProductName(line.getProduct().getName());
                item.setLotNo(line.getLotNo());
                item.setExpiryDate(line.getExpiryDate());
                item.setPurchasedQty(line.getQuantityInBase());
                item.setReturnedQty(returnedQty);
                item.setAvailableQty(available);
                item.setUnitCost(line.getUnitCost());
                result.add(item);
            }
            return result;
        }
    }

    public long createReturn(long receiptId, int storeId, Integer createdBy, Integer returnedBy, String returnedByName,
            Long reasonId, String reasonName, String notes, List<StockInReturnEntry> entries) throws SQLException {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("No return quantities were entered.");
        }

        Map<String, BigDecimal> availableMap = new LinkedHashMap<>();
        for (ReturnableLine line : listReturnableLines(receiptId)) {
            availableMap.put(returnKey(line.getProductId(), line.getLotId(), line.getUnitCost()), line.getAvailableQty());
        }

        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);
            String returnNo = "STKINRET-" + LocalDateTime.now().format(REF_FORMAT);
            String fullNotes = buildReturnNotes(createdBy, returnedBy, returnedByName, reasonId, reasonName, notes);
            long txnId = insertInventoryTxn(conn, storeId, "ADJUSTMENT", returnNo, null, receiptId, fullNotes, createdBy);

            String sql = """
                    INSERT INTO inventory_txn_line
                    (txn_id, product_id, lot_id, from_status, to_status, qty_in_base, unit_cost, total_cost)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (StockInReturnEntry entry : entries) {
                    BigDecimal qty = safe(entry.getQuantity());
                    if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    String key = returnKey(entry.getProductId(), entry.getLotId(), entry.getUnitCost());
                    BigDecimal available = availableMap.getOrDefault(key, BigDecimal.ZERO);
                    if (qty.compareTo(available) > 0) {
                        throw new IllegalArgumentException("Return quantity exceeds available quantity for a line.");
                    }
                    ps.setLong(1, txnId);
                    ps.setLong(2, entry.getProductId());
                    if (entry.getLotId() == null) {
                        ps.setNull(3, java.sql.Types.BIGINT);
                    } else {
                        ps.setLong(3, entry.getLotId());
                    }
                    ps.setString(4, STATUS_ONHAND);
                    ps.setString(5, STATUS_RETURNED);
                    ps.setBigDecimal(6, qty.setScale(4, RoundingMode.HALF_UP));
                    ps.setBigDecimal(7, safe(entry.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                    ps.setBigDecimal(8, entry.getTotalCost());
                    ps.addBatch();
                    applyStockBalance(conn, storeId, entry.getProductId(), STATUS_ONHAND, STATUS_RETURNED, qty);
                    availableMap.put(key, available.subtract(qty));
                }
                ps.executeBatch();
            }
            conn.commit();
            return txnId;
        } catch (SQLException | RuntimeException ex) {
            Db.rollbackQuietly(conn);
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private LoadedStockIn loadReceipt(Connection conn, long receiptId) throws SQLException {
        StockInHeader header = loadReceiptHeader(conn, receiptId);
        if (header == null) {
            return null;
        }
        LoadedStockIn loaded = new LoadedStockIn();
        loaded.setHeader(header);
        loaded.setLines(loadReceiptLines(conn, receiptId));
        return loaded;
    }

    private StockInHeader loadReceiptHeader(Connection conn, Long receiptId) throws SQLException {
        String sql = """
                SELECT id, receipt_no, store_id, supplier_id, received_by, notes
                FROM purchase_receipt
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockInHeader header = new StockInHeader();
                    header.setReceiptId(rs.getLong("id"));
                    header.setReceiptNo(rs.getString("receipt_no"));
                    header.setStoreId(rs.getInt("store_id"));
                    header.setSupplierId((Long) rs.getObject("supplier_id"));
                    Integer receivedBy = (Integer) rs.getObject("received_by");
                    header.setReceivedBy(receivedBy);
                    header.setNotes(rs.getString("notes"));
                    return header;
                }
            }
        }
        return null;
    }

    private List<StockInLine> loadReceiptLines(Connection conn, Long receiptId) throws SQLException {
        String sql = """
                SELECT prl.id,
                       prl.product_id,
                       p.sku,
                       p.name,
                       u.id AS uom_id,
                       u.code AS uom_code,
                       prl.lot_id,
                       il.lot_no,
                       il.expiry_date,
                       prl.qty_in_base,
                       prl.unit_cost
                FROM purchase_receipt_line prl
                JOIN product p ON p.id = prl.product_id
                JOIN uom u ON u.id = p.base_uom_id
                LEFT JOIN inventory_lot il ON il.id = prl.lot_id
                WHERE prl.purchase_receipt_id = ?
                ORDER BY prl.id
                """;
        List<StockInLine> lines = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockInLine line = new StockInLine();
                    line.setLineId(rs.getLong("id"));
                    line.setProduct(new ProductOption(
                            rs.getLong("product_id"),
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getLong("uom_id"),
                            rs.getString("uom_code")));
                    line.setUnit(new LookupOption(rs.getLong("uom_id"), rs.getString("uom_code"), rs.getString("uom_code")));
                    line.setLotId((Long) rs.getObject("lot_id"));
                    line.setLotNo(rs.getString("lot_no"));
                    Date expiry = rs.getDate("expiry_date");
                    if (expiry != null) {
                        line.setExpiryDate(new java.util.Date(expiry.getTime()));
                    }
                    line.setQuantityInBase(rs.getBigDecimal("qty_in_base"));
                    line.setUnitCost(rs.getBigDecimal("unit_cost"));
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private long insertReceipt(Connection conn, StockInHeader header) throws SQLException {
        String sql = """
                INSERT INTO purchase_receipt
                (store_id, supplier_id, receipt_no, received_at, received_by, notes)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, header.getStoreId());
            if (header.getSupplierId() == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, header.getSupplierId());
            }
            ps.setString(3, header.getReceiptNo());
            if (header.getReceivedBy() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, header.getReceivedBy());
            }
            ps.setString(5, blankToNull(header.getNotes()));
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private void updateReceipt(Connection conn, StockInHeader header) throws SQLException {
        String sql = """
                UPDATE purchase_receipt
                SET receipt_no = ?, supplier_id = ?, received_by = ?, notes = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, blankToNull(header.getReceiptNo()));
            if (header.getSupplierId() == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, header.getSupplierId());
            }
            if (header.getReceivedBy() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, header.getReceivedBy());
            }
            ps.setString(4, blankToNull(header.getNotes()));
            ps.setLong(5, header.getReceiptId());
            ps.executeUpdate();
        }
    }

    private void insertReceiptLines(Connection conn, long receiptId, List<StockInLine> lines) throws SQLException {
        String sql = """
                INSERT INTO purchase_receipt_line
                (purchase_receipt_id, product_id, lot_id, qty_in_base, unit_cost, total_cost)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockInLine line : lines) {
                Long lotId = resolveLotId(conn, line);
                line.setLotId(lotId);
                ps.setLong(1, receiptId);
                ps.setLong(2, line.getProduct().getId());
                if (lotId == null) {
                    ps.setNull(3, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(3, lotId);
                }
                ps.setBigDecimal(4, safe(line.getQuantityInBase()).setScale(4, RoundingMode.HALF_UP));
                ps.setBigDecimal(5, safe(line.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                ps.setBigDecimal(6, line.getTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteReceiptLines(Connection conn, long receiptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM purchase_receipt_line WHERE purchase_receipt_id = ?")) {
            ps.setLong(1, receiptId);
            ps.executeUpdate();
        }
    }

    private Long resolveLotId(Connection conn, StockInLine line) throws SQLException {
        String lotNo = blankToNull(line.getLotNo());
        java.util.Date expiryDate = line.getExpiryDate();
        if (lotNo == null && expiryDate == null) {
            return null;
        }
        String findSql = """
                SELECT id
                FROM inventory_lot
                WHERE product_id = ?
                  AND ((lot_no = ?) OR (lot_no IS NULL AND ? IS NULL))
                  AND ((expiry_date = ?) OR (expiry_date IS NULL AND ? IS NULL))
                FETCH FIRST 1 ROW ONLY
                """;
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setLong(1, line.getProduct().getId());
            ps.setString(2, lotNo);
            ps.setString(3, lotNo);
            if (expiryDate == null) {
                ps.setNull(4, java.sql.Types.DATE);
                ps.setNull(5, java.sql.Types.DATE);
            } else {
                Date sqlDate = new Date(expiryDate.getTime());
                ps.setDate(4, sqlDate);
                ps.setDate(5, sqlDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        String insertSql = "INSERT INTO inventory_lot(product_id, lot_no, expiry_date, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, line.getProduct().getId());
            ps.setString(2, lotNo);
            if (expiryDate == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, new Date(expiryDate.getTime()));
            }
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private long insertInventoryTxn(Connection conn, int storeId, String txnType, String refNo, Long saleId,
            Long purchaseReceiptId, String notes, Integer createdBy) throws SQLException {
        String sql = """
                INSERT INTO inventory_txn
                (store_id, txn_type, ref_no, sale_id, purchase_receipt_id, notes, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, storeId);
            ps.setString(2, txnType);
            ps.setString(3, refNo);
            if (saleId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, saleId);
            }
            if (purchaseReceiptId == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, purchaseReceiptId);
            }
            ps.setString(6, blankToNull(notes));
            if (createdBy == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, createdBy);
            }
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private void insertInventoryTxnLinesForStockIn(Connection conn, long txnId, List<StockInLine> lines,
            String fromStatus, String toStatus) throws SQLException {
        String sql = """
                INSERT INTO inventory_txn_line
                (txn_id, product_id, lot_id, from_status, to_status, qty_in_base, unit_cost, total_cost)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockInLine line : lines) {
                ps.setLong(1, txnId);
                ps.setLong(2, line.getProduct().getId());
                if (line.getLotId() == null) {
                    ps.setNull(3, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(3, line.getLotId());
                }
                ps.setString(4, fromStatus);
                ps.setString(5, toStatus);
                ps.setBigDecimal(6, safe(line.getQuantityInBase()).setScale(4, RoundingMode.HALF_UP));
                ps.setBigDecimal(7, safe(line.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                ps.setBigDecimal(8, line.getTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void applyStockInBalance(Connection conn, int storeId, List<StockInLine> lines,
            String fromStatus, String toStatus) throws SQLException {
        for (StockInLine line : lines) {
            applyStockBalance(conn, storeId, line.getProduct().getId(), fromStatus, toStatus, line.getQuantityInBase());
        }
    }

    private void applyStockBalance(Connection conn, int storeId, Long productId,
            String fromStatus, String toStatus, BigDecimal qty) throws SQLException {
        BigDecimal value = safe(qty).setScale(4, RoundingMode.HALF_UP);
        if (fromStatus != null) {
            BigDecimal current = getBalance(conn, storeId, productId, fromStatus);
            if (current.compareTo(value) < 0) {
                throw new IllegalArgumentException("Insufficient stock in status " + fromStatus + " for product ID " + productId + ".");
            }
            upsertBalance(conn, storeId, productId, fromStatus, current.subtract(value));
        }
        if (toStatus != null) {
            BigDecimal current = getBalance(conn, storeId, productId, toStatus);
            upsertBalance(conn, storeId, productId, toStatus, current.add(value));
        }
    }

    private BigDecimal getBalance(Connection conn, int storeId, Long productId, String statusCode) throws SQLException {
        String sql = "SELECT qty_in_base FROM stock_balance WHERE store_id = ? AND product_id = ? AND status_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setLong(2, productId);
            ps.setString(3, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private void upsertBalance(Connection conn, int storeId, Long productId, String statusCode, BigDecimal qty) throws SQLException {
        String updateSql = "UPDATE stock_balance SET qty_in_base = ? WHERE store_id = ? AND product_id = ? AND status_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, qty.setScale(4, RoundingMode.HALF_UP));
            ps.setInt(2, storeId);
            ps.setLong(3, productId);
            ps.setString(4, statusCode);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                String insertSql = "INSERT INTO stock_balance(store_id, product_id, status_code, qty_in_base) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                    ips.setInt(1, storeId);
                    ips.setLong(2, productId);
                    ips.setString(3, statusCode);
                    ips.setBigDecimal(4, qty.setScale(4, RoundingMode.HALF_UP));
                    ips.executeUpdate();
                }
            }
        }
    }

    private void ensureEnoughOnHandForReverse(Connection conn, int storeId, List<StockInLine> oldLines) throws SQLException {
        for (StockInLine line : oldLines) {
            BigDecimal onHand = getBalance(conn, storeId, line.getProduct().getId(), STATUS_ONHAND);
            if (onHand.compareTo(line.getQuantityInBase()) < 0) {
                throw new IllegalArgumentException(
                        "Cannot edit this receipt because part of its stock has already been consumed or moved. "
                        + "Product: " + line.getProduct().getName());
            }
        }
    }

    private String generateReceiptNo() {
        return "PR-" + LocalDateTime.now().format(REF_FORMAT);
    }

    private String buildReturnNotes(Integer createdBy, Integer returnedBy, String returnedByName,
            Long reasonId, String reasonName, String notes) {
        StringBuilder sb = new StringBuilder("STOCKIN_RETURN");
        if (createdBy != null) {
            sb.append(" | createdBy=").append(createdBy);
        }
        if (returnedBy != null) {
            sb.append(" | returnedBy=").append(returnedBy);
        }
        if (returnedByName != null && !returnedByName.isBlank()) {
            sb.append(" | returnedByName=").append(returnedByName.trim());
        }
        if (reasonId != null) {
            sb.append(" | reasonId=").append(reasonId);
        }
        if (reasonName != null && !reasonName.isBlank()) {
            sb.append(" | reason=").append(reasonName.trim());
        }
        if (notes != null && !notes.isBlank()) {
            sb.append(" | notes=").append(notes.trim());
        }
        return sb.toString();
    }

    private long generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new SQLException("No generated key returned.");
    }

    private List<StockInLine> normalizeLines(List<StockInLine> rawLines) {
        Map<String, StockInLine> merged = new LinkedHashMap<>();
        if (rawLines == null) {
            return new ArrayList<>();
        }
        for (StockInLine src : rawLines) {
            if (src == null || src.getProduct() == null || src.getProduct().getId() == null) {
                continue;
            }
            if (safe(src.getQuantityInBase()).compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String key = src.getProduct().getId() + "|"
                    + Objects.toString(blankToNull(src.getLotNo()), "") + "|"
                    + Objects.toString(src.getExpiryDate() == null ? null : new java.sql.Date(src.getExpiryDate().getTime()), "") + "|"
                    + safe(src.getUnitCost()).setScale(4, RoundingMode.HALF_UP).toPlainString();
            StockInLine target = merged.get(key);
            if (target == null) {
                target = cloneLine(src);
                merged.put(key, target);
            } else {
                target.setQuantityInBase(target.getQuantityInBase().add(src.getQuantityInBase()));
            }
        }
        return new ArrayList<>(merged.values());
    }

    private StockInLine cloneLine(StockInLine src) {
        StockInLine line = new StockInLine();
        line.setLineId(src.getLineId());
        line.setProduct(src.getProduct());
        line.setLotId(src.getLotId());
        line.setLotNo(blankToNull(src.getLotNo()));
        line.setExpiryDate(src.getExpiryDate());
        line.setUnit(src.getUnit());
        line.setQuantityInBase(safe(src.getQuantityInBase()).setScale(4, RoundingMode.HALF_UP));
        line.setUnitCost(safe(src.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
        return line;
    }

    private String returnKey(Long productId, Long lotId, BigDecimal unitCost) {
        return productId + "|" + (lotId == null ? "NULL" : lotId.toString()) + "|"
                + safe(unitCost).setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String valueOrFallback(String value, String fallback) {
        String v = blankToNull(value);
        return v == null ? fallback : v;
    }
}
