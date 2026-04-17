package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.PermissionRow;
import com.aldrin.ensarium.model.RoleRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleService {
    public List<RoleRow> listRoles() {
        List<RoleRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name, description FROM roles ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new RoleRow(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load roles", ex);
        }
    }

    public List<PermissionRow> listPermissionsForRole(int roleId) {
        String sql = "SELECT p.id, p.code, p.description, CASE WHEN rp.role_id IS NULL THEN 0 ELSE 1 END assigned "
                + "FROM permissions p "
                + "LEFT JOIN role_permissions rp ON rp.permission_id = p.id AND rp.role_id = ? "
                + "ORDER BY p.code";
        List<PermissionRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PermissionRow(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("description"),
                            rs.getInt("assigned") == 1
                    ));
                }
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load permissions", ex);
        }
    }

    public int createRole(Integer actorUserId, String name, String description) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO roles(name, description) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, description == null ? null : description.trim());
            ps.executeUpdate();
            int id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            new AuditService().log(actorUserId, "ROLE_CREATE", "Created role: " + name);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create role", ex);
        }
    }

    public void updateRole(Integer actorUserId, int roleId, String name, String description) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE roles SET name=?, description=? WHERE id=?")) {
            ps.setString(1, name.trim());
            ps.setString(2, description == null ? null : description.trim());
            ps.setInt(3, roleId);
            ps.executeUpdate();
            new AuditService().log(actorUserId, "ROLE_UPDATE", "Updated role id=" + roleId + " name=" + name);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update role", ex);
        }
    }

    public void deleteRole(Integer actorUserId, int roleId) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM roles WHERE id = ?")) {
            ps.setInt(1, roleId);
            ps.executeUpdate();
            new AuditService().log(actorUserId, "ROLE_DELETE", "Deleted role id=" + roleId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete role", ex);
        }
    }

    public void saveRolePermissions(Integer actorUserId, int roleId, Set<Integer> permissionIds) {
        if (permissionIds == null) permissionIds = new HashSet<>();
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement delete = con.prepareStatement("DELETE FROM role_permissions WHERE role_id = ?")) {
                delete.setInt(1, roleId);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO role_permissions(role_id, permission_id) VALUES(?, ?)")) {
                for (Integer permissionId : permissionIds) {
                    insert.setInt(1, roleId);
                    insert.setInt(2, permissionId);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            con.commit();
            new AuditService().log(actorUserId, "ROLE_PERMISSIONS_SAVE", "Updated permissions for role id=" + roleId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to save role permissions", ex);
        }
    }

    public int countRoles() {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM roles");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to count roles", ex);
        }
    }
}
