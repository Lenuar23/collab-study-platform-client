package com.example.messenger.ui.controllers;

import com.example.messenger.net.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Лейбл для помилок
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onRegister(ActionEvent event) {
        hideError(); // Очищуємо стару помилку

        String name = nameField.getText().strip();
        String email = emailField.getText().strip();
        String password = passwordField.getText().strip();
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText().strip() : "";

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        // Можна додати перевірку паролів, якщо confirmPasswordField використовується
        // if (!password.equals(confirm)) { showError("Passwords do not match!"); return; }

        try {
            authService.register(name, email, password);

            // Якщо успішно - можна показати Alert (інформацію), бо далі йде перехід на логін
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Registration successful! You can now log in.");
            alert.showAndWait();

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
            // Підключаємо стилі, якщо вони не підключені в самому fxml
            // scene.getStylesheets().add(getClass().getResource("/ui/css/theme.css").toExternalForm());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Messenger - Login");
            stage.show();
        } catch (Exception e) {
            showError("Unable to open login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            // Фолбек на Alert, якщо лейбла немає
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            errorLabel.setText("");
        }
    }
}