module org.example.idrolog.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.example.idrolog.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens org.example.idrolog.client.ui.controller to javafx.fxml;
    
    exports org.example.idrolog.client.ui to javafx.graphics;
    
    exports org.example.idrolog.client;
}
