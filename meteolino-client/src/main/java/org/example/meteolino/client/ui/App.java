package org.example.meteolino.client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/meteolino/client/view/dashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1024, 768);
        
        stage.setTitle("Meteolino Dashboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
