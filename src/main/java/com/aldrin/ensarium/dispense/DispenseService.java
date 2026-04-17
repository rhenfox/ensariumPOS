package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.AppConfig;
import com.aldrin.ensarium.db.Db;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.aldrin.ensarium.security.Session;
import java.util.Objects;

public class DispenseService {

    private final ProductDao productDao = new ProductDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final HeldSaleStore heldSaleStore = new HeldSaleStore();

    private final List<CartLine> lines = new ArrayList<>();
    private Long customerId;
    private CustomerRef selectedCustomer;
    private BigDecimal automaticDiscountRate = BigDecimal.ZERO;
    private String automaticDiscountName;
    private SaleDiscountInfo saleDiscount;
    private Long editingSaleId;
    private String editingSaleNo;

//    private final int storeId = AppConfig.getInt("app.storeId", 1);
//    private final int terminalId = AppConfig.getInt("app.terminalId", 1);
//    private final int userId = AppConfig.getInt("app.userId", 1);
//    private final boolean allowChangeOfMindReturns = AppConfig.getBool("app.return.allowChangeOfMind", false);
    private final Session session;
    private final boolean allowChangeOfMindReturns = AppConfig.getBool("app.return.allowChangeOfMind", false);

    public DispenseService(Session session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    private int currentStoreId() {
        return session.getStoreId() > 0 ? session.getStoreId() : AppConfig.getInt("app.storeId", 1);
    }

    private int currentTerminalId() {
        return session.getTerminalId() > 0 ? session.getTerminalId() : AppConfig.getInt("app.terminalId", 1);
    }

    private int currentUserId() {
        int id = session.userId();
        if (id <= 0) {
            throw new IllegalStateException("No logged-in user found in session.");
        }
        return id;
    }
    
    
    
    

    public List<CartLine> lines() {
        return lines;
    }

    public Long customerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) throws Exception {
        this.customerId = customerId;
        refreshSelectedCustomer();
    }

    public CustomerRef selectedCustomer() {
        return selectedCustomer;
    }

    public SaleDiscountInfo saleDiscount() {
        return saleDiscount;
    }

    public void setSaleDiscount(SaleDiscountInfo saleDiscount) {
        this.saleDiscount = saleDiscount;
    }

    public void clearSaleDiscount() {
        this.saleDiscount = null;
    }

    public boolean isEditingSale() {
        return editingSaleId != null;
    }

    public String editingSaleNo() {
        return editingSaleNo;
    }

