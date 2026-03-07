package org.example.meteolino.server;

import org.example.meteolino.server.api.ApiOneClient;
import org.example.meteolino.server.api.ApiTwoClient;
import org.example.meteolino.server.db.DatabaseManager;
import org.example.meteolino.server.db.SnapshotsRepository;
import org.example.meteolino.server.http.ApiServer;
import org.example.meteolino.server.scheduler.DataCollectorService;

import java.sql.SQLException;

public class ServerMain {
    public static void main(String[] args) throws SQLException {
        String dbPath = System.getenv().getOrDefault("DB_PATH", "data/meteolino.db");

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