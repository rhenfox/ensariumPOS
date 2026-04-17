package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.ReturnReasonRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReturnReasonAdminService {
    private final AuditService auditService = new AuditService();

    public List<ReturnReasonRow> listReturnReasons() {
        String sql = """
                SELECT id, code, name
                FROM return_reason
                ORDER BY name, id
                """;
        List<ReturnReasonRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new ReturnReasonRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load return reasons", ex);
        }
    }

    public int create(Integer actorUserId, String code, String name) {
        String sql = """
                INSERT INTO return_reason(code, name)
                VALUES(?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.executeUpdate();
            int id = generatedIntId(ps);
            auditService.log(actorUserId, "RETURN_REASON_CREATE", "Created return reason: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create return reason", ex);
        }
    }

    public void update(Integer actorUserId, int id, String code, String name) {
        String sql = """
                UPDATE return_reason
                   SET code=?, name=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "RETURN_REASON_UPDATE", "Updated return reason id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update return reason", ex);
        }
    }

    public void delete(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM return_reason WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "RETURN_REASON_DELETE", "Deleted return reason id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete return reason", ex);
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
