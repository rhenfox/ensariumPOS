package com.aldrin.ensarium.shift;




import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class AuthDao {

    private static Session session;

    public Session login(String username) throws SQLException {
        String userSql = """
                SELECT id, username, password_hash, COALESCE(full_name, username) AS full_name
                FROM users
                WHERE username = ? AND active = 1
                """;

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(userSql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

//                String stored = rs.getString("password_hash");
//                if (!PasswordUtil.matches(password, stored)) {
//                    return null;
//                }
                int userId = rs.getInt("id");
                String uname = rs.getString("username");
                String fullName = rs.getString("full_name");

                TerminalInfo terminal = resolveActiveTerminal(con);
                if (terminal == null) {
                    throw new SQLException("No active terminal found. Insert at least one active store and terminal first.");
                }

                Set<String> permissions = loadPermissions(con, userId);

                session = new Session(userId, username, fullName, permissions);
                session.setStoreId(terminal.storeId);
                session.setStoreCode(terminal.storeCode);
                session.setStoreName(terminal.storeName);
                session.setTerminalId(terminal.terminalId);
                session.setTerminalCode(terminal.terminalCode);
                session.setTerminalName(terminal.terminalName);
                Session.session = session;
                return session;
//                return new Session(
//                        userId,
//                        uname,
//                        fullName,
//                        permissions,
//                        terminal.storeId(),
//                        terminal.storeCode(),
//                        terminal.storeName(),
//                        terminal.terminalId(),
//                        terminal.terminalCode(),
//                        terminal.terminalName()
//                );
            }
        }
    }

    private Set<String> loadPermissions(Connection con, int userId) throws SQLException {
        String sql = """
                SELECT DISTINCT p.code
                FROM user_roles ur
                JOIN role_permissions rp ON rp.role_id = ur.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE ur.user_id = ?
                """;

        Set<String> perms = new HashSet<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    perms.add(rs.getString("code"));
                }
            }
        }

        return perms;
    }

    private TerminalInfo resolveActiveTerminal(Connection con) throws SQLException {
        String sql = """
                SELECT s.id AS store_id,
                       s.code AS store_code,
                       s.name AS store_name,
                       t.id AS terminal_id,
                       t.code AS terminal_code,
                       COALESCE(t.name, t.code) AS terminal_name
                FROM store s
                JOIN terminal t ON t.store_id = s.id
                WHERE s.active = 1 AND t.active = 1
                ORDER BY s.id, t.id
                FETCH FIRST ROW ONLY
                """;

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            return new TerminalInfo(
                    rs.getInt("store_id"),
                    rs.getString("store_code"),
                    rs.getString("store_name"),
                    rs.getInt("terminal_id"),
                    rs.getString("terminal_code"),
                    rs.getString("terminal_name")
            );
        }
    }

    private record TerminalInfo(
            int storeId,
            String storeCode,
            String storeName,
            int terminalId,
            String terminalCode,
            String terminalName
            ) {

    }

    /**
     * @return the session
     */
    public static Session getSession() {
        return session;
    }

    /**
     * @param aSession the session to set
     */
    public static void setSession(Session aSession) {
        session = aSession;
    }
}
