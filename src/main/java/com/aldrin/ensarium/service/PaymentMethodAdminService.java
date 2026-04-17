package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.PaymentMethodRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdminService {
    private final AuditService auditService = new AuditService();

    public List<PaymentMethodRow> listPaymentMethods() {
        String sql = """
                SELECT id, code, name, active
                FROM payment_method
                ORDER BY name, id
                """;
        List<PaymentMethodRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new PaymentMethodRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
             System.out.println(ex.getMessage());
            throw new IllegalStateException("Unable to load payment methods", ex);
//            ex.printStackTrace();
        }
    }

    public int create(Integer actorUserId, String code, String name, boolean active) {
        String sql = """
                INSERT INTO payment_method(code, name, active)
                VALUES(?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, active ? 1 : 0);
            ps.executeUpdate();
            int id = generatedIntId(ps);
            auditService.log(actorUserId, "PAYMENT_METHOD_CREATE", "Created payment method: " + code);
            return id;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new IllegalStateException("Unable to create payment method", ex);
        }
    }

    public void update(Integer actorUserId, int id, String code, String name, boolean active) {
        String sql = """
                UPDATE payment_method
                   SET code=?, name=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, active ? 1 : 0);
            ps.setInt(4, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "PAYMENT_METHOD_UPDATE", "Updated payment method id=" + id);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new IllegalStateException("Unable to update payment method", ex);
        }
    }

    public void delete(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM payment_method WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "PAYMENT_METHOD_DELETE", "Deleted payment method id=" + id);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new IllegalStateException("Unable to delete payment method", ex);
        }
    }

    private int generatedIntId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new IllegalStateException("Unable to get generated id");
        }
    }
}
