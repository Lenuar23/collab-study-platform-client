package com.example.messenger.ui.controllers;

import com.example.messenger.dto.convers.ConversationDetailsResponse;
import com.example.messenger.dto.convers.ConversationSummary;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.ChatStompClient;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.MessageService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.util.*;

public class ChatController {

    @FXML private VBox chatListContainer;
    @FXML private VBox createChatPane;
    @FXML private VBox chatViewContainer;
    @FXML private VBox participantsPane;
    @FXML private VBox deleteConfirmationPane;

    @FXML private VBox editMessagePane;
    @FXML private VBox deleteMessagePane;
    @FXML private TextField editMessageTextField;
    @FXML private HBox messageActionsBox;

    @FXML private Button mainBackButton;
    @FXML private Button plusButton;
    @FXML private Label headerTitle;
    @FXML private ImageView headerAvatar;

    @FXML private ListView<ConversationItem> conversationsList;
    @FXML private ListView<MessageDto> messagesList;
    @FXML private ListView<ConversationDetailsResponse.ParticipantInfo> participantsList;

    @FXML private TextField newChatUserIdField;
    @FXML private TextField messageField;
    @FXML private TextField searchField;

    @FXML private TextField groupNameField;
    @FXML private TextField groupParticipantsField;
    @FXML private TextField directUserIdField;
    @FXML private TextField addParticipantField;

    private final MessageService messageService = new MessageService();
    private final ConversationService conversationService = new ConversationService();
    private final UserService userService = new UserService();
    // WEBSOCKET CLIENT
    private final ChatStompClient stompClient = new ChatStompClient();

    private final ObservableList<ConversationItem> conversations = FXCollections.observableArrayList();
    private Long activeConversationId = null;
    private ConversationItem activeItem = null;
    private List<MessageDto> currentMessages = new ArrayList<>();
    private MessageDto pendingMessageAction = null;

    private final Map<Long, Image> avatarCache = new HashMap<>();
    private final Map<Long, String> nameCache = new HashMap<>();

    private Runnable closeAction;
    private Pane rootContainer;

    public void setup(Pane root, Runnable closeAction) {
        this.rootContainer = root;
        this.closeAction = closeAction;
        // Підключаємо WebSocket при старті
        new Thread(stompClient::connect).start();

        try {
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
                root.getScene().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } else {
                root.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
                root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            }
        } catch (Exception e) {}

        if (headerAvatar != null) {
            Circle clip = new Circle(15, 15, 15);
            headerAvatar.setClip(clip);
        }

