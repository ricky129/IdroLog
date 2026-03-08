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
import org.example.idrolog.core.model.WeatherSnapshot;
import java.util.List;

public class DashboardController {
    @FXML private Label lastUpdateLabel;
    @FXML private Label currentLevelLabel;
    @FXML private Label trendLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<Integer> hoursComboBox;
    @FXML private LineChart<String, Number> riverLevelChart;
    @FXML
    private NumberAxis levelAxis;
    @FXML private Label precipitationTitleLabel;
    @FXML private Label precipitationLabel;
    @FXML private Label precipitationStatusLabel;
    
    private final RemoteRepository remoteRepository = new RemoteRepository();

    public DashboardController() {}

    private double currentUpperBound;
    private double currentTickUnit;
    private String color = "#247ba0";
    private String status = "Stato: Normale";
    @FXML
    public void initialize() {
        levelAxis.setAutoRanging(false);
        levelAxis.setLowerBound(0.0);
        currentUpperBound = 1.5;
        levelAxis.setUpperBound(currentUpperBound);
        currentTickUnit = 0.25;
        levelAxis.setTickUnit(currentTickUnit);
        hoursComboBox.getItems().addAll(1, 2, 6, 12, 24, 48);
        hoursComboBox.setValue(6);
        hoursComboBox.setConverter(new StringConverter<Integer>() {
            @Override public String toString(Integer h) { return h + "h"; }
            @Override public Integer fromString(String s) { return Integer.parseInt(s.replace("h", "")); }
        });

        hoursComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                refreshData(newVal);
        });
        refreshData(6);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(5), e -> refreshData(hoursComboBox.getValue()))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refreshData(int hoursField) {
        System.out.println("refreshData called with hours: " + hoursField);
        try {
            List<WeatherSnapshot> riverData = remoteRepository.fetch(hoursField, RemoteRepository.TABLE_RIVER);
            if (!riverData.isEmpty()) {
                WeatherSnapshot last = riverData.getLast();
                currentLevelLabel.setText(String.format("%.2f m", last.value()));

                if (last.value() > 1.5) {
                    currentUpperBound*=2;
                    levelAxis.setUpperBound(currentUpperBound);
                    currentTickUnit*=2;
                    levelAxis.setTickUnit(currentTickUnit);
                }

                if (last.value() >= 3.0)
                    color = "#fb3640";
                else if (last.value() >= 2.0)
                    color = "#f5a623";

                if (last.value() >= 3.0)
                    status = "Stato: Allerta Rossa";
                else if (last.value() >= 2.0)
                    status = "Stato: Allerta Arancione";

                currentLevelLabel.setStyle("-fx-text-fill: " + color + ";");
                statusLabel.setText(status);
                lastUpdateLabel.setText(last.getLocalTime());

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (WeatherSnapshot s : riverData)
                    series.getData().add(new XYChart.Data<>(s.getLocalTime(), s.value()));

                riverLevelChart.getData().setAll(series);
            }

            List<WeatherSnapshot> rainData = remoteRepository.fetch(hoursField, RemoteRepository.TABLE_PRECIPITATION);
            precipitationTitleLabel.setText("Precipitazioni " + hoursField + "h");

            if (!rainData.isEmpty()) {
                double totalRain = rainData.stream().mapToDouble(WeatherSnapshot::value).sum();
                precipitationLabel.setText(String.format("%.1f mm", totalRain));
                precipitationStatusLabel.setText("Sensore attivo");
            } else {
                precipitationLabel.setText("0.0 mm");
                precipitationStatusLabel.setText("Nessun dato");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
