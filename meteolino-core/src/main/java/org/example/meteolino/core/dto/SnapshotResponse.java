package org.example.meteolino.core.dto;

import org.example.meteolino.core.model.WeatherSnapshot;
import java.util.List;

public record SnapshotResponse(List<WeatherSnapshot> snapshots, int count) {
}
