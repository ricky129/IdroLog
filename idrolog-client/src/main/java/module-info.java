module org.example.idrolog.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.example.idrolog.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    // Permette a JavaFX di caricare i controller FXML
    opens org.example.idrolog.client.ui.controller to javafx.fxml;
    
    // Permette a JavaFX di avviare la classe App
    exports org.example.idrolog.client.ui to javafx.graphics;
    
    exports org.example.idrolog.client;
}
