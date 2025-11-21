module org.example.cspclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports com.example.messenger;

    opens com.example.messenger.ui.controllers to javafx.fxml;
    opens com.example.messenger.dto to com.fasterxml.jackson.databind;
}
