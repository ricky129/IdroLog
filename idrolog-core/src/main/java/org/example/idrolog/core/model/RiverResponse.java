package org.example.idrolog.core.model;

import java.util.List;

/**
 *
 * @author ricky
 */
public record RiverResponse(List<WeatherSnapshot> snapshots, AlertLevel alert, Trend trend) {

}
