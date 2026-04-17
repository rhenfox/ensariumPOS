package com.aldrin.ensarium.db;




import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public final class Db {

    static {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private Db() {}

    private static DbConfig defaultConfig() {
        return new DbConfig(
                DbConfig.DEFAULT_HOST,
                DbConfig.DEFAULT_PORT,
                DbConfig.DEFAULT_DB,
                "",
                "",
                true
        );
    }

    public static Optional<DbConfig> loadSavedConfig() {
        return DbConfigStore.load();
    }

    public static boolean hasWorkingSavedConfig() {
        return loadSavedConfig().map(Db::testConnection).orElse(false);
    }

    public static Connection getConnection() throws SQLException {
        DbConfig config = loadSavedConfig().orElse(defaultConfig());
        return getConnection(config);
    }

    public static Connection getConnection(DbConfig config) throws SQLException {
        return getConnection(config, false);
    }

    public static Connection getConnection(DbConfig config, boolean forceCreate) throws SQLException {
        Properties props = new Properties();
        if (!config.username().isBlank()) {
            props.setProperty("user", config.username());
        }
        if (!config.password().isBlank()) {
            props.setProperty("password", config.password());
        }

        String url = buildJdbcUrl(config, forceCreate || config.createIfMissing());
        return DriverManager.getConnection(url, props);
    }

    public static Connection getBootstrapConnection() throws SQLException {
        DbConfig config = loadSavedConfig().orElseGet(() -> {
            DbConfig cfg = defaultConfig();
            DbConfigStore.save(cfg);
            return cfg;
        });

        try {
            return getConnection(config, false);
        } catch (SQLException ex) {
            return getConnection(config, true);
        }
    }

    public static boolean testConnection(DbConfig config) {
        try (Connection con = getConnection(config)) {
            return con != null && !con.isClosed();
        } catch (Exception ex) {
            return false;
        }
    }

    private static String buildJdbcUrl(DbConfig config, boolean create) {
        return "jdbc:derby://" + config.host() + ":" + config.port() + "/" + config.databaseName()
                + (create ? ";create=true" : "");
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }
}


//ghggg