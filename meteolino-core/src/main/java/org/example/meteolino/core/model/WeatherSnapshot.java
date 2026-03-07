package org.example.meteolino.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherSnapshot(String source, double value, java.time.Instant timestamp) {
}
