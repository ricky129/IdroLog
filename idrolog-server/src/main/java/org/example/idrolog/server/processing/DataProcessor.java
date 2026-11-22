package org.example.idrolog.server.processing;

import java.util.List;
import org.example.idrolog.core.model.AlertLevel;
import org.example.idrolog.core.model.Trend;
import org.example.idrolog.core.model.WeatherSnapshot;

/**
 *
 * @author ricky
 */
public class DataProcessor {

    private static final double ORANGE = 2.0;
    private static final double RED = 3.0;
    private static final double TREND_EPSILON = 0.02;

    private DataProcessor() {
    }

    public static AlertLevel alertFor(List<WeatherSnapshot> river) {
        if (river.isEmpty())
            return AlertLevel.NORMAL;
        double level = river.getLast().value();
        if (level >= RED)
            return AlertLevel.RED;
        if (level >= ORANGE)
            return AlertLevel.ORANGE;
        return AlertLevel.NORMAL;
    }

    public static Trend trendOf(List<WeatherSnapshot> river) {
        if (river.size() < 2)
            return Trend.STEADY;
        double diff = river.getLast().value() - river.get(river.size() - 2).value();
        if (diff > TREND_EPSILON)
            return Trend.RISING;
        if (diff < -TREND_EPSILON)
            return Trend.FALLING;
        return Trend.STEADY;
    }

    public static double totalRain(List<WeatherSnapshot> rain) {
        return rain.stream().mapToDouble(WeatherSnapshot::value).sum();
    }
}
