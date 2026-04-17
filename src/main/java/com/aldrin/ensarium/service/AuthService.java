package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.shift.AuthDao;
import com.aldrin.ensarium.util.Passwords;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

public class AuthService {

    private final AuditService auditService = new AuditService();

    public Session login(String username, String rawPassword) {
        return loginByPassword(username, rawPassword, true);
    }

//    public Session autoLogin(String username, String storedPasswordHash) {
//        return loginByStoredHash(username, storedPasswordHash, false);
//    }
    public Session autoLogin(String username, String savedPasswordHash) {
    if (username == null || username.isBlank() || savedPasswordHash == null || savedPasswordHash.isBlank()) {
        return null;
    }

    String sql = "SELECT id, username, full_name, password_hash " +
                 "FROM users WHERE username = ? AND active = 1";

    try (Connection con = Db.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, username);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }

            int userId = rs.getInt("id");
            String dbPasswordHash = rs.getString("password_hash");

            if (dbPasswordHash == null || dbPasswordHash.isBlank()) {
                return null;
            }

            // compare stored hash to saved hash directly
            if (!dbPasswordHash.equals(savedPasswordHash)) {
                return null;
            }

            Session session = new Session(
                    userId,
                    rs.getString("username"),
                    rs.getString("full_name"),
                    loadPermissions(con, userId)
            );

            try {
                AuthDao auth = new AuthDao();
                auth.login(rs.getString("username"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return session;
        }

    } catch (Exception ex) {
        ex.printStackTrace();
        return null;
    }
}



    
    public String getStoredPasswordHash(String username) {
    String sql = "SELECT password_hash FROM users WHERE username = ? AND active = 1";

    try (Connection con = Db.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, username);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    }

    return null;
}
    
    

    
    private Session loginByPassword(String username, String rawPassword, boolean auditLogin) {
    String sql = "SELECT id, username, full_name, password_hash FROM users WHERE username = ? AND active = 1";
    try (Connection con = Db.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;

            int userId = rs.getInt("id");
            String storedHash = rs.getString("password_hash");

            if (storedHash == null || storedHash.trim().isEmpty()) {
                return null;
            }

            if (!Passwords.matches(rawPassword, storedHash)) {
                return null;
            }

            migrateLegacyHashIfNeeded(con, userId, rawPassword, storedHash);

            Session session = new Session(
                    userId,
                    rs.getString("username"),
                    rs.getString("full_name"),
                    loadPermissions(con, userId)
            );

            try {
                AuthDao auth = new AuthDao();
                auth.login(rs.getString("username"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (auditLogin) {
                auditService.log(userId, "LOGIN", "User logged in");
            }

            return session;
        }
    } catch (Exception ex) {
        throw new IllegalStateException("Unable to login", ex);
    }
}

    private Session loginByStoredHash(String username, String storedPasswordHash, boolean auditLogin) {
        String sql = "SELECT id, username, full_name, password_hash FROM users WHERE username = ? AND active = 1";
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int userId = rs.getInt("id");
                String dbStoredHash = rs.getString("password_hash");
                if (dbStoredHash == null || !dbStoredHash.equals(storedPasswordHash)) {
                    return null;
                }
                Session session = new Session(
                        userId,
                        rs.getString("username"),
                        rs.getString("full_name"),
                        loadPermissions(con, userId)
                );
                AuthDao auth = new AuthDao();
                auth.login(rs.getString("username"));
                if (auditLogin) {
                    auditService.log(userId, "LOGIN", "User logged in");
//                    new AuthDao().login(rs.getString("username"));
                }
                return session;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to login", ex);
        }
    }

    private void migrateLegacyHashIfNeeded(Connection con, int userId, String rawPassword, String storedHash) throws Exception {
        if (Passwords.isModernHash(storedHash)) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, Passwords.hash(rawPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void logout(Session session) {
        if (session != null) {
            auditService.log(session.userId(), "LOGOUT", "User logged out");
        }
    }

    private Set<String> loadPermissions(Connection con, int userId) throws Exception {
        String sql = "SELECT DISTINCT p.code "
                + "FROM permissions p "
                + "JOIN role_permissions rp ON rp.permission_id = p.id "
                + "JOIN user_roles ur ON ur.role_id = rp.role_id "
                + "WHERE ur.user_id = ?";
        Set<String> permissions = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString(1));
                }
            }
        }
        return permissions;
    }
}
