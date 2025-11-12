package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.util.AlertUtils;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void onLogin(ActionEvent e) {
        try {
            var api = ServiceLocator.getApiClient();
            var opt = api.login(emailField.getText(), passwordField.getText());
            if (opt.isPresent()) {
                ServiceLocator.setCurrentUser(opt.get());
                ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadHomeScene());
            } else {
                AlertUtils.error("Sign in", "Invalid email or password.");
            }
        } catch (Exception ex) {
            AlertUtils.error("Sign in", ex.getMessage());
        }
    }

    @FXML
    public void goToRegister(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadRegisterScene());
        } catch (Exception ex) {
            AlertUtils.error("Navigation", ex.getMessage());
        }
    }
}
