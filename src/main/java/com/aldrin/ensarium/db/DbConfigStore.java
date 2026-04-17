package com.aldrin.ensarium.db;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public final class DbConfigStore {
    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".ensariumDB");
    private static final Path FILE = DIR.resolve("db.properties");

    private DbConfigStore() {}

    public static Optional<DbConfig> load() {
        if (!Files.exists(FILE)) {
            return Optional.empty();
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);

            String host = p.getProperty("host", DbConfig.DEFAULT_HOST).trim();
            int port = Integer.parseInt(p.getProperty("port", String.valueOf(DbConfig.DEFAULT_PORT)).trim());
            String database = p.getProperty("database", DbConfig.DEFAULT_DB).trim();
            String username = p.getProperty("username", "").trim();
            String password = p.getProperty("password", "");
            boolean create = Boolean.parseBoolean(p.getProperty("createIfMissing", "true"));

            return Optional.of(new DbConfig(host, port, database, username, password, create));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static void save(DbConfig config) {
        try {
            Files.createDirectories(DIR);

            Properties p = new Properties();
            p.setProperty("host", config.host());
            p.setProperty("port", Integer.toString(config.port()));
            p.setProperty("database", config.databaseName());
            p.setProperty("username", config.username());
            p.setProperty("password", config.password());
            p.setProperty("createIfMissing", Boolean.toString(config.createIfMissing()));

            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Ensarium Derby network database configuration");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save database configuration", ex);
        }
    }
}
