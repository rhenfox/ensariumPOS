package com.aldrin.ensarium.db;

import com.aldrin.ensarium.security.PermissionCodes;
import com.aldrin.ensarium.util.Passwords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DatabaseBootstrap {

    private static final String MAIN_BOOTSTRAP_TABLE = "USERS";

    public static final String TABLE_USERS = "USERS";
    public static final String TABLE_ROLES = "ROLES";
    public static final String TABLE_PERMISSIONS = "PERMISSIONS";
    public static final String TABLE_USER_ROLES = "USER_ROLES";
    public static final String TABLE_ROLE_PERMISSIONS = "ROLE_PERMISSIONS";
    public static final String TABLE_AUDIT_LOG = "AUDIT_LOG";
    public static final String TABLE_DATABASE_TRAIL = "DATABASE_TRAIL";

    public static final String CREATE_DATABASE_TRAIL = "CREATE TABLE database_trail ("
            + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
            + "location VARCHAR(255) NOT NULL,"
            + "date_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "transactions INT NOT NULL DEFAULT 0"
            + ")";

    public DatabaseBootstrap() {
    }

    public static void initialize() {
        try (Connection connection = Db.getBootstrapConnection()) {
            new DatabaseBootstrap().initialize(connection);
        } catch (Exception ex) {
            throw new IllegalStateException("Database initialization failed", ex);
        }
    }

    public void initialize(Connection connection) throws Exception {
        if (!tableExists(connection, MAIN_BOOTSTRAP_TABLE)) {
            runDatabasePropertySetup(connection);
            runSchema(connection);
        } else {
            migrateIfNeeded(connection);
        }
        seed(connection);
    }

    private void runDatabasePropertySetup(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.language.sequence.preallocator', '1')");
        } catch (SQLException ex) {
            // Safe to ignore when Derby rejects duplicate property application in some environments.
        }
    }

    private void runSchema(Connection connection) throws IOException, SQLException {
        runSqlScript(connection, "/schema/derby-schema.sql");
    }

    private void migrateIfNeeded(Connection connection) throws SQLException {
        // Reserved for future schema migration steps.
        // Current bootstrap is additive and the seed process is idempotent.
        runDatabasePropertySetup(connection);
        ensureAdditiveTables(connection);
    }

    private void seed(Connection connection) throws SQLException {
        seedPermissions(connection);
        migrateLegacySidebarPermissions(connection);
        seedRoles(connection);
        seedUsers(connection);
    }

    private void ensureAdditiveTables(Connection connection) throws SQLException {
        if (!tableExists(connection, TABLE_USERS)) {
            createTable(connection, "CREATE TABLE users ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password_hash VARCHAR(100) NOT NULL,"
                    + "full_name VARCHAR(120),"
                    + "photo BLOB(16777216),"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT ck_users_active CHECK (active IN (0,1))"
                    + ")");
        }
        if (!tableExists(connection, TABLE_ROLES)) {
            createTable(connection, "CREATE TABLE roles ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "name VARCHAR(60) NOT NULL UNIQUE,"
                    + "description VARCHAR(200),"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");
        }
        if (!tableExists(connection, TABLE_PERMISSIONS)) {
            createTable(connection, "CREATE TABLE permissions ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "code VARCHAR(80) NOT NULL UNIQUE,"
                    + "description VARCHAR(200)"
                    + ")");
        }
        if (!tableExists(connection, TABLE_USER_ROLES)) {
            createTable(connection, "CREATE TABLE user_roles ("
                    + "user_id INT NOT NULL,"
                    + "role_id INT NOT NULL,"
                    + "PRIMARY KEY (user_id, role_id),"
                    + "CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE"
                    + ")");
        }
        if (!tableExists(connection, TABLE_ROLE_PERMISSIONS)) {
            createTable(connection, "CREATE TABLE role_permissions ("
                    + "role_id INT NOT NULL,"
                    + "permission_id INT NOT NULL,"
                    + "PRIMARY KEY (role_id, permission_id),"
                    + "CONSTRAINT fk_role_perms_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,"
                    + "CONSTRAINT fk_role_perms_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE"
                    + ")");
        }
        if (!tableExists(connection, TABLE_AUDIT_LOG)) {
            createTable(connection, "CREATE TABLE audit_log ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "actor_user_id INT,"
                    + "action_code VARCHAR(30) NOT NULL,"
                    + "details VARCHAR(1000),"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL"
                    + ")");
        }
        if (!tableExists(connection, TABLE_DATABASE_TRAIL)) {
            createTable(connection, CREATE_DATABASE_TRAIL);
        }
        if (!tableExists(connection, "STORE")) {
            createTable(connection, "CREATE TABLE store ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "code VARCHAR(30) NOT NULL UNIQUE,"
                    + "name VARCHAR(120) NOT NULL,"
                    + "address VARCHAR(250),"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT ck_store_active CHECK (active IN (0,1))"
                    + ")");
        }
        if (!tableExists(connection, "TERMINAL")) {
            createTable(connection, "CREATE TABLE terminal ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "store_id INT NOT NULL,"
                    + "code VARCHAR(30) NOT NULL,"
                    + "name VARCHAR(120),"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT fk_terminal_store FOREIGN KEY (store_id) REFERENCES store(id),"
                    + "CONSTRAINT uq_terminal_store_code UNIQUE (store_id, code),"
                    + "CONSTRAINT ck_terminal_active CHECK (active IN (0,1))"
                    + ")");
        }
        if (!tableExists(connection, "POS_SHIFT")) {
            createTable(connection, "CREATE TABLE pos_shift ("
                    + "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "store_id INT NOT NULL,"
                    + "terminal_id INT NOT NULL,"
                    + "opened_by INT NOT NULL,"
                    + "opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "closed_by INT,"
                    + "closed_at TIMESTAMP,"
                    + "opening_cash DECIMAL(19,4) NOT NULL DEFAULT 0,"
                    + "closing_cash DECIMAL(19,4),"
                    + "status VARCHAR(20) NOT NULL DEFAULT 'OPEN',"
                    + "remarks VARCHAR(300),"
                    + "CONSTRAINT fk_shift_store FOREIGN KEY (store_id) REFERENCES store(id),"
                    + "CONSTRAINT fk_shift_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),"
                    + "CONSTRAINT fk_shift_opened_by FOREIGN KEY (opened_by) REFERENCES users(id),"
                    + "CONSTRAINT fk_shift_closed_by FOREIGN KEY (closed_by) REFERENCES users(id),"
                    + "CONSTRAINT ck_shift_status CHECK (status IN ('OPEN','CLOSED'))"
                    + ")");
        }
        if (!tableExists(connection, "TAXPAYER_PROFILE")) {
            createTable(connection, "CREATE TABLE taxpayer_profile ("
                    + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "registered_name VARCHAR(160) NOT NULL,"
                    + "trade_name VARCHAR(160),"
                    + "tin_no VARCHAR(20) NOT NULL UNIQUE,"
                    + "head_office_address VARCHAR(250) NOT NULL,"
                    + "vat_registration_type VARCHAR(10) NOT NULL DEFAULT 'VAT',"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT ck_tp_vat_type CHECK (vat_registration_type IN ('VAT','NONVAT')),"
                    + "CONSTRAINT ck_tp_active CHECK (active IN (0,1))"
                    + ")");
        }
        if (!tableExists(connection, "STORE_FISCAL_PROFILE")) {
            createTable(connection, "CREATE TABLE store_fiscal_profile ("
                    + "store_id INT PRIMARY KEY,"
                    + "taxpayer_profile_id INT NOT NULL,"
                    + "branch_code VARCHAR(5) NOT NULL,"
                    + "registered_business_address VARCHAR(250) NOT NULL,"
                    + "pos_vendor_name VARCHAR(160),"
                    + "pos_vendor_tin_no VARCHAR(20),"
                    + "pos_vendor_address VARCHAR(250),"
                    + "supplier_accreditation_no VARCHAR(60),"
                    + "accreditation_issued_at DATE,"
                    + "accreditation_valid_until DATE,"
                    + "bir_permit_to_use_no VARCHAR(60),"
                    + "permit_to_use_issued_at DATE,"
                    + "atp_no VARCHAR(60),"
                    + "atp_issued_at DATE,"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT fk_sfp_store FOREIGN KEY (store_id) REFERENCES store(id),"
                    + "CONSTRAINT fk_sfp_taxpayer FOREIGN KEY (taxpayer_profile_id) REFERENCES taxpayer_profile(id),"
                    + "CONSTRAINT ck_sfp_active CHECK (active IN (0,1))"
                    + ")");
        }
        if (!tableExists(connection, "TERMINAL_FISCAL_SERIES")) {
            createTable(connection, "CREATE TABLE terminal_fiscal_series ("
                    + "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "terminal_id INT NOT NULL,"
                    + "doc_type VARCHAR(20) NOT NULL DEFAULT 'INVOICE',"
                    + "prefix VARCHAR(20),"
                    + "serial_from BIGINT NOT NULL,"
                    + "serial_to BIGINT NOT NULL,"
                    + "next_serial BIGINT NOT NULL,"
                    + "active SMALLINT NOT NULL DEFAULT 1,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "CONSTRAINT fk_tfs_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),"
                    + "CONSTRAINT uq_tfs UNIQUE (terminal_id, doc_type, serial_from, serial_to),"
                    + "CONSTRAINT ck_tfs_doc CHECK (doc_type IN ('INVOICE','SUPP_RECEIPT')) ,"
                    + "CONSTRAINT ck_tfs_active CHECK (active IN (0,1)),"
                    + "CONSTRAINT ck_tfs_range CHECK (serial_from > 0 AND serial_to >= serial_from AND next_serial >= serial_from AND next_serial <= serial_to + 1)"
                    + ")");
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getTables(null, null, tableName.toLowerCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private void runSqlScript(Connection connection, String resourcePath) throws IOException, SQLException {
        try (InputStream in = DatabaseBootstrap.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("SQL script not found: " + resourcePath);
            }
            String sql = readAll(in);
            for (String statement : splitSqlStatements(sql)) {
                String executable = normalizeExecutableSql(statement);
                if (executable.isBlank()) {
                    continue;
                }
                try (Statement st = connection.createStatement()) {
                    st.execute(executable);
                }
            }
        }
    }

    private String normalizeExecutableSql(String statement) {
        if (statement == null) {
            return "";
        }
        String sql = statement.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        return sql;
    }

    private String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--")) {
                    continue;
                }
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inTriggerBlock = false;

        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(line);

            if (!inTriggerBlock && current.toString().trim().toUpperCase().startsWith("CREATE TRIGGER ")) {
                inTriggerBlock = true;
            }

            if (inTriggerBlock) {
                if (trimmed.equalsIgnoreCase("END;")) {
                    statements.add(current.toString().trim());
                    current.setLength(0);
                    inTriggerBlock = false;
                }
                continue;
            }

            if (trimmed.endsWith(";")) {
                statements.add(current.toString().trim());
                current.setLength(0);
            }
        }

        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }

    private void createTable(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }

    private void seedPermissions(Connection con) throws SQLException {
        Map<String, String> perms = new LinkedHashMap<>();
        perms.put(PermissionCodes.DASH, "Dashboard page");
        perms.put(PermissionCodes.SALES, "Sales page");
        perms.put(PermissionCodes.SHIFT, "Shift page");
        perms.put(PermissionCodes.STOCKIN, "Stock-in products");
        perms.put(PermissionCodes.ORDER, "Order products");
        perms.put(PermissionCodes.CHARTS, "Charts page");
        perms.put(PermissionCodes.INVENTORY, "TRANSACTION");
        perms.put(PermissionCodes.INVENTORY_TXN, "Transaction");
        perms.put(PermissionCodes.INVENTORY_ONHAND, "On-hand");
        perms.put(PermissionCodes.MAINTENANCE, "Maintenance page");
        perms.put(PermissionCodes.STATUS, "Inventory page");
        perms.put(PermissionCodes.CUSTOMER, "Customer page");
        perms.put(PermissionCodes.SUPPLIER, "Supplier page");
        perms.put(PermissionCodes.MASTER_DATA, "Master data page");
        perms.put(PermissionCodes.PRINTER, "Printer page");
        perms.put(PermissionCodes.PAYMENT_METHOD, "payment methods page");
        perms.put(PermissionCodes.PRODUCTS, "Products page");
        perms.put(PermissionCodes.STORE, "Store page");
        perms.put(PermissionCodes.FISCAL_BIR, "Fiscal (BIR) page");
        perms.put(PermissionCodes.SETUP_SALE, "Setup sale");
        perms.put(PermissionCodes.SETUP_INVENTORY, "Setup inventory");
        perms.put(PermissionCodes.USERS_PAGE, "Users page");
        perms.put(PermissionCodes.ROLES_PERMS_PAGE, "Roles and permissions page");
        perms.put(PermissionCodes.AUDIT_PAGE, "Audit log page");
        perms.put(PermissionCodes.USER_WRITE, "Create/update/delete users");
        perms.put(PermissionCodes.ROLE_WRITE, "Create/update/delete roles and grant permissions");
        perms.put(PermissionCodes.BIR_TAX, "BIR Tax");
        perms.put(PermissionCodes.TAX_SUMMARY, "Tax summary");
        perms.put(PermissionCodes.POS_PROFIT, "POS Profit");
        perms.put(PermissionCodes.FINANCIAL, "Finanacial");

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO permissions(code, description) VALUES(?, ?)")) {
            for (var e : perms.entrySet()) {
                if (exists(con, "SELECT 1 FROM permissions WHERE code = ?", e.getKey())) {
                    continue;
                }
                ps.setString(1, e.getKey());
                ps.setString(2, e.getValue());
                ps.executeUpdate();
            }
        }
    }

    private void migrateLegacySidebarPermissions(Connection con) throws SQLException {
        migrateLegacyPermission(con, "USER_READ", PermissionCodes.USERS_PAGE, "Users page");
        migrateLegacyPermission(con, "ROLE_READ", PermissionCodes.ROLES_PERMS_PAGE, "Roles and permissions page");
        migrateLegacyPermission(con, "AUDIT_VIEW", PermissionCodes.AUDIT_PAGE, "Audit log page");
    }

    private void migrateLegacyPermission(Connection con, String legacyCode, String newCode, String newDescription) throws SQLException {
        Integer legacyPermId = permissionId(con, legacyCode);
        if (legacyPermId == null) {
            return;
        }

        Integer newPermId = permissionId(con, newCode);
        if (newPermId == null) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO permissions(code, description) VALUES(?, ?)")) {
                ps.setString(1, newCode);
                ps.setString(2, newDescription);
                ps.executeUpdate();
            }
            newPermId = permissionId(con, newCode);
        }

        if (newPermId == null) {
            return;
        }

        try (PreparedStatement ps = con.prepareStatement("SELECT role_id FROM role_permissions WHERE permission_id = ?")) {
            ps.setInt(1, legacyPermId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roleId = rs.getInt(1);
                    if (exists(con, "SELECT 1 FROM role_permissions WHERE role_id = ? AND permission_id = ?", roleId, newPermId)) {
                        continue;
                    }
                    try (PreparedStatement ins = con.prepareStatement("INSERT INTO role_permissions(role_id, permission_id) VALUES(?, ?)")) {
                        ins.setInt(1, roleId);
                        ins.setInt(2, newPermId);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    private void seedRoles(Connection con) throws SQLException {
        ensureRole(con, "ADMIN", "Full access");
        ensureRole(con, "MANAGER", "Business operations and read-only admin access");
        ensureRole(con, "CASHIER", "Frontline operations");

        int adminRoleId = roleId(con, "ADMIN");
        int managerRoleId = roleId(con, "MANAGER");
        int cashierRoleId = roleId(con, "CASHIER");

        grantPermissionsByCode(con, adminRoleId, PermissionCodes.allPermissionCodes());
        grantPermissionsByCode(con, managerRoleId, List.of(PermissionCodes.DASH, PermissionCodes.SALES, PermissionCodes.CUSTOMER, PermissionCodes.STATUS, PermissionCodes.PRODUCTS, PermissionCodes.STORE, PermissionCodes.FISCAL_BIR,
                PermissionCodes.USERS_PAGE, PermissionCodes.ROLES_PERMS_PAGE, PermissionCodes.AUDIT_PAGE
        ));
        grantPermissionsByCode(con, cashierRoleId, List.of(
                PermissionCodes.DASH, PermissionCodes.SALES, PermissionCodes.CUSTOMER
        ));
    }

    private void seedUsers(Connection con) throws SQLException {
        int adminId = ensureUser(con, "admin", "admin123", "System Administrator", true);
        int adminRoleId = roleId(con, "ADMIN");
        ensureUserRole(con, adminId, adminRoleId);

        int cashierId = ensureUser(con, "cashier", "cashier123", "Sample Cashier", true);
        int cashierRoleId = roleId(con, "CASHIER");
        ensureUserRole(con, cashierId, cashierRoleId);
    }

    
    
    
    
    private int ensureUser(Connection con, String username, String rawPassword, String fullName, boolean active) throws SQLException {
    String selectSql = "SELECT id, password_hash FROM users WHERE username = ?";
    try (PreparedStatement ps = con.prepareStatement(selectSql)) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int existingId = rs.getInt("id");
                String storedHash = rs.getString("password_hash");

                // Only repair missing password hash.
                // Do NOT overwrite full_name or active for existing users.
                boolean needsPasswordRepair = (storedHash == null || storedHash.trim().isEmpty());

                if (needsPasswordRepair) {
                    try (PreparedStatement ups = con.prepareStatement(
                            "UPDATE users SET password_hash=? WHERE id=?")) {
                        ups.setString(1, Passwords.hash(rawPassword));
                        ups.setInt(2, existingId);
                        ups.executeUpdate();
                    }
                }

                return existingId;
            }
        }
    }

    try (PreparedStatement ps = con.prepareStatement(
            "INSERT INTO users(username, password_hash, full_name, active) VALUES(?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, username);
        ps.setString(2, Passwords.hash(rawPassword));
        ps.setString(3, fullName);
        ps.setInt(4, active ? 1 : 0);
        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }

    throw new SQLException("Unable to create user " + username);
}

    private void ensureRole(Connection con, String name, String description) throws SQLException {
        if (exists(con, "SELECT 1 FROM roles WHERE name = ?", name)) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO roles(name, description) VALUES(?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.executeUpdate();
        }
    }

    private void ensureUserRole(Connection con, int userId, int roleId) throws SQLException {
        if (exists(con, "SELECT 1 FROM user_roles WHERE user_id = ? AND role_id = ?", userId, roleId)) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO user_roles(user_id, role_id) VALUES(?, ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    private void grantPermissionsByCode(Connection con, int roleId, List<String> permissionCodes) throws SQLException {
        for (String code : permissionCodes) {
            Integer permId = permissionId(con, code);
            if (permId == null) {
                continue;
            }
            if (exists(con, "SELECT 1 FROM role_permissions WHERE role_id = ? AND permission_id = ?", roleId, permId)) {
                continue;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO role_permissions(role_id, permission_id) VALUES(?, ?)")) {
                ps.setInt(1, roleId);
                ps.setInt(2, permId);
                ps.executeUpdate();
            }
        }
    }

    private boolean exists(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Integer userId(Connection con, String username) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private int roleId(Connection con, String name) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM roles WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Role not found: " + name);
    }

    private Integer permissionId(Connection con, String code) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM permissions WHERE code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
