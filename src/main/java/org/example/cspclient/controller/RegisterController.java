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
    public void initialize() {}

    @FXML
    public void onRegister(ActionEvent e) {
        String name = nameField.getText();
        String email = emailField.getText();
        String pwd = passwordField.getText();
        if (Validation.isNullOrBlank(name) || Validation.isNullOrBlank(email) || Validation.isNullOrBlank(pwd)) {
            AlertUtils.error("Помилка", "Заповніть усі поля");
            return;
        }
        try {
            User u = ServiceLocator.getApiClient().register(name, email, pwd);
            AlertUtils.info("Успіх", "Користувача створено: " + u.getEmail());
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadLoginScene());
        } catch (Exception ex) {
            AlertUtils.error("Реєстрація", ex.getMessage());
        }
    }

    @FXML
    public void backToLogin(ActionEvent e) {
        try {
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadLoginScene());
        } catch (Exception ex) {
            AlertUtils.error("Помилка", ex.getMessage());
        }
    }
}
