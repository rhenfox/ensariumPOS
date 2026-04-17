
package com.aldrin.ensarium.txn;

import com.aldrin.ensarium.db.Db;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InventoryTxnDao {

    public List<InventoryTxn> findTransactions(String keyword, int limit) throws SQLException {
        String sql = """
                select it.id,
                       it.store_id,
                       it.txn_type,
                       it.ref_no,
                       it.sale_id,
                       it.sales_return_id,
                       it.purchase_receipt_id,
                       it.notes,
                       it.created_by,
                       u.full_name created_by_name,
                       it.created_at
                from inventory_txn it
                left join users u on u.id = it.created_by
                order by it.created_at desc, it.id desc
                """;
        String kw = keyword == null ? "" : keyword.trim().toUpperCase();
        List<InventoryTxn> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryTxn txn = mapTxn(rs);
                if (matchesKeyword(txn, kw)) {
                    rows.add(txn);
                    if (rows.size() >= limit) {
                        break;
                    }
                }
            }
        }
        return rows;
    }

    private boolean matchesKeyword(InventoryTxn txn, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return upper(txn.getTxnType()).contains(keyword)
                || upper(txn.getRefNo()).contains(keyword)
                || upper(txn.getNotes()).contains(keyword)
                || upper(txn.getCreatedByName()).contains(keyword)
                || String.valueOf(txn.getId()).contains(keyword);
    }

    public InventoryTxn findById(long txnId) throws SQLException {
        String sql = """
                select it.id,
                       it.store_id,
                       it.txn_type,
                       it.ref_no,
                       it.sale_id,
                       it.sales_return_id,
                       it.purchase_receipt_id,
                       it.notes,
                       it.created_by,
                       u.full_name created_by_name,
                       it.created_at
                from inventory_txn it
                left join users u on u.id = it.created_by
                where it.id = ?
                """;
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTxn(rs);
                }
            }
        }
        return null;
    }

    public List<InventoryTxnLine> findDetailsByTxnId(long txnId) throws SQLException {
        String sql = """
                select itl.id,
                       p.id as product_id,
                       p.sku,
                       p.name product_name,
                       l.lot_no lot_no,
                       coalesce(itl.from_status, '') from_status,
                       coalesce(itl.to_status, '') to_status,
                       itl.qty_in_base,
                       itl.unit_cost,
                       itl.total_cost
                from inventory_txn_line itl
                join product p on p.id = itl.product_id
                left join inventory_lot l on l.id = itl.lot_id
                where itl.txn_id = ?
                order by itl.id
                """;
        List<InventoryTxnLine> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryTxnLine row = new InventoryTxnLine();
                    row.setId(rs.getLong("id"));
                    row.setProductId(rs.getLong("product_id"));
                    row.setSku(rs.getString("sku"));
                    row.setProductName(rs.getString("product_name"));
                    row.setLotNo(rs.getString("lot_no"));
                    row.setFromStatus(rs.getString("from_status"));
                    row.setToStatus(rs.getString("to_status"));
                    row.setQtyInBase(rs.getBigDecimal("qty_in_base"));
                    row.setUnitCost(rs.getBigDecimal("unit_cost"));
                    row.setTotalCost(rs.getBigDecimal("total_cost"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<SummaryRow> findSummaryRows(InventoryTxn txn) throws SQLException {
        String txnType = upper(txn.getTxnType());
        if ("PURCHASE_RECEIPT".equals(txnType) && txn.getPurchaseReceiptId() != null) {
            return findPurchaseSummary(txn);
        }
        if ("SALE".equals(txnType) && txn.getSaleId() != null) {
            return findSaleSummary(txn);
        }
        if ("SALE_RETURN".equals(txnType) && txn.getSalesReturnId() != null) {
            return findReturnSummary(txn);
        }
        return findGenericInventorySummary(txn);
    }

    public List<TraceRow> findTraceRows(InventoryTxn txn) throws SQLException {
        String txnType = upper(txn.getTxnType());
        if ("PURCHASE_RECEIPT".equals(txnType) && txn.getPurchaseReceiptId() != null) {
            return findPurchaseTraceRows(txn);
        }
        if ("SALE".equals(txnType) && txn.getSaleId() != null) {
            return findSaleTraceRows(txn);
        }
        if ("SALE_RETURN".equals(txnType) && txn.getSalesReturnId() != null) {
            return findReturnTraceRows(txn);
        }
        return findGenericTraceRows(txn);
    }

    private List<SummaryRow> findPurchaseSummary(InventoryTxn txn) throws SQLException {
        String sql = """
                select pr.receipt_no,
                       pr.received_at,
                       coalesce(s.name, '') supplier_name,
                       count(prl.id) line_count,
                       coalesce(sum(prl.qty_in_base),0) total_qty,
                       coalesce(sum(prl.total_cost),0) total_cost,
                       coalesce(pr.notes, '') notes
                from purchase_receipt pr
                left join supplier s on s.id = pr.supplier_id
                left join purchase_receipt_line prl on prl.purchase_receipt_id = pr.id
                where pr.id = ?
                group by pr.receipt_no, pr.received_at, s.name, pr.notes
                """;
        List<SummaryRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txn.getPurchaseReceiptId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rows.add(new SummaryRow("Transaction Type", "Purchase Receipt"));
                    rows.add(new SummaryRow("Receipt No", nvl(rs.getString("receipt_no"))));
                    rows.add(new SummaryRow("Received At", SwingUtils.formatDateTime(rs.getTimestamp("received_at"))));
                    rows.add(new SummaryRow("Supplier", nvl(rs.getString("supplier_name"))));
                    rows.add(new SummaryRow("Line Count", String.valueOf(rs.getInt("line_count"))));
                    rows.add(new SummaryRow("Total Qty", SwingUtils.formatQty(rs.getBigDecimal("total_qty"))));
                    rows.add(new SummaryRow("Total Cost", SwingUtils.formatMoney(rs.getBigDecimal("total_cost"))));
                    rows.add(new SummaryRow("Notes", nvl(rs.getString("notes"))));
                }
            }
        }
        return rows;
    }

    private List<SummaryRow> findSaleSummary(InventoryTxn txn) throws SQLException {
        String sql = """
                select s.sale_no,
                       s.sold_at,
                       coalesce(c.full_name, '') customer_name,
                       vt.gross_amount,
                       vt.discount_total,
                       vt.tax_total,
                       vt.total_due,
                       vt.cost_total,
                       vt.profit_amount,
                       coalesce(sum(sp.amount),0) payment_total,
                       max(si.invoice_no) invoice_no,
                       coalesce(s.notes, '') notes
                from sale s
                left join customer c on c.id = s.customer_id
                left join v_sale_totals vt on vt.sale_id = s.id
                left join sale_payment sp on sp.sale_id = s.id
                left join sale_invoice si on si.sale_id = s.id
                where s.id = ?
                group by s.sale_no, s.sold_at, c.full_name, vt.gross_amount, vt.discount_total, vt.tax_total,
                         vt.total_due, vt.cost_total, vt.profit_amount, s.notes
                """;
        List<SummaryRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txn.getSaleId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rows.add(new SummaryRow("Transaction Type", "Sale"));
                    rows.add(new SummaryRow("Sale No", nvl(rs.getString("sale_no"))));
                    rows.add(new SummaryRow("Invoice No", nvl(rs.getString("invoice_no"))));
                    rows.add(new SummaryRow("Sold At", SwingUtils.formatDateTime(rs.getTimestamp("sold_at"))));
                    rows.add(new SummaryRow("Customer", nvl(rs.getString("customer_name"))));
                    rows.add(new SummaryRow("Gross Sales", SwingUtils.formatMoney(rs.getBigDecimal("gross_amount"))));
                    rows.add(new SummaryRow("Discount", SwingUtils.formatMoney(rs.getBigDecimal("discount_total"))));
                    rows.add(new SummaryRow("Tax", SwingUtils.formatMoney(rs.getBigDecimal("tax_total"))));
                    rows.add(new SummaryRow("Total Due", SwingUtils.formatMoney(rs.getBigDecimal("total_due"))));
                    rows.add(new SummaryRow("Cost Total", SwingUtils.formatMoney(rs.getBigDecimal("cost_total"))));
                    rows.add(new SummaryRow("Profit", SwingUtils.formatMoney(rs.getBigDecimal("profit_amount"))));
                    rows.add(new SummaryRow("Payment Total", SwingUtils.formatMoney(rs.getBigDecimal("payment_total"))));
                    rows.add(new SummaryRow("Notes", nvl(rs.getString("notes"))));
                }
            }
        }
        return rows;
    }

    private List<SummaryRow> findReturnSummary(InventoryTxn txn) throws SQLException {
        String sql = """
                select sr.return_no,
                       sr.returned_at,
                       coalesce(c.full_name, '') customer_name,
                       coalesce(sum(srl.qty_in_base),0) total_qty,
                       coalesce(sum((srl.qty_in_base * srl.unit_price_refund) - srl.discount_refund),0) refund_gross,
                       coalesce(sum(srl.tax_refund),0) tax_refund,
                       coalesce(sum(srl.discount_refund),0) discount_refund,
                       coalesce(sum(srl.cost_total_refund),0) restored_cost,
                       coalesce(sum(srr.amount),0) refund_paid,
                       coalesce(sr.notes, '') notes
                from sales_return sr
                left join customer c on c.id = sr.customer_id
                left join sales_return_line srl on srl.sales_return_id = sr.id
                left join sales_return_refund srr on srr.sales_return_id = sr.id
                where sr.id = ?
                group by sr.return_no, sr.returned_at, c.full_name, sr.notes
                """;
        List<SummaryRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txn.getSalesReturnId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rows.add(new SummaryRow("Transaction Type", "Sales Return"));
                    rows.add(new SummaryRow("Return No", nvl(rs.getString("return_no"))));
                    rows.add(new SummaryRow("Returned At", SwingUtils.formatDateTime(rs.getTimestamp("returned_at"))));
                    rows.add(new SummaryRow("Customer", nvl(rs.getString("customer_name"))));
                    rows.add(new SummaryRow("Total Qty Returned", SwingUtils.formatQty(rs.getBigDecimal("total_qty"))));
                    rows.add(new SummaryRow("Refund Gross", SwingUtils.formatMoney(rs.getBigDecimal("refund_gross"))));
                    rows.add(new SummaryRow("Tax Refund", SwingUtils.formatMoney(rs.getBigDecimal("tax_refund"))));
                    rows.add(new SummaryRow("Discount Refund", SwingUtils.formatMoney(rs.getBigDecimal("discount_refund"))));
                    rows.add(new SummaryRow("Restored Cost", SwingUtils.formatMoney(rs.getBigDecimal("restored_cost"))));
                    rows.add(new SummaryRow("Refund Paid", SwingUtils.formatMoney(rs.getBigDecimal("refund_paid"))));
                    rows.add(new SummaryRow("Notes", nvl(rs.getString("notes"))));
                }
            }
        }
        return rows;
    }

    private List<SummaryRow> findGenericInventorySummary(InventoryTxn txn) throws SQLException {
        String sql = """
                select count(itl.id) line_count,
                       count(distinct itl.product_id) product_count,
                       coalesce(sum(itl.qty_in_base),0) total_qty,
                       coalesce(sum(itl.total_cost),0) total_cost,
                       coalesce(sum(case when coalesce(itl.to_status, '') <> '' then itl.qty_in_base else 0 end),0) qty_in,
                       coalesce(sum(case when coalesce(itl.from_status, '') <> '' then itl.qty_in_base else 0 end),0) qty_out
                from inventory_txn it
                left join inventory_txn_line itl on itl.txn_id = it.id
                where it.id = ?
                """;
        List<SummaryRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txn.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rows.add(new SummaryRow("Transaction Type", friendlyTxnType(txn.getTxnType())));
                    rows.add(new SummaryRow("Reference No", nvl(txn.getRefNo())));
                    rows.add(new SummaryRow("Created At", SwingUtils.formatDateTime(txn.getCreatedAt())));
                    rows.add(new SummaryRow("Created By", nvl(txn.getCreatedByName())));
                    rows.add(new SummaryRow("Store ID", String.valueOf(txn.getStoreId())));
                    rows.add(new SummaryRow("Line Count", String.valueOf(rs.getInt("line_count"))));
                    rows.add(new SummaryRow("Distinct Products", String.valueOf(rs.getInt("product_count"))));
                    rows.add(new SummaryRow("Total Qty", SwingUtils.formatQty(rs.getBigDecimal("total_qty"))));
                    rows.add(new SummaryRow("Qty In", SwingUtils.formatQty(rs.getBigDecimal("qty_in"))));
                    rows.add(new SummaryRow("Qty Out", SwingUtils.formatQty(rs.getBigDecimal("qty_out"))));
                    rows.add(new SummaryRow("Total Cost", SwingUtils.formatMoney(rs.getBigDecimal("total_cost"))));
                    rows.add(new SummaryRow("Notes", nvl(txn.getNotes())));
                }
            }
        }
        return rows;
    }

    private List<TraceRow> findPurchaseTraceRows(InventoryTxn txn) throws SQLException {
        String sql = """
                select pr.receipt_no document_no,
                       p.id product_id,
                       p.name product_name,
                       l.lot_no lot_no,
                       '' from_status,
                       'ONHAND' to_status,
                       prl.qty_in_base qty,
                       prl.unit_cost unit_cost,
                       prl.total_cost total_cost,
                       cast(null as decimal(19,4)) unit_price,
                       cast(null as decimal(19,4)) line_gross,
                       cast(null as decimal(19,4)) tax_amount,
                       cast(null as decimal(19,4)) discount_amount,
                       cast(null as decimal(19,4)) net_amount,
                       cast(null as decimal(19,4)) buying_price,
                       prl.total_cost cost_snapshot,
                       coalesce(pr.notes, '') trace_notes
                from purchase_receipt_line prl
                join purchase_receipt pr on pr.id = prl.purchase_receipt_id
                join product p on p.id = prl.product_id
                left join inventory_lot l on l.id = prl.lot_id
                where pr.id = ?
                order by prl.id
                """;
        return runTraceQuery(sql, txn, txn.getPurchaseReceiptId());
    }

    private List<TraceRow> findSaleTraceRows(InventoryTxn txn) throws SQLException {
        String sql = """
                select s.sale_no document_no,
                       p.id product_id,
                       p.name product_name,
                       l.lot_no lot_no,
                       'ONHAND' from_status,
                       '' to_status,
                       sl.qty_in_base qty,
                       sl.buying_price unit_cost,
                       sl.cost_total total_cost,
                       sl.unit_price unit_price,
                       (sl.qty_in_base * sl.unit_price) line_gross,
                       coalesce(sum(slt.tax_amount),0) tax_amount,
                       coalesce(sum(sd.amount),0) discount_amount,
                       ((sl.qty_in_base * sl.unit_price) - coalesce(sum(sd.amount),0)) net_amount,
                       sl.buying_price buying_price,
                       sl.cost_total cost_snapshot,
                       coalesce(s.notes, '') trace_notes
                from sale_line sl
                join sale s on s.id = sl.sale_id
                join product p on p.id = sl.product_id
                left join inventory_lot l on l.id = sl.lot_id
                left join sale_line_tax slt on slt.sale_line_id = sl.id
                left join sale_discount sd on sd.sale_line_id = sl.id
                where s.id = ?
                group by s.sale_no, p.id, p.name, l.lot_no, sl.qty_in_base, sl.buying_price, sl.cost_total, sl.unit_price, s.notes, sl.id
                order by sl.id
                """;
        return runTraceQuery(sql, txn, txn.getSaleId());
    }

    private List<TraceRow> findReturnTraceRows(InventoryTxn txn) throws SQLException {
        String sql = """
                select sr.return_no document_no,
                       p.id product_id,
                       p.name product_name,
                       l.lot_no lot_no,
                       '' from_status,
                       srl.restock_to_status to_status,
                       srl.qty_in_base qty,
                       srl.buying_price unit_cost,
                       srl.cost_total_refund total_cost,
                       srl.unit_price_refund unit_price,
                       (srl.qty_in_base * srl.unit_price_refund) line_gross,
                       srl.tax_refund tax_amount,
                       srl.discount_refund discount_amount,
                       ((srl.qty_in_base * srl.unit_price_refund) - srl.discount_refund) net_amount,
                       srl.buying_price buying_price,
                       srl.cost_total_refund cost_snapshot,
                       coalesce(sr.notes, '') trace_notes
                from sales_return_line srl
                join sales_return sr on sr.id = srl.sales_return_id
                join product p on p.id = srl.product_id
                left join inventory_lot l on l.id = srl.lot_id
                where sr.id = ?
                order by srl.id
                """;
        return runTraceQuery(sql, txn, txn.getSalesReturnId());
    }

    private List<TraceRow> findGenericTraceRows(InventoryTxn txn) throws SQLException {
        String sql = """
                select it.ref_no document_no,
                       p.id product_id,
                       p.name product_name,
                       l.lot_no lot_no,
                       coalesce(itl.from_status, '') from_status,
                       coalesce(itl.to_status, '') to_status,
                       itl.qty_in_base qty,
                       itl.unit_cost unit_cost,
                       itl.total_cost total_cost,
                       cast(null as decimal(19,4)) unit_price,
                       cast(null as decimal(19,4)) line_gross,
                       cast(null as decimal(19,4)) tax_amount,
                       cast(null as decimal(19,4)) discount_amount,
                       cast(null as decimal(19,4)) net_amount,
                       itl.unit_cost buying_price,
                       itl.total_cost cost_snapshot,
                       coalesce(it.notes, '') trace_notes
                from inventory_txn_line itl
                join inventory_txn it on it.id = itl.txn_id
                join product p on p.id = itl.product_id
                left join inventory_lot l on l.id = itl.lot_id
                where it.id = ?
                order by itl.id
                """;
        List<TraceRow> rows = runTraceQuery(sql, txn, txn.getId());
        String documentNo = nvl(txn.getRefNo()).isBlank() ? "TXN-" + txn.getId() : nvl(txn.getRefNo());
        for (TraceRow row : rows) {
            row.setDocumentNo(documentNo);
        }
        return rows;
    }

    private List<TraceRow> runTraceQuery(String sql, InventoryTxn txn, Long documentId) throws SQLException {
        List<TraceRow> rows = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId == null ? -1L : documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TraceRow row = new TraceRow();
                    row.setDocumentNo(nvl(rs.getString("document_no")));
                    row.setProductId(rs.getLong("product_id"));
                    row.setProductName(nvl(rs.getString("product_name")));
                    row.setLotNo(nvl(rs.getString("lot_no")));
                    row.setFromStatus(nvl(rs.getString("from_status")));
                    row.setToStatus(nvl(rs.getString("to_status")));
                    row.setQty(decimal(rs, "qty"));
                    row.setUnitCost(decimal(rs, "unit_cost"));
                    row.setTotalCost(decimal(rs, "total_cost"));
                    row.setUnitPrice(decimal(rs, "unit_price"));
                    row.setLineGross(decimal(rs, "line_gross"));
                    row.setTaxAmount(decimal(rs, "tax_amount"));
                    row.setDiscountAmount(decimal(rs, "discount_amount"));
                    row.setNetAmount(decimal(rs, "net_amount"));
                    row.setBuyingPrice(decimal(rs, "buying_price"));
                    row.setCostSnapshot(decimal(rs, "cost_snapshot"));
                    row.setTraceNotes(nvl(rs.getString("trace_notes")));
                    rows.add(row);
                }
            }
            attachInventoryQuantities(conn, txn, rows);
        }
        return rows;
    }

    private void attachInventoryQuantities(Connection conn, InventoryTxn txn, List<TraceRow> rows) throws SQLException {
        List<InventoryTraceLine> traceLines = findInventoryTraceLines(conn, txn.getId());
        boolean[] used = new boolean[traceLines.size()];

        for (TraceRow row : rows) {
            InventoryTraceLine match = null;
            for (int i = 0; i < traceLines.size(); i++) {
                if (used[i]) {
                    continue;
                }
                InventoryTraceLine candidate = traceLines.get(i);
                if (matches(row, candidate)) {
                    used[i] = true;
                    match = candidate;
                    break;
                }
            }
            if (match == null) {
                for (int i = 0; i < traceLines.size(); i++) {
                    if (!used[i]) {
                        used[i] = true;
                        match = traceLines.get(i);
                        break;
                    }
                }
            }
            if (match != null) {
                row.setBeforeQty(match.beforeQty());
                row.setTxnQty(match.txnQty());
                row.setAfterQty(match.afterQty());
            }
        }
    }

    private boolean matches(TraceRow row, InventoryTraceLine line) {
        return row.getProductId() == line.productId()
                && nvl(row.getLotNo()).equals(nvl(line.lotNo()))
                && nvl(row.getFromStatus()).equals(nvl(line.fromStatus()))
                && nvl(row.getToStatus()).equals(nvl(line.toStatus()))
                && sameDecimal(row.getQty(), line.qty());
    }

    private boolean sameDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }

    private List<InventoryTraceLine> findInventoryTraceLines(Connection conn, long txnId) throws SQLException {
        String sql = """
                select itl.id,
                       itl.product_id,
                       l.lot_no lot_no,
                       coalesce(itl.from_status, '') from_status,
                       coalesce(itl.to_status, '') to_status,
                       itl.qty_in_base,
                       it.store_id,
                       it.created_at,
                       it.id as txn_id
                from inventory_txn_line itl
                join inventory_txn it on it.id = itl.txn_id
                left join inventory_lot l on l.id = itl.lot_id
                where it.id = ?
                order by itl.id
                """;
        List<InventoryTraceLine> lines = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long lineId = rs.getLong("id");
                    long productId = rs.getLong("product_id");
                    String lotNo = nvl(rs.getString("lot_no"));
                    String fromStatus = nvl(rs.getString("from_status"));
                    String toStatus = nvl(rs.getString("to_status"));
                    BigDecimal qty = decimal(rs, "qty_in_base");
                    int storeId = rs.getInt("store_id");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    long currentTxnId = rs.getLong("txn_id");
                    String trackedStatus = !toStatus.isBlank() ? toStatus : fromStatus;
                    BigDecimal beforeQty = findBeforeQty(conn, storeId, productId, trackedStatus, createdAt, currentTxnId, lineId);
                    BigDecimal txnQty = signedQty(qty, fromStatus, toStatus, trackedStatus);
                    BigDecimal afterQty = beforeQty.add(txnQty);
                    lines.add(new InventoryTraceLine(lineId, productId, lotNo, fromStatus, toStatus, qty, beforeQty, txnQty, afterQty));
                }
            }
        }
        return lines;
    }

    private BigDecimal findBeforeQty(Connection conn, int storeId, long productId, String trackedStatus,
            Timestamp createdAt, long txnId, long lineId) throws SQLException {
        if (trackedStatus == null || trackedStatus.isBlank()) {
            return BigDecimal.ZERO;
        }
        String sql = """
                select coalesce(sum(case when itl.to_status = ? then itl.qty_in_base else 0 end), 0)
                     - coalesce(sum(case when itl.from_status = ? then itl.qty_in_base else 0 end), 0) as qty_before
                from inventory_txn_line itl
                join inventory_txn it on it.id = itl.txn_id
                where it.store_id = ?
                  and itl.product_id = ?
                  and (
                        it.created_at < ?
                        or (it.created_at = ? and (it.id < ? or (it.id = ? and itl.id < ?)))
                  )
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trackedStatus);
            ps.setString(2, trackedStatus);
            ps.setInt(3, storeId);
            ps.setLong(4, productId);
            ps.setTimestamp(5, createdAt);
            ps.setTimestamp(6, createdAt);
            ps.setLong(7, txnId);
            ps.setLong(8, txnId);
            ps.setLong(9, lineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal("qty_before");
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal signedQty(BigDecimal qty, String fromStatus, String toStatus, String trackedStatus) {
        if (qty == null || trackedStatus == null || trackedStatus.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal signed = BigDecimal.ZERO;
        if (trackedStatus.equals(nvl(toStatus))) {
            signed = signed.add(qty);
        }
        if (trackedStatus.equals(nvl(fromStatus))) {
            signed = signed.subtract(qty);
        }
        return signed;
    }

    private record InventoryTraceLine(long lineId, long productId, String lotNo, String fromStatus, String toStatus,
            BigDecimal qty, BigDecimal beforeQty, BigDecimal txnQty, BigDecimal afterQty) {
    }

    private InventoryTxn mapTxn(ResultSet rs) throws SQLException {
        InventoryTxn row = new InventoryTxn();
        row.setId(rs.getLong("id"));
        row.setStoreId(rs.getInt("store_id"));
        row.setTxnType(rs.getString("txn_type"));
        row.setRefNo(rs.getString("ref_no"));
        row.setSaleId(nullableLong(rs, "sale_id"));
        row.setSalesReturnId(nullableLong(rs, "sales_return_id"));
        row.setPurchaseReceiptId(nullableLong(rs, "purchase_receipt_id"));
        row.setNotes(rs.getString("notes"));
        int createdBy = rs.getInt("created_by");
        row.setCreatedBy(rs.wasNull() ? null : createdBy);
        row.setCreatedByName(rs.getString("created_by_name"));
        row.setCreatedAt(rs.getTimestamp("created_at"));
        return row;
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private BigDecimal decimal(ResultSet rs, String column) throws SQLException {
        return rs.getBigDecimal(column);
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String upper(String value) {
        return nvl(value).trim().toUpperCase();
    }

    private String friendlyTxnType(String value) {
        String normalized = upper(value);
        return switch (normalized) {
            case "PURCHASE_RECEIPT" -> "Purchase Receipt";
            case "SALE" -> "Sale";
            case "SALE_RETURN" -> "Sales Return";
            case "DAMAGE" -> "Damage";
            case "EXPIRE" -> "Expire";
            case "ADJUSTMENT" -> "Adjustment";
            default -> normalized.isBlank() ? "Inventory Transaction" : normalized.replace('_', ' ');
        };
    }
}

