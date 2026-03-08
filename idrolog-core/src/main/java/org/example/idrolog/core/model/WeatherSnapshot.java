package org.example.idrolog.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherSnapshot(double value, java.time.Instant timestamp) {
    public String getLocalTime() {
        return timestamp.atZone(ZoneId.of("Europe/Rome"))
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
