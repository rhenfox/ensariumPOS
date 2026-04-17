package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductAdminService {

    private final AuditService auditService = new AuditService();

    public boolean userHasRole(int userId, String roleName) {
        String sql = """
                SELECT 1
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND UPPER(r.name) = UPPER(?)
                FETCH FIRST ROW ONLY
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify user role", ex);
        }
    }

    public boolean isAdminUser(int userId) {
        return userHasRole(userId, "ADMIN");
    }

    public List<CategoryRow> listCategories() {
        String sql = """
                SELECT c.id, c.name, c.parent_id, p.name AS parent_name
                FROM product_category c
                LEFT JOIN product_category p ON p.id = c.parent_id
                ORDER BY c.name
                """;
        List<CategoryRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new CategoryRow(
                        rs.getInt("id"),
                        rs.getString("name"),
                        (Integer) rs.getObject("parent_id"),
                        rs.getString("parent_name")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load categories", ex);
        }
    }

    public int createCategory(Integer actorUserId, String name, Integer parentId) {
        String sql = "INSERT INTO product_category(name, parent_id) VALUES(?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            if (parentId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, parentId);
            ps.executeUpdate();
            int id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            auditService.log(actorUserId, "CATEGORY_CREATE", "Created category: " + name);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create category", ex);
        }
    }

    public void updateCategory(Integer actorUserId, int id, String name, Integer parentId) {
        String sql = "UPDATE product_category SET name=?, parent_id=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            if (parentId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, parentId);
            ps.setInt(3, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CATEGORY_UPDATE", "Updated category id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update category", ex);
        }
    }

    public void deleteCategory(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM product_category WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CATEGORY_DELETE", "Deleted category id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete category", ex);
        }
    }

    public List<UnitRow> listUnits() {
        String sql = "SELECT id, code, name FROM uom ORDER BY code";
        List<UnitRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new UnitRow(rs.getInt("id"), rs.getString("code"), rs.getString("name")));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load units", ex);
        }
    }

    public int createUnit(Integer actorUserId, String code, String name) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO uom(code, name) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.executeUpdate();
            int id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            auditService.log(actorUserId, "UNIT_CREATE", "Created unit: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create unit", ex);
        }
    }

    public void updateUnit(Integer actorUserId, int id, String code, String name) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE uom SET code=?, name=? WHERE id=?")) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "UNIT_UPDATE", "Updated unit id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update unit", ex);
        }
    }

    public void deleteUnit(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM uom WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "UNIT_DELETE", "Deleted unit id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete unit", ex);
        }
    }

    public List<TaxRow> listTaxes() {
        String sql = "SELECT id, code, name, rate, active FROM tax ORDER BY code";
        List<TaxRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new TaxRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getBigDecimal("rate"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load taxes", ex);
        }
    }

    public int createTax(Integer actorUserId, String code, String name, BigDecimal rate, boolean active) {
        String sql = "INSERT INTO tax(code, name, rate, active) VALUES(?, ?, ?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setBigDecimal(3, rate);
            ps.setInt(4, active ? 1 : 0);
            ps.executeUpdate();
            int id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            auditService.log(actorUserId, "TAX_CREATE", "Created tax: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create tax", ex);
        }
    }

    public void updateTax(Integer actorUserId, int id, String code, String name, BigDecimal rate, boolean active) {
        String sql = "UPDATE tax SET code=?, name=?, rate=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setBigDecimal(3, rate);
            ps.setInt(4, active ? 1 : 0);
            ps.setInt(5, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TAX_UPDATE", "Updated tax id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update tax", ex);
        }
    }

    public void deleteTax(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tax WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TAX_DELETE", "Deleted tax id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete tax", ex);
        }
    }

    public List<ProductRow> listProducts() {
        String sql = """
                SELECT p.id, p.sku, p.name, p.category_id, c.name AS category_name,
                       p.base_uom_id, u.code AS unit_code,
                       p.buying_price, p.selling_price,
                       p.default_tax_id, t.code AS tax_code,
                       p.active
                FROM product p
                LEFT JOIN product_category c ON c.id = p.category_id
                JOIN uom u ON u.id = p.base_uom_id
                LEFT JOIN tax t ON t.id = p.default_tax_id
                ORDER BY p.name
                """;
        List<ProductRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new ProductRow(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        (Integer) rs.getObject("category_id"),
                        rs.getString("category_name"),
                        rs.getInt("base_uom_id"),
                        rs.getString("unit_code"),
                        rs.getBigDecimal("buying_price"),
                        rs.getBigDecimal("selling_price"),
                        (Integer) rs.getObject("default_tax_id"),
                        rs.getString("tax_code"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load products", ex);
        }
    }

    public long createProduct(Integer actorUserId, String sku, String name, Integer categoryId, int unitId,
                              BigDecimal buyingPrice, BigDecimal sellingPrice, Integer taxId, boolean active) {
        String sql = """
                INSERT INTO product(sku, name, category_id, base_uom_id, buying_price, selling_price, default_tax_id, active)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sku.trim());
            ps.setString(2, name.trim());
            if (categoryId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, categoryId);
            ps.setInt(4, unitId);
            ps.setBigDecimal(5, buyingPrice);
            ps.setBigDecimal(6, sellingPrice);
            if (taxId == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, taxId);
            ps.setInt(8, active ? 1 : 0);
            ps.executeUpdate();
            long id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getLong(1);
            }
            auditService.log(actorUserId, "PRODUCT_CREATE", "Created product: " + sku + " / " + name);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create product", ex);
        }
    }

    public void updateProduct(Integer actorUserId, long id, String sku, String name, Integer categoryId, int unitId,
                              BigDecimal buyingPrice, BigDecimal sellingPrice, Integer taxId, boolean active) {
        String sql = """
                UPDATE product
                   SET sku=?, name=?, category_id=?, base_uom_id=?, buying_price=?, selling_price=?, default_tax_id=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            ps.setString(2, name.trim());
            if (categoryId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, categoryId);
            ps.setInt(4, unitId);
            ps.setBigDecimal(5, buyingPrice);
            ps.setBigDecimal(6, sellingPrice);
            if (taxId == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, taxId);
            ps.setInt(8, active ? 1 : 0);
            ps.setLong(9, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "PRODUCT_UPDATE", "Updated product id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update product", ex);
        }
    }

    public void deleteProduct(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM product WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "PRODUCT_DELETE", "Deleted product id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete product", ex);
        }
    }

    public List<BarcodeRow> listBarcodes() {
        String sql = """
                SELECT b.id, b.product_id, p.name AS product_name, b.barcode, b.is_primary, b.active
                FROM product_barcode b
                JOIN product p ON p.id = b.product_id
                ORDER BY p.name, b.barcode
                """;
        List<BarcodeRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new BarcodeRow(
                        rs.getLong("id"),
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getString("barcode"),
                        rs.getInt("is_primary") == 1,
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load barcodes", ex);
        }
    }

    public long createBarcode(Integer actorUserId, long productId, String barcode, boolean primary, boolean active) {
        String sql = "INSERT INTO product_barcode(product_id, barcode, is_primary, active) VALUES(?, ?, ?, ?)";
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (primary) {
                    clearPrimaryForProduct(con, productId);
                }
                long id;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, productId);
                    ps.setString(2, barcode.trim());
                    ps.setInt(3, primary ? 1 : 0);
                    ps.setInt(4, active ? 1 : 0);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                con.commit();
                auditService.log(actorUserId, "BARCODE_CREATE", "Created barcode: " + barcode);
                return id;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create barcode", ex);
        }
    }

    public void updateBarcode(Integer actorUserId, long id, long productId, String barcode, boolean primary, boolean active) {
        String sql = "UPDATE product_barcode SET product_id=?, barcode=?, is_primary=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (primary) {
                    clearPrimaryForProduct(con, productId);
                }
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, productId);
                    ps.setString(2, barcode.trim());
                    ps.setInt(3, primary ? 1 : 0);
                    ps.setInt(4, active ? 1 : 0);
                    ps.setLong(5, id);
                    ps.executeUpdate();
                }
                con.commit();
                auditService.log(actorUserId, "BARCODE_UPDATE", "Updated barcode id=" + id);
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update barcode", ex);
        }
    }

    public void deleteBarcode(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM product_barcode WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "BARCODE_DELETE", "Deleted barcode id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete barcode", ex);
        }
    }

    private void clearPrimaryForProduct(Connection con, long productId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE product_barcode SET is_primary = 0 WHERE product_id = ?")) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        }
    }

    public List<LookupOption> categoryOptions() {
        List<LookupOption> out = new ArrayList<>();
        for (CategoryRow row : listCategories()) {
            out.add(new LookupOption(row.id(), row.name()));
        }
        return out;
    }

    public List<LookupOption> unitOptions() {
        List<LookupOption> out = new ArrayList<>();
        for (UnitRow row : listUnits()) {
            out.add(new LookupOption(row.id(), row.code() + " - " + row.name()));
        }
        return out;
    }

    public List<LookupOption> taxOptions() {
        List<LookupOption> out = new ArrayList<>();
        for (TaxRow row : listTaxes()) {
            out.add(new LookupOption(row.id(), row.code() + " - " + row.name()));
        }
        return out;
    }

    public List<LookupOption> productOptions() {
        List<LookupOption> out = new ArrayList<>();
        for (ProductRow row : listProducts()) {
            out.add(new LookupOption((int) row.id(), row.name()));
        }
        return out;
    }
}
