package org.example.meteolino.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// server/db/DatabaseManager.java
public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(String dbPath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        initSchema();
    }

    private void initSchema() throws SQLException {
        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS snapshots (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                source    TEXT NOT NULL,
                value     REAL NOT NULL,
                timestamp TEXT NOT NULL
            )
        """);
    }

    public Connection getConnection() { return connection; }
}
