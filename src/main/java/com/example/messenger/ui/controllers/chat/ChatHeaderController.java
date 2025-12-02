package com.example.messenger.ui.controllers.chat;

import com.example.messenger.dto.UserDto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class ChatHeaderController {

    @FXML private Label activeConversationLabel;
    @FXML private Label currentUserLabel;

    private MainChatController main;

    void setMain(MainChatController m) {
        this.main = m;
    }

    void setConversationTitle(String t) {
        activeConversationLabel.setText(t);
    }

    void setCurrentUser(UserDto u) {
        if (u == null) {
            currentUserLabel.setText("Unknown user");
            return;
        }
        String d;
        if (u.getName() != null && !u.getName().isBlank())
            d = u.getName();
        else if (u.getEmail() != null && !u.getEmail().isBlank())
            d = u.getEmail();
        else d = "User " + u.getUserId();
        currentUserLabel.setText("Logged in as: " + d + " (ID " + u.getUserId() + ")");
    }

    @FXML
    void onShowStats() {
        main.showStats();
    }

    @FXML
    void onOpenTasks() {
        error("Tasks window is opened from TasksController in original code. Attach manually.");
    }

    @FXML
    void onShowParticipants() {
        main.showParticipants();
    }

    @FXML
    void onOpenProfile() {
        main.openProfile();
    }

    @FXML
    void onLogout() {
        main.logout();
    }

    void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Parent root = loader.load();
            Stage s = (Stage) currentUserLabel.getScene().getWindow();
            s.setScene(new Scene(root));
            s.setTitle("Messenger - Login");
            s.show();
        } catch (Exception ignored) {}
    }

    void openGroupsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/groups.fxml"));
            Parent root = loader.load();
            Stage st = new Stage();
            st.setScene(new Scene(root));
            st.setTitle("Groups");
            st.show();
        } catch (Exception ignored) {}
    }

    void openProfileWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user_profile.fxml"));
            Parent root = loader.load();
            Stage st = new Stage();
            st.setScene(new Scene(root));
            st.setTitle("User profile");
            st.show();
        } catch (Exception ignored) {}
    }

    void error(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(m);
        a.showAndWait();
    }

    void info(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(m);
        a.showAndWait();
    }
}
