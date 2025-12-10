package com.example.messenger.ui.controllers;

import com.example.messenger.dto.dtuser.AuthResponse;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.ApiClient;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private StackPane webViewContainer;
    @FXML private WebView googleWebView;

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    @FXML
    private void onLoginClick(ActionEvent event) {
        hideError();
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

            AuthResponse response = authService.login(email, pass);
            SessionStore.setSession(response.getUserId(), response.getToken());

            UserDto user = userService.getUserById(response.getUserId());
            openMainWindow(user);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void onGoogleLoginClick(ActionEvent event) {
        webViewContainer.setVisible(true);
        webViewContainer.setManaged(true);

        WebEngine engine = googleWebView.getEngine();
        engine.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1");

        ApiClient.clearCookies();
        engine.load("http://localhost:8080/oauth2/authorization/google");

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String currentLocation = engine.getLocation();

                if (currentLocation.equals("http://localhost:8080/") || currentLocation.contains("jsessionid")) {
                    handleGoogleSuccess();
                }

                if (currentLocation.contains("error")) {
                    showError("Google login failed.");
                    onCloseWebView(null);
                }
            }
        });
    }

    private void handleGoogleSuccess() {
        onCloseWebView(null);

        try {
            AuthResponse response = ApiClient.get("/auth/oauth2/success", AuthResponse.class);

            if (response != null && response.getToken() != null) {
                SessionStore.setSession(response.getUserId(), response.getToken());
                UserDto user = userService.getUserById(response.getUserId());
                openMainWindow(user);
            } else {
                showError("Failed to exchange session for token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Google Auth Error: " + e.getMessage());
        }
    }

    @FXML
    private void onCloseWebView(ActionEvent event) {
        googleWebView.getEngine().load(null);
        webViewContainer.setVisible(false);
        webViewContainer.setManaged(false);
    }

    @FXML
    private void onGoToRegister(ActionEvent event) {
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

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
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