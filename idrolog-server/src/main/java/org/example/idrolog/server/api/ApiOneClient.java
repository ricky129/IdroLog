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

    public WeatherSnapshot fetch() throws Exception {
        URI uri = new URI(
                "https",
                "allertameteo.regione.emilia-romagna.it",
                "/o/api/allerta/get-time-series/",
                "stazione=53429&variabile=254,0,0/1,-,-,-/B13215",
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
                new TypeReference<List<RiverDataPoint>>() {}
        );

        if (points == null || points.isEmpty()) return null;

        RiverDataPoint last = points.getLast();
        return new WeatherSnapshot(
                "river_level",
                last.v(),
                Instant.ofEpochMilli(last.t())
        );
    }

    private record RiverDataPoint(long t, double v) {}
}