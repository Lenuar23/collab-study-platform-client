package org.example.cspclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.util.AlertUtils;

public class HomeController {
    @FXML private TabPane tabs;

    @FXML
    public void initialize() {
        if (tabs != null) tabs.getSelectionModel().select(0); // Chats tab default
    }

    @FXML
    public void onLogout() { ServiceLocator.logout(); }

    @FXML
    public void openUserSettings() {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadUserSettingsScene());
        } catch (Exception e) {
            AlertUtils.error("Navigation", "Failed to open User Settings: " + e.getMessage());
        }
    }
}
