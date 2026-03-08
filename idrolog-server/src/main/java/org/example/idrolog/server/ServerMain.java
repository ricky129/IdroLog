package org.example.idrolog.server;

import org.example.idrolog.server.api.ApiOneClient;
import org.example.idrolog.server.api.ApiTwoClient;
import org.example.idrolog.server.db.DatabaseManager;
import org.example.idrolog.server.db.SnapshotsRepository;
import org.example.idrolog.server.http.ApiServer;
import org.example.idrolog.server.scheduler.DataCollectorService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

public class ServerMain {
    public static void main(String[] args) throws SQLException {
        String dbPath = System.getenv().getOrDefault("DB_PATH", "data/idrolog.db");
        Properties props = new Properties();
        Path configPath = Path.of(System.getProperty("user.home"), ".idrolog", "config.properties");
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
        } catch (Exception e) {
            System.out.println("Config not found, using defaults");
        }

        DatabaseManager db = new DatabaseManager(dbPath);
        SnapshotsRepository repository = new SnapshotsRepository(db);

        String stationId = props.getProperty("station.id");
        if (stationId == null) throw new IllegalStateException("station.id mancante in config.properties");
        ApiOneClient apiOne = new ApiOneClient(stationId);
        String latStr = props.getProperty("station.latitude");
        String lonStr = props.getProperty("station.longitude");
        if (latStr == null || lonStr == null) throw new IllegalStateException("coordinate mancanti in config.properties");
        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);
        ApiTwoClient apiTwo = new ApiTwoClient(lat, lon);

        DataCollectorService collector = new DataCollectorService(apiOne, apiTwo, repository);
        ApiServer apiServer = new ApiServer(repository);

        collector.start();
        apiServer.start(7000);
    }
}