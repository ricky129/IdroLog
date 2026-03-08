package org.example.idrolog.server;

import org.example.idrolog.server.api.ApiOneClient;
import org.example.idrolog.server.api.ApiTwoClient;
import org.example.idrolog.server.db.DatabaseManager;
import org.example.idrolog.server.db.SnapshotsRepository;
import org.example.idrolog.server.http.ApiServer;
import org.example.idrolog.server.scheduler.DataCollectorService;

import java.sql.SQLException;

public class ServerMain {
    public static void main(String[] args) throws SQLException {
        String dbPath = System.getenv().getOrDefault("DB_PATH", "data/idrolog.db");

        DatabaseManager db = new DatabaseManager(dbPath);
        SnapshotsRepository repository = new SnapshotsRepository(db);

        ApiOneClient apiOne = new ApiOneClient();
        ApiTwoClient apiTwo = new ApiTwoClient();

        DataCollectorService collector = new DataCollectorService(apiOne, apiTwo, repository);
        ApiServer apiServer = new ApiServer(repository);

        collector.start();
        apiServer.start(7000);
    }
}