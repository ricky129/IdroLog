package org.example.idrolog.server.db;

import org.example.idrolog.core.model.WeatherSnapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SnapshotsRepository {

    private final Connection connection;

    public SnapshotsRepository(DatabaseManager dbManager) {
        this.connection = dbManager.getConnection();
    }

    public void save(WeatherSnapshot snapshot, String table) throws SQLException {
        if (!DatabaseManager.TABLE_RIVER.equals(table) && !DatabaseManager.TABLE_PRECIPITATION.equals(table)) {
            throw new IllegalArgumentException("Invalid table name: " + table);
        }

        String insertSQL = "INSERT OR IGNORE INTO " + table + " (value, timestamp) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setDouble(1, snapshot.value());
            stmt.setString(2, snapshot.timestamp().toString());
            stmt.executeUpdate();
        }
    }

    public List<WeatherSnapshot> getLastHours(int hours, String table) throws SQLException {
        if (!DatabaseManager.TABLE_RIVER.equals(table) && !DatabaseManager.TABLE_PRECIPITATION.equals(table)) {
            throw new IllegalArgumentException("Invalid table name: " + table);
        }

        String sql =
                "SELECT value, timestamp FROM " + table + " WHERE timestamp >= strftime('%Y-%m-%dT%H:%M:%SZ', datetime('now', ?))" + " ORDER BY timestamp ASC";

        List<WeatherSnapshot> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "-" + hours + " hours");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new WeatherSnapshot(
                            rs.getDouble("value"),
                            Instant.parse(rs.getString("timestamp"))
                    ));
                }
            }
        }

        return results;
    }
}