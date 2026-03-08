package org.example.idrolog.client.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.example.idrolog.client.remote.RemoteRepository;
import org.example.idrolog.core.model.WeatherSnapshot;
import java.util.List;

public class DashboardController {
    @FXML private Label lastUpdateLabel;
    @FXML private Label currentLevelLabel;
    @FXML private Label trendLabel;
    @FXML private ComboBox<Integer> hoursComboBox;
    @FXML private LineChart<String, Number> riverLevelChart;
    
    private final RemoteRepository remoteRepository = new RemoteRepository();

    public DashboardController() {}

    @FXML
    public void initialize() throws Exception {
        hoursComboBox.getItems().addAll(6, 12, 24, 48);
        hoursComboBox.setValue(24);
        hoursComboBox.setOnAction(e -> refreshData());
        refreshData();
    }

    private void refreshData() {
        try {
            List<WeatherSnapshot> dati = remoteRepository.fetchSnapshots();
            if (dati.isEmpty()) return;

            WeatherSnapshot last = dati.get(dati.size() - 1);
            currentLevelLabel.setText(String.format("%.2f m", last.value()));
            
            String ts = last.timestamp();
            lastUpdateLabel.setText(ts.length() >= 16 ? ts.substring(11, 16) : ts);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (WeatherSnapshot s : dati) {
                String label = s.timestamp();
                if (label.length() >= 16) label = label.substring(11, 16);
                series.getData().add(new XYChart.Data<>(label, s.value()));
            }
            riverLevelChart.getData().setAll(series);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
}
