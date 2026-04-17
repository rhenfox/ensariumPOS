package com.aldrin.ensarium.benefit;

import com.aldrin.ensarium.db.Db;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductBenefitRuleService {

    public List<ProductBenefitRuleRow> listProductBenefitRules() {
        String sql = "SELECT r.id, r.product_id, p.sku, p.name, r.benefit_type, r.benefit_mode, "
                + "r.vat_exempt, r.active, r.effective_from, r.effective_to "
                + "FROM product_benefit_rule r "
                + "JOIN product p ON p.id = r.product_id "
                + "ORDER BY p.name, r.benefit_type, r.effective_from DESC";

        List<ProductBenefitRuleRow> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new ProductBenefitRuleRow(
                        rs.getLong("id"),
                        rs.getLong("product_id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getString("benefit_type"),
                        rs.getString("benefit_mode"),
                        rs.getShort("vat_exempt") == 1,
                        rs.getShort("active") == 1,
                        rs.getDate("effective_from") == null ? null : rs.getDate("effective_from").toLocalDate(),
                        rs.getDate("effective_to") == null ? null : rs.getDate("effective_to").toLocalDate()
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return out;
    }

    public List<LookupOption> productOptions() {
        String sql = "SELECT id, sku, name, active FROM product ORDER BY name, sku";
        List<LookupOption> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String sku = rs.getString("sku");
                String name = rs.getString("name");
                boolean active = rs.getShort("active") == 1;

                StringBuilder label = new StringBuilder();
                if (sku != null && !sku.trim().isEmpty()) {
                    label.append(sku.trim()).append(" - ");
                }
                label.append(name == null ? "" : name.trim());
                if (!active) {
                    label.append(" [Inactive]");
                }

                out.add(new LookupOption(id, label.toString()));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return out;
    }

    public long createProductBenefitRule(long productId, String benefitType, String benefitMode,
            boolean vatExempt, boolean active, LocalDate effectiveFrom, LocalDate effectiveTo) {
        String sql = "INSERT INTO product_benefit_rule "
                + "(product_id, benefit_type, benefit_mode, vat_exempt, active, effective_from, effective_to) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, productId);
            ps.setString(2, benefitType);
            ps.setString(3, benefitMode);
            ps.setShort(4, (short) (vatExempt ? 1 : 0));
            ps.setShort(5, (short) (active ? 1 : 0));
            ps.setDate(6, Date.valueOf(effectiveFrom));
            if (effectiveTo == null) {
                ps.setNull(7, Types.DATE);
            } else {
                ps.setDate(7, Date.valueOf(effectiveTo));
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            throw new SQLException("Insert succeeded but no generated key was returned.");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void updateProductBenefitRule(long id, long productId, String benefitType, String benefitMode,
            boolean vatExempt, boolean active, LocalDate effectiveFrom, LocalDate effectiveTo) {
        String sql = "UPDATE product_benefit_rule SET "
                + "product_id = ?, benefit_type = ?, benefit_mode = ?, vat_exempt = ?, active = ?, "
                + "effective_from = ?, effective_to = ? "
                + "WHERE id = ?";

        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ps.setString(2, benefitType);
            ps.setString(3, benefitMode);
            ps.setShort(4, (short) (vatExempt ? 1 : 0));
            ps.setShort(5, (short) (active ? 1 : 0));
            ps.setDate(6, Date.valueOf(effectiveFrom));
            if (effectiveTo == null) {
                ps.setNull(7, Types.DATE);
            } else {
                ps.setDate(7, Date.valueOf(effectiveTo));
            }
            ps.setLong(8, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void deleteProductBenefitRule(long id) {
        String sql = "DELETE FROM product_benefit_rule WHERE id = ?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public List<String> benefitTypeOptions() {
    String sql = "SELECT DISTINCT TRIM(benefit_type) AS benefit_type "
            + "FROM product_benefit_rule "
            + "WHERE benefit_type IS NOT NULL AND TRIM(benefit_type) <> '' "
            + "ORDER BY benefit_type";

    List<String> out = new ArrayList<>();
    try (Connection cn = Db.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            out.add(rs.getString("benefit_type"));
        }
    } catch (SQLException ex) {
        throw new RuntimeException(ex);
    }

    // keep defaults available even if table is empty
    if (!containsIgnoreCase(out, "SENIOR")) {
        out.add("SENIOR");
    }
    if (!containsIgnoreCase(out, "PWD")) {
        out.add("PWD");
    }

    return out;
}

private boolean containsIgnoreCase(List<String> list, String value) {
    for (String s : list) {
        if (s != null && s.trim().equalsIgnoreCase(value)) {
            return true;
        }
    }
    return false;
}
}
