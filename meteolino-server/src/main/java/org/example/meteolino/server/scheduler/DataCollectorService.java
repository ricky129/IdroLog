package org.example.meteolino.server.scheduler;

import org.example.meteolino.server.api.ApiOneClient;
import org.example.meteolino.server.api.ApiTwoClient;
import org.example.meteolino.server.db.SnapshotsRepository;

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
        scheduler.scheduleAtFixedRate(this::collect, 0, 15, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void collect() {
        try {
            var snapshotOne = apiOneClient.fetch();
            if (snapshotOne != null) repository.save(snapshotOne);

            /*var snapshotTwo = apiTwoClient.fetchD();
            if (snapshotTwo != null) repository.save(snapshotTwo);*/

        } catch (Exception e) {
            System.err.println("Errore durante la raccolta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}