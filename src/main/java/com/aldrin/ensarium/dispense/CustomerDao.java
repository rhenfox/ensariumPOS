package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public List<CustomerRef> search(Connection conn, String keyword) throws Exception {
        String sql = """
            SELECT c.id, c.customer_no, c.full_name, c.tin_no, c.address,
                   c.is_senior, c.is_vat_exempt,
                   CASE WHEN EXISTS (
                        SELECT 1 FROM customer_benefit_profile cbp
                        WHERE cbp.customer_id = c.id
                          AND cbp.active = 1
                          AND cbp.benefit_type = 'PWD'
                          AND (cbp.effective_from IS NULL OR cbp.effective_from <= CURRENT_DATE)
                          AND (cbp.effective_to IS NULL OR cbp.effective_to >= CURRENT_DATE)
                   ) THEN 1 ELSE 0 END AS is_pwd
              FROM customer c
             WHERE c.active = 1
               AND (UPPER(c.full_name) LIKE ? OR UPPER(COALESCE(c.customer_no,'')) LIKE ? OR UPPER(COALESCE(c.tin_no,'')) LIKE ?)
             ORDER BY c.full_name
            FETCH FIRST 200 ROWS ONLY
            """;
        String like = "%" + keyword.toUpperCase() + "%";
        List<CustomerRef> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public CustomerRef findById(Connection conn, Long id) throws Exception {
        if (id == null) return null;
        String sql = """
            SELECT c.id, c.customer_no, c.full_name, c.tin_no, c.address,
                   c.is_senior, c.is_vat_exempt,
                   CASE WHEN EXISTS (
                        SELECT 1 FROM customer_benefit_profile cbp
                        WHERE cbp.customer_id = c.id
                          AND cbp.active = 1
                          AND cbp.benefit_type = 'PWD'
                          AND (cbp.effective_from IS NULL OR cbp.effective_from <= CURRENT_DATE)
                          AND (cbp.effective_to IS NULL OR cbp.effective_to >= CURRENT_DATE)
                   ) THEN 1 ELSE 0 END AS is_pwd
              FROM customer c
             WHERE c.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public BigDecimal findBenefitRate(Connection conn, Long customerId) throws Exception {
        CustomerRef c = findById(conn, customerId);
        if (c == null) return BigDecimal.ZERO;
        String benefitType = c.senior ? "SENIOR" : (c.pwd ? "PWD" : null);
        if (benefitType == null) return BigDecimal.ZERO;
        String sql = """
            SELECT default_rate
              FROM benefit_policy
             WHERE active = 1
               AND benefit_type = ?
               AND effective_from <= CURRENT_DATE
               AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
             ORDER BY effective_from DESC
            FETCH FIRST ROW ONLY
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, benefitType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    private CustomerRef map(ResultSet rs) throws Exception {
        CustomerRef c = new CustomerRef();
        c.customerId = rs.getLong("id");
        c.customerNo = rs.getString("customer_no");
        c.fullName = rs.getString("full_name");
        c.tinNo = rs.getString("tin_no");
        c.address = rs.getString("address");
        c.senior = rs.getInt("is_senior") == 1;
        c.pwd = rs.getInt("is_pwd") == 1;
        c.vatExempt = rs.getInt("is_vat_exempt") == 1 || c.senior || c.pwd;
        return c;
    }
}
