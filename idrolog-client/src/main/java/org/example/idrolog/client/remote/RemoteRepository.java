package org.example.idrolog.client.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.idrolog.core.model.WeatherSnapshot;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class RemoteRepository {

    private static final String LOCAL_URL;
    private static final String TAILSCALE_URL;
    public static final String TABLE_RIVER = "river_levels";
    public static final String TABLE_PRECIPITATION = "precipitation";

    static {
        Properties props = new Properties();
        Path configPath = Path.of(System.getProperty("user.dir"), "config.properties");
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
        } catch (Exception e) {
            System.out.println("Config file not found, using defaults");
        }
        LOCAL_URL = props.getProperty("server.local");
        TAILSCALE_URL = props.getProperty("server.tailscale");
        if (LOCAL_URL == null || TAILSCALE_URL == null)
            throw new IllegalStateException("Server URLs missing in config.properties");
        }

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    private String resolvedBaseUrl = null;

    private String resolveBaseUrl() {
        if (resolvedBaseUrl != null) return resolvedBaseUrl;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOCAL_URL + "/api/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Using local address");
                resolvedBaseUrl = LOCAL_URL;
                return LOCAL_URL;
            }
        } catch (Exception e) {
            System.out.println("Local address unreachable, falling back to Tailscale");
        }
        resolvedBaseUrl = TAILSCALE_URL;
        return TAILSCALE_URL;
    }

    public List<WeatherSnapshot> fetch(int hours, String table) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolveBaseUrl() + "/api/" + table + "?hours=" + hours))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("HTTP Error: " + response.statusCode());
            return List.of();
        }
        return mapper.readValue(response.body(), new TypeReference<>() {
        });
    }
}
