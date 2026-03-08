package org.example.idrolog.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseManager {
    private final Connection connection;
    public static final String TABLE_RIVER = "river_levels";
    public static final String TABLE_PRECIPITATION = "precipitation";

    public DatabaseManager(String dbPath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        initSchema();
    }

    private void initSchema() {
        try {
            connection.createStatement().execute("""
        CREATE TABLE IF NOT EXISTS river_levels (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            value     REAL    NOT NULL,
            timestamp TEXT    NOT NULL UNIQUE
        )
    """);
            connection.createStatement().execute("""
        CREATE TABLE IF NOT EXISTS precipitation (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            value     REAL    NOT NULL,
            timestamp TEXT    NOT NULL UNIQUE
        )
    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() { return connection; }
}
