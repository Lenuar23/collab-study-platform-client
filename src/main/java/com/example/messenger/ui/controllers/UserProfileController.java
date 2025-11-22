package com.example.messenger.ui.controllers;

import com.example.messenger.config.Env;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class UserProfileController {

    @FXML
    private ImageView avatarImageView;

    @FXML
    private TextField nameField;

    @FXML
    private Label emailLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private UserDto user;
    private UserService userService;
    private Consumer<UserDto> onUserUpdated;

    private File selectedAvatarFile;

    public void setUserAndServices(UserDto user, UserService userService, Consumer<UserDto> onUserUpdated) {
        this.user = user;
        this.userService = userService;
        this.onUserUpdated = onUserUpdated;

        if (user != null) {
            nameField.setText(user.getName());
            emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
            loadAvatarFromUrl(user.getAvatarUrl());
        }
    }

    private void loadAvatarFromUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarImageView.setImage(null);
            return;
        }

        String url = avatarUrl;
        try {
            // Якщо сервер повертає відносний шлях типу "/uploads/users/1.png",
            // добудовуємо повний URL на основі Env.API_BASE_URL.
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                String apiBase = Env.API_BASE_URL; // напр. http://localhost:8080/api
                String root = apiBase;
                int idx = apiBase.indexOf("/api");
                if (idx > 0) {
                    root = apiBase.substring(0, idx);
                }
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                url = root + url;
            }

            Image image = new Image(url, true);
            avatarImageView.setImage(image);
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to load avatar from url '" + avatarUrl + "': " + ex.getMessage());
            avatarImageView.setImage(null);
        }
    }

    private void loadAvatarFromFile(File file) {
        if (file == null) {
            return;
        }
        try {
            Image image = new Image(file.toURI().toString(), true);
            avatarImageView.setImage(image);
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to load avatar from file '" + file + "': " + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot load image");
            alert.setContentText("Selected file is not a valid image.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onChooseAvatar(ActionEvent event) {
        Window window = avatarImageView.getScene() != null ? avatarImageView.getScene().getWindow() : null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose avatar image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            selectedAvatarFile = file;
            loadAvatarFromFile(file);
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (user == null || userService == null) {
            return;
        }

        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (newName.isEmpty()) {
            newName = user.getName();
        }

        try {
            // Оновлюємо профіль (ім'я та avatarUrl як є)
            UserDto updated = userService.updateUserProfile(
                    user.getUserId(),
                    newName,
                    user.getAvatarUrl()
            );

            // Якщо вибрано новий файл аватарки — завантажуємо його
            if (selectedAvatarFile != null) {
                updated = userService.uploadAvatarFile(updated.getUserId(), selectedAvatarFile);
            }

            this.user = updated;

            if (onUserUpdated != null) {
                onUserUpdated.accept(updated);
            }

            // Закриваємо вікно
            Window window = saveButton.getScene() != null ? saveButton.getScene().getWindow() : null;
            if (window != null) {
                window.hide();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Profile update failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Window window = cancelButton.getScene() != null ? cancelButton.getScene().getWindow() : null;
        if (window != null) {
            window.hide();
        }
    }
}