        loadUserConversations();
        showListMode();
    }

    @FXML
    private void initialize() {
        conversationsList.setItems(conversations);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) return;
            String lower = newValue.toLowerCase();
            for (MessageDto m : messagesList.getItems()) {
                if (m.getContent() != null && m.getContent().toLowerCase().contains(lower)) {
                    messagesList.getSelectionModel().select(m);
                    messagesList.scrollTo(m);
                    return;
                }
            }
        });

        messagesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            Long myId = SessionStore.getUserId();
            if (newVal != null && newVal.getSenderUserId() != null && newVal.getSenderUserId().equals(myId)) {
                messageActionsBox.setVisible(true);
                messageActionsBox.setManaged(true);
            } else {
                onCancelMessageAction(null);
            }
        });

        // Cell Factories (Залишено без змін візуалізацію)
        setupCellFactories();
    }

    private void setupCellFactories() {
        conversationsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ConversationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(15);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 5; -fx-cursor: hand;");

                    Circle avatar = new Circle(20, Color.web("#7f8c8d"));
                    if (item.getAvatarUrl() != null) {
                        loadAvatar(item.getOtherUserId(), item.getAvatarUrl(), avatar);
                    } else if (item.getOtherUserId() != null) {
                        loadUserAvatar(item.getOtherUserId(), avatar);
                    }

                    VBox textContainer = new VBox(2);
                    String displayTitle = item.getTitle();
                    if (item.getOtherUserId() != null && nameCache.containsKey(item.getOtherUserId())) {
                        displayTitle = nameCache.get(item.getOtherUserId());
                    }

                    Label nameLabel = new Label(displayTitle);
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                    Label subLabel = new Label(item.getType());
                    subLabel.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 10px;");

                    textContainer.getChildren().addAll(nameLabel, subLabel);
                    HBox.setHgrow(textContainer, Priority.ALWAYS);

                    Button openBtn = new Button("Open");
                    openBtn.getStyleClass().add("button");
                    openBtn.setStyle("-fx-background-color: #4A5C6A; -fx-text-fill: white; -fx-font-size: 11px;");

                    openBtn.setOnAction(e -> {
                        e.consume();
                        openConversation(item);
                    });

                    box.getChildren().addAll(avatar, textContainer, openBtn);
                    box.setOnMouseClicked(e -> openConversation(item));
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2;");
                }
            }
        });

        messagesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MessageDto msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Long myId = SessionStore.getUserId();
                    boolean isMe = (myId != null && myId.equals(msg.getSenderUserId()));

                    VBox bubble = new VBox(5);
                    Label content = new Label(msg.getContent());
                    content.setWrapText(true);
                    content.setMaxWidth(350);

                    Label senderName = new Label(isMe ? "You" : (msg.getSenderName() != null ? msg.getSenderName() : "User " + msg.getSenderUserId()));
                    senderName.setStyle("-fx-font-size: 10px; -fx-text-fill: #9BA8AB;");

                    Circle avatar = new Circle(18, Color.web("#7f8c8d"));
                    if (msg.getSenderUserId() != null) {
                        loadUserAvatar(msg.getSenderUserId(), avatar);
                    }

                    if (isMe) {
                        content.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 15 15 0 15;");
                        bubble.setAlignment(Pos.CENTER_RIGHT);
                        bubble.getChildren().addAll(senderName, content);
                        HBox row = new HBox(10, bubble, avatar);
                        row.setAlignment(Pos.BOTTOM_RIGHT);
                        setGraphic(row);
                    } else {
                        content.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 15 15 15 0;");
                        bubble.setAlignment(Pos.CENTER_LEFT);
                        bubble.getChildren().addAll(senderName, content);
                        HBox row = new HBox(10, avatar, bubble);
                        row.setAlignment(Pos.BOTTOM_LEFT);
                        setGraphic(row);
                    }
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        participantsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ConversationDetailsResponse.ParticipantInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setStyle("-fx-padding: 5;");
                    Circle avatar = new Circle(15, Color.web("#7f8c8d"));
                    loadUserAvatar(item.getUserId(), avatar);

                    String displayName = (item.getName() != null && !item.getName().isBlank())
                            ? item.getName()
                            : (nameCache.containsKey(item.getUserId()) ? nameCache.get(item.getUserId()) : "User " + item.getUserId());

                    Label name = new Label(displayName);
                    name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                    box.getChildren().addAll(avatar, name);
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    // --- ACTIONS ---
    @FXML protected void onCancelMessageAction(ActionEvent event) {
        messageActionsBox.setVisible(false);
        messageActionsBox.setManaged(false);
        messagesList.getSelectionModel().clearSelection();
        pendingMessageAction = null;
    }

    @FXML protected void onEditMessage(ActionEvent event) {
        MessageDto msg = messagesList.getSelectionModel().getSelectedItem();
        if (msg == null) return;
        pendingMessageAction = msg;
        editMessageTextField.setText(msg.getContent());
        chatViewContainer.setVisible(false); chatViewContainer.setManaged(false);
        editMessagePane.setVisible(true); editMessagePane.setManaged(true);
    }

    @FXML protected void onCancelEdit(ActionEvent event) {
        editMessagePane.setVisible(false); editMessagePane.setManaged(false);
        chatViewContainer.setVisible(true); chatViewContainer.setManaged(true);
        pendingMessageAction = null;
    }

    @FXML protected void onConfirmEdit(ActionEvent event) {
        if (pendingMessageAction == null) return;
        String newText = editMessageTextField.getText();
        if (newText == null || newText.isBlank()) return;
        try {
            messageService.updateMessage(pendingMessageAction.getMessageId(), newText);
            loadMessages(activeConversationId);
            onCancelEdit(null);
            onCancelMessageAction(null);
        } catch (Exception e) { showError("Update failed: " + e.getMessage()); }
    }

    @FXML protected void onDeleteMessage(ActionEvent event) {
        MessageDto msg = messagesList.getSelectionModel().getSelectedItem();
        if (msg == null) return;
        pendingMessageAction = msg;
        chatViewContainer.setVisible(false); chatViewContainer.setManaged(false);
        deleteMessagePane.setVisible(true); deleteMessagePane.setManaged(true);
    }

    @FXML protected void onCancelDeleteMessage(ActionEvent event) {
        deleteMessagePane.setVisible(false); deleteMessagePane.setManaged(false);
        chatViewContainer.setVisible(true); chatViewContainer.setManaged(true);
        pendingMessageAction = null;
    }

    @FXML protected void onConfirmDeleteMessage(ActionEvent event) {
        if (pendingMessageAction == null) return;
        try {
            messageService.deleteMessage(pendingMessageAction.getMessageId());
            loadMessages(activeConversationId);
            onCancelDeleteMessage(null);
            onCancelMessageAction(null);
        } catch (Exception e) { showError("Delete failed: " + e.getMessage()); }
    }

    private void loadUserAvatar(Long userId, Circle target) {
        if (avatarCache.containsKey(userId)) {
            target.setFill(new ImagePattern(avatarCache.get(userId)));
            return;
        }
        new Thread(() -> {
            try {
                UserDto u = userService.getUserById(userId);
                if (u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) {
                    loadAvatar(userId, u.getAvatarUrl(), target);
                }
                if (u.getName() != null) nameCache.put(userId, u.getName());
            } catch (Exception e) {}
        }).start();
    }

    private void loadAvatar(Long userId, String urlStr, Circle target) {
        if (urlStr == null) return;
        if (!urlStr.startsWith("http")) urlStr = "http://localhost:8080" + (urlStr.startsWith("/") ? "" : "/") + urlStr;
        String finalUrl = urlStr;
        Image img = new Image(finalUrl, true);
        img.progressProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() == 1.0 && !img.isError()) {
                Platform.runLater(() -> {
                    avatarCache.put(userId, img);
                    target.setFill(new ImagePattern(img));
                });
            }
        });
    }

    private void showListMode() {
        chatListContainer.setVisible(true); chatListContainer.setManaged(true);
        createChatPane.setVisible(false); createChatPane.setManaged(false);
        chatViewContainer.setVisible(false); chatViewContainer.setManaged(false);
        participantsPane.setVisible(false); participantsPane.setManaged(false);
        deleteConfirmationPane.setVisible(false); deleteConfirmationPane.setManaged(false);
        editMessagePane.setVisible(false); editMessagePane.setManaged(false);
        deleteMessagePane.setVisible(false); deleteMessagePane.setManaged(false);

        headerTitle.setText("My Chats");
        headerAvatar.setImage(null);
        mainBackButton.setText("← Back");
        mainBackButton.setOnAction(this::onBackToMenu);
        plusButton.setVisible(true);
        onCancelMessageAction(null);
    }

    private void showCreateMode() {
        hideAllPanes();
        createChatPane.setVisible(true); createChatPane.setManaged(true);
        headerTitle.setText("New Chat");
        headerAvatar.setImage(null);
        plusButton.setVisible(false);
        newChatUserIdField.clear();
    }

    private void showChatMode(String title, Image avatar) {
        hideAllPanes();
        chatViewContainer.setVisible(true); chatViewContainer.setManaged(true);
        headerTitle.setText(title);
        headerAvatar.setImage(avatar);
        mainBackButton.setText("← Chats");
        mainBackButton.setOnAction(this::onBackToList);
        plusButton.setVisible(false);
        onCancelMessageAction(null);
    }

    private void showParticipantsMode() {
        hideAllPanes();
        participantsPane.setVisible(true); participantsPane.setManaged(true);
    }

    private void hideAllPanes() {
        chatListContainer.setVisible(false); chatListContainer.setManaged(false);
        createChatPane.setVisible(false); createChatPane.setManaged(false);
        chatViewContainer.setVisible(false); chatViewContainer.setManaged(false);
        participantsPane.setVisible(false); participantsPane.setManaged(false);
        deleteConfirmationPane.setVisible(false); deleteConfirmationPane.setManaged(false);
        editMessagePane.setVisible(false); editMessagePane.setManaged(false);
        deleteMessagePane.setVisible(false); deleteMessagePane.setManaged(false);
    }

    @FXML protected void onBackToMenu(ActionEvent event) {
        stompClient.disconnect(); // Відключаємо WS при виході
        if (closeAction != null) closeAction.run();
    }

    @FXML protected void onBackToList(ActionEvent event) {
        activeConversationId = null; activeItem = null; showListMode(); loadUserConversations();
    }
    @FXML protected void onShowCreate(ActionEvent event) { showCreateMode(); }
    @FXML protected void onCancelCreate(ActionEvent event) { showListMode(); }

    @FXML protected void onCreateChat(ActionEvent event) {
        String idStr = newChatUserIdField.getText();
        if (idStr == null || idStr.isBlank()) { showError("Please enter User ID"); return; }
        try {
            long userId = Long.parseLong(idStr.trim());
            long convId = conversationService.createDirectConversation(userId);
            loadUserConversations();
            onBackToList(null);
        } catch (NumberFormatException e) { showError("User ID must be a number"); }
        catch (Exception e) { showError("Failed: " + e.getMessage()); }
    }

    private void openConversation(ConversationItem item) {
        activeConversationId = item.getConversationId();
        activeItem = item;
        Image avatarImg = null;
        if (item.getOtherUserId() != null && avatarCache.containsKey(item.getOtherUserId())) {
            avatarImg = avatarCache.get(item.getOtherUserId());
        }
        String currentTitle = item.getTitle();
        if (item.getOtherUserId() != null && nameCache.containsKey(item.getOtherUserId())) {
            currentTitle = nameCache.get(item.getOtherUserId());
        }
        showChatMode(currentTitle, avatarImg);

        // 1. Завантажуємо історію
        loadMessages(activeConversationId);

        // 2. Підписуємось на нові повідомлення по WebSocket
        stompClient.subscribeToConversation(activeConversationId, (newMessage) -> {
            Platform.runLater(() -> {
                messagesList.getItems().add(newMessage);
                messagesList.scrollTo(messagesList.getItems().size() - 1);
            });
        });

        if ("DIRECT".equals(item.getType()) && item.getOtherUserId() != null) {
            new Thread(() -> {
                try {
                    UserDto u = userService.getUserById(item.getOtherUserId());
                    if (u.getName() != null) {
                        nameCache.put(item.getOtherUserId(), u.getName());
                        String finalName = u.getName();
                        String finalAvatarUrl = u.getAvatarUrl();
                        if (finalAvatarUrl != null && !finalAvatarUrl.startsWith("http")) {
                            finalAvatarUrl = "http://localhost:8080" + (finalAvatarUrl.startsWith("/") ? "" : "/") + finalAvatarUrl;
                        }
                        String urlToLoad = finalAvatarUrl;
                        Platform.runLater(() -> {
                            if (activeConversationId != null && activeConversationId.equals(item.getConversationId())) {
                                headerTitle.setText(finalName);
                                if (urlToLoad != null) headerAvatar.setImage(new Image(urlToLoad, true));
                            }
                        });
                    }
                } catch (Exception e) {}
            }).start();
        }
    }

    @FXML protected void onSendMessage(ActionEvent event) {
        if (activeConversationId == null) return;
        String content = messageField.getText();
        if (content == null || content.isBlank()) return;
        try {
            // Використовуємо WebSocket для відправки
            stompClient.sendMessage(activeConversationId, content);

            // Якщо WS не працює, розкоментуйте нижче для fallback на REST:
            // messageService.sendMessage(activeConversationId, content);

            messageField.clear();
            // Повідомлення прийде через підписку subscribeToConversation, тому тут не додаємо вручну
        } catch (Exception e) { showError("Failed to send: " + e.getMessage()); }
    }

    @FXML protected void onShowParticipants(ActionEvent event) {
        if (activeConversationId == null) return;
        showParticipantsMode();
        new Thread(() -> {
            try {
                ConversationDetailsResponse details = conversationService.getConversationDetails(activeConversationId);
                if(details != null && details.getParticipants() != null) {
                    for (var p : details.getParticipants()) {
                        if (p.getName() == null || p.getName().isBlank() || p.getName().startsWith("User ")) {
                            try {
                                UserDto u = userService.getUserById(p.getUserId());
                                if (u.getName() != null) {
                                    p.setName(u.getName());
                                    nameCache.put(p.getUserId(), u.getName());
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                    Platform.runLater(() -> participantsList.setItems(FXCollections.observableArrayList(details.getParticipants())));
                }
            } catch (Exception e) { Platform.runLater(() -> showError("Failed to load participants")); }
        }).start();
    }

    @FXML protected void onCloseParticipants(ActionEvent event) {
        if (activeItem != null) {
            String title = activeItem.getTitle();
            if (activeItem.getOtherUserId() != null && nameCache.containsKey(activeItem.getOtherUserId())) {
                title = nameCache.get(activeItem.getOtherUserId());
            }
            Image avatarImg = (activeItem.getOtherUserId() != null) ? avatarCache.get(activeItem.getOtherUserId()) : null;
            showChatMode(title, avatarImg);
        }
    }

    @FXML protected void onDeleteChat(ActionEvent event) {
        participantsPane.setVisible(false); participantsPane.setManaged(false);
        deleteConfirmationPane.setVisible(true); deleteConfirmationPane.setManaged(true);
    }

    @FXML protected void onCancelDeleteConfirmation(ActionEvent event) {
        deleteConfirmationPane.setVisible(false); deleteConfirmationPane.setManaged(false);
        participantsPane.setVisible(true); participantsPane.setManaged(true);
    }

    @FXML protected void onConfirmDeleteChat(ActionEvent event) {
        if (activeConversationId == null) return;
        new Thread(() -> {
            try {
                conversationService.removeParticipant(activeConversationId, SessionStore.getUserId());
                Platform.runLater(() -> onBackToList(null));
            } catch (Exception e) { Platform.runLater(() -> showError("Failed to delete chat: " + e.getMessage())); }
        }).start();
    }

    private void loadUserConversations() {
        new Thread(() -> {
            try {
                ConversationSummary[] summaries = conversationService.listUserConversations();
                if (summaries == null) return;
                Long myId = SessionStore.getUserId();
                List<ConversationItem> loadedItems = new ArrayList<>();
                for (ConversationSummary s : summaries) {
                    Long convId = s.getConversationId();
                    if (convId == null) continue;
                    String type = s.getType();
                    if ("GROUP".equalsIgnoreCase(type)) continue;
                    String displayName = "User " + convId;
                    Long otherUserId = null;
                    String avatarUrl = null;
                    try {
                        ConversationDetailsResponse details = conversationService.getConversationDetails(convId);
                        if (details != null && details.getParticipants() != null) {
                            for (var p : details.getParticipants()) {
                                if (!p.getUserId().equals(myId)) {
                                    otherUserId = p.getUserId();
                                    if (nameCache.containsKey(otherUserId)) displayName = nameCache.get(otherUserId);
                                    else {
                                        if (p.getName() != null && !p.getName().isBlank()) {
                                            displayName = p.getName(); nameCache.put(otherUserId, displayName);
                                        } else {
                                            UserDto u = userService.getUserById(otherUserId);
                                            displayName = u.getName(); avatarUrl = u.getAvatarUrl(); nameCache.put(otherUserId, displayName);
                                        }
                                    }
                                    if (avatarUrl == null) {
                                        UserDto u = userService.getUserById(otherUserId);
                                        avatarUrl = u.getAvatarUrl();
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) { }
                    ConversationItem item = new ConversationItem(convId, displayName, type, otherUserId, avatarUrl);
                    try {
                        long unread = conversationService.getUnreadCount(convId);
                        item.setUnreadCount(unread);
                    } catch (Exception ignored) {}
                    loadedItems.add(item);
                }
                Platform.runLater(() -> { conversations.clear(); conversations.addAll(loadedItems); });
            } catch (Exception e) { e.printStackTrace(); Platform.runLater(() -> showError("Failed to load chats")); }
        }).start();
    }

    private void loadMessages(long conversationId) {
        new Thread(() -> {
            try {
                MessageDto[] messages = messageService.listMessages(conversationId);
                if (messages == null) messages = new MessageDto[0];
                final List<MessageDto> msgs = Arrays.asList(messages);
                Platform.runLater(() -> {
                    currentMessages = msgs;
                    messagesList.setItems(FXCollections.observableArrayList(currentMessages));
                    messagesList.scrollTo(currentMessages.size() - 1);
                });
                try {
                    Long maxId = msgs.stream().map(MessageDto::getMessageId).max(Long::compare).orElse(null);
                    conversationService.markAsRead(conversationId, maxId);
                } catch (Exception ignored) {}
            } catch (Exception e) { Platform.runLater(() -> showError("Failed to load messages: " + e.getMessage())); }
        }).start();
    }

    @FXML protected void onOpenDirectChat(ActionEvent event) {}
    @FXML protected void onCreateGroup(ActionEvent event) {}
    @FXML protected void onAddParticipant(ActionEvent event) {}
    @FXML protected void onRemoveParticipant(ActionEvent event) {}
    @FXML protected void onShowStats(ActionEvent event) {}
    @FXML protected void onSearch(ActionEvent event) {}
    @FXML protected void onClearSearch(ActionEvent event) {}
    @FXML protected void onOpenTasks(ActionEvent event) {}

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ConversationItem {
        private final long conversationId;
        private final String title;
        private final String type;
        private final Long otherUserId;
        private final String avatarUrl;
        private long unreadCount;

        ConversationItem(long conversationId, String title, String type, Long otherUserId, String avatarUrl) {
            this.conversationId = conversationId;
            this.title = title;
            this.type = type;
            this.otherUserId = otherUserId;
            this.avatarUrl = avatarUrl;
        }
        long getConversationId() { return conversationId; }
        String getType() { return type; }
        String getTitle() { return unreadCount > 0 ? title + " (" + unreadCount + ")" : title; }
        Long getOtherUserId() { return otherUserId; }
        String getAvatarUrl() { return avatarUrl; }
        void setUnreadCount(long c) { this.unreadCount = c; }
    }
}