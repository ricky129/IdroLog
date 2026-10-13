package org.example.idrolog.server.db;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseManager {
    private final Connection connection;
    public static final String TABLE_RIVER = "river_levels";
    public static final String TABLE_PRECIPITATION = "precipitation";

    public DatabaseManager(String dbPath) throws SQLException {
        var config = new org.sqlite.SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setTempStore(SQLiteConfig.TempStore.MEMORY);
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath, config.toProperties());
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
