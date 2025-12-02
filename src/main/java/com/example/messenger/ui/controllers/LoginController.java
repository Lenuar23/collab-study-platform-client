package com.example.messenger.ui.controllers;

import com.example.messenger.dto.AuthResponse;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberBox;

    // Новий лейбл для помилок
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    @FXML
    private void onLoginClick(ActionEvent event) {
        hideError(); // Ховаємо стару помилку перед новим запитом

        try {
            String email = emailField.getText();
            String pass = passwordField.getText();

            if (email == null || email.isBlank()) {
                showError("Enter email!");
                return;
            }
            if (pass == null || pass.isBlank()) {
                showError("Enter password!");
                return;
            }

            // 1. Логін
            AuthResponse response = authService.login(email, pass);

            // 2. Отримання даних користувача (фікс твоєї червоної помилки)
            Long userId = response.getUserId();
            UserDto user = userService.getUserById(userId);

            System.out.println("Login success for: " + user.getName());

            // 3. Відкриття головного вікна
            openMainWindow(user);

        } catch (Exception e) {
            e.printStackTrace();
            // Показуємо помилку знизу червоним
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void onGoToRegister(ActionEvent event) {
        openRegisterScreen();
    }

    private void openMainWindow(UserDto user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main-window.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setCurrentUser(user);

            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Study Platform - Dashboard");
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            showError("Unable to open main window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Messenger - Register");
            stage.show();
        } catch (IOException e) {
            showError("Unable to open register screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Новий метод для показу помилки в Label
    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            // Фолбек, якщо label чомусь не прив'язався
            System.err.println("Error: " + msg);
        }
    }

    // Метод для приховування помилки
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            errorLabel.setText("");
        }
    }
}