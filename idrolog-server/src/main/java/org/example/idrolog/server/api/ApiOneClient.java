package org.example.idrolog.server.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.idrolog.core.model.WeatherSnapshot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

public class ApiOneClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String stationId;

    public ApiOneClient(String stationId) {
        this.stationId = stationId;
    }

    public WeatherSnapshot fetchHydro() throws Exception {
        URI uri = new URI(
                "https",
                "allertameteo.regione.emilia-romagna.it",
                "/o/api/allerta/get-time-series/",
                "stazione=" + stationId + "&variabile=254,0,0/1,-,-,-/B13215",
                null
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Errore HTTP: " + response.statusCode());

        List<RiverDataPoint> points = objectMapper.readValue(
                response.body(),
                new TypeReference<>() {
                }
        );

        if (points == null || points.isEmpty())
            return null;

        RiverDataPoint last = points.getLast();
        return new WeatherSnapshot(
                last.v(),
                Instant.ofEpochMilli(last.t())
        );
    }

    private record RiverDataPoint(long t, double v) {}
}