    public void addByBarcode(String barcode) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            ProductOption p = productDao.findByBarcode(conn, barcode);
            conn.commit();
            if (p == null) {
                throw new IllegalArgumentException("Barcode not found: " + barcode);
            }
            addByProductUom(p, BigDecimal.ONE);
        }
    }

    public void addByProductUom(ProductOption product, BigDecimal qty) throws Exception {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        for (int i = 0; i < lines.size(); i++) {
            CartLine line = lines.get(i);
            if (line.productId == product.productId && eq(line.unitPrice, product.unitPrice)) {
                BigDecimal newQty = line.qtyUom.add(qty);
                ensureAvailableQty(product.productId, newQty, i, product.productName);
                line.qtyUom = newQty;
                recomputeLineCost(line);
                applyBenefitRuleToLine(line);
                return;
            }
        }
        ensureAvailableQty(product.productId, qty, null, product.productName);
        CartLine l = new CartLine();
        l.productId = product.productId;
        l.barcode = product.barcode;
        l.sku = product.sku;
        l.productName = product.productName;
        l.uomCode = product.uomCode;
        l.unitPrice = product.unitPrice;
        l.buyingPrice = Money.safe(product.buyingPrice);
        l.taxRate = product.taxRate;
        l.priceIncludesTax = product.priceIncludesTax;
        l.taxId = product.taxId;
        l.qtyUom = qty;
        recomputeLineCost(l);
        applyBenefitRuleToLine(l);
        lines.add(l);
    }

    public void setQtyAt(int index, BigDecimal qty) throws Exception {
        CartLine line = lines.get(index);
        ensureAvailableQty(line.productId, qty, index, line.productName);
        line.qtyUom = qty;
        recomputeLineCost(line);
    }

    public void deleteAt(int index) {
        lines.remove(index);
    }

    private void recomputeLineCost(CartLine line) {
        if (line == null) {
            return;
        }
        BigDecimal buyingPrice = Money.safe(line.buyingPrice);
        BigDecimal qty = line.qtyUom == null ? BigDecimal.ZERO : line.qtyUom;
        line.buyingPrice = buyingPrice;
        line.costTotal = scale(buyingPrice.multiply(qty));
    }

    public CartTotals computeTotals() {
        CartTotals t = new CartTotals();
        for (CartLine line : lines) {
            BigDecimal transactionGross = transactionGross(line);
            BigDecimal lineTax = effectiveLineTax(line);
            BigDecimal lineDiscount = automaticLineDiscount(line);

            t.grossSales = t.grossSales.add(transactionGross);
            t.subtotal = t.subtotal.add(transactionGross);
            t.lineDiscounts = t.lineDiscounts.add(lineDiscount);
            t.tax = t.tax.add(lineTax);

            if (lineBenefitVatExempt(line)) {
                t.vatExemptSales = t.vatExemptSales.add(transactionGross);
            } else if (line.taxRate != null && line.taxRate.compareTo(BigDecimal.ZERO) > 0) {
                t.vatableSales = t.vatableSales.add(taxableBase(line));
            } else {
                t.zeroRatedSales = t.zeroRatedSales.add(transactionGross);
            }

            if (lineDiscount.compareTo(BigDecimal.ZERO) > 0) {
                t.benefitEligibleAmount = t.benefitEligibleAmount.add(transactionGross);
                t.benefitDiscountAmount = t.benefitDiscountAmount.add(lineDiscount);
                if (lineBenefitVatExempt(line)) {
                    t.benefitVatExemptAmount = t.benefitVatExemptAmount.add(transactionGross);
                }
            }
        }
        t.grossSales = scale(t.grossSales);
        t.subtotal = scale(t.subtotal);
        t.lineDiscounts = scale(t.lineDiscounts);
        t.tax = scale(t.tax);
        t.vatableSales = scale(t.vatableSales);
        t.vatExemptSales = scale(t.vatExemptSales);
        t.zeroRatedSales = scale(t.zeroRatedSales);
        t.benefitEligibleAmount = scale(t.benefitEligibleAmount);
        t.benefitVatExemptAmount = scale(t.benefitVatExemptAmount);
        t.benefitDiscountAmount = scale(t.benefitDiscountAmount);
        BigDecimal afterLineDiscounts = scale(t.subtotal.subtract(t.lineDiscounts).max(BigDecimal.ZERO));
        t.saleDiscount = computeSaleDiscountAmount(afterLineDiscounts);
        t.grandTotal = scale(afterLineDiscounts.subtract(t.saleDiscount).max(BigDecimal.ZERO));
        return t;
    }

    public long holdCurrent() throws Exception {
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }
        HeldSale hs = new HeldSale();
        hs.heldAt = LocalDateTime.now();
        hs.customerId = customerId;
        hs.saleDiscount = saleDiscount == null ? null : saleDiscount.copy();
        hs.lines = new ArrayList<>();
        for (CartLine line : lines) {
            hs.lines.add(line.copy());
        }
        if (customerId != null) {
            try (Connection conn = Db.getConnection()) {
                conn.setAutoCommit(false);
                CustomerRef cr = customerDao.findById(conn, customerId);
                conn.commit();
                hs.customerName = cr == null ? null : cr.fullName;
            }
        }
        long id = heldSaleStore.save(hs);
        resetNewSale();
        return id;
    }

    public List<HeldSale> listHolds() throws Exception {
        return heldSaleStore.list();
    }

    public void loadHold(long holdId) throws Exception {
        HeldSale hs = heldSaleStore.take(holdId);
        if (hs == null) {
            throw new IllegalArgumentException("Held ticket not found.");
        }
        resetNewSale();
        for (CartLine line : hs.lines) {
            lines.add(line.copy());
        }
        customerId = hs.customerId;
        saleDiscount = hs.saleDiscount;
        refreshSelectedCustomer();
    }

    public void resetNewSale() {
        lines.clear();
        customerId = null;
        selectedCustomer = null;
        automaticDiscountRate = BigDecimal.ZERO;
        automaticDiscountName = null;
        saleDiscount = null;
        editingSaleId = null;
        editingSaleNo = null;
    }

    public PayResult pay(BigDecimal amountRendered, int methodId, String referenceNo) throws Exception {
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }
        validateWholeCartStock();
        CartTotals totals = computeTotals();
        if (amountRendered.compareTo(totals.grandTotal) < 0) {
            throw new IllegalArgumentException("Rendered amount is less than total due.");
        }
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long shiftId = ensureOpenShift(conn);
                long saleId = upsertSale(conn, shiftId);
                List<Long> saleLineIds = recreateSaleLines(conn, saleId);
                recreateSaleDiscount(conn, saleId, saleLineIds, totals);
                recreateSalePayment(conn, saleId, methodId, amountRendered, referenceNo);
                rebuildSaleInvoice(conn, saleId, totals, saleLineIds);
                postSaleInventory(conn, saleId, shiftId);
                String saleNo = currentSaleNo(conn, saleId);
                conn.commit();

                PayResult pr = new PayResult();
                pr.saleId = saleId;
                pr.saleNo = saleNo;
                pr.total = totals.grandTotal;
                pr.cash = amountRendered;
                pr.change = scale(amountRendered.subtract(totals.grandTotal));
                resetNewSale();
                return pr;
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
    }

    public List<SaleHistoryRow> findHistory(String keyword) throws Exception {
        return findHistory(keyword, null, null, 200);
    }

    public List<SaleHistoryRow> findHistory(String keyword, Timestamp fromSoldAt, Timestamp toSoldAtExclusive, int limit) throws Exception {
        String like = "%" + (keyword == null ? "" : keyword.toUpperCase()) + "%";
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        List<SaleHistoryRow> out = new ArrayList<>();
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            String sql = """
                SELECT s.id, s.sale_no, c.full_name, s.sold_at,
                       COALESCE(v.total_due, 0) AS total_due,
                       (SELECT si.invoice_no FROM sale_invoice si WHERE si.sale_id = s.id FETCH FIRST ROW ONLY) AS invoice_no,
                       (SELECT COUNT(*) FROM sales_return sr WHERE sr.sale_id = s.id) AS return_count,
                       (SELECT COUNT(*) FROM sale_payment sp WHERE sp.sale_id = s.id) AS payment_count,
                       (SELECT COUNT(*) FROM sale_invoice si WHERE si.sale_id = s.id) AS invoice_count
                  FROM sale s
                  LEFT JOIN customer c ON c.id = s.customer_id
                  LEFT JOIN v_sale_totals v ON v.sale_id = s.id
                 WHERE s.status = 'POSTED'
                   AND (UPPER(COALESCE(s.sale_no,'')) LIKE ? OR UPPER(COALESCE(c.full_name,'')) LIKE ?)
                   AND (? IS NULL OR s.sold_at >= ?)
                   AND (? IS NULL OR s.sold_at < ?)
                 ORDER BY s.sold_at DESC
            FETCH FIRST __LIMIT__ ROWS ONLY
                """.replace("__LIMIT__", String.valueOf(safeLimit));
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, like);
                ps.setString(2, like);
                if (fromSoldAt == null) {
                    ps.setNull(3, java.sql.Types.TIMESTAMP);
                    ps.setNull(4, java.sql.Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(3, fromSoldAt);
                    ps.setTimestamp(4, fromSoldAt);
                }
                if (toSoldAtExclusive == null) {
                    ps.setNull(5, java.sql.Types.TIMESTAMP);
                    ps.setNull(6, java.sql.Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(5, toSoldAtExclusive);
                    ps.setTimestamp(6, toSoldAtExclusive);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int paymentCount = rs.getInt("payment_count");
                        int invoiceCount = rs.getInt("invoice_count");
                        int returnCount = rs.getInt("return_count");
                        SaleHistoryRow r = new SaleHistoryRow();
                        r.saleId = rs.getLong("id");
                        r.saleNo = rs.getString("sale_no");
                        r.customerName = rs.getString("full_name");
                        r.soldAt = rs.getTimestamp("sold_at");
                        r.total = rs.getBigDecimal("total_due");
                        r.invoiceNo = rs.getString("invoice_no");
                        r.returnCount = returnCount;
                        r.canEdit = paymentCount == 0 && invoiceCount == 0 && returnCount == 0;
                        r.editStatus = buildSaleEditRestriction(paymentCount, invoiceCount, returnCount);
                        out.add(r);
                    }
                }
            }
            conn.commit();
        }
        return out;
    }

    public SaleDetailView getSaleDetail(long saleId) throws Exception {
        SaleDetailView detail = new SaleDetailView();
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String headerSql = """
                    SELECT s.id,
                           s.sale_no,
                           s.sold_at,
                           COALESCE(c.full_name, 'Walk-in') AS customer_name,
                           COALESCE(u.username, '') AS cashier_username,
                           COALESCE(s.notes, '') AS notes,
                           COALESCE(v.total_due, 0) AS total_due,
                           (SELECT si.invoice_no FROM sale_invoice si WHERE si.sale_id = s.id FETCH FIRST ROW ONLY) AS invoice_no,
                           COALESCE((SELECT COUNT(*) FROM sale_line sl WHERE sl.sale_id = s.id), 0) AS item_count
                      FROM sale s
                      LEFT JOIN customer c ON c.id = s.customer_id
                      LEFT JOIN users u ON u.id = s.cashier_user_id
                      LEFT JOIN v_sale_totals v ON v.sale_id = s.id
                     WHERE s.id = ?
                    """;
                try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                    ps.setLong(1, saleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.commit();
                            throw new IllegalArgumentException("Sale not found.");
                        }
                        detail.saleId = rs.getLong("id");
                        detail.saleNo = rs.getString("sale_no");
                        detail.soldAt = rs.getTimestamp("sold_at");
                        detail.customerName = rs.getString("customer_name");
                        detail.cashierUsername = rs.getString("cashier_username");
                        detail.notes = rs.getString("notes");
                        detail.total = rs.getBigDecimal("total_due");
                        detail.invoiceNo = rs.getString("invoice_no");
                        detail.itemCount = rs.getInt("item_count");
                    }
                }

                String lineSql = """
                    SELECT sl.line_no,
                           COALESCE(p.name, 'Item') AS product_name,
                           COALESCE(p.sku, '') AS sku,
                           COALESCE(pb.barcode, '') AS barcode,
                           sl.qty_in_base,
                           sl.unit_price,
                           (sl.qty_in_base * sl.unit_price) AS line_total
                      FROM sale_line sl
                      LEFT JOIN product p ON p.id = sl.product_id
                      LEFT JOIN product_barcode pb ON pb.product_id = p.id AND pb.is_primary = 1
                     WHERE sl.sale_id = ?
                     ORDER BY sl.line_no
                    """;
                try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                    ps.setLong(1, saleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            SaleDetailLine line = new SaleDetailLine();
                            line.lineNo = rs.getInt("line_no");
                            line.productName = rs.getString("product_name");
                            line.sku = rs.getString("sku");
                            line.barcode = rs.getString("barcode");
                            line.qty = rs.getBigDecimal("qty_in_base");
                            line.unitPrice = rs.getBigDecimal("unit_price");
                            line.lineTotal = rs.getBigDecimal("line_total");
                            detail.lines.add(line);
                        }
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                Db.rollbackQuietly(conn);
                throw ex;
            }
        }
        return detail;
    }

    public void loadSaleForEdit(long saleId) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            SaleEditState editState = loadSaleEditState(conn, saleId);
            if (editState == null) {
                conn.commit();
                throw new IllegalArgumentException("Sale not found.");
            }
            if (!editState.canEdit()) {
                conn.commit();
                throw new IllegalStateException(editState.blockReason());
            }
            resetNewSale();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT sale_no, customer_id FROM sale WHERE id = ?")) {
                ps.setLong(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Sale not found.");
                    }
                    editingSaleId = saleId;
                    editingSaleNo = rs.getString("sale_no");
                    long cid = rs.getLong("customer_id");
                    customerId = rs.wasNull() ? null : cid;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT sl.product_id, sl.lot_id, sl.qty_in_base, sl.unit_price, sl.tax_rate, sl.price_includes_tax, sl.buying_price, sl.cost_total, p.name, p.sku, u.code AS uom_code, pb.barcode, COALESCE(slt.tax_id,0) AS tax_id FROM sale_line sl JOIN product p ON p.id = sl.product_id JOIN uom u ON u.id = p.base_uom_id LEFT JOIN product_barcode pb ON pb.product_id = p.id AND pb.is_primary = 1 LEFT JOIN sale_line_tax slt ON slt.sale_line_id = sl.id WHERE sl.sale_id = ? ORDER BY sl.line_no")) {
                ps.setLong(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CartLine l = new CartLine();
                        l.productId = rs.getLong("product_id");
                        long lot = rs.getLong("lot_id");
                        l.lotId = rs.wasNull() ? null : lot;
                        l.qtyUom = rs.getBigDecimal("qty_in_base");
                        l.unitPrice = rs.getBigDecimal("unit_price");
                        l.taxRate = rs.getBigDecimal("tax_rate");
                        l.buyingPrice = Money.safe(rs.getBigDecimal("buying_price"));
                        l.priceIncludesTax = rs.getInt("price_includes_tax") == 1;
                        l.costTotal = Money.safe(rs.getBigDecimal("cost_total"));
                        if (l.costTotal.compareTo(BigDecimal.ZERO) <= 0 && l.buyingPrice.compareTo(BigDecimal.ZERO) > 0) {
                            recomputeLineCost(l);
                        }
                        l.productName = rs.getString("name");
                        l.sku = rs.getString("sku");
                        l.uomCode = rs.getString("uom_code");
                        l.barcode = rs.getString("barcode");
                        l.taxId = rs.getLong("tax_id");
                        lines.add(l);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT discount_type_id, kind, value, discount_name FROM sale_discount WHERE sale_id = ? AND sale_line_id IS NULL FETCH FIRST ROW ONLY")) {
                ps.setLong(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        SaleDiscountInfo di = new SaleDiscountInfo();
                        int discountTypeId = rs.getInt("discount_type_id");
                        di.discountTypeId = rs.wasNull() ? null : discountTypeId;
                        di.kind = rs.getString("kind");
                        di.value = rs.getBigDecimal("value");
                        di.name = rs.getString("discount_name");
                        saleDiscount = di;
                    }
                }
            }
            conn.commit();
        }
        refreshSelectedCustomer();
    }

    public List<ReturnLineInput> loadReturnableLines(String saleOrInvoiceNumber) throws Exception {
        if (saleOrInvoiceNumber == null || saleOrInvoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Enter the Sale No. or Invoice No.");
        }
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ResolvedSaleRef saleRef = resolveReturnSale(conn, saleOrInvoiceNumber.trim());
                List<ReturnableLineState> states = loadReturnableLineStates(conn, saleRef.saleId);
                conn.commit();
                List<ReturnLineInput> out = new ArrayList<>();
                for (ReturnableLineState st : states) {
                    if (st.remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    ReturnLineInput r = new ReturnLineInput();
                    r.saleLineId = st.saleLineId;
                    r.productId = st.productId;
                    r.lotId = st.lotId;
                    r.productName = st.productName;
                    r.uomCode = st.uomCode;
                    r.soldQty = st.remainingQty;
                    r.unitPrice = scale(st.unitPrice);
                    r.taxAmount = scale(st.returnableTaxAmount);
                    r.discountAmount = scale(st.returnableDiscountAmount);
                    r.refundableAmount = scale(st.remainingRefundableAmount);
                    r.saleReference = saleRef.saleNo;
                    r.invoiceReference = saleRef.invoiceNo;
                    r.returnQty = BigDecimal.ZERO;
                    out.add(r);
                }
                if (out.isEmpty()) {
                    throw new IllegalStateException("No remaining returnable items for this sale.");
                }
                return out;
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
    }

    public ReturnResult processReturnBySaleNumber(String saleNumber, List<ReturnLineInput> selectedLines,
            int reasonId, String restockStatus,
            int refundMethodId, String refundReferenceNo) throws Exception {
        if (selectedLines == null || selectedLines.isEmpty()) {
            throw new IllegalArgumentException("No return lines selected.");
        }
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ResolvedSaleRef saleRef = resolveReturnSale(conn, saleNumber);
                validateReturnReason(conn, reasonId);
                long shiftId = ensureOpenShift(conn);
                List<ReturnableLineState> states = loadReturnableLineStates(conn, saleRef.saleId);
                Map<Long, ReturnableLineState> bySaleLineId = new HashMap<>();
                for (ReturnableLineState st : states) {
                    bySaleLineId.put(st.saleLineId, st);
                }

                BigDecimal totalRefund = BigDecimal.ZERO;
                List<PostableReturnLine> posting = new ArrayList<>();
                for (ReturnLineInput rl : selectedLines) {
                    if (rl == null || rl.returnQty == null || rl.returnQty.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    ReturnableLineState st = bySaleLineId.get(rl.saleLineId);
                    if (st == null) {
                        throw new IllegalArgumentException("A selected return line no longer matches the original sale.");
                    }
                    BigDecimal qty = scaleQty(rl.returnQty);
                    if (qty.compareTo(st.remainingQty) > 0) {
                        throw new IllegalArgumentException("Return quantity for " + st.productName + " exceeds the remaining returnable quantity of " + st.remainingQty.toPlainString() + ".");
                    }
                    if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Return quantity must be greater than zero.");
                    }
                    BigDecimal ratio = qty.divide(st.originalQty, 12, RoundingMode.HALF_UP);
                    BigDecimal lineTaxRefund = scale(st.remainingQty.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : st.returnableTaxAmount.multiply(qty).divide(st.remainingQty, 6, RoundingMode.HALF_UP));
                    BigDecimal lineDiscountRefund = scale(st.remainingQty.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : st.returnableDiscountAmount.multiply(qty).divide(st.remainingQty, 6, RoundingMode.HALF_UP));
                    BigDecimal grossShare = scale(st.unitPrice.multiply(qty));
                    BigDecimal lineTotalRefund = st.priceIncludesTax
                            ? scale(grossShare.subtract(lineDiscountRefund).max(BigDecimal.ZERO))
                            : scale(grossShare.add(lineTaxRefund).subtract(lineDiscountRefund).max(BigDecimal.ZERO));

                    PostableReturnLine pr = new PostableReturnLine();
                    pr.state = st;
                    pr.returnQty = qty;
                    pr.unitPriceRefund = scale(st.unitPrice);
                    pr.taxRefund = st.priceIncludesTax ? BigDecimal.ZERO : lineTaxRefund;
                    pr.discountRefund = lineDiscountRefund;
                    pr.buyingPrice = Money.safe(st.buyingPrice);
                    pr.costTotalRefund = st.remainingQty.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : scale(st.remainingCostAmount.multiply(qty).divide(st.remainingQty, 6, RoundingMode.HALF_UP));
                    pr.totalRefund = lineTotalRefund;
                    posting.add(pr);
                    totalRefund = totalRefund.add(lineTotalRefund);
                }
                if (posting.isEmpty()) {
                    throw new IllegalArgumentException("Enter at least one return quantity.");
                }

                long returnId;
                String returnNo = nextReturnNo();
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sales_return(store_id, terminal_id, shift_id, return_no, status, returned_at, returned_by, sale_id, customer_id, notes) VALUES (?, ?, ?, ?, 'POSTED', CURRENT_TIMESTAMP, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentStoreId());
                    ps.setInt(2, currentTerminalId());
                    ps.setLong(3, shiftId);
                    ps.setString(4, returnNo);
                    ps.setInt(5, currentUserId());
                    ps.setLong(6, saleRef.saleId);
                    if (saleRef.customerId == null) {
                        ps.setNull(7, java.sql.Types.BIGINT);
                    } else {
                        ps.setLong(7, saleRef.customerId);
                    }
                    ps.setString(8, buildReturnNote(saleRef));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        returnId = rs.getLong(1);
                    }
                }

                int lineNo = 1;
                for (PostableReturnLine pr : posting) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO sales_return_line(sales_return_id, line_no, sale_line_id, product_id, lot_id, qty_in_base, unit_price_refund, buying_price, cost_total_refund, tax_refund, discount_refund, reason_id, restock_to_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setLong(1, returnId);
                        ps.setInt(2, lineNo++);
                        ps.setLong(3, pr.state.saleLineId);
                        ps.setLong(4, pr.state.productId);
                        if (pr.state.lotId == null) {
                            ps.setNull(5, java.sql.Types.BIGINT);
                        } else {
                            ps.setLong(5, pr.state.lotId);
                        }
                        ps.setBigDecimal(6, pr.returnQty);
                        ps.setBigDecimal(7, pr.unitPriceRefund);
                        ps.setBigDecimal(8, pr.buyingPrice);
                        ps.setBigDecimal(9, pr.costTotalRefund);
                        ps.setBigDecimal(10, pr.taxRefund);
                        ps.setBigDecimal(11, pr.discountRefund);
                        ps.setInt(12, reasonId);
                        ps.setString(13, restockStatus);
                        ps.executeUpdate();
                    }
                    increaseStock(conn, pr.state.productId, restockStatus, pr.returnQty);
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sales_return_refund(sales_return_id, method_id, amount, reference_no, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
                    ps.setLong(1, returnId);
                    ps.setInt(2, refundMethodId);
                    ps.setBigDecimal(3, scale(totalRefund));
                    ps.setString(4, refundReferenceNo);
                    ps.executeUpdate();
                }

                long txnId = insertInventoryTxn(conn, "SALE_RETURN", returnNo, saleRef.saleId, returnId, null, "Return posted for " + saleRef.saleNo);
                for (PostableReturnLine pr : posting) {
                    insertInventoryTxnLine(conn, txnId, pr.state.productId, pr.state.lotId, null, restockStatus, pr.returnQty, pr.buyingPrice, pr.costTotalRefund);
                }

                conn.commit();
                ReturnResult rr = new ReturnResult();
                rr.returnId = returnId;
                rr.returnNumber = returnNo;
                rr.totalRefund = scale(totalRefund);
                return rr;
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
    }

    private ResolvedSaleRef resolveReturnSale(Connection conn, String saleOrInvoiceNumber) throws Exception {
        String token = saleOrInvoiceNumber == null ? "" : saleOrInvoiceNumber.trim();
        if (token.isBlank()) {
            throw new IllegalArgumentException("Enter the Sale No. or Invoice No.");
        }
        String sql = """
            SELECT s.id, s.sale_no, s.customer_id, s.status, si.invoice_no
              FROM sale s
              LEFT JOIN sale_invoice si ON si.sale_id = s.id
             WHERE s.sale_no = ? OR si.invoice_no = ?
             FETCH FIRST ROW ONLY
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Sale/Invoice number not found.");
                }
                String status = rs.getString("status");
                if (!"POSTED".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Only POSTED sales can be returned.");
                }
                ResolvedSaleRef ref = new ResolvedSaleRef();
                ref.saleId = rs.getLong("id");
                ref.saleNo = rs.getString("sale_no");
                ref.invoiceNo = rs.getString("invoice_no");
                long c = rs.getLong("customer_id");
                ref.customerId = rs.wasNull() ? null : c;
                return ref;
            }
        }
    }

    private void validateReturnReason(Connection conn, int reasonId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT code, name FROM return_reason WHERE id = ?")) {
            ps.setInt(1, reasonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Return reason not found.");
                }
                String code = rs.getString("code");
                if (!allowChangeOfMindReturns && "CHANGE".equalsIgnoreCase(code)) {
                    throw new IllegalStateException("'Change of Mind' returns are disabled in this build. By default, only defect/non-conformity returns are enabled. Set app.return.allowChangeOfMind=true in db.properties if your store policy allows discretionary returns.");
                }
            }
        }
    }

    private String buildReturnNote(ResolvedSaleRef saleRef) {
        if (saleRef == null) {
            return "Return";
        }
        if (saleRef.invoiceNo != null && !saleRef.invoiceNo.isBlank()) {
            return "Return for Sale " + saleRef.saleNo + " / Invoice " + saleRef.invoiceNo;
        }
        return "Return for Sale " + saleRef.saleNo;
    }

    private List<ReturnableLineState> loadReturnableLineStates(Connection conn, long saleId) throws Exception {
        List<ReturnableLineState> states = new ArrayList<>();
        BigDecimal saleHeaderDiscount = BigDecimal.ZERO;
        BigDecimal saleNetBeforeHeader = BigDecimal.ZERO;
        String sql = """
            SELECT sl.id AS sale_line_id,
                   sl.product_id,
                   sl.lot_id,
                   p.name,
                   u.code AS uom_code,
                   sl.qty_in_base,
                   sl.unit_price,
                   sl.price_includes_tax,
                   COALESCE(sl.buying_price, 0) AS buying_price,
                   COALESCE(sl.cost_total, 0) AS line_cost_total,
                   COALESCE((SELECT SUM(t.tax_amount) FROM sale_line_tax t WHERE t.sale_line_id = sl.id), 0) AS line_tax_total,
                   COALESCE((SELECT SUM(sd.amount) FROM sale_discount sd WHERE sd.sale_line_id = sl.id), 0) AS line_discount_total,
                   COALESCE((SELECT SUM(sd.amount) FROM sale_discount sd WHERE sd.sale_id = s.id AND sd.sale_line_id IS NULL), 0) AS sale_discount_total,
                   COALESCE((SELECT SUM(srl.qty_in_base) FROM sales_return sr JOIN sales_return_line srl ON srl.sales_return_id = sr.id WHERE sr.sale_id = s.id AND srl.sale_line_id = sl.id), 0) AS already_returned_qty,
                   COALESCE((SELECT SUM(srl.tax_refund) FROM sales_return sr JOIN sales_return_line srl ON srl.sales_return_id = sr.id WHERE sr.sale_id = s.id AND srl.sale_line_id = sl.id), 0) AS already_returned_tax,
                   COALESCE((SELECT SUM(srl.discount_refund) FROM sales_return sr JOIN sales_return_line srl ON srl.sales_return_id = sr.id WHERE sr.sale_id = s.id AND srl.sale_line_id = sl.id), 0) AS already_returned_discount,
                   COALESCE((SELECT SUM(srl.cost_total_refund) FROM sales_return sr JOIN sales_return_line srl ON srl.sales_return_id = sr.id WHERE sr.sale_id = s.id AND srl.sale_line_id = sl.id), 0) AS already_returned_cost
              FROM sale s
              JOIN sale_line sl ON sl.sale_id = s.id
              JOIN product p ON p.id = sl.product_id
              JOIN uom u ON u.id = p.base_uom_id
             WHERE s.id = ?
             ORDER BY sl.line_no
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnableLineState st = new ReturnableLineState();
                    st.saleLineId = rs.getLong("sale_line_id");
                    st.productId = rs.getLong("product_id");
                    long lot = rs.getLong("lot_id");
                    st.lotId = rs.wasNull() ? null : lot;
                    st.productName = rs.getString("name");
                    st.uomCode = rs.getString("uom_code");
                    st.originalQty = scaleQty(rs.getBigDecimal("qty_in_base"));
                    st.unitPrice = Money.safe(rs.getBigDecimal("unit_price"));
                    st.priceIncludesTax = rs.getInt("price_includes_tax") == 1;
                    st.buyingPrice = Money.safe(rs.getBigDecimal("buying_price"));
                    st.lineCostTotal = Money.safe(rs.getBigDecimal("line_cost_total"));
                    st.lineTaxTotal = Money.safe(rs.getBigDecimal("line_tax_total"));
                    st.lineDiscountTotal = Money.safe(rs.getBigDecimal("line_discount_total"));
                    saleHeaderDiscount = Money.safe(rs.getBigDecimal("sale_discount_total"));
                    st.alreadyReturnedQty = scaleQty(rs.getBigDecimal("already_returned_qty"));
                    st.alreadyReturnedTax = Money.safe(rs.getBigDecimal("already_returned_tax"));
                    st.alreadyReturnedDiscount = Money.safe(rs.getBigDecimal("already_returned_discount"));
                    st.alreadyReturnedCost = Money.safe(rs.getBigDecimal("already_returned_cost"));
                    st.lineGrossPaid = scale(st.unitPrice.multiply(st.originalQty));
                    st.linePaidBeforeHeader = st.priceIncludesTax
                            ? scale(st.lineGrossPaid.subtract(st.lineDiscountTotal).max(BigDecimal.ZERO))
                            : scale(st.lineGrossPaid.add(st.lineTaxTotal).subtract(st.lineDiscountTotal).max(BigDecimal.ZERO));
                    saleNetBeforeHeader = saleNetBeforeHeader.add(st.linePaidBeforeHeader);
                    states.add(st);
                }
            }
        }
        saleNetBeforeHeader = scale(saleNetBeforeHeader);
        for (ReturnableLineState st : states) {
            st.headerDiscountShare = saleNetBeforeHeader.compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ZERO
                    : scale(saleHeaderDiscount.multiply(st.linePaidBeforeHeader).divide(saleNetBeforeHeader, 6, RoundingMode.HALF_UP));
            st.totalDiscountForLine = scale(st.lineDiscountTotal.add(st.headerDiscountShare));
            st.remainingQty = scaleQty(st.originalQty.subtract(st.alreadyReturnedQty));
            st.remainingQty = st.remainingQty.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : st.remainingQty;
            st.returnableTaxAmount = scale(st.lineTaxTotal.subtract(st.alreadyReturnedTax).max(BigDecimal.ZERO));
            st.returnableDiscountAmount = scale(st.totalDiscountForLine.subtract(st.alreadyReturnedDiscount).max(BigDecimal.ZERO));
            st.remainingCostAmount = scale(st.lineCostTotal.subtract(st.alreadyReturnedCost).max(BigDecimal.ZERO));
            BigDecimal lineRefundableTotal = st.priceIncludesTax
                    ? scale(st.lineGrossPaid.subtract(st.totalDiscountForLine).max(BigDecimal.ZERO))
                    : scale(st.lineGrossPaid.add(st.lineTaxTotal).subtract(st.totalDiscountForLine).max(BigDecimal.ZERO));
            BigDecimal alreadyRefundedTotal = st.priceIncludesTax
                    ? scale(st.unitPrice.multiply(st.alreadyReturnedQty).subtract(st.alreadyReturnedDiscount).max(BigDecimal.ZERO))
                    : scale(st.unitPrice.multiply(st.alreadyReturnedQty).add(st.alreadyReturnedTax).subtract(st.alreadyReturnedDiscount).max(BigDecimal.ZERO));
            st.remainingRefundableAmount = scale(lineRefundableTotal.subtract(alreadyRefundedTotal).max(BigDecimal.ZERO));
        }
        return states;
    }

    private BigDecimal scaleQty(BigDecimal qty) {
        if (qty == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return qty.setScale(4, RoundingMode.HALF_UP);
    }

    private static class ResolvedSaleRef {

        long saleId;
        String saleNo;
        String invoiceNo;
        Long customerId;
    }

    private static class ReturnableLineState {

        long saleLineId;
        long productId;
        Long lotId;
        String productName;
        String uomCode;
        BigDecimal originalQty = BigDecimal.ZERO;
        BigDecimal remainingQty = BigDecimal.ZERO;
        BigDecimal unitPrice = BigDecimal.ZERO;
        BigDecimal buyingPrice = BigDecimal.ZERO;
        boolean priceIncludesTax;
        BigDecimal lineGrossPaid = BigDecimal.ZERO;
        BigDecimal lineCostTotal = BigDecimal.ZERO;
        BigDecimal lineTaxTotal = BigDecimal.ZERO;
        BigDecimal lineDiscountTotal = BigDecimal.ZERO;
        BigDecimal headerDiscountShare = BigDecimal.ZERO;
        BigDecimal totalDiscountForLine = BigDecimal.ZERO;
        BigDecimal linePaidBeforeHeader = BigDecimal.ZERO;
        BigDecimal alreadyReturnedQty = BigDecimal.ZERO;
        BigDecimal alreadyReturnedTax = BigDecimal.ZERO;
        BigDecimal alreadyReturnedDiscount = BigDecimal.ZERO;
        BigDecimal alreadyReturnedCost = BigDecimal.ZERO;
        BigDecimal returnableTaxAmount = BigDecimal.ZERO;
        BigDecimal returnableDiscountAmount = BigDecimal.ZERO;
        BigDecimal remainingRefundableAmount = BigDecimal.ZERO;
        BigDecimal remainingCostAmount = BigDecimal.ZERO;
    }

    private static class PostableReturnLine {

        ReturnableLineState state;
        BigDecimal returnQty = BigDecimal.ZERO;
        BigDecimal unitPriceRefund = BigDecimal.ZERO;
        BigDecimal buyingPrice = BigDecimal.ZERO;
        BigDecimal costTotalRefund = BigDecimal.ZERO;
        BigDecimal taxRefund = BigDecimal.ZERO;
        BigDecimal discountRefund = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
    }

    private SaleEditState loadSaleEditState(Connection conn, long saleId) throws Exception {
        String sql = """
            SELECT s.id,
                   (SELECT COUNT(*) FROM sale_payment sp WHERE sp.sale_id = s.id) AS payment_count,
                   (SELECT COUNT(*) FROM sale_invoice si WHERE si.sale_id = s.id) AS invoice_count,
                   (SELECT COUNT(*) FROM sales_return sr WHERE sr.sale_id = s.id) AS return_count
              FROM sale s
             WHERE s.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new SaleEditState(rs.getInt("payment_count"), rs.getInt("invoice_count"), rs.getInt("return_count"));
            }
        }
    }

    private String buildSaleEditRestriction(int paymentCount, int invoiceCount, int returnCount) {
        if (invoiceCount > 0) {
            return "LOCKED - INVOICED";
        }
        if (returnCount > 0) {
            return returnCount > 1 ? "LOCKED - RETURNS" : "LOCKED - RETURNED";
        }
        if (paymentCount > 0) {
            return "LOCKED - PAID";
        }
        return "EDITABLE";
    }

    private final class SaleEditState {

        private final int paymentCount;
        private final int invoiceCount;
        private final int returnCount;

        private SaleEditState(int paymentCount, int invoiceCount, int returnCount) {
            this.paymentCount = paymentCount;
            this.invoiceCount = invoiceCount;
            this.returnCount = returnCount;
        }

        private boolean canEdit() {
            return paymentCount == 0 && invoiceCount == 0 && returnCount == 0;
        }

        private String blockReason() {
            if (invoiceCount > 0) {
                return "This sale already has a Sales Invoice. Do not edit the dispense. Use Return, then create a new sale for replacement items.";
            }
            if (returnCount > 0) {
                return "This sale already has a return transaction. Do not edit the original dispense. Use a new sale instead.";
            }
            if (paymentCount > 0) {
                return "This sale is already paid. To preserve audit and inventory history, use Hold/Unhold before payment or use Return plus a new sale after payment.";
            }
            return "This sale cannot be edited.";
        }
    }

    private long upsertSale(Connection conn, long shiftId) throws Exception {
        String saleNo = editingSaleNo != null ? editingSaleNo : nextSaleNo();
        if (editingSaleId == null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sale(store_id, terminal_id, shift_id, sale_no, sale_type, status, customer_id, cashier_user_id, sold_at, notes) VALUES (?, ?, ?, ?, 'RETAIL', 'POSTED', ?, ?, CURRENT_TIMESTAMP, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, currentStoreId());
                ps.setInt(2, currentTerminalId());
                ps.setLong(3, shiftId);
                ps.setString(4, saleNo);
                if (customerId == null) {
                    ps.setNull(5, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(5, customerId);
                }
                ps.setInt(6, currentUserId());
                ps.setString(7, null);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE sale SET customer_id = ?, cashier_user_id = ?, sold_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            if (customerId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, customerId);
            }
            ps.setInt(2, currentUserId());
            ps.setLong(3, editingSaleId);
            ps.executeUpdate();
        }
        restoreSaleInventory(conn, editingSaleId);
        deleteSaleChildren(conn, editingSaleId);
        return editingSaleId;
    }

    private void restoreSaleInventory(Connection conn, long saleId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id, qty_in_base FROM sale_line WHERE sale_id = ?")) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    increaseStock(conn, rs.getLong(1), "ONHAND", rs.getBigDecimal(2));
                }
            }
        }
    }

    private void deleteSaleChildren(Connection conn, long saleId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_invoice_line WHERE sale_invoice_id IN (SELECT id FROM sale_invoice WHERE sale_id = ?)")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_invoice WHERE sale_id = ?")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_discount WHERE sale_id = ?")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_payment WHERE sale_id = ?")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_line_tax WHERE sale_line_id IN (SELECT id FROM sale_line WHERE sale_id = ?)")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_line WHERE sale_id = ?")) {
            ps.setLong(1, saleId);
            ps.executeUpdate();
        }
    }

    private List<Long> recreateSaleLines(Connection conn, long saleId) throws Exception {
        List<Long> saleLineIds = new ArrayList<>();
        int lineNo = 1;
        for (CartLine line : lines) {
            long saleLineId;
            BigDecimal saleUnitPrice = effectiveSaleUnitPrice(line);
            BigDecimal saleTaxRate = effectiveSaleTaxRate(line);
            boolean salePriceIncludesTax = effectiveSalePriceIncludesTax(line);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sale_line(sale_id, line_no, product_id, lot_id, qty_in_base, unit_price, price_includes_tax, tax_rate, buying_price, cost_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, saleId);
                ps.setInt(2, lineNo++);
                ps.setLong(3, line.productId);
                if (line.lotId == null) {
                    ps.setNull(4, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(4, line.lotId);
                }
                ps.setBigDecimal(5, line.qtyUom);
                ps.setBigDecimal(6, saleUnitPrice);
                ps.setInt(7, salePriceIncludesTax ? 1 : 0);
                ps.setBigDecimal(8, saleTaxRate);
                ps.setBigDecimal(9, Money.safe(line.buyingPrice));
                ps.setBigDecimal(10, scale(line.costTotal));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    saleLineId = rs.getLong(1);
                }
            }
            saleLineIds.add(saleLineId);
            BigDecimal tax = effectiveLineTax(line);
            if (tax.compareTo(BigDecimal.ZERO) > 0 || (!lineBenefitVatExempt(line) && line.taxId > 0 && saleTaxRate.compareTo(BigDecimal.ZERO) > 0)) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sale_line_tax(sale_line_id, tax_id, tax_code, tax_rate, tax_amount) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setLong(1, saleLineId);
                    if (line.taxId <= 0) {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        ps.setLong(2, line.taxId);
                    }
                    ps.setString(3, saleTaxRate.compareTo(BigDecimal.ZERO) > 0 ? "VAT" : "NO_TAX");
                    ps.setBigDecimal(4, saleTaxRate);
                    ps.setBigDecimal(5, tax);
                    ps.executeUpdate();
                }
            }
        }
        return saleLineIds;
    }

    private void recreateSaleDiscount(Connection conn, long saleId, List<Long> saleLineIds, CartTotals totals) throws Exception {
        for (int i = 0; i < lines.size() && i < saleLineIds.size(); i++) {
            CartLine line = lines.get(i);
            BigDecimal lineDiscountAmount = automaticLineDiscount(line);
            if (lineDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sale_discount(sale_id, sale_line_id, discount_type_id, discount_name, kind, value, amount, created_at) VALUES (?, ?, NULL, ?, 'PERCENT', ?, ?, CURRENT_TIMESTAMP)")) {
                ps.setLong(1, saleId);
                ps.setLong(2, saleLineIds.get(i));
                ps.setString(3, automaticDiscountName(line));
                ps.setBigDecimal(4, line.benefitDiscountRate == null ? BigDecimal.ZERO : line.benefitDiscountRate);
                ps.setBigDecimal(5, lineDiscountAmount);
                ps.executeUpdate();
            }
        }

        BigDecimal amount = totals.saleDiscount;
        SaleDiscountInfo effective = effectiveSaleDiscount();
        if (effective == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sale_discount(sale_id, sale_line_id, discount_type_id, discount_name, kind, value, amount, created_at) VALUES (?, NULL, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
            ps.setLong(1, saleId);
            if (effective.discountTypeId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, effective.discountTypeId);
            }
            ps.setString(3, effective.name == null ? "Discount" : effective.name);
            ps.setString(4, effective.kind);
            ps.setBigDecimal(5, effective.value);
            ps.setBigDecimal(6, amount);
            ps.executeUpdate();
        }
    }

    private void recreateSalePayment(Connection conn, long saleId, int methodId, BigDecimal amount, String referenceNo) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sale_payment(sale_id, method_id, amount, reference_no, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
            ps.setLong(1, saleId);
            ps.setInt(2, methodId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, referenceNo);
            ps.executeUpdate();
        }
    }

    private void rebuildSaleInvoice(Connection conn, long saleId, CartTotals totals, List<Long> saleLineIds) throws Exception {
        String q = """
            SELECT s.sale_no, sfp.branch_code, sfp.registered_business_address,
                   tp.registered_name, tp.trade_name, tp.tin_no, tp.vat_registration_type,
                   sfp.pos_vendor_name, sfp.pos_vendor_tin_no, sfp.pos_vendor_address,
                   sfp.supplier_accreditation_no, sfp.accreditation_issued_at, sfp.accreditation_valid_until,
                   sfp.bir_permit_to_use_no, sfp.permit_to_use_issued_at, sfp.atp_no, sfp.atp_issued_at,
                   t.code AS terminal_code, t.name AS terminal_name
              FROM sale s
              JOIN store_fiscal_profile sfp ON sfp.store_id = s.store_id
              JOIN taxpayer_profile tp ON tp.id = sfp.taxpayer_profile_id
              JOIN terminal t ON t.id = s.terminal_id
             WHERE s.id = ?
            """;
        long invoiceId;
        FiscalSeriesAllocation allocation = allocateFiscalSeries(conn);
        if (allocation == null) {
            return;
        }
        CustomerRef buyer = customerDao.findById(conn, customerId);
        BenefitSnapshot benefit = loadBenefitSnapshot(conn, customerId);
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO sale_invoice(sale_id, store_id, terminal_id, fiscal_series_id, invoice_no, serial_no, seller_registered_name, seller_trade_name, seller_tin_no, seller_branch_code, seller_business_address, seller_vat_registration_type, buyer_registered_name, buyer_tin_no, buyer_business_address, cash_sales, charge_sales, gross_sales, vatable_sales, vat_amount, vat_exempt_sales, zero_rated_sales, discount_total, withholding_tax_amount, total_amount_due, pos_vendor_name, pos_vendor_tin_no, pos_vendor_address, supplier_accreditation_no, accreditation_issued_at, accreditation_valid_until, bir_permit_to_use_no, permit_to_use_issued_at, atp_no, atp_issued_at, approved_series_from, approved_series_to, is_printed, reprint_count, voided, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, CURRENT_TIMESTAMP)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ins.setLong(1, saleId);
                    ins.setInt(2, currentStoreId());
                    ins.setInt(3, currentTerminalId());
                    ins.setLong(4, allocation.seriesId);
                    ins.setString(5, allocation.invoiceNo);
                    ins.setLong(6, allocation.serialNo);
                    ins.setString(7, rs.getString("registered_name"));
                    ins.setString(8, rs.getString("trade_name"));
                    ins.setString(9, rs.getString("tin_no"));
                    ins.setString(10, rs.getString("branch_code"));
                    ins.setString(11, rs.getString("registered_business_address"));
                    ins.setString(12, rs.getString("vat_registration_type"));
                    ins.setString(13, buyer == null ? null : buyer.fullName);
                    ins.setString(14, buyer == null ? null : buyer.tinNo);
                    ins.setString(15, buyer == null ? null : buyer.address);
                    ins.setBigDecimal(16, totals.grossSales);
                    ins.setBigDecimal(17, totals.vatableSales);
                    ins.setBigDecimal(18, totals.tax);
                    ins.setBigDecimal(19, totals.vatExemptSales);
                    ins.setBigDecimal(20, totals.zeroRatedSales);
                    ins.setBigDecimal(21, scale(totals.lineDiscounts.add(totals.saleDiscount)));
                    ins.setBigDecimal(22, totals.withholdingTaxAmount);
                    ins.setBigDecimal(23, totals.grandTotal);
                    ins.setString(24, rs.getString("pos_vendor_name"));
                    ins.setString(25, rs.getString("pos_vendor_tin_no"));
                    ins.setString(26, rs.getString("pos_vendor_address"));
                    ins.setString(27, rs.getString("supplier_accreditation_no"));
                    ins.setDate(28, rs.getDate("accreditation_issued_at"));
                    ins.setDate(29, rs.getDate("accreditation_valid_until"));
                    ins.setString(30, rs.getString("bir_permit_to_use_no"));
                    ins.setDate(31, rs.getDate("permit_to_use_issued_at"));
                    ins.setString(32, rs.getString("atp_no"));
                    ins.setDate(33, rs.getDate("atp_issued_at"));
                    ins.setLong(34, allocation.serialFrom);
                    ins.setLong(35, allocation.serialTo);
                    ins.executeUpdate();
                    try (ResultSet gk = ins.getGeneratedKeys()) {
                        gk.next();
                        invoiceId = gk.getLong(1);
                    }
                }
                int n = 1;
                for (int i = 0; i < lines.size(); i++) {
                    CartLine line = lines.get(i);
                    BigDecimal lineTransactionGross = transactionGross(line);
                    BigDecimal lineVat = effectiveLineTax(line);
                    BigDecimal lineDiscount = automaticLineDiscount(line);
                    BigDecimal lineNet = scale(lineTransactionGross.subtract(lineDiscount).max(BigDecimal.ZERO));
                    BigDecimal unitPriceForInvoice = scale(line.qtyUom.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : lineTransactionGross.divide(line.qtyUom, 6, RoundingMode.HALF_UP));
                    try (PreparedStatement ips = conn.prepareStatement(
                            "INSERT INTO sale_invoice_line(sale_invoice_id, sale_line_id, line_no, product_id, item_description, qty_in_base, unit_price, buying_price, cost_total, line_gross_amount, line_discount_amount, line_vat_amount, line_net_amount, tax_code, tax_rate, is_vat_exempt, is_zero_rated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ips.setLong(1, invoiceId);
                        ips.setLong(2, i < saleLineIds.size() ? saleLineIds.get(i) : 0L);
                        ips.setInt(3, n++);
                        ips.setLong(4, line.productId);
                        ips.setString(5, line.productName);
                        ips.setBigDecimal(6, line.qtyUom);
                        ips.setBigDecimal(7, unitPriceForInvoice);
                        ips.setBigDecimal(8, Money.safe(line.buyingPrice));
                        ips.setBigDecimal(9, scale(line.costTotal));
                        ips.setBigDecimal(10, lineTransactionGross);
                        ips.setBigDecimal(11, lineDiscount);
                        ips.setBigDecimal(12, lineVat);
                        ips.setBigDecimal(13, lineNet);
                        ips.setString(14, lineBenefitVatExempt(line) ? "VAT-EXEMPT" : (line.taxRate.compareTo(BigDecimal.ZERO) > 0 ? "VAT" : "NO_TAX"));
                        ips.setBigDecimal(15, lineBenefitVatExempt(line) ? BigDecimal.ZERO : line.taxRate);
                        ips.setInt(16, lineBenefitVatExempt(line) ? 1 : 0);
                        ips.setInt(17, (!lineBenefitVatExempt(line) && line.taxRate.compareTo(BigDecimal.ZERO) <= 0) ? 1 : 0);
                        ips.executeUpdate();
                    }
                }
                insertBenefitClaim(conn, invoiceId, totals, benefit);
            }
        }
    }

    private void postSaleInventory(Connection conn, long saleId, long shiftId) throws Exception {
        long txnId = insertInventoryTxn(conn, "SALE", currentSaleNo(conn, saleId), saleId, null, null, "Sale posted");
        for (CartLine line : lines) {
            reduceOnHand(conn, line.productId, line.qtyUom);
            insertInventoryTxnLine(conn, txnId, line.productId, line.lotId, "ONHAND", null, line.qtyUom, Money.safe(line.buyingPrice), scale(line.costTotal));
        }
    }

    private long insertInventoryTxn(Connection conn, String type, String refNo, Long saleId, Long salesReturnId, Long purchaseReceiptId, String notes) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO inventory_txn(store_id, txn_type, ref_no, sale_id, sales_return_id, purchase_receipt_id, notes, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, currentStoreId());
            ps.setString(2, type);
            ps.setString(3, refNo);
            if (saleId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, saleId);
            }
            if (salesReturnId == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, salesReturnId);
            }
            if (purchaseReceiptId == null) {
                ps.setNull(6, java.sql.Types.BIGINT);
            } else {
                ps.setLong(6, purchaseReceiptId);
            }
            ps.setString(7, notes);
            ps.setInt(8, currentUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void insertInventoryTxnLine(Connection conn, long txnId, long productId, Long lotId, String fromStatus, String toStatus,
            BigDecimal qty, BigDecimal unitCost, BigDecimal totalCost) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO inventory_txn_line(txn_id, product_id, lot_id, from_status, to_status, qty_in_base, unit_cost, total_cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, txnId);
            ps.setLong(2, productId);
            if (lotId == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, lotId);
            }
            if (fromStatus == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, fromStatus);
            }
            if (toStatus == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, toStatus);
            }
            ps.setBigDecimal(6, qty);
            ps.setBigDecimal(7, unitCost);
            ps.setBigDecimal(8, totalCost);
            ps.executeUpdate();
        }
    }

    private void reduceOnHand(Connection conn, long productId, BigDecimal qty) throws Exception {
        BigDecimal current = getStock(conn, productId, "ONHAND");
        if (current.compareTo(qty) < 0) {
            throw new IllegalStateException("Insufficient ONHAND stock for product ID " + productId + ". Available: " + current + ", Needed: " + qty);
        }
        setStock(conn, productId, "ONHAND", current.subtract(qty));
    }

    private void increaseStock(Connection conn, long productId, String statusCode, BigDecimal qty) throws Exception {
        BigDecimal current = getStock(conn, productId, statusCode);
        setStock(conn, productId, statusCode, current.add(qty));
    }

    private BigDecimal getStock(Connection conn, long productId, String statusCode) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT qty_in_base FROM stock_balance WHERE store_id = ? AND product_id = ? AND status_code = ?")) {
            ps.setInt(1, currentStoreId());
            ps.setLong(2, productId);
            ps.setString(3, statusCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private void setStock(Connection conn, long productId, String statusCode, BigDecimal qty) throws Exception {
        int updated;
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE stock_balance SET qty_in_base = ? WHERE store_id = ? AND product_id = ? AND status_code = ?")) {
            ps.setBigDecimal(1, scale(qty));
            ps.setInt(2, currentStoreId());
            ps.setLong(3, productId);
            ps.setString(4, statusCode);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO stock_balance(store_id, product_id, status_code, qty_in_base) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, currentStoreId());
                ps.setLong(2, productId);
                ps.setString(3, statusCode);
                ps.setBigDecimal(4, scale(qty));
                ps.executeUpdate();
            }
        }
    }

    public void assertCurrentUserOpenShift() throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ensureOpenShift(conn);
                conn.commit();
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
    }

    private Long findOpenShiftId(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM pos_shift WHERE store_id = ? AND terminal_id = ? AND opened_by = ? AND status = 'OPEN' ORDER BY opened_at DESC FETCH FIRST ROW ONLY")) {
            ps.setInt(1, currentStoreId());
            ps.setInt(2, currentTerminalId());
            ps.setInt(3, currentUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    private long ensureOpenShift(Connection conn) throws Exception {
        Long shiftId = findOpenShiftId(conn);
        if (shiftId != null) {
            return shiftId;
        }
        throw new IllegalStateException("No OPEN POS shift found for the current user on this terminal. Open your shift first before dispensing.");
    }

    private FiscalSeriesAllocation allocateFiscalSeries(Connection conn) throws Exception {
        String sql = "SELECT id, COALESCE(prefix, ''), serial_from, serial_to, next_serial FROM terminal_fiscal_series "
                + "WHERE terminal_id = ? AND active = 1 AND doc_type = 'INVOICE' ORDER BY created_at FETCH FIRST ROW ONLY";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentTerminalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long id = rs.getLong(1);
                String prefix = rs.getString(2);
                long serialFrom = rs.getLong(3);
                long serialTo = rs.getLong(4);
                long nextSerial = rs.getLong(5);
                if (nextSerial > serialTo) {
                    throw new IllegalStateException("No remaining approved invoice serial numbers for this terminal.");
                }
                try (PreparedStatement upd = conn.prepareStatement("UPDATE terminal_fiscal_series SET next_serial = ? WHERE id = ?")) {
                    upd.setLong(1, nextSerial + 1);
                    upd.setLong(2, id);
                    upd.executeUpdate();
                }
                FiscalSeriesAllocation a = new FiscalSeriesAllocation();
                a.seriesId = id;
                a.prefix = prefix == null ? "" : prefix;
                a.serialFrom = serialFrom;
                a.serialTo = serialTo;
                a.serialNo = nextSerial;
                a.invoiceNo = (a.prefix == null ? "" : a.prefix) + nextSerial;
                return a;
            }
        }
    }

    private BigDecimal saleBaseAmount(CartLine line, boolean vatExemptCustomer) {
        BigDecimal lineGross = gross(line);
        if (!vatExemptCustomer) {
            return lineGross;
        }
        if (line.taxRate == null || line.taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return lineGross;
        }
        if (!line.priceIncludesTax) {
            return lineGross;
        }
        BigDecimal divisor = BigDecimal.ONE.add(line.taxRate);
        return scale(lineGross.divide(divisor, 6, RoundingMode.HALF_UP));
    }

    private String currentBenefitType() {
        if (selectedCustomer == null) {
            return null;
        }
        if (selectedCustomer.senior) {
            return "SENIOR";
        }
        if (selectedCustomer.pwd) {
            return "PWD";
        }
        return null;
    }

    private void applyBenefitRulesToLines() throws Exception {
        for (CartLine line : lines) {
            applyBenefitRuleToLine(line);
        }
    }

    private void applyBenefitRuleToLine(CartLine line) throws Exception {
        line.benefitType = null;
        line.benefitMode = "NONE";
        line.benefitVatExempt = false;
        line.benefitDiscountRate = BigDecimal.ZERO;
        String benefitType = currentBenefitType();
        if (benefitType == null || line == null) {
            return;
        }
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ProductBenefitRule rule = productDao.findBenefitRule(conn, line.productId, benefitType);
                if (rule != null && rule.active && rule.benefitMode != null && !"NONE".equalsIgnoreCase(rule.benefitMode)) {
                    line.benefitType = benefitType;
                    line.benefitMode = rule.benefitMode;
                    line.benefitVatExempt = rule.vatExempt;
                    line.benefitDiscountRate = resolveBenefitRate(conn, benefitType, rule.benefitMode);
                }
                conn.commit();
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
    }

    private BigDecimal resolveBenefitRate(Connection conn, String benefitType, String benefitMode) throws Exception {
        if (benefitMode == null || "NONE".equalsIgnoreCase(benefitMode)) {
            return BigDecimal.ZERO;
        }
        if ("DISCOUNT_5_BNPC".equalsIgnoreCase(benefitMode)) {
            return new BigDecimal("0.05");
        }
        BigDecimal rate = customerDao.findBenefitRate(conn, customerId);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.20");
        }
        return rate;
    }

    private boolean lineBenefitVatExempt(CartLine line) {
        return line != null && line.benefitVatExempt;
    }

    private BigDecimal transactionGross(CartLine line) {
        return lineBenefitVatExempt(line) ? saleBaseAmount(line, true) : gross(line);
    }

    private BigDecimal taxableBase(CartLine line) {
        if (lineBenefitVatExempt(line)) {
            return BigDecimal.ZERO;
        }
        BigDecimal lineGross = gross(line);
        if (line.taxRate == null || line.taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return lineGross;
        }
        if (!line.priceIncludesTax) {
            return lineGross;
        }
        BigDecimal divisor = BigDecimal.ONE.add(line.taxRate);
        return scale(lineGross.divide(divisor, 6, RoundingMode.HALF_UP));
    }

    private BigDecimal effectiveLineTax(CartLine line) {
        return lineBenefitVatExempt(line) ? BigDecimal.ZERO : lineTax(line);
    }

    private BigDecimal automaticLineDiscount(CartLine line) {
        if (line == null || line.benefitDiscountRate == null || line.benefitDiscountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = lineBenefitVatExempt(line) ? saleBaseAmount(line, true) : transactionGross(line);
        return scale(base.multiply(line.benefitDiscountRate));
    }

    private BigDecimal effectiveSaleUnitPrice(CartLine line) {
        BigDecimal txGross = transactionGross(line);
        if (line.qtyUom == null || line.qtyUom.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return scale(txGross.divide(line.qtyUom, 6, RoundingMode.HALF_UP));
    }

    private BigDecimal effectiveSaleTaxRate(CartLine line) {
        return lineBenefitVatExempt(line) ? BigDecimal.ZERO : Money.safe(line.taxRate);
    }

    private boolean effectiveSalePriceIncludesTax(CartLine line) {
        return !lineBenefitVatExempt(line) && line.priceIncludesTax;
    }

    private String automaticDiscountName(CartLine line) {
        String type = line == null || line.benefitType == null ? "Benefit" : line.benefitType;
        if (line != null && "DISCOUNT_5_BNPC".equalsIgnoreCase(line.benefitMode)) {
            return type + " 5% BNPC Discount";
        }
        return type + " 20% Discount";
    }

    private BenefitSnapshot loadBenefitSnapshot(Connection conn, Long customerId) throws Exception {
        if (customerId == null) {
            return null;
        }
        CustomerRef c = customerDao.findById(conn, customerId);
        if (c == null || (!c.senior && !c.pwd)) {
            return null;
        }
        BenefitSnapshot b = new BenefitSnapshot();
        b.customerId = customerId;
        b.customerName = c.fullName;
        b.customerTin = c.tinNo;
        b.benefitType = c.senior ? "SENIOR" : "PWD";
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT gov_id_no FROM customer_benefit_profile WHERE customer_id = ? AND active = 1 AND benefit_type = ? ORDER BY created_at DESC FETCH FIRST ROW ONLY")) {
            ps.setLong(1, customerId);
            ps.setString(2, b.benefitType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    b.govIdNo = rs.getString(1);
                }
            }
        }
        if ((b.govIdNo == null || b.govIdNo.isBlank()) && "SENIOR".equals(b.benefitType)) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT senior_id_no FROM customer WHERE id = ?")) {
                ps.setLong(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        b.govIdNo = rs.getString(1);
                    }
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, default_rate FROM benefit_policy WHERE active = 1 AND benefit_type = ? AND effective_from <= CURRENT_DATE AND (effective_to IS NULL OR effective_to >= CURRENT_DATE) ORDER BY effective_from DESC FETCH FIRST ROW ONLY")) {
            ps.setString(1, b.benefitType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    b.policyId = rs.getInt(1);
                    b.appliedRate = rs.getBigDecimal(2);
                }
            }
        }
        return b.policyId > 0 ? b : null;
    }

    private void insertBenefitClaim(Connection conn, long saleInvoiceId, CartTotals totals, BenefitSnapshot benefit) throws Exception {
        if (benefit == null || benefit.appliedRate == null || benefit.appliedRate.compareTo(BigDecimal.ZERO) <= 0 || totals.benefitDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sale_benefit_claim(sale_invoice_id, sale_invoice_line_id, policy_id, customer_id, benefit_type, beneficiary_name, beneficiary_tin_no, gov_id_no, signature_name, gross_eligible_amount, vat_exempt_amount, applied_rate, discount_amount, override_reason, approved_by, created_at) VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, CURRENT_TIMESTAMP)")) {
            ps.setLong(1, saleInvoiceId);
            ps.setInt(2, benefit.policyId);
            ps.setLong(3, benefit.customerId);
            ps.setString(4, benefit.benefitType);
            ps.setString(5, benefit.customerName);
            ps.setString(6, benefit.customerTin);
            ps.setString(7, benefit.govIdNo);
            ps.setString(8, benefit.customerName);
            ps.setBigDecimal(9, totals.benefitEligibleAmount);
            ps.setBigDecimal(10, totals.benefitVatExemptAmount);
            ps.setBigDecimal(11, benefit.appliedRate);
            ps.setBigDecimal(12, totals.benefitDiscountAmount);
            ps.setInt(13, currentUserId());
            ps.executeUpdate();
        }
    }

    public static class CurrentSession {

        public String username;
        public String fullName;
        public Long shiftId;
        public Timestamp shiftOpenedAt;
        public String shiftLabel;
    }

    private static class FiscalSeriesAllocation {

        long seriesId;
        String prefix;
        long serialFrom;
        long serialTo;
        long serialNo;
        String invoiceNo;
    }

    private static class BenefitSnapshot {

        int policyId;
        Long customerId;
        String benefitType;
        String customerName;
        String customerTin;
        String govIdNo;
        BigDecimal appliedRate = BigDecimal.ZERO;
    }

    private String nextSaleNo() {
        return "SALE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private String nextReturnNo() {
        return "RET-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    public CurrentSession loadCurrentSession() throws Exception {
        CurrentSession info = new CurrentSession();
        info.username = "USER-" + currentUserId();
        info.fullName = "";
        info.shiftLabel = "No open shift for current user";
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT username, full_name FROM users WHERE id = ?")) {
                    ps.setInt(1, currentUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String username = rs.getString("username");
                            String fullName = rs.getString("full_name");
                            if (username != null && !username.isBlank()) {
                                info.username = username;
                            }
                            info.fullName = fullName == null ? "" : fullName;
                        }
                    }
                }

                Long shiftId = null;
                Timestamp openedAt = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, opened_at FROM pos_shift WHERE store_id = ? AND terminal_id = ? AND opened_by = ? AND status = 'OPEN' ORDER BY opened_at DESC FETCH FIRST ROW ONLY")) {
                    ps.setInt(1, currentStoreId());
                    ps.setInt(2, currentTerminalId());
                    ps.setInt(3, currentUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            shiftId = rs.getLong("id");
                            openedAt = rs.getTimestamp("opened_at");
                        }
                    }
                }

                if (shiftId != null) {
                    info.shiftId = shiftId;
                    info.shiftOpenedAt = openedAt;
                    info.shiftLabel = "Shift #" + shiftId + " OPEN"
                            + (openedAt == null ? "" : " @ " + openedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
                conn.commit();
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
        return info;
    }

    private String currentSaleNo(Connection conn, long saleId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT sale_no FROM sale WHERE id = ?")) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private BigDecimal gross(CartLine line) {
        return scale(line.unitPrice.multiply(line.qtyUom));
    }

    private BigDecimal lineTax(CartLine line) {
        BigDecimal gross = gross(line);
        if (line.taxRate == null || line.taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (line.priceIncludesTax) {
            BigDecimal divisor = BigDecimal.ONE.add(line.taxRate);
            return gross.subtract(gross.divide(divisor, 6, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
        }
        return gross.multiply(line.taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSaleDiscountAmount(BigDecimal subtotal) {
        SaleDiscountInfo info = effectiveSaleDiscount();
        if (info == null || info.value == null) {
            return BigDecimal.ZERO;
        }
        if (info.isPercent()) {
            return subtotal.multiply(info.value).setScale(2, RoundingMode.HALF_UP);
        }
        return scale(info.value.min(subtotal));
    }

    private void refreshSelectedCustomer() throws Exception {
        selectedCustomer = null;
        automaticDiscountRate = BigDecimal.ZERO;
        automaticDiscountName = null;
        if (customerId == null) {
            applyBenefitRulesToLines();
            return;
        }
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                selectedCustomer = customerDao.findById(conn, customerId);
                conn.commit();
            } catch (Exception e) {
                Db.rollbackQuietly(conn);
                throw e;
            }
        }
        applyBenefitRulesToLines();
    }

    private void validateWholeCartStock() throws Exception {
        for (int i = 0; i < lines.size(); i++) {
            CartLine line = lines.get(i);
            ensureAvailableQty(line.productId, line.qtyUom, i, line.productName);
        }
    }

    private void ensureAvailableQty(long productId, BigDecimal desiredQtyForLine, Integer excludeIndex, String productName) throws Exception {
        BigDecimal totalDesired = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            CartLine line = lines.get(i);
            if (line.productId != productId) {
                continue;
            }
            if (excludeIndex != null && i == excludeIndex) {
                continue;
            }
            totalDesired = totalDesired.add(Money.safe(line.qtyUom));
        }
        totalDesired = totalDesired.add(Money.safe(desiredQtyForLine));
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            BigDecimal onHand = productDao.getOnHandQty(conn, productId);
            conn.commit();
            if (onHand.compareTo(totalDesired) < 0) {
                String name = productName == null || productName.isBlank() ? ("Product ID " + productId) : productName;
                throw new IllegalStateException(name + " only has " + onHand.stripTrailingZeros().toPlainString()
                        + " remaining. Requested cart quantity: " + totalDesired.stripTrailingZeros().toPlainString());
            }
        }
    }

    private SaleDiscountInfo effectiveSaleDiscount() {
        if (saleDiscount != null && saleDiscount.value != null && saleDiscount.value.compareTo(BigDecimal.ZERO) > 0) {
            return saleDiscount;
        }
        return null;
    }

    private boolean eq(BigDecimal a, BigDecimal b) {
        return Money.safe(a).compareTo(Money.safe(b)) == 0;
    }

    private BigDecimal scale(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : v.setScale(2, RoundingMode.HALF_UP);
    }

    public static class PayResult {

        public long saleId;
        public String saleNo;
        public BigDecimal total;
        public BigDecimal cash;
        public BigDecimal change;
    }

    public static class ReturnResult {

        public long returnId;
        public String returnNumber;
        public BigDecimal totalRefund;
    }
}
