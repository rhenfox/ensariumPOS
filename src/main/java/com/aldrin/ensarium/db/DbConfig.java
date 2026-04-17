package com.aldrin.ensarium.db;



public record DbConfig(
        String host,
        int port,
        String databaseName,
        String username,
        String password,
        boolean createIfMissing
) {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 1527;
    public static final String DEFAULT_DB = "ensariumdb";

    public DbConfig {
        host = (host == null || host.isBlank()) ? DEFAULT_HOST : host.trim();
        if (port <= 0) port = DEFAULT_PORT;
        databaseName = (databaseName == null || databaseName.isBlank()) ? DEFAULT_DB : databaseName.trim();
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password;
    }

    public String jdbcUrl() {
        return "jdbc:derby://" + host + ":" + port + "/" + databaseName
                + (createIfMissing ? ";create=true" : "");
    }
}