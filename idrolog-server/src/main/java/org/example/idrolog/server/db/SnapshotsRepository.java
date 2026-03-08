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
        PreparedStatement check = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + table + " WHERE value = ? AND timestamp = ?"
        );
        check.setDouble(1, snapshot.value());
        check.setString(2, snapshot.timestamp().toString());
        ResultSet rs = check.executeQuery();
        if (rs.next() && rs.getInt(1) > 0)
            return;

        PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO " + table + " (value, timestamp) VALUES (?, ?)"
        );
        stmt.setDouble(1, snapshot.value());
        stmt.setString(2, snapshot.timestamp().toString());
        stmt.executeUpdate();
    }

    public List<WeatherSnapshot> getLastHours(int hours, String table) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT value, timestamp FROM " + table +
                        " WHERE timestamp >= strftime('%Y-%m-%dT%H:%M:%SZ', datetime('now', ?))" +
                        " ORDER BY timestamp ASC"
        );
        stmt.setString(1, "-" + hours + " hours");
        ResultSet rs = stmt.executeQuery();

        List<WeatherSnapshot> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new WeatherSnapshot(
                    rs.getDouble("value"),
                    Instant.parse(rs.getString("timestamp"))
            ));
        }
        return results;
    }
}