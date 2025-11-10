module org.example.cspclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // Open only what's needed:
    // - controllers to JavaFX FXMLLoader
    opens org.example.cspclient.controller to javafx.fxml;
    // - model classes to Jackson for reflection-based (de)serialization
    opens org.example.cspclient.model to com.fasterxml.jackson.databind;

    // Export base package for the Application entry point
    exports org.example.cspclient;
}
