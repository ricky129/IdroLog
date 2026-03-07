package org.example.meteolino.server.http;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.example.meteolino.server.db.SnapshotsRepository;

public class ApiServer {

    private final SnapshotsRepository repository;
    private Javalin app;

    public ApiServer(SnapshotsRepository repository) {
        this.repository = repository;
    }

    public void start(int port) {
        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(m -> {
                m.registerModule(new JavaTimeModule());
                m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }));

            config.routes.get("/", ctx -> ctx.result("Meteolino server is running"));
            config.routes.get("/api/health", ctx -> ctx.result("OK"));
            config.routes.get("/api/snapshots", ctx -> {
                int hours = Integer.parseInt(
                        ctx.queryParamAsClass("hours", String.class).getOrDefault("24")
                );
                ctx.json(repository.getLastHours(hours));
            });
        }).start(port);
    }

    public void stop() {
        if (app != null) app.stop();
    }
}