package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.util.AlertUtils;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void onRegister(ActionEvent e) {
        try {
            var api = ServiceLocator.getApiClient();
            var user = api.register(nameField.getText(), emailField.getText(), passwordField.getText());
            ServiceLocator.setCurrentUser(user);
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadHomeScene());
        } catch (Exception ex) {
            AlertUtils.error("Sign up", ex.getMessage());
        }
    }

    @FXML
    public void backToLogin(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadLoginScene());
        } catch (Exception ex) {
            AlertUtils.error("Navigation", ex.getMessage());
        }
    }
}
