package com.example.messenger.ui.controllers;

import com.example.messenger.net.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onRegister(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            authService.register(name, email, password);
            showInfo("Registration successful. You can now log in.");
            openLoginScreen();
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    protected void onBackToLogin(ActionEvent event) {
        openLoginScreen();
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Messenger - Login");
            stage.show();
        } catch (Exception e) {
            showError("Unable to open login screen: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Registration error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
