module org.example.cspclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires java.net.http;
    requires java.desktop;
    requires jdk.jsobject;

    requires com.fasterxml.jackson.databind;
    requires spring.websocket;
    requires spring.messaging;
    requires spring.core;
    requires com.fasterxml.jackson.annotation;

    exports com.example.messenger;

    opens com.example.messenger.ui.controllers to javafx.fxml;
    opens com.example.messenger.dto to com.fasterxml.jackson.databind;
    opens com.example.messenger.dto.convers to com.fasterxml.jackson.databind;
    opens com.example.messenger.dto.dtgroup to com.fasterxml.jackson.databind;
    opens com.example.messenger.dto.dtuser to com.fasterxml.jackson.databind;
    opens com.example.messenger.dto.dttask to com.fasterxml.jackson.databind;
    opens com.example.messenger.net to com.fasterxml.jackson.databind;

    exports com.example.messenger.net;
}