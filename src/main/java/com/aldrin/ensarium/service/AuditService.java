package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.AuditRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuditService {
    public void log(Integer actorUserId, String actionCode, String details) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO audit_log(actor_user_id, action_code, details) VALUES(?,?,?)")) {
            if (actorUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, actorUserId);
            ps.setString(2, actionCode);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to write audit log", ex);
        }
    }

    public List<AuditRow> listAudit() {
        String sql = "SELECT a.id, "
                + "COALESCE(u.username, 'SYSTEM') actor_username, "
                + "COALESCE(NULLIF(TRIM(u.full_name), ''), 'SYSTEM') actor_full_name, "
                + "a.action_code, a.details, a.created_at "
                + "FROM audit_log a "
                + "LEFT JOIN users u ON a.actor_user_id = u.id "
                + "ORDER BY a.id DESC";
        List<AuditRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new AuditRow(
                        rs.getInt("id"),
                        rs.getString("actor_username"),
                        rs.getString("actor_full_name"),
                        rs.getString("action_code"),
                        rs.getString("details"),
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load audit log", ex);
        }
    }
}
