package org.example.idrolog.core.dto;

import org.example.idrolog.core.model.WeatherSnapshot;
import java.util.List;

public record SnapshotResponse(List<WeatherSnapshot> snapshots, int count) {}
