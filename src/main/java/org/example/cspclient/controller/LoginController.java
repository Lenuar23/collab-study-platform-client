package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.User;
import org.example.cspclient.util.AlertUtils;
import org.example.cspclient.util.Validation;

import java.util.Optional;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void initialize() {}

    @FXML
    public void onLogin(ActionEvent e) {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (Validation.isNullOrBlank(email) || Validation.isNullOrBlank(password)) {
            AlertUtils.error("Помилка", "Введіть email і пароль");
            return;
        }
        try {
            Optional<User> user = ServiceLocator.getApiClient().login(email, password);
            if (user.isPresent()) {
                ServiceLocator.setCurrentUser(user.get());
                ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadDashboardScene());
            } else {
                AlertUtils.error("Авторизація", "Невірні дані.");
            }
        } catch (Exception ex) {
            AlertUtils.error("Авторизація", ex.getMessage());
        }
    }

    @FXML
    public void goToRegister(ActionEvent e) {
        try {
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadRegisterScene());
        } catch (Exception ex) {
            AlertUtils.error("Помилка", ex.getMessage());
        }
    }
}
