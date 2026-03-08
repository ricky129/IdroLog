package org.example.idrolog.server.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.idrolog.core.model.WeatherSnapshot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApiTwoClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final double latitude;
    private final double longitude;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public ApiTwoClient(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public List<WeatherSnapshot> fetchRain() throws Exception {
        URI uri = new URI(
                "https",
                "api.open-meteo.com",
                "/v1/forecast",
                "latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&hourly=precipitation" +
                        "&past_days=2" +
                        "&forecast_days=1" +
                        "&timezone=Europe/Rome",
                null
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Errore HTTP Open-Meteo: " + response.statusCode());

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode times = root.path("hourly").path("time");
        JsonNode precips = root.path("hourly").path("precipitation");

        List<WeatherSnapshot> results = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            String timeStr = times.get(i).asText();
            double value = precips.get(i).asDouble();

            Instant timestamp = LocalDateTime.parse(timeStr, FORMATTER)
                    .atZone(ZoneId.of("Europe/Rome"))
                    .toInstant();

            results.add(new WeatherSnapshot(value, timestamp));
        }
        return results;
    }
}
