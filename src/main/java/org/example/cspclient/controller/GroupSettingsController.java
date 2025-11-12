package org.example.cspclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.Group;
import org.example.cspclient.util.AlertUtils;

import java.io.File;

public class GroupSettingsController {

    @FXML private ImageView avatarView;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    private Group group;

    @FXML
    public void initialize() {
        Object g = ServiceLocator.getStage().getProperties().get("selectedGroup");
        if (g instanceof Group) group = (Group) g;
        if (group != null) {
            nameField.setText(group.getName());
            descriptionField.setText(group.getDescription());
            loadAvatar(group.getAvatarUrl());
        }
    }

    private void loadAvatar(String url) {
        try {
            if (url == null || url.isBlank() || url.startsWith("resource:/")) {
                avatarView.setImage(new Image(GroupSettingsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
            } else {
                avatarView.setImage(new Image(new File(url).toURI().toString()));
            }
        } catch (Exception e) {
            avatarView.setImage(new Image(GroupSettingsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
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
            String avatar = avatarView.getUserData() instanceof String ? (String) avatarView.getUserData() : group.getAvatarUrl();
            Group updated = ServiceLocator.getApiClient().updateGroupProfile(group.getId(), nameField.getText(), descriptionField.getText(), avatar);
            ServiceLocator.getStage().getProperties().put("selectedGroup", updated);
            AlertUtils.info("Saved", "Group updated");
        } catch (Exception ex) {
            AlertUtils.error("Save", ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadGroupDetailsScene());
        } catch (Exception ex) {
            AlertUtils.error("Navigation", ex.getMessage());
        }
    }
}
