package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.User;
import org.example.cspclient.util.AlertUtils;
import org.example.cspclient.util.Validation;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void onRegister(ActionEvent e) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        if (Validation.isNullOrBlank(name) || Validation.isNullOrBlank(email) || Validation.isNullOrBlank(password)) {
            AlertUtils.error("Помилка", "Заповніть усі поля");
            return;
        }
        try {
            User u = ServiceLocator.getApiClient().register(name, email, password);
            ServiceLocator.setCurrentUser(u);
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadHomeScene());
        } catch (Exception ex) {
            AlertUtils.error("Реєстрація", ex.getMessage());
        }
    }

    @FXML
    public void backToLogin(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadLoginScene());
        } catch (Exception ex) {
            AlertUtils.error("Навігація", ex.getMessage());
        }
    }
}
