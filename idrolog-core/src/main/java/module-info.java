module org.example.idrolog.core {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    opens org.example.idrolog.core.model to com.fasterxml.jackson.databind;
    
    exports org.example.idrolog.core.model;
    exports org.example.idrolog.core.processing;
}
