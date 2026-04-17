package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.InventoryStatusRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventoryStatusAdminService {

    private final AuditService auditService = new AuditService();

    public List<InventoryStatusRow> listInventoryStatuses() {
        String sql = """
                SELECT code, name, sellable
                FROM inventory_status
                ORDER BY code
                """;
        List<InventoryStatusRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new InventoryStatusRow(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getInt("sellable") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load inventory statuses", ex);
        }
    }

    public void create(Integer actorUserId, String code, String name, boolean sellable) {
        String sql = """
                INSERT INTO inventory_status(code, name, sellable)
                VALUES(?,?,?)
                """;
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, sellable ? 1 : 0);
            ps.executeUpdate();
            auditService.log(actorUserId, "INVENTORY_STATUS_CREATE", "Created inventory status: " + code);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create inventory status", ex);
        }
    }

    public void update(Integer actorUserId, String oldCode, String code, String name, boolean sellable) {
        String sql = """
                UPDATE inventory_status
                   SET code=?, name=?, sellable=?
                 WHERE code=?
                """;
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, sellable ? 1 : 0);
            ps.setString(4, oldCode);
            ps.executeUpdate();
            auditService.log(actorUserId, "INVENTORY_STATUS_UPDATE", "Updated inventory status code=" + oldCode);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update inventory status", ex);
        }
    }

    public void delete(Integer actorUserId, String code) {
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM inventory_status WHERE code=?")) {
            ps.setString(1, code);
            ps.executeUpdate();
            auditService.log(actorUserId, "INVENTORY_STATUS_DELETE", "Deleted inventory status code=" + code);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete inventory status", ex);
        }
    }
}
