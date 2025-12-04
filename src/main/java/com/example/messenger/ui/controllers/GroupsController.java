package com.example.messenger.ui.controllers;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.GroupService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.Collections;

public class GroupsController {

    @FXML private ListView<GroupDto> groupsListView;

    // ПОВЕРНУТО ТИП НА VBox
    @FXML private VBox createGroupPane;
    @FXML private TextField newGroupName;
    @FXML private TextField newGroupDesc;

    @FXML private Label errorLabel;

    private final GroupService groupService = new GroupService();
    private final ConversationService conversationService = new ConversationService();

    private StackPane rootStack;
    private Runnable closeCallback;

    public void setup(StackPane rootStack, Runnable closeCallback) {
        this.rootStack = rootStack;
        this.closeCallback = closeCallback;
        loadGroups();
    }

    @FXML
    public void initialize() {
        groupsListView.setCellFactory(param -> new ListCell<GroupDto>() {
            @Override
            protected void updateItem(GroupDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // --- ВІДНОВЛЕНО ТЕМНИЙ СТИЛЬ ---
                    HBox box = new HBox(15);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    // Використовуємо оригінальний темний колір
                    box.setStyle("-fx-padding: 10; -fx-background-color: #253745; -fx-background-radius: 5; -fx-cursor: hand;");

                    // Аватар
                    ImageView avatarView = new ImageView();
                    avatarView.setFitWidth(40);
                    avatarView.setFitHeight(40);
                    avatarView.setPreserveRatio(true);
                    Circle clip = new Circle(20, 20, 20);
                    avatarView.setClip(clip);

                    String url = item.getAvatarUrl();
                    if (url != null && !url.isBlank()) {
                        if (!url.startsWith("http")) {
                            url = "http://localhost:8080" + (url.startsWith("/") ? "" : "/") + url;
                        }
                        try {
                            avatarView.setImage(new Image(url, true));
                        } catch (Exception e) {}
                    }

                    // Текст
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                    String cleanDesc = item.getDescription() != null
                            ? item.getDescription().replaceAll("\\[CHAT:\\d+\\]", "").trim()
                            : "";

                    Label descLabel = new Label(cleanDesc);
                    descLabel.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 12px;");

                    VBox textContainer = new VBox(2, nameLabel, descLabel);
                    HBox.setHgrow(textContainer, Priority.ALWAYS);

                    Button openBtn = new Button("Open");
                    openBtn.setStyle("-fx-background-color: #4A5C6A; -fx-text-fill: white; -fx-font-size: 11px;");
                    openBtn.setOnAction(e -> openGroupDetails(item));

                    box.getChildren().addAll(avatarView, textContainer, openBtn);
                    box.setOnMouseClicked(e -> openGroupDetails(item));

                    setGraphic(box);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    private void loadGroups() {
        new Thread(() -> {
            try {
                GroupDto[] groups = groupService.listGroups();
                Platform.runLater(() -> {
                    if (groups != null) {
                        groupsListView.setItems(FXCollections.observableArrayList(groups));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openGroupDetails(GroupDto group) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/group_details.fxml"));
            Parent detailsView = loader.load();

            GroupDetailsController controller = loader.getController();
            controller.setGroupData(group, groupService, () -> {
                rootStack.getChildren().remove(detailsView);
                loadGroups();
            });

            StackPane.setAlignment(detailsView, javafx.geometry.Pos.CENTER);
            rootStack.getChildren().add(detailsView);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open group details: " + e.getMessage());
            alert.show();
        }
    }

    // --- СТВОРЕННЯ ГРУПИ ---

    @FXML private void onShowCreate() {
        hideError();
        // Просто перемикаємо видимість VBox
        createGroupPane.setVisible(true);
        createGroupPane.setManaged(true);
        groupsListView.setVisible(false);
        groupsListView.setManaged(false);
    }

    @FXML private void onCancelCreate() {
        createGroupPane.setVisible(false);
        createGroupPane.setManaged(false);
        groupsListView.setVisible(true);
        groupsListView.setManaged(true);
        newGroupName.clear();
        newGroupDesc.clear();
        hideError();
    }

    @FXML private void onCreateGroup() {
        String name = newGroupName.getText();
        String desc = newGroupDesc.getText();

        hideError();

        if (name == null || name.isBlank()) {
            showError("Group Name is required!");
            return;
        }
        if (SessionStore.getUserId() == null) {
            showError("Session error. Please relogin.");
            return;
        }

        new Thread(() -> {
            try {
                long chatId = conversationService.createGroupConversation(name, Collections.emptyList());
                String descWithChatId = (desc == null ? "" : desc) + " [CHAT:" + chatId + "]";
                groupService.createGroup(name, descWithChatId, null);

                Platform.runLater(() -> {
                    onCancelCreate();
                    loadGroups();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg.contains("500")) msg = "Server Error (500).";
                    else if (msg.contains("Connection refused")) msg = "Server offline.";
                    showError("Failed: " + msg);
                });
            }
        }).start();
    }

    @FXML private void onClose() {
        if (closeCallback != null) closeCallback.run();
    }

    // --- МЕТОДИ ДЛЯ ЛЕЙБЛА ---

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            System.err.println("Error label missing: " + msg);
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