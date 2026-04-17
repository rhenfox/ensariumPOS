package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.DiscountTypeRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiscountTypeAdminService {
    private final AuditService auditService = new AuditService();

    public List<DiscountTypeRow> listDiscountTypes() {
        String sql = """
                SELECT id, code, name, kind, applies_to, active
                FROM discount_type
                ORDER BY name, id
                """;
        List<DiscountTypeRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new DiscountTypeRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("kind"),
                        rs.getString("applies_to"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load discount types", ex);
        }
    }

    public int create(Integer actorUserId, String code, String name, String kind, String appliesTo, boolean active) {
        String sql = """
                INSERT INTO discount_type(code, name, kind, applies_to, active)
                VALUES(?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setString(3, kind);
            ps.setString(4, appliesTo);
            ps.setInt(5, active ? 1 : 0);
            ps.executeUpdate();
            int id = generatedIntId(ps);
            auditService.log(actorUserId, "DISCOUNT_TYPE_CREATE", "Created discount type: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create discount type", ex);
        }
    }

    public void update(Integer actorUserId, int id, String code, String name, String kind, String appliesTo, boolean active) {
        String sql = """
                UPDATE discount_type
                   SET code=?, name=?, kind=?, applies_to=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setString(3, kind);
            ps.setString(4, appliesTo);
            ps.setInt(5, active ? 1 : 0);
            ps.setInt(6, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "DISCOUNT_TYPE_UPDATE", "Updated discount type id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update discount type", ex);
        }
    }

    public void delete(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM discount_type WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "DISCOUNT_TYPE_DELETE", "Deleted discount type id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete discount type", ex);
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
