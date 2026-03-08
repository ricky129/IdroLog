package org.example.idrolog.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherSnapshot(String source, double value, java.time.Instant timestamp) {
}
