package org.example.idrolog.core.model;

import java.util.List;

/**
 *
 * @author ricky
 */
public record RainResponse(List<WeatherSnapshot> snapshots, double total) {

}
