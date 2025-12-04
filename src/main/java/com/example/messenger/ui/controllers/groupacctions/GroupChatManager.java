package com.example.messenger.ui.controllers.groupacctions;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.MaterialDto;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.MaterialService;
import com.example.messenger.net.MessageService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

public class GroupChatManager {

    private final ListView<MessageDto> chatListView;
    private final TextField messageField;
    private final MessageService messageService;
    private final MaterialService materialService;
    private final UserService userService;
    private final Map<Long, String> userNamesCache = new HashMap<>();
    private final Map<Long, Image> userAvatarsCache = new HashMap<>();

    private GroupDto group;
    private Long conversationId;
    private Timer chatUpdater;
    private final BiConsumer<String, Boolean> notificationCallback; // Для показу повідомлень у головному вікні

    public GroupChatManager(ListView<MessageDto> chatListView, TextField messageField,
                            BiConsumer<String, Boolean> notificationCallback) {
        this.chatListView = chatListView;
        this.messageField = messageField;
        this.notificationCallback = notificationCallback;
        this.messageService = new MessageService();
        this.materialService = new MaterialService();
        this.userService = new UserService();
    }

    public void setup(GroupDto group, Long conversationId) {
        this.group = group;
        this.conversationId = conversationId;
        setupChatList();
        if (conversationId != null) startChatUpdates();
        else chatListView.setPlaceholder(new Label("Chat not linked."));
    }

