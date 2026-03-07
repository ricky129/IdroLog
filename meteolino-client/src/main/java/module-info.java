module org.example.meteolino.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.example.meteolino.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    // Permette a JavaFX di caricare i controller FXML
    opens org.example.meteolino.client.ui.controller to javafx.fxml;
    
    // Permette a JavaFX di avviare la classe App
    exports org.example.meteolino.client.ui to javafx.graphics;
    
    exports org.example.meteolino.client;
}
