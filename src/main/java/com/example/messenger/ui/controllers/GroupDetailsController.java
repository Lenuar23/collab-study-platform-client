package com.example.messenger.ui.controllers;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.net.ChatStompClient;
import com.example.messenger.net.GroupService;
import com.example.messenger.ui.controllers.groupacctions.GroupChatManager;
import com.example.messenger.ui.controllers.groupacctions.GroupMembersManager;
import com.example.messenger.ui.controllers.groupacctions.GroupSettingsManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform; // <--- ДОДАНО ІМПОРТ
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupDetailsController {

    @FXML private Label groupNameLabel;
    @FXML private Label groupIdLabel;
    @FXML private ImageView groupAvatarHeader;
    @FXML private VBox chatView;
    @FXML private VBox tasksView;
    @FXML private StackPane settingsView;
    @FXML private Canvas matrixCanvas;
    @FXML private Label quoteLabel;
    @FXML private ListView<MessageDto> chatListView;
    @FXML private TextField messageField;
    @FXML private TextField editNameField;
    @FXML private TextField editDescField;
    @FXML private TextField addMemberIdField;
    @FXML private Label settingsInfoLabel;
    @FXML private ListView<Long> membersListView;
    @FXML private VBox notificationPane;
    @FXML private Label notificationLabel;

    // --- НОВІ ЕЛЕМЕНТИ ---
    @FXML private VBox settingsFormPane;
    @FXML private VBox groupDeleteConfirmationPane;

    // --- DATA ---
    private GroupDto group;
    private Runnable backCallback;

    // --- WS Client ---
    private final ChatStompClient stompClient = new ChatStompClient();

    // --- DELEGATES (MANAGERS) ---
    private GroupChatManager chatManager;
    private GroupMembersManager membersManager;
    private GroupSettingsManager settingsManager;
    private TasksController embeddedTasksController;


    @FXML
    public void initialize() {

        if (matrixCanvas != null && settingsView != null) {
            matrixCanvas.widthProperty().bind(settingsView.widthProperty());
            matrixCanvas.heightProperty().bind(settingsView.heightProperty());
        }

        new Thread(stompClient::connect).start();

        this.chatManager = new GroupChatManager(chatListView, messageField, this::showNotification);
        this.chatManager.setStompClient(stompClient);

        this.membersManager = new GroupMembersManager(membersListView, addMemberIdField, this::showNotification);

        // Ініціалізація менеджера налаштувань
        this.settingsManager = new GroupSettingsManager(
                editNameField, editDescField, groupAvatarHeader,
                groupNameLabel, groupIdLabel, matrixCanvas, quoteLabel,
                settingsFormPane, groupDeleteConfirmationPane,
                this::showNotification
        );
    }

    public void setGroupData(GroupDto group, GroupService service, Runnable backCallback) {
        this.group = group;
        this.backCallback = backCallback;

        Long linkedChatId = extractChatId(group.getDescription());

        chatManager.setup(group, linkedChatId);
        membersManager.setup(group, linkedChatId);
        settingsManager.setup(group, linkedChatId);

        tasksView.getChildren().clear();
    }

    // --- NOTIFICATIONS ---
    private void showNotification(String message, boolean isError) {
        if (notificationPane == null) return;
        notificationLabel.setText(message);
        String color = isError ? "#e74c3c" : "#27ae60";
        notificationPane.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");
        notificationPane.setVisible(true);
        notificationPane.setManaged(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            notificationPane.setVisible(false);
            notificationPane.setManaged(false);
        });
        delay.play();
    }

    // --- FXML ACTIONS (Delegated) ---

    @FXML private void onSendMessage() { chatManager.sendMessage(); }
    @FXML private void onUploadFile() { chatManager.uploadFile(chatView.getScene().getWindow()); }

    @FXML private void onAddMember() { membersManager.addMember(); }

    @FXML private void onSaveSettings() { settingsManager.saveSettings(); }
    @FXML private void onChangeGroupAvatar() { settingsManager.changeAvatar(chatView.getScene().getWindow()); }

    // --- NEW DELETE GROUP ACTIONS ---
    @FXML private void onDeleteGroup() { settingsManager.showDeleteConfirmation(); }
    @FXML private void onConfirmDeleteGroup() {
        // При успішному видаленні повертаємось назад (onBack)
        settingsManager.confirmDelete(() -> Platform.runLater(this::onBack));
    }
    @FXML private void onCancelDeleteGroup() { settingsManager.cancelDelete(); }
    // --------------------------------

    // --- TAB NAVIGATION ---
    @FXML private void onTabChat() { switchTab(chatView); }
    @FXML private void onTabTasks() { switchTab(tasksView); if (tasksView.getChildren().isEmpty()) loadTasksInterface(); }
    @FXML private void onTabSettings() {
        switchTab(settingsView);
        settingsManager.startVisuals();
        membersManager.loadMembers();
    }

    private void switchTab(Pane activeView) {
        chatView.setVisible(activeView == chatView); chatView.setManaged(activeView == chatView);
        tasksView.setVisible(activeView == tasksView); tasksView.setManaged(activeView == tasksView);
        settingsView.setVisible(activeView == settingsView); settingsView.setManaged(activeView == settingsView);

        if (activeView != chatView) chatManager.stopChatUpdates();
        else chatManager.startChatUpdates();

        if (activeView != settingsView) settingsManager.stopVisuals();
    }

    @FXML private void onBack() {
        chatManager.stopChatUpdates();
        settingsManager.stopVisuals();
        stompClient.disconnect();
        if (backCallback != null) backCallback.run();
    }

    private Long extractChatId(String desc) {
        if (desc == null) return null;
        Pattern p = Pattern.compile("\\[CHAT:(\\d+)\\]");
        Matcher m = p.matcher(desc);
        if (m.find()) return Long.parseLong(m.group(1));
        return null;
    }

    private void loadTasksInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/tasks.fxml"));
            Parent tasksRoot = loader.load();
            embeddedTasksController = loader.getController();
            embeddedTasksController.setupGroupMode(group.getGroupId(), group.getName());
            VBox.setVgrow(tasksRoot, Priority.ALWAYS);
            if (tasksRoot instanceof Region) { ((Region) tasksRoot).setMaxWidth(Double.MAX_VALUE); ((Region) tasksRoot).setMaxHeight(Double.MAX_VALUE); }
            tasksView.getChildren().clear();
            tasksView.getChildren().add(tasksRoot);
        } catch (IOException e) { e.printStackTrace(); }
    }
}