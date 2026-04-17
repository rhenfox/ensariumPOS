package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.AppConfig;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private final int storeId = AppConfig.getInt("app.storeId", 1);

    public ProductOption findByBarcode(Connection conn, String barcode) throws Exception {
        String sql = """
            SELECT p.id, pb.barcode, p.sku, p.name, u.code AS uom_code,
                   p.selling_price, p.buying_price, p.price_includes_tax,
                   COALESCE(t.id, 0) AS tax_id, COALESCE(t.rate, 0) AS tax_rate,
                   COALESCE(sb.qty_in_base, 0) AS on_hand_qty
              FROM product_barcode pb
              JOIN product p ON p.id = pb.product_id
              JOIN uom u ON u.id = p.base_uom_id
              LEFT JOIN tax t ON t.id = p.default_tax_id
              LEFT JOIN stock_balance sb ON sb.product_id = p.id AND sb.store_id = ? AND sb.status_code = 'ONHAND'
             WHERE pb.barcode = ? AND p.active = 1 AND pb.active = 1
            FETCH FIRST ROW ONLY
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setString(2, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<ProductOption> search(Connection conn, String keyword) throws Exception {
        String sql = """
            SELECT DISTINCT p.id, pb.barcode, p.sku, p.name, u.code AS uom_code,
                   p.selling_price, p.buying_price, p.price_includes_tax,
                   COALESCE(t.id, 0) AS tax_id, COALESCE(t.rate, 0) AS tax_rate,
                   COALESCE(sb.qty_in_base, 0) AS on_hand_qty
              FROM product p
              JOIN uom u ON u.id = p.base_uom_id
              LEFT JOIN product_barcode pb ON pb.product_id = p.id AND pb.active = 1
              LEFT JOIN tax t ON t.id = p.default_tax_id
              LEFT JOIN stock_balance sb ON sb.product_id = p.id AND sb.store_id = ? AND sb.status_code = 'ONHAND'
             WHERE p.active = 1
               AND (UPPER(p.name) LIKE ? OR UPPER(p.sku) LIKE ? OR UPPER(COALESCE(pb.barcode,'')) LIKE ?)
             ORDER BY p.name
            FETCH FIRST 200 ROWS ONLY
            """;
        String like = "%" + keyword.toUpperCase() + "%";
        List<ProductOption> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public ProductOption findById(Connection conn, long id) throws Exception {
        String sql = """
            SELECT p.id, pb.barcode, p.sku, p.name, u.code AS uom_code,
                   p.selling_price, p.buying_price, p.price_includes_tax,
                   COALESCE(t.id, 0) AS tax_id, COALESCE(t.rate, 0) AS tax_rate,
                   COALESCE(sb.qty_in_base, 0) AS on_hand_qty
              FROM product p
              JOIN uom u ON u.id = p.base_uom_id
              LEFT JOIN product_barcode pb ON pb.product_id = p.id AND pb.is_primary = 1
              LEFT JOIN tax t ON t.id = p.default_tax_id
              LEFT JOIN stock_balance sb ON sb.product_id = p.id AND sb.store_id = ? AND sb.status_code = 'ONHAND'
             WHERE p.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public BigDecimal getOnHandQty(Connection conn, long productId) throws Exception {
        String sql = "SELECT COALESCE(qty_in_base, 0) FROM stock_balance WHERE store_id = ? AND product_id = ? AND status_code = 'ONHAND'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }


    public ProductBenefitRule findBenefitRule(Connection conn, long productId, String benefitType) throws Exception {
        if (benefitType == null || benefitType.isBlank()) return null;
        String sql = """
            SELECT id, product_id, benefit_type, benefit_mode, vat_exempt, active
              FROM product_benefit_rule
             WHERE product_id = ?
               AND benefit_type = ?
               AND active = 1
               AND effective_from <= CURRENT_DATE
               AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
             ORDER BY effective_from DESC, id DESC
            FETCH FIRST ROW ONLY
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, benefitType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductBenefitRule r = new ProductBenefitRule();
                    r.id = rs.getLong("id");
                    r.productId = rs.getLong("product_id");
                    r.benefitType = rs.getString("benefit_type");
                    r.benefitMode = rs.getString("benefit_mode");
                    r.vatExempt = rs.getInt("vat_exempt") == 1;
                    r.active = rs.getInt("active") == 1;
                    return r;
                }
            }
        } catch (SQLException e) {
            if ("42X05".equals(e.getSQLState())) return null;
            throw e;
        }
        return null;
    }

    private ProductOption map(ResultSet rs) throws Exception {
        ProductOption p = new ProductOption();
        p.productId = rs.getLong("id");
        p.barcode = rs.getString("barcode");
        p.sku = rs.getString("sku");
        p.productName = rs.getString("name");
        p.uomCode = rs.getString("uom_code");
        p.unitPrice = rs.getBigDecimal("selling_price");
        p.buyingPrice = rs.getBigDecimal("buying_price");
        p.priceIncludesTax = rs.getInt("price_includes_tax") == 1;
        p.taxId = rs.getLong("tax_id");
        p.taxRate = rs.getBigDecimal("tax_rate");
        p.onHandQty = rs.getBigDecimal("on_hand_qty");
        if (p.buyingPrice == null) p.buyingPrice = BigDecimal.ZERO;
        if (p.taxRate == null) p.taxRate = BigDecimal.ZERO;
        if (p.onHandQty == null) p.onHandQty = BigDecimal.ZERO;
        return p;
    }
}
