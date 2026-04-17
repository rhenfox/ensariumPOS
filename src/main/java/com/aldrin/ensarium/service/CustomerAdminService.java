package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.CustomerBenefitProfileRow;
import com.aldrin.ensarium.model.CustomerRow;
import com.aldrin.ensarium.model.LookupOption;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CustomerAdminService {
    private final AuditService auditService = new AuditService();

    public List<CustomerRow> listCustomers() {
        String sql = """
                SELECT id, customer_no, full_name, tin_no, phone, email, address,
                       is_senior, senior_id_no, is_vat_exempt, active, created_at
                FROM customer
                ORDER BY full_name, id
                """;
        List<CustomerRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new CustomerRow(
                        rs.getLong("id"),
                        rs.getString("customer_no"),
                        rs.getString("full_name"),
                        rs.getString("tin_no"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("is_senior") == 1,
                        rs.getString("senior_id_no"),
                        rs.getInt("is_vat_exempt") == 1,
                        rs.getInt("active") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load customers", ex);
        }
    }

    public long createCustomer(Integer actorUserId,
                               String customerNo,
                               String fullName,
                               String tinNo,
                               String phone,
                               String email,
                               String address,
                               boolean senior,
                               String seniorIdNo,
                               boolean vatExempt,
                               boolean active) {
        String sql = """
                INSERT INTO customer(
                    customer_no, full_name, tin_no, phone, email, address,
                    is_senior, senior_id_no, is_vat_exempt, active
                ) VALUES(?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCustomer(ps, customerNo, fullName, tinNo, phone, email, address, senior, seniorIdNo, vatExempt, active);
            ps.executeUpdate();
            long id = generatedLongId(ps);
            auditService.log(actorUserId, "CUSTOMER_CREATE", "Created customer: " + fullName);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create customer", ex);
        }
    }

    public void updateCustomer(Integer actorUserId,
                               long id,
                               String customerNo,
                               String fullName,
                               String tinNo,
                               String phone,
                               String email,
                               String address,
                               boolean senior,
                               String seniorIdNo,
                               boolean vatExempt,
                               boolean active) {
        String sql = """
                UPDATE customer
                   SET customer_no=?, full_name=?, tin_no=?, phone=?, email=?, address=?,
                       is_senior=?, senior_id_no=?, is_vat_exempt=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindCustomer(ps, customerNo, fullName, tinNo, phone, email, address, senior, seniorIdNo, vatExempt, active);
            ps.setLong(11, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CUSTOMER_UPDATE", "Updated customer id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update customer", ex);
        }
    }

    public void deleteCustomer(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM customer WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CUSTOMER_DELETE", "Deleted customer id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete customer", ex);
        }
    }

    public List<CustomerBenefitProfileRow> listBenefitProfiles() {
        String sql = """
                SELECT cbp.id,
                       cbp.customer_id,
                       c.full_name AS customer_name,
                       cbp.benefit_type,
                       cbp.gov_id_no,
                       cbp.tin_no,
                       cbp.active,
                       cbp.effective_from,
                       cbp.effective_to,
                       cbp.created_at
                FROM customer_benefit_profile cbp
                JOIN customer c ON c.id = cbp.customer_id
                ORDER BY c.full_name, cbp.benefit_type, cbp.id
                """;
        List<CustomerBenefitProfileRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new CustomerBenefitProfileRow(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("benefit_type"),
                        rs.getString("gov_id_no"),
                        rs.getString("tin_no"),
                        rs.getInt("active") == 1,
                        rs.getDate("effective_from"),
                        rs.getDate("effective_to"),
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load customer benefit profiles", ex);
        }
    }

    public long createBenefitProfile(Integer actorUserId,
                                     long customerId,
                                     String benefitType,
                                     String govIdNo,
                                     String tinNo,
                                     boolean active,
                                     Date effectiveFrom,
                                     Date effectiveTo) {
        String sql = """
                INSERT INTO customer_benefit_profile(
                    customer_id, benefit_type, gov_id_no, tin_no, active, effective_from, effective_to
                ) VALUES(?,?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindBenefit(ps, customerId, benefitType, govIdNo, tinNo, active, effectiveFrom, effectiveTo);
            ps.executeUpdate();
            long id = generatedLongId(ps);
            auditService.log(actorUserId, "CUSTOMER_BENEFIT_CREATE", "Created customer benefit profile id=" + id);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create customer benefit profile", ex);
        }
    }

    public void updateBenefitProfile(Integer actorUserId,
                                     long id,
                                     long customerId,
                                     String benefitType,
                                     String govIdNo,
                                     String tinNo,
                                     boolean active,
                                     Date effectiveFrom,
                                     Date effectiveTo) {
        String sql = """
                UPDATE customer_benefit_profile
                   SET customer_id=?, benefit_type=?, gov_id_no=?, tin_no=?, active=?, effective_from=?, effective_to=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindBenefit(ps, customerId, benefitType, govIdNo, tinNo, active, effectiveFrom, effectiveTo);
            ps.setLong(8, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CUSTOMER_BENEFIT_UPDATE", "Updated customer benefit profile id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update customer benefit profile", ex);
        }
    }

    public void deleteBenefitProfile(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM customer_benefit_profile WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "CUSTOMER_BENEFIT_DELETE", "Deleted customer benefit profile id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete customer benefit profile", ex);
        }
    }

    public List<LookupOption> customerOptions() {
        String sql = "SELECT id, full_name FROM customer WHERE active = 1 ORDER BY full_name";
        List<LookupOption> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new LookupOption(rs.getInt(1), rs.getString(2)));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load customer options", ex);
        }
    }

    private void bindCustomer(PreparedStatement ps,
                              String customerNo,
                              String fullName,
                              String tinNo,
                              String phone,
                              String email,
                              String address,
                              boolean senior,
                              String seniorIdNo,
                              boolean vatExempt,
                              boolean active) throws Exception {
        ps.setString(1, nullable(customerNo));
        ps.setString(2, trim(fullName));
        ps.setString(3, nullable(tinNo));
        ps.setString(4, nullable(phone));
        ps.setString(5, nullable(email));
        ps.setString(6, nullable(address));
        ps.setInt(7, senior ? 1 : 0);
        ps.setString(8, nullable(seniorIdNo));
        ps.setInt(9, vatExempt ? 1 : 0);
        ps.setInt(10, active ? 1 : 0);
    }

    private void bindBenefit(PreparedStatement ps,
                             long customerId,
                             String benefitType,
                             String govIdNo,
                             String tinNo,
                             boolean active,
                             Date effectiveFrom,
                             Date effectiveTo) throws Exception {
        ps.setLong(1, customerId);
        ps.setString(2, trim(benefitType));
        ps.setString(3, trim(govIdNo));
        ps.setString(4, nullable(tinNo));
        ps.setInt(5, active ? 1 : 0);
        if (effectiveFrom == null) ps.setNull(6, java.sql.Types.DATE); else ps.setDate(6, effectiveFrom);
        if (effectiveTo == null) ps.setNull(7, java.sql.Types.DATE); else ps.setDate(7, effectiveTo);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullable(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private long generatedLongId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new IllegalStateException("No generated key returned.");
    }
}
