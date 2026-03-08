package org.example.idrolog.server.scheduler;

import org.example.idrolog.server.api.ApiOneClient;
import org.example.idrolog.server.api.ApiTwoClient;
import org.example.idrolog.server.db.DatabaseManager;
import org.example.idrolog.server.db.SnapshotsRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataCollectorService {

    private final ApiOneClient apiOneClient;
    private final ApiTwoClient apiTwoClient;
    private final SnapshotsRepository repository;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public DataCollectorService(ApiOneClient apiOneClient, ApiTwoClient apiTwoClient, SnapshotsRepository repository) {
        this.apiOneClient = apiOneClient;
        this.apiTwoClient = apiTwoClient;
        this.repository = repository;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::collectOne, 0, 15, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::collectTwo, 0, 60, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void collectOne() {
        try {
            var snapshot = apiOneClient.fetchHydro();
            if (snapshot != null)
                repository.save(snapshot, DatabaseManager.TABLE_RIVER);
        } catch (Exception e) {
            System.err.println("Errore durante la raccolta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void collectTwo() {
        try {
            var snapshots = apiTwoClient.fetchRain();
            if (snapshots != null) {
                for (var snapshot : snapshots) {
                    repository.save(snapshot, DatabaseManager.TABLE_PRECIPITATION);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la raccolta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}