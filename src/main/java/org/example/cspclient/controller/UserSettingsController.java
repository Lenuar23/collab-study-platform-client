package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.User;
import org.example.cspclient.util.AlertUtils;

import java.io.File;

public class UserSettingsController {

    @FXML private ImageView avatarView;
    @FXML private TextField nameField;
    @FXML private TextArea aboutField;

    private User user;

    @FXML
    public void initialize() {
        user = ServiceLocator.getCurrentUser();
        if (user != null) {
            nameField.setText(user.getName());
            aboutField.setText(user.getAbout());
            loadAvatar(user.getAvatarUrl());
        }
    }

    private void loadAvatar(String url) {
        try {
            if (url == null || url.isBlank()) {
                avatarView.setImage(new Image(UserSettingsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
            } else if (url.startsWith("resource:")) {
                String path = url.replace("resource:", "");
                avatarView.setImage(new Image(UserSettingsController.class.getResourceAsStream(path)));
            } else {
                avatarView.setImage(new Image(new File(url).toURI().toString()));
            }
        } catch (Exception e) {
            // fallback
            avatarView.setImage(new Image(UserSettingsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
        }
    }

    @FXML
    public void onPickAvatar(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(ServiceLocator.getStage());
        if (f != null) {
            avatarView.setImage(new Image(f.toURI().toString()));
            avatarView.setUserData(f.getAbsolutePath());
        }
    }

    @FXML
    public void onSave(ActionEvent e) {
        try {
            String avatar = avatarView.getUserData() instanceof String ? (String) avatarView.getUserData() : user.getAvatarUrl();
            User updated = ServiceLocator.getApiClient().updateUserProfile(user.getId(), nameField.getText(), aboutField.getText(), avatar);
            ServiceLocator.setCurrentUser(updated);
            AlertUtils.info("Saved", "Profile updated");
        } catch (Exception ex) {
            AlertUtils.error("Save", ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadHomeScene());
        } catch (Exception ex) {
            AlertUtils.error("Navigation", ex.getMessage());
        }
    }
}
