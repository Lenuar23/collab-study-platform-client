package com.example.messenger.ui.controllers.groupacctions;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.GroupService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.util.function.BiConsumer;

public class GroupMembersManager {

    private final ListView<Long> membersListView;
    private final TextField addMemberIdField;
    private final GroupService groupService;
    private final ConversationService conversationService;
    private final UserService userService;
    private final BiConsumer<String, Boolean> notificationCallback;

    private GroupDto group;
    private Long conversationId;

    public GroupMembersManager(ListView<Long> membersListView, TextField addMemberIdField,
                               BiConsumer<String, Boolean> notificationCallback) {
        this.membersListView = membersListView;
        this.addMemberIdField = addMemberIdField;
        this.notificationCallback = notificationCallback;
        this.groupService = new GroupService();
        this.conversationService = new ConversationService();
        this.userService = new UserService();
    }

    public void setup(GroupDto group, Long conversationId) {
        this.group = group;
        this.conversationId = conversationId;
        loadMembers();
    }

    public void loadMembers() {
        new Thread(() -> {
            try {
                Long[] memberIds = groupService.getGroupMembers(group.getGroupId());
                Platform.runLater(() -> {
                    if (memberIds != null) {
                        setupMembersList();
                        membersListView.setItems(FXCollections.observableArrayList(memberIds));
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void addMember() {
        String idStr = addMemberIdField.getText().trim();
        if (idStr.isEmpty()) return;
        try {
            Long userId = Long.parseLong(idStr);
            new Thread(() -> {
                try {
                    groupService.addMember(group.getGroupId(), userId);
                    if (conversationId != null) conversationService.addParticipant(conversationId, userId);
                    Platform.runLater(() -> {
                        addMemberIdField.clear();
                        notificationCallback.accept("User " + userId + " added!", false);
                        loadMembers();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> notificationCallback.accept("Failed to add user", true));
                }
            }).start();
        } catch (NumberFormatException e) {
            notificationCallback.accept("ID must be a number", true);
        }
    }

    private void setupMembersList() {
        Long myId = SessionStore.getUserId();
        boolean amIOwner = group.getOwnerUserId() != null && group.getOwnerUserId().equals(myId);

        membersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Long userId, boolean empty) {
                super.updateItem(userId, empty);
                if (empty || userId == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Circle avatar = new Circle(15);
                    avatar.setFill(Color.GRAY);
                    loadUserAvatar(userId, avatar);

                    Label nameLabel = new Label("User " + userId);
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    loadUserName(userId, nameLabel);

                    if (userId.equals(group.getOwnerUserId())) {
                        nameLabel.setText(nameLabel.getText() + " (Owner)");
                        nameLabel.setTextFill(Color.GOLD);
                    }
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    box.getChildren().addAll(avatar, nameLabel, spacer);

                    if (amIOwner && !userId.equals(myId)) {
                        Button kickBtn = new Button("Kick");
                        kickBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 10px;");
                        kickBtn.setOnAction(e -> onKickMember(userId));
                        box.getChildren().add(kickBtn);
                    }
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    private void onKickMember(Long userId) {
        membersListView.getItems().remove(userId);
        new Thread(() -> {
            try {
                groupService.removeMember(group.getGroupId(), userId);
                Platform.runLater(() -> notificationCallback.accept("User " + userId + " kicked.", false));
            } catch (Exception e) {
                Platform.runLater(() -> notificationCallback.accept("Kicked (Local only)", true));
            }
        }).start();
    }

    private void loadUserAvatar(Long userId, Circle circle) {
        new Thread(() -> { try { UserDto u = userService.getUserById(userId); if (u.getAvatarUrl() != null) { Image img = new Image("http://localhost:8080" + u.getAvatarUrl(), true); img.progressProperty().addListener((o,ov,nv) -> { if(nv.doubleValue()==1.0) Platform.runLater(()->circle.setFill(new ImagePattern(img))); }); } } catch (Exception e) {} }).start();
    }
    private void loadUserName(Long userId, Label label) {
        new Thread(() -> { try { UserDto u = userService.getUserById(userId); Platform.runLater(() -> label.setText(u.getName())); } catch (Exception e) {} }).start();
    }
}