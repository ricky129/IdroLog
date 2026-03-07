package org.example.meteolino.server.db;

import org.example.meteolino.core.model.WeatherSnapshot;

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

    public void save(WeatherSnapshot snapshot) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO snapshots (source, value, timestamp) VALUES (?, ?, ?)"
        );
        stmt.setString(1, snapshot.source());
        stmt.setDouble(2, snapshot.value());
        stmt.setString(3, snapshot.timestamp().toString());
        stmt.executeUpdate();
    }

    public List<WeatherSnapshot> getLastHours(int hours) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT source, value, timestamp FROM snapshots " +
                        "WHERE timestamp >= datetime('now', ?) " +
                        "ORDER BY timestamp ASC"
        );
        stmt.setString(1, "-" + hours + " hours");
        ResultSet rs = stmt.executeQuery();

        List<WeatherSnapshot> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new WeatherSnapshot(
                    rs.getString("source"),
                    rs.getDouble("value"),
                    Instant.parse(rs.getString("timestamp"))
            ));
        }
        return results;
    }
}