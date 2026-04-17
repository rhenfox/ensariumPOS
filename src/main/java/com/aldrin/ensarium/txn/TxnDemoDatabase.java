package com.aldrin.ensarium.txn;

import com.aldrin.ensarium.db.Db;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class TxnDemoDatabase {
    private final Map<Integer, String> users = new HashMap<>();
    private final Map<Long, String> suppliers = new HashMap<>();
    private final Map<Long, String> customers = new HashMap<>();
    private final Map<Long, ProductRec> products = new HashMap<>();
    private final Map<Long, LotRec> lots = new HashMap<>();
    private final Map<Long, PurchaseReceiptRec> purchaseReceipts = new HashMap<>();
    private final List<PurchaseReceiptLineRec> purchaseReceiptLines = new ArrayList<>();
    private final Map<Long, SaleRec> sales = new HashMap<>();
    private final List<SaleLineRec> saleLines = new ArrayList<>();
    private final List<SaleTaxRec> saleTaxes = new ArrayList<>();
    private final List<SaleDiscountRec> saleDiscounts = new ArrayList<>();
    private final List<SalePaymentRec> salePayments = new ArrayList<>();
    private final Map<Long, String> saleInvoices = new HashMap<>();
    private final Map<Long, ReturnRec> returns = new HashMap<>();
    private final List<ReturnLineRec> returnLines = new ArrayList<>();
    private final List<ReturnRefundRec> returnRefunds = new ArrayList<>();
    private final Map<Long, TxnRec> txns = new HashMap<>();
    private final List<LineRec> lines = new ArrayList<>();

    static void installSampleData() {
        TxnDemoDatabase db = new TxnDemoDatabase();
        db.seedCurrent();
//        Db.useProvider(db::openConnection);
    }

    void seedCurrent() {
        users.clear(); suppliers.clear(); customers.clear(); products.clear(); lots.clear();
        purchaseReceipts.clear(); purchaseReceiptLines.clear(); sales.clear(); saleLines.clear();
        saleTaxes.clear(); saleDiscounts.clear(); salePayments.clear(); saleInvoices.clear();
        returns.clear(); returnLines.clear(); returnRefunds.clear(); txns.clear(); lines.clear();

        users.put(1, "Administrator");
        suppliers.put(1L, "Demo Supplier");
        customers.put(1L, "Demo Customer");
        products.put(1L, new ProductRec(1L, "SKU-001", "Demo Product A"));
        products.put(2L, new ProductRec(2L, "SKU-002", "Demo Product B"));
        lots.put(1L, new LotRec(1L, 1L, "LOT-A1"));
        lots.put(2L, new LotRec(2L, 2L, "LOT-B1"));

        purchaseReceipts.put(1L, new PurchaseReceiptRec(1L, "PR-001", Timestamp.valueOf("2026-03-01 08:00:00"), 1L, "Initial stock receipt"));
        purchaseReceiptLines.add(new PurchaseReceiptLineRec(1L, 1L, 1L, 1L, bd("10.0000"), bd("10.0000"), bd("100.0000")));
        purchaseReceiptLines.add(new PurchaseReceiptLineRec(2L, 1L, 2L, 2L, bd("5.0000"), bd("20.0000"), bd("100.0000")));

        sales.put(1L, new SaleRec(1L, "SALE-001", Timestamp.valueOf("2026-03-02 09:00:00"), 1L, "Demo sale transaction"));
        saleLines.add(new SaleLineRec(1L, 1L, 1L, 1L, bd("2.0000"), bd("15.0000"), bd("10.0000"), bd("20.0000")));
        saleLines.add(new SaleLineRec(2L, 1L, 2L, 2L, bd("1.0000"), bd("30.0000"), bd("20.0000"), bd("20.0000")));
        saleTaxes.add(new SaleTaxRec(1L, bd("3.2143")));
        saleTaxes.add(new SaleTaxRec(2L, bd("3.2143")));
        saleDiscounts.add(new SaleDiscountRec(1L, 1L, bd("1.0000")));
        salePayments.add(new SalePaymentRec(1L, bd("59.0000")));
        saleInvoices.put(1L, "INV-001");

        returns.put(1L, new ReturnRec(1L, "SR-001", Timestamp.valueOf("2026-03-03 10:00:00"), 1L, "Demo sales return"));
        returnLines.add(new ReturnLineRec(1L, 1L, 1L, 1L, bd("1.0000"), bd("15.0000"), bd("10.0000"), bd("10.0000"), bd("1.6071"), bd("0.5000"), "RETURNED"));
        returnRefunds.add(new ReturnRefundRec(1L, bd("14.5000")));

        txns.put(1L, new TxnRec(1L, 1, "PURCHASE_RECEIPT", "PR-001", null, null, 1L, "Initial stock receipt", 1, Timestamp.valueOf("2026-03-01 08:00:00")));
        txns.put(2L, new TxnRec(2L, 1, "SALE", "SALE-001", 1L, null, null, "Demo sale transaction", 1, Timestamp.valueOf("2026-03-02 09:00:00")));
        txns.put(3L, new TxnRec(3L, 1, "SALE_RETURN", "SR-001", 1L, 1L, null, "Demo sales return", 1, Timestamp.valueOf("2026-03-03 10:00:00")));
        txns.put(4L, new TxnRec(4L, 1, "DAMAGE", "DMG-001", null, null, null, "Broken unit", 1, Timestamp.valueOf("2026-03-04 11:00:00")));
        txns.put(5L, new TxnRec(5L, 1, "EXPIRE", "EXP-001", null, null, null, "Expired lot", 1, Timestamp.valueOf("2026-03-05 12:00:00")));
        txns.put(6L, new TxnRec(6L, 1, "ADJUSTMENT", "ADJ-001", null, null, null, "Cycle count gain", 1, Timestamp.valueOf("2026-03-06 13:00:00")));

        lines.add(new LineRec(1L, 1L, 1L, 1L, null, "ONHAND", bd("10.0000"), bd("10.0000"), bd("100.0000")));
        lines.add(new LineRec(2L, 1L, 2L, 2L, null, "ONHAND", bd("5.0000"), bd("20.0000"), bd("100.0000")));
        lines.add(new LineRec(3L, 2L, 1L, 1L, "ONHAND", null, bd("2.0000"), bd("10.0000"), bd("20.0000")));
        lines.add(new LineRec(4L, 2L, 2L, 2L, "ONHAND", null, bd("1.0000"), bd("20.0000"), bd("20.0000")));
        lines.add(new LineRec(5L, 3L, 1L, 1L, null, "RETURNED", bd("1.0000"), bd("10.0000"), bd("10.0000")));
        lines.add(new LineRec(6L, 4L, 1L, 1L, "ONHAND", "DAMAGED", bd("1.0000"), bd("10.0000"), bd("10.0000")));
        lines.add(new LineRec(7L, 5L, 1L, 1L, "ONHAND", "EXPIRED", bd("1.0000"), bd("10.0000"), bd("10.0000")));
        lines.add(new LineRec(8L, 6L, 1L, 1L, null, "ONHAND", bd("3.0000"), bd("10.0000"), bd("30.0000")));
    }

    Connection openConnection() {
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "prepareStatement" -> createPreparedStatement((String) args[0]);
            case "close", "rollback" -> null;
            case "isClosed" -> false;
            case "unwrap" -> null;
            case "isWrapperFor" -> false;
            default -> throw new UnsupportedOperationException("Connection method not implemented: " + method.getName());
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, handler);
    }

    private PreparedStatement createPreparedStatement(String sql) {
        Map<Integer, Object> params = new HashMap<>();
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "setLong", "setInt", "setString", "setTimestamp" -> { params.put((Integer) args[0], args[1]); yield null; }
            case "executeQuery" -> createResultSet(runQuery(sql, params));
            case "close" -> null;
            case "unwrap" -> null;
            case "isWrapperFor" -> false;
            default -> throw new UnsupportedOperationException("PreparedStatement method not implemented: " + method.getName());
        };
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class[]{PreparedStatement.class}, handler);
    }

    private ResultSet createResultSet(List<Map<String, Object>> rows) {
        final int[] index = {-1};
        final boolean[] wasNull = {false};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "next":
                    index[0]++;
                    return index[0] < rows.size();
                case "getString": {
                    Object value = rowValue(rows, index[0], (String) args[0]);
                    wasNull[0] = value == null;
                    return value == null ? null : String.valueOf(value);
                }
                case "getLong": {
                    Object value = rowValue(rows, index[0], (String) args[0]);
                    wasNull[0] = value == null;
                    return value == null ? 0L : Long.parseLong(String.valueOf(value));
                }
                case "getInt": {
                    Object value = rowValue(rows, index[0], (String) args[0]);
                    wasNull[0] = value == null;
                    return value == null ? 0 : Integer.parseInt(String.valueOf(value));
                }
                case "getBigDecimal": {
                    Object value = rowValue(rows, index[0], (String) args[0]);
                    wasNull[0] = value == null;
                    return value == null ? null : (value instanceof BigDecimal bd ? bd : new BigDecimal(String.valueOf(value)));
                }
                case "getTimestamp": {
                    Object value = rowValue(rows, index[0], (String) args[0]);
                    wasNull[0] = value == null;
                    return (Timestamp) value;
                }
                case "wasNull": return wasNull[0];
                case "close": return null;
                case "unwrap": return null;
                case "isWrapperFor": return false;
                default: throw new UnsupportedOperationException("ResultSet method not implemented: " + name);
            }
        };
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class}, handler);
    }

    private Object rowValue(List<Map<String, Object>> rows, int rowIndex, String column) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalStateException("ResultSet cursor out of range");
        }
        return rows.get(rowIndex).get(column);
    }

    private List<Map<String, Object>> runQuery(String sql, Map<Integer, Object> params) {
        if (sql.contains("from inventory_txn it") && sql.contains("left join users u on u.id = it.created_by") && sql.contains("order by it.created_at desc, it.id desc")) return queryFindTransactions(params);
        if (sql.contains("left join users u on u.id = it.created_by") && sql.contains("where it.id = ?")) return queryFindById((Long) params.get(1));
        if (sql.contains("where itl.txn_id = ?") && sql.contains("join product p on p.id = itl.product_id") && sql.contains("product_name")) return queryDetailsByTxnId((Long) params.get(1));
        if (sql.contains("from purchase_receipt pr") && sql.contains("group by pr.receipt_no")) return queryPurchaseSummary((Long) params.get(1));
        if (sql.contains("from sale s") && sql.contains("left join v_sale_totals vt")) return querySaleSummary((Long) params.get(1));
        if (sql.contains("from sales_return sr") && sql.contains("left join sales_return_refund srr")) return queryReturnSummary((Long) params.get(1));
        if (sql.contains("count(distinct itl.product_id)")) return queryGenericSummary((Long) params.get(1));
        if (sql.contains("from purchase_receipt_line prl")) return queryPurchaseTrace((Long) params.get(1));
        if (sql.contains("from sale_line sl")) return querySaleTrace((Long) params.get(1));
        if (sql.contains("from sales_return_line srl")) return queryReturnTrace((Long) params.get(1));
        if (sql.contains("select it.ref_no document_no") && sql.contains("from inventory_txn_line itl") && sql.contains("join inventory_txn it on it.id = itl.txn_id")) return queryGenericTrace((Long) params.get(1));
        if (sql.contains("it.id as txn_id")) return queryInventoryTraceLines((Long) params.get(1));
        if (sql.contains("qty_before")) return queryBeforeQty(params);
        throw new UnsupportedOperationException("No demo query handler for SQL:\n" + sql);
    }

    private List<Map<String, Object>> queryFindTransactions(Map<Integer, Object> params) {
        String keyword = Objects.toString(params.getOrDefault(2, ""), "").trim().toUpperCase();
        int limit = params.get(7) instanceof Integer i ? i : Integer.MAX_VALUE;
        return txns.values().stream()
                .sorted(Comparator.comparing(TxnRec::createdAt).reversed().thenComparing(TxnRec::id).reversed())
                .filter(txn -> keyword.isBlank() || txnMatches(txn, keyword))
                .limit(limit)
                .map(this::txnRow)
                .collect(Collectors.toList());
    }

    private boolean txnMatches(TxnRec txn, String keyword) {
        return upper(txn.txnType()).contains(keyword)
                || upper(txn.refNo()).contains(keyword)
                || upper(txn.notes()).contains(keyword)
                || upper(users.get(txn.createdBy())).contains(keyword);
    }

    private List<Map<String, Object>> queryFindById(long txnId) {
        TxnRec txn = txns.get(txnId);
        return txn == null ? List.of() : List.of(txnRow(txn));
    }

    private List<Map<String, Object>> queryDetailsByTxnId(long txnId) {
        return lines.stream().filter(line -> line.txnId() == txnId).sorted(Comparator.comparing(LineRec::id)).map(line -> {
            ProductRec p = products.get(line.productId());
            LotRec lot = lots.get(line.lotId());
            return row(
                    "id", line.id(),
                    "product_id", line.productId(),
                    "sku", p.sku(),
                    "product_name", p.name(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", blank(line.fromStatus()),
                    "to_status", blank(line.toStatus()),
                    "qty_in_base", line.qtyInBase(),
                    "unit_cost", line.unitCost(),
                    "total_cost", line.totalCost());
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryPurchaseSummary(long id) {
        PurchaseReceiptRec pr = purchaseReceipts.get(id);
        if (pr == null) return List.of();
        List<PurchaseReceiptLineRec> rows = purchaseReceiptLines.stream().filter(r -> r.purchaseReceiptId() == id).toList();
        BigDecimal totalQty = rows.stream().map(PurchaseReceiptLineRec::qty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = rows.stream().map(PurchaseReceiptLineRec::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(row(
                "receipt_no", pr.receiptNo(),
                "received_at", pr.receivedAt(),
                "supplier_name", suppliers.get(pr.supplierId()),
                "line_count", rows.size(),
                "total_qty", totalQty,
                "total_cost", totalCost,
                "notes", pr.notes()));
    }

    private List<Map<String, Object>> querySaleSummary(long id) {
        SaleRec sale = sales.get(id);
        if (sale == null) return List.of();
        List<SaleLineRec> rows = saleLines.stream().filter(r -> r.saleId() == id).toList();
        BigDecimal gross = rows.stream().map(r -> r.qty().multiply(r.unitPrice())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = saleDiscounts.stream().filter(d -> d.saleId() == id).map(SaleDiscountRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = saleTaxes.stream().filter(t -> rows.stream().anyMatch(r -> r.id() == t.saleLineId())).map(SaleTaxRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDue = gross.subtract(discount);
        BigDecimal cost = rows.stream().map(SaleLineRec::costTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payment = salePayments.stream().filter(p -> p.saleId() == id).map(SalePaymentRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = totalDue.subtract(tax).subtract(cost);
        return List.of(row(
                "sale_no", sale.saleNo(),
                "sold_at", sale.soldAt(),
                "customer_name", customers.get(sale.customerId()),
                "gross_amount", gross,
                "discount_total", discount,
                "tax_total", tax,
                "total_due", totalDue,
                "cost_total", cost,
                "profit_amount", profit,
                "payment_total", payment,
                "invoice_no", saleInvoices.getOrDefault(id, ""),
                "notes", sale.notes()));
    }

    private List<Map<String, Object>> queryReturnSummary(long id) {
        ReturnRec ret = returns.get(id);
        if (ret == null) return List.of();
        List<ReturnLineRec> rows = returnLines.stream().filter(r -> r.returnId() == id).toList();
        BigDecimal totalQty = rows.stream().map(ReturnLineRec::qty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gross = rows.stream().map(r -> r.qty().multiply(r.unitPriceRefund())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = rows.stream().map(ReturnLineRec::taxRefund).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = rows.stream().map(ReturnLineRec::discountRefund).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal restoredCost = rows.stream().map(ReturnLineRec::costTotalRefund).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refund = returnRefunds.stream().filter(r -> r.returnId() == id).map(ReturnRefundRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(row(
                "return_no", ret.returnNo(),
                "returned_at", ret.returnedAt(),
                "customer_name", customers.get(ret.customerId()),
                "total_qty", totalQty,
                "refund_gross", gross.subtract(discount),
                "tax_refund", tax,
                "discount_refund", discount,
                "restored_cost", restoredCost,
                "refund_paid", refund,
                "notes", ret.notes()));
    }

    private List<Map<String, Object>> queryGenericSummary(long txnId) {
        TxnRec txn = txns.get(txnId);
        List<LineRec> txnLines = lines.stream().filter(line -> line.txnId() == txnId).toList();
        BigDecimal totalQty = txnLines.stream().map(LineRec::qtyInBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = txnLines.stream().map(LineRec::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal qtyIn = txnLines.stream().filter(line -> !blank(line.toStatus()).isBlank()).map(LineRec::qtyInBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal qtyOut = txnLines.stream().filter(line -> !blank(line.fromStatus()).isBlank()).map(LineRec::qtyInBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        long productCount = txnLines.stream().map(LineRec::productId).distinct().count();
        return List.of(row(
                "line_count", txnLines.size(),
                "product_count", (int) productCount,
                "total_qty", totalQty,
                "total_cost", totalCost,
                "qty_in", qtyIn,
                "qty_out", qtyOut,
                "reference_no", txn.refNo(),
                "notes", blank(txn.notes())));
    }

    private List<Map<String, Object>> queryPurchaseTrace(long purchaseReceiptId) {
        PurchaseReceiptRec pr = purchaseReceipts.get(purchaseReceiptId);
        return purchaseReceiptLines.stream().filter(l -> l.purchaseReceiptId() == purchaseReceiptId).sorted(Comparator.comparing(PurchaseReceiptLineRec::id)).map(line -> {
            ProductRec p = products.get(line.productId());
            LotRec lot = lots.get(line.lotId());
            return row(
                    "document_no", pr.receiptNo(),
                    "product_id", p.id(),
                    "product_name", p.name(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", "",
                    "to_status", "ONHAND",
                    "qty", line.qty(),
                    "unit_cost", line.unitCost(),
                    "total_cost", line.totalCost(),
                    "unit_price", null,
                    "line_gross", null,
                    "tax_amount", null,
                    "discount_amount", null,
                    "net_amount", null,
                    "buying_price", null,
                    "cost_snapshot", line.totalCost(),
                    "trace_notes", pr.notes());
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> querySaleTrace(long saleId) {
        SaleRec sale = sales.get(saleId);
        return saleLines.stream().filter(l -> l.saleId() == saleId).sorted(Comparator.comparing(SaleLineRec::id)).map(line -> {
            ProductRec p = products.get(line.productId());
            LotRec lot = lots.get(line.lotId());
            BigDecimal tax = saleTaxes.stream().filter(t -> t.saleLineId() == line.id()).map(SaleTaxRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal discount = saleDiscounts.stream().filter(d -> Objects.equals(d.saleLineId(), line.id())).map(SaleDiscountRec::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal gross = line.qty().multiply(line.unitPrice());
            return row(
                    "document_no", sale.saleNo(),
                    "product_id", p.id(),
                    "product_name", p.name(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", "ONHAND",
                    "to_status", "",
                    "qty", line.qty(),
                    "unit_cost", line.buyingPrice(),
                    "total_cost", line.costTotal(),
                    "unit_price", line.unitPrice(),
                    "line_gross", gross,
                    "tax_amount", tax,
                    "discount_amount", discount,
                    "net_amount", gross.subtract(discount),
                    "buying_price", line.buyingPrice(),
                    "cost_snapshot", line.costTotal(),
                    "trace_notes", sale.notes());
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryReturnTrace(long returnId) {
        ReturnRec ret = returns.get(returnId);
        return returnLines.stream().filter(l -> l.returnId() == returnId).sorted(Comparator.comparing(ReturnLineRec::id)).map(line -> {
            ProductRec p = products.get(line.productId());
            LotRec lot = lots.get(line.lotId());
            BigDecimal gross = line.qty().multiply(line.unitPriceRefund());
            return row(
                    "document_no", ret.returnNo(),
                    "product_id", p.id(),
                    "product_name", p.name(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", "",
                    "to_status", line.restockToStatus(),
                    "qty", line.qty(),
                    "unit_cost", line.buyingPrice(),
                    "total_cost", line.costTotalRefund(),
                    "unit_price", line.unitPriceRefund(),
                    "line_gross", gross,
                    "tax_amount", line.taxRefund(),
                    "discount_amount", line.discountRefund(),
                    "net_amount", gross.subtract(line.discountRefund()),
                    "buying_price", line.buyingPrice(),
                    "cost_snapshot", line.costTotalRefund(),
                    "trace_notes", ret.notes());
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryGenericTrace(long txnId) {
        TxnRec txn = txns.get(txnId);
        return lines.stream().filter(line -> line.txnId() == txnId).sorted(Comparator.comparing(LineRec::id)).map(line -> {
            ProductRec p = products.get(line.productId());
            LotRec lot = lots.get(line.lotId());
            return row(
                    "document_no", "TXN-" + txn.id(),
                    "product_id", p.id(),
                    "product_name", p.name(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", blank(line.fromStatus()),
                    "to_status", blank(line.toStatus()),
                    "qty", line.qtyInBase(),
                    "unit_cost", line.unitCost(),
                    "total_cost", line.totalCost(),
                    "unit_price", null,
                    "line_gross", null,
                    "tax_amount", null,
                    "discount_amount", null,
                    "net_amount", null,
                    "buying_price", line.unitCost(),
                    "cost_snapshot", line.totalCost(),
                    "trace_notes", blank(txn.notes()));
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryInventoryTraceLines(long txnId) {
        TxnRec txn = txns.get(txnId);
        return lines.stream().filter(line -> line.txnId() == txnId).sorted(Comparator.comparing(LineRec::id)).map(line -> {
            LotRec lot = lots.get(line.lotId());
            return row(
                    "id", line.id(),
                    "product_id", line.productId(),
                    "lot_no", lot == null ? "" : lot.lotNo(),
                    "from_status", blank(line.fromStatus()),
                    "to_status", blank(line.toStatus()),
                    "qty_in_base", line.qtyInBase(),
                    "store_id", txn.storeId(),
                    "created_at", txn.createdAt(),
                    "txn_id", txn.id());
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> queryBeforeQty(Map<Integer, Object> params) {
        String trackedStatus = (String) params.get(1);
        int storeId = (Integer) params.get(3);
        long productId = (Long) params.get(4);
        Timestamp createdAt = (Timestamp) params.get(5);
        long txnId = (Long) params.get(7);
        long lineId = (Long) params.get(9);

        BigDecimal qtyBefore = BigDecimal.ZERO;
        for (LineRec line : lines) {
            TxnRec txn = txns.get(line.txnId());
            if (txn.storeId() != storeId || line.productId() != productId) continue;
            boolean earlier = txn.createdAt().before(createdAt)
                    || (txn.createdAt().equals(createdAt)
                    && (txn.id() < txnId || (txn.id() == txnId && line.id() < lineId)));
            if (!earlier) continue;
            if (trackedStatus.equals(blank(line.toStatus()))) qtyBefore = qtyBefore.add(line.qtyInBase());
            if (trackedStatus.equals(blank(line.fromStatus()))) qtyBefore = qtyBefore.subtract(line.qtyInBase());
        }
        return List.of(row("qty_before", qtyBefore));
    }

    private Map<String, Object> txnRow(TxnRec txn) {
        return row(
                "id", txn.id(),
                "store_id", txn.storeId(),
                "txn_type", txn.txnType(),
                "ref_no", txn.refNo(),
                "sale_id", txn.saleId(),
                "sales_return_id", txn.salesReturnId(),
                "purchase_receipt_id", txn.purchaseReceiptId(),
                "notes", blank(txn.notes()),
                "created_by", txn.createdBy(),
                "created_by_name", users.get(txn.createdBy()),
                "created_at", txn.createdAt());
    }

    private Map<String, Object> row(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private static BigDecimal bd(String value) { return new BigDecimal(value); }
    private static String blank(String value) { return value == null ? "" : value; }
    private static String upper(String value) { return blank(value).toUpperCase(); }

    private record ProductRec(long id, String sku, String name) {}
    private record LotRec(long id, long productId, String lotNo) {}
    private record PurchaseReceiptRec(long id, String receiptNo, Timestamp receivedAt, long supplierId, String notes) {}
    private record PurchaseReceiptLineRec(long id, long purchaseReceiptId, long productId, Long lotId, BigDecimal qty, BigDecimal unitCost, BigDecimal totalCost) {}
    private record SaleRec(long id, String saleNo, Timestamp soldAt, long customerId, String notes) {}
    private record SaleLineRec(long id, long saleId, long productId, Long lotId, BigDecimal qty, BigDecimal unitPrice, BigDecimal buyingPrice, BigDecimal costTotal) {}
    private record SaleTaxRec(long saleLineId, BigDecimal amount) {}
    private record SaleDiscountRec(long saleId, Long saleLineId, BigDecimal amount) {}
    private record SalePaymentRec(long saleId, BigDecimal amount) {}
    private record ReturnRec(long id, String returnNo, Timestamp returnedAt, long customerId, String notes) {}
    private record ReturnLineRec(long id, long returnId, long productId, Long lotId, BigDecimal qty, BigDecimal unitPriceRefund, BigDecimal buyingPrice, BigDecimal costTotalRefund, BigDecimal taxRefund, BigDecimal discountRefund, String restockToStatus) {}
    private record ReturnRefundRec(long returnId, BigDecimal amount) {}
    private record TxnRec(long id, int storeId, String txnType, String refNo, Long saleId, Long salesReturnId, Long purchaseReceiptId, String notes, int createdBy, Timestamp createdAt) {}
    private record LineRec(long id, long txnId, long productId, Long lotId, String fromStatus, String toStatus, BigDecimal qtyInBase, BigDecimal unitCost, BigDecimal totalCost) {}
}
