package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.RoleRow;
import com.aldrin.ensarium.model.UserRow;
import com.aldrin.ensarium.util.Passwords;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserService {

    public List<UserRow> listUsers() {
        String sql = "SELECT u.id, u.username, u.full_name, u.active, r.name role_name "
                + "FROM users u "
                + "LEFT JOIN user_roles ur ON ur.user_id = u.id "
                + "LEFT JOIN roles r ON r.id = ur.role_id "
                + "ORDER BY u.id, r.name";
        java.util.Map<Integer, StringBuilder> rolesByUser = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, UserRow> users = new java.util.LinkedHashMap<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                users.putIfAbsent(id, new UserRow(
                        id,
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getInt("active") == 1,
                        ""
                ));
                String roleName = rs.getString("role_name");
                if (roleName != null && !roleName.isBlank()) {
                    rolesByUser.computeIfAbsent(id, k -> new StringBuilder());
                    StringBuilder sb = rolesByUser.get(id);
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(roleName);
                }
            }
            List<UserRow> out = new ArrayList<>();
            for (UserRow u : users.values()) {
                String roles = rolesByUser.getOrDefault(u.id(), new StringBuilder()).toString();
                out.add(new UserRow(u.id(), u.username(), u.fullName(), u.active(), roles));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load users", ex);
        }
    }

    public List<RoleRow> listRoles() {
        List<RoleRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT id, name, description FROM roles ORDER BY name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new RoleRow(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load roles", ex);
        }
    }

    public Set<Integer> roleIdsForUser(int userId) {
        Set<Integer> ids = new LinkedHashSet<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT role_id FROM user_roles WHERE user_id = ? ORDER BY role_id")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
            return ids;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load user roles", ex);
        }
    }

//    public void createUser(Integer actorUserId, String username, String password, String fullName, boolean active, Set<Integer> roleIds) {
    public void createUser(int actorUserId, String username, String password, String fullName, boolean active, Set<Integer> roleIds, byte[] photo) {
//        String sql = "INSERT INTO users(username, password_hash, full_name, active) VALUES(?,?,?,?)";
        String sql = "INSERT INTO users (username, password_hash, full_name, photo, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            int userId;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username.trim());
                ps.setString(2, Passwords.hash(password));
                ps.setString(3, fullName == null ? null : fullName.trim());
                if (photo != null && photo.length > 0) {
                    ps.setBytes(4, photo);
                } else {
                    ps.setNull(4, java.sql.Types.BLOB);
                }
                ps.setInt(5, active ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    userId = rs.getInt(1);
                }
            }
            saveUserRoles(con, userId, roleIds);
            con.commit();
            new AuditService().log(actorUserId, "USER_CREATE", "Created user: " + username);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create user", ex);
        }
    }

//    public void updateUser(Integer actorUserId, int userId, String username, String fullName, boolean active, Set<Integer> roleIds) {
    public void updateUser(int actorUserId, int userId, String username, String fullName, boolean active, Set<Integer> roleIds, byte[] photo) {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET username = ?, full_name = ?, photo = ?, active = ? WHERE id = ?")) {
                ps.setString(1, username.trim());
                ps.setString(2, fullName == null ? null : fullName.trim());
                
                if (photo != null && photo.length > 0) {
                    ps.setBytes(3, photo);
                } else {
                    ps.setNull(3, java.sql.Types.BLOB);
                }
                ps.setInt(4, active ? 1 : 0);
                ps.setInt(5, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM user_roles WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            saveUserRoles(con, userId, roleIds);
            con.commit();
            new AuditService().log(actorUserId, "USER_UPDATE", "Updated user id=" + userId + " username=" + username);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update user", ex);
        }
    }

    public void deleteUser(Integer actorUserId, int userId) {
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            new AuditService().log(actorUserId, "USER_DELETE", "Deleted user id=" + userId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete user", ex);
        }
    }

    public void resetPassword(Integer actorUserId, int userId, String newPassword) {
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, Passwords.hash(newPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
            new AuditService().log(actorUserId, "PASSWORD_RESET", "Reset password for user id=" + userId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to reset password", ex);
        }
    }

    public boolean verifyAndChangePassword(int userId, String oldPassword, String newPassword) {
        try (Connection con = Db.getConnection(); PreparedStatement check = con.prepareStatement("SELECT password_hash FROM users WHERE id = ?")) {
            check.setInt(1, userId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                if (!Passwords.matches(oldPassword, rs.getString(1))) {
                    return false;
                }
            }
            try (PreparedStatement update = con.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                update.setString(1, Passwords.hash(newPassword));
                update.setInt(2, userId);
                update.executeUpdate();
            }
            return true;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to change password", ex);
        }
    }

    public int countUsers() {
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM users"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to count users", ex);
        }
    }

    private void saveUserRoles(Connection con, int userId, Set<Integer> roleIds) throws Exception {
        if (roleIds == null) {
            roleIds = new HashSet<>();
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO user_roles(user_id, role_id) VALUES(?, ?)")) {
            for (Integer roleId : roleIds) {
                ps.setInt(1, userId);
                ps.setInt(2, roleId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public byte[] getUserPhoto(int userId) throws Exception {
        String sql = "SELECT photo FROM users WHERE id = ?";

        try (java.sql.Connection con = com.aldrin.ensarium.db.Db.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("photo");
                }
            }
        }
        return null;
    }
}
