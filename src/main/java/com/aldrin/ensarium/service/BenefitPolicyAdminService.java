package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.BenefitPolicyRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BenefitPolicyAdminService {
    private final AuditService auditService = new AuditService();

    public List<BenefitPolicyRow> listBenefitPolicies() {
        String sql = """
                SELECT id, code, name, benefit_type, kind,
                       default_rate, min_rate, max_rate,
                       vat_exempt, allow_manual_override,
                       legal_basis, effective_from, effective_to,
                       active, created_at
                FROM benefit_policy
                ORDER BY name, id
                """;
        List<BenefitPolicyRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new BenefitPolicyRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("benefit_type"),
                        rs.getString("kind"),
                        nz(rs.getBigDecimal("default_rate")),
                        nz(rs.getBigDecimal("min_rate")),
                        nz(rs.getBigDecimal("max_rate")),
                        rs.getInt("vat_exempt") == 1,
                        rs.getInt("allow_manual_override") == 1,
                        rs.getString("legal_basis"),
                        rs.getDate("effective_from"),
                        rs.getDate("effective_to"),
                        rs.getInt("active") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Unable to load benefit policies", ex);
        }
    }

    public int create(Integer actorUserId,
                      String code,
                      String name,
                      String benefitType,
                      String kind,
                      BigDecimal defaultRate,
                      BigDecimal minRate,
                      BigDecimal maxRate,
                      boolean vatExempt,
                      boolean allowManualOverride,
                      String legalBasis,
                      Date effectiveFrom,
                      Date effectiveTo,
                      boolean active) {
        String sql = """
                INSERT INTO benefit_policy(
                    code, name, benefit_type, kind,
                    default_rate, min_rate, max_rate,
                    vat_exempt, allow_manual_override,
                    legal_basis, effective_from, effective_to, active
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, code, name, benefitType, kind, defaultRate, minRate, maxRate,
                    vatExempt, allowManualOverride, legalBasis, effectiveFrom, effectiveTo, active);
            ps.executeUpdate();
            int id = generatedIntId(ps);
            auditService.log(actorUserId, "BENEFIT_POLICY_CREATE", "Created benefit policy: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create benefit policy", ex);
        }
    }

    public void update(Integer actorUserId,
                       int id,
                       String code,
                       String name,
                       String benefitType,
                       String kind,
                       BigDecimal defaultRate,
                       BigDecimal minRate,
                       BigDecimal maxRate,
                       boolean vatExempt,
                       boolean allowManualOverride,
                       String legalBasis,
                       Date effectiveFrom,
                       Date effectiveTo,
                       boolean active) {
        String sql = """
                UPDATE benefit_policy
                   SET code=?, name=?, benefit_type=?, kind=?,
                       default_rate=?, min_rate=?, max_rate=?,
                       vat_exempt=?, allow_manual_override=?,
                       legal_basis=?, effective_from=?, effective_to=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, code, name, benefitType, kind, defaultRate, minRate, maxRate,
                    vatExempt, allowManualOverride, legalBasis, effectiveFrom, effectiveTo, active);
            ps.setInt(14, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "BENEFIT_POLICY_UPDATE", "Updated benefit policy id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update benefit policy", ex);
        }
    }

    public void delete(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM benefit_policy WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "BENEFIT_POLICY_DELETE", "Deleted benefit policy id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete benefit policy", ex);
        }
    }

    private void bind(PreparedStatement ps,
                      String code,
                      String name,
                      String benefitType,
                      String kind,
                      BigDecimal defaultRate,
                      BigDecimal minRate,
                      BigDecimal maxRate,
                      boolean vatExempt,
                      boolean allowManualOverride,
                      String legalBasis,
                      Date effectiveFrom,
                      Date effectiveTo,
                      boolean active) throws Exception {
        ps.setString(1, code.trim());
        ps.setString(2, name.trim());
        ps.setString(3, benefitType);
        ps.setString(4, kind);
        ps.setBigDecimal(5, defaultRate);
        ps.setBigDecimal(6, minRate);
        ps.setBigDecimal(7, maxRate);
        ps.setInt(8, vatExempt ? 1 : 0);
        ps.setInt(9, allowManualOverride ? 1 : 0);
        if (legalBasis == null || legalBasis.isBlank()) {
            ps.setNull(10, java.sql.Types.VARCHAR);
        } else {
            ps.setString(10, legalBasis.trim());
        }
        ps.setDate(11, effectiveFrom);
        if (effectiveTo == null) {
            ps.setNull(12, java.sql.Types.DATE);
        } else {
            ps.setDate(12, effectiveTo);
        }
        ps.setInt(13, active ? 1 : 0);
    }

    private int generatedIntId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new IllegalStateException("Unable to get generated id");
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
