package com.example.messenger.ui.controllers;

import com.example.messenger.net.AuthService;
import com.example.messenger.dto.AuthResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            AuthResponse response = authService.login(email, password);
            openChatScreen();
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    protected void onGoToRegister(ActionEvent event) {
        openRegisterScreen();
    }

    private void openChatScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/chat.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Messenger - Chat");
            stage.show();
        } catch (Exception e) {
            showError("Unable to open chat screen: " + e.getMessage());
        }
    }

    private void openRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Messenger - Register");
            stage.show();
        } catch (Exception e) {
            showError("Unable to open register screen: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Login error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
