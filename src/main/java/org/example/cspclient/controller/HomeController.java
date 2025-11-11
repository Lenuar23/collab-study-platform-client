package org.example.cspclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import org.example.cspclient.di.ServiceLocator;

public class HomeController {
    @FXML private TabPane tabs;

    @FXML
    public void initialize() {
        // Select Chats tab by default
        if (tabs != null) tabs.getSelectionModel().select(0);
    }

    @FXML
    public void onLogout() {
        ServiceLocator.logout();
    }
}
