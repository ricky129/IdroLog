module org.example.meteolino.core {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    opens org.example.meteolino.core.model to com.fasterxml.jackson.databind;
    
    exports org.example.meteolino.core.model;
    exports org.example.meteolino.core.processing;
}