    public void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || conversationId == null) return;

        // Оптимістичне додавання повідомлення в UI
        MessageDto localMsg = new MessageDto();
        localMsg.setSenderUserId(SessionStore.getUserId());
        localMsg.setSenderName("Me");
        localMsg.setContent(msg);
        chatListView.getItems().add(localMsg);
        chatListView.scrollTo(chatListView.getItems().size() - 1);
        messageField.clear();

        new Thread(() -> {
            try { messageService.sendMessage(conversationId, msg); }
            catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void uploadFile(Window window) {
        if (conversationId == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            new Thread(() -> {
                try {
                    MaterialDto uploaded = materialService.uploadFile(group.getGroupId(), file);
                    // Формуємо URL (або використовуємо ID для посилання)
                    String url = uploaded.getFileUrl();
                    if (url == null && uploaded.getResourceId() != null) {
                        url = "http://localhost:8080/api/resources/" + uploaded.getResourceId() + "/download";
                    }
                    String message = "[FILE] " + file.getName() + " : " + url;
                    messageService.sendMessage(conversationId, message);
                } catch (Exception e) {
                    Platform.runLater(() -> notificationCallback.accept("Upload failed: " + e.getMessage(), true));
                }
            }).start();
        }
    }

    public void startChatUpdates() {
        stopChatUpdates();
        chatUpdater = new Timer(true);
        chatUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (conversationId == null) return;
                try {
                    MessageDto[] messages = messageService.listMessages(conversationId);
                    Platform.runLater(() -> {
                        ObservableList<MessageDto> items = FXCollections.observableArrayList(messages);
                        if (chatListView.getItems().size() != items.size()) {
                            chatListView.setItems(items);
                            chatListView.scrollTo(items.size() - 1);
                        }
                    });
                } catch (Exception e) { /* quiet error */ }
            }
        }, 0, 2000);
    }

    public void stopChatUpdates() {
        if (chatUpdater != null) {
            chatUpdater.cancel();
            chatUpdater = null;
        }
    }

    private void setupChatList() {
        chatListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MessageDto msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    renderMessageCell(this, msg);
                }
            }
        });
    }

    private void renderMessageCell(ListCell<MessageDto> cell, MessageDto msg) {
        Long myId = SessionStore.getUserId();
        Long senderId = msg.getSenderUserId();
        boolean isMe = (senderId != null && senderId.equals(myId));

        HBox root = new HBox(10);
        root.setPadding(new Insets(5));
        Circle avatar = new Circle(18);
        avatar.setFill(Color.web("#7f8c8d"));
        if (senderId != null) loadUserAvatar(senderId, avatar);

        VBox bubbleContainer = new VBox(5);
        TextFlow bubble = new TextFlow();
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setMaxWidth(350);

        String textContent = msg.getContent();
        boolean isFile = textContent != null && textContent.startsWith("[FILE]");

        if (isFile) {
            renderFileMessage(bubbleContainer, textContent, isMe, cell);
        } else {
            Text text = new Text(textContent);
            text.setFill(Color.WHITE);
            text.setStyle("-fx-font-size: 14px;");
            bubble.getChildren().add(text);
            bubbleContainer.getChildren().add(bubble);
        }

        if (isMe) {
            root.setAlignment(Pos.TOP_RIGHT);
            bubbleContainer.setAlignment(Pos.TOP_RIGHT);
            if (!isFile) bubble.setStyle("-fx-background-color: #3498db; -fx-background-radius: 15 15 0 15;");
            root.getChildren().addAll(bubbleContainer, avatar);
        } else {
            root.setAlignment(Pos.TOP_LEFT);
            bubbleContainer.setAlignment(Pos.TOP_LEFT);
            if (!isFile) bubble.setStyle("-fx-background-color: #34495e; -fx-background-radius: 15 15 15 0;");
            Label nameLabel = new Label("User " + senderId);
            nameLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10px; -fx-font-weight: bold;");
            loadUserName(senderId, nameLabel);
            VBox wrapper = new VBox(2);
            wrapper.getChildren().addAll(nameLabel, bubbleContainer);
            root.getChildren().addAll(avatar, wrapper);
        }
        cell.setGraphic(root);
        cell.setStyle("-fx-background-color: transparent;");
    }

    private void renderFileMessage(VBox container, String textContent, boolean isMe, ListCell<?> cell) {
        try {
            int urlIdx = textContent.indexOf("http");
            if (urlIdx > 0) {
                String fileName = textContent.substring(7, textContent.indexOf(" :")).trim();
                String fileUrl = textContent.substring(urlIdx).trim();
                Label fileIcon = new Label("📄 " + fileName);
                fileIcon.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Button downloadBtn = new Button("Download");
                downloadBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                downloadBtn.setOnAction(e -> downloadFile(fileName, fileUrl, cell.getListView().getScene().getWindow()));
                container.getChildren().addAll(fileIcon, downloadBtn);
                container.setStyle("-fx-padding: 10; -fx-background-color: " + (isMe ? "#2980b9" : "#34495e") + "; -fx-background-radius: 10;");
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("File error"));
        }
    }

    private void downloadFile(String fileName, String url, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File dest = fileChooser.showSaveDialog(window);
        if (dest != null) {
            String destPath = dest.getAbsolutePath();
            String ext = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) ext = fileName.substring(i);
            if (!destPath.endsWith(ext) && !ext.isEmpty()) dest = new File(destPath + ext);
            final File finalDest = dest;
            new Thread(() -> {
                try {
                    materialService.downloadFileFromUrl(url, finalDest);
                    Platform.runLater(() -> notificationCallback.accept("File downloaded successfully!", false));
                } catch (Exception e) {
                    Platform.runLater(() -> notificationCallback.accept("Download failed: " + e.getMessage(), true));
                }
            }).start();
        }
    }

    // Методи кешування (спрощені для економії місця)
    private void loadUserAvatar(Long userId, Circle circle) {
        if (userAvatarsCache.containsKey(userId)) { circle.setFill(new ImagePattern(userAvatarsCache.get(userId))); return; }
        new Thread(() -> { try { UserDto u = userService.getUserById(userId); if (u.getAvatarUrl() != null) { Image img = new Image("http://localhost:8080" + u.getAvatarUrl(), true); img.progressProperty().addListener((o,ov,nv) -> { if(nv.doubleValue()==1.0) Platform.runLater(()->{ userAvatarsCache.put(userId, img); circle.setFill(new ImagePattern(img)); }); }); } } catch (Exception e) {} }).start();
    }
    private void loadUserName(Long userId, Label label) {
        if (userNamesCache.containsKey(userId)) { label.setText(userNamesCache.get(userId)); return; }
        new Thread(() -> { try { UserDto u = userService.getUserById(userId); Platform.runLater(() -> { userNamesCache.put(userId, u.getName()); label.setText(u.getName()); }); } catch (Exception e) {} }).start();
    }
}