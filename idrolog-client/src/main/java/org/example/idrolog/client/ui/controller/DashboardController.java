package org.example.idrolog.client.ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.example.idrolog.client.remote.RemoteRepository;
import org.example.idrolog.core.model.RainResponse;
import org.example.idrolog.core.model.RiverResponse;
import org.example.idrolog.core.model.WeatherSnapshot;

public class DashboardController {

    @FXML
    private Label lastUpdateLabel;
    @FXML
    private Label currentLevelLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<Integer> hoursComboBox;
    @FXML
    private LineChart<String, Number> riverLevelChart;
    @FXML
    private NumberAxis levelAxis;
    @FXML
    private Label precipitationTitleLabel;
    @FXML
    private Label precipitationLabel;
    @FXML
    private Label precipitationStatusLabel;

    private final RemoteRepository remoteRepository = new RemoteRepository();

    @FXML
    public void initialize() {
        levelAxis.setAutoRanging(false);
        levelAxis.setLowerBound(0.0);
        levelAxis.setUpperBound(1.5);
        levelAxis.setTickUnit(0.25);
        hoursComboBox.getItems().addAll(1, 2, 6, 12, 24, 48);
        hoursComboBox.setValue(6);
        hoursComboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer h) {
                return h + "h";
            }

            @Override
            public Integer fromString(String s) {
                return Integer.valueOf(s.replace("h", ""));
            }
        });

        hoursComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                refreshData(newVal);
        });
        refreshData(6);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(5), e -> refreshData(hoursComboBox.getValue())));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refreshData(int hoursField) {
        System.out.println("refreshData called with hours: " + hoursField);
        try {
            RiverResponse river = remoteRepository.fetchRiver(hoursField);
            double max = river.snapshots().stream()
                    .mapToDouble(WeatherSnapshot::value)
                    .max().orElse(0);
            double upper = 1.5;
            while (upper < max)
                upper *= 2;
            levelAxis.setUpperBound(upper);
            levelAxis.setTickUnit(upper / 6);
            if (!river.snapshots().isEmpty()) {
                WeatherSnapshot last = river.snapshots().getLast();
                currentLevelLabel.setText(String.format("%.2f m", last.value()));

                String color = switch (river.alert()) {
                    case RED ->
                        "#fb3640";
                    case ORANGE ->
                        "#f5a623";
                    case NORMAL ->
                        "#247ba0";
                };
                String status = switch (river.alert()) {
                    case RED ->
                        "Stato: Allerta Rossa";
                    case ORANGE ->
                        "Stato: Allerta Arancione";
                    case NORMAL ->
                        "Stato: Normale";
                };
                currentLevelLabel.setStyle("-fx-text-fill: " + color + ";");
                statusLabel.setText(status);
                lastUpdateLabel.setText(last.getLocalTime());

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (WeatherSnapshot s : river.snapshots()) {
                    java.time.ZonedDateTime dt = s.timestamp().atZone(java.time.ZoneId.of("Europe/Rome"));
                    int min = dt.getMinute();
                    String xLabel = "";
                    switch (hoursField) {
                        case 48 -> {
                            if (min == 0) {
                                xLabel = s.getLocalTime();
                            }
                        }
                        case 24 -> {
                            if (min == 0 || min == 30) {
                                xLabel = s.getLocalTime();
                            }
                        }
                        default ->
                            xLabel = s.getLocalTime();
                    }
                    series.getData().add(new XYChart.Data<>(xLabel, s.value()));
                }

                riverLevelChart.getData().setAll(series);
            }

            RainResponse rain = remoteRepository.fetchRain(hoursField);
            precipitationTitleLabel.setText("Precipitazioni " + hoursField + "h");
            if (!rain.snapshots().isEmpty()) {
                precipitationLabel.setText(String.format("%.1f mm", rain.total()));
                precipitationStatusLabel.setText("Sensore attivo");
            } else {
                precipitationLabel.setText("0.0 mm");
                precipitationStatusLabel.setText("Nessun dato");
            }
        } catch (Exception e) {
            System.err.println("Errore aggiornamento dati: " + e.getMessage());
            statusLabel.setText("Stato: Server non raggiungibile");
        }
    }
}
