package com.example.messenger.ui.controllers;

import com.example.messenger.net.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button registerButton; // Додали кнопку, щоб можна було її вимикати
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onRegister(ActionEvent event) {
        hideError(); // Ховаємо попередні повідомлення

        String name = nameField.getText().strip();
        String email = emailField.getText().strip();
        String password = passwordField.getText().strip();
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText().strip() : "";

        // 1. Перевірка на порожні поля
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        // 2. Перевірка збігу паролів (НОВЕ)
        if (!password.equals(confirm)) {
            showError("Passwords do not match!");
            return;
        }

        try {
            // Спроба реєстрації
            authService.register(name, email, password);

            // ЯКЩО УСПІШНО -> Запускаємо сценарій "Плавний успіх"
            handleSuccessRegistration();

        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    // Логіка успішної реєстрації (Варіант 2Б)
    private void handleSuccessRegistration() {
        if (errorLabel != null) {
            // Змінюємо стиль на "Успіх" (Зелений колір)
            errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 14px;");
            errorLabel.setText("Registration successful! Redirecting...");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }

        // Блокуємо інтерфейс, щоб користувач нічого не зламав під час паузи
        if (registerButton != null) registerButton.setDisable(true);
        nameField.setEditable(false);
        emailField.setEditable(false);
        passwordField.setEditable(false);
        confirmPasswordField.setEditable(false);

        // Запускаємо таймер в окремому потоці, щоб не завис інтерфейс
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Чекаємо 1.5 секунди (щоб користувач прочитав текст)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Повертаємось у головний потік JavaFX для зміни сцени
            Platform.runLater(this::openLoginScreen);
        }).start();
    }

    @FXML
    protected void onBackToLogin(ActionEvent event) {
        openLoginScreen();
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Отримуємо поточне вікно через будь-який елемент (наприклад, emailField)
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Messenger - Login");
            stage.show();
        } catch (Exception e) {
            // Якщо раптом перехід не вдався, показуємо це червоним
            if (errorLabel != null) errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 12px;");
            showError("Unable to open login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            // ВАЖЛИВО: Скидаємо стиль назад на червоний (для помилок)
            errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 12px;");
            
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            
            // Розблокуємо кнопку, якщо це була помилка, щоб можна було спробувати ще раз
            if (registerButton != null) registerButton.setDisable(false);
        } else {
            // Запасний варіант, якщо лейбла немає
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