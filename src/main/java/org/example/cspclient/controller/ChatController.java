package org.example.cspclient.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.ChatMessage;
import org.example.cspclient.model.Conversation;
import org.example.cspclient.model.User;
import org.example.cspclient.util.AlertUtils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChatController {

    @FXML private ListView<Conversation> conversationsList;
    @FXML private ListView<ChatMessage> messagesList;
    @FXML private TextField inputField;
    @FXML private Label chatTitle;

    private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();
    private Conversation currentConv;
    private Timeline poller;

    @FXML
    public void initialize() {
        conversationsList.setItems(conversations);
        messagesList.setItems(messages);

        conversationsList.setCellFactory(list -> new ListCell<>() {
            private final HBox box = new HBox(10);
            private final ImageView avatar = new ImageView();
            private final Label name = new Label();
            {
                box.setAlignment(Pos.CENTER_LEFT);
                avatar.setFitWidth(28); avatar.setFitHeight(28); avatar.setPreserveRatio(true);
                box.getChildren().addAll(avatar, name);
            }
            @Override protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setGraphic(null); return; }
                long me = ServiceLocator.getCurrentUser().getId();
                long otherId = (c.getUserAId() == me) ? c.getUserBId() : c.getUserAId();
                try {
                    User other = ServiceLocator.getApiClient().getUserById(otherId);
                    name.setText(other.getName() != null ? other.getName() : other.getEmail());
                    String url = other.getAvatarUrl();
                    if (url == null || url.isBlank()) {
                        // initials-like placeholder: use app icon for now
                        avatar.setImage(new Image(ChatController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
                    } else if (url.startsWith("resource:/")) {
                        avatar.setImage(new Image(ChatController.class.getResourceAsStream(url.replace("resource:", ""))));
                    } else {
                        avatar.setImage(new Image(new File(url).toURI().toString()));
                    }
                } catch (Exception e) {
                    name.setText("User " + otherId);
                    avatar.setImage(new Image(ChatController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
                }
                setGraphic(box);
            }
        });

        messagesList.setCellFactory(list -> new ListCell<>() {
            private final HBox box = new HBox();
            private final Label bubble = new Label();
            private final Label time = new Label();
            private final Region spacer = new Region();
            {
                bubble.setWrapText(true);
                bubble.maxWidthProperty().bind(list.widthProperty().multiply(0.65));
                time.getStyleClass().add("msg-time");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                box.setPadding(new Insets(6,10,6,10));
            }
            @Override protected void updateItem(ChatMessage m, boolean empty) {
                super.updateItem(m, empty);
                box.getChildren().clear();
                if (empty || m == null) { setGraphic(null); return; }
                boolean mine = m.getSenderId() == ServiceLocator.getCurrentUser().getId();
                bubble.setText(m.getContent());
                bubble.getStyleClass().setAll("bubble", mine ? "mine" : "other");
                time.setText(m.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
                if (mine) {
                    box.setAlignment(Pos.CENTER_RIGHT);
                    box.getChildren().addAll(time, bubble);
                } else {
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.getChildren().addAll(bubble, time);
                }
                setGraphic(box);
            }
        });

        loadConversations();
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) openConversation(n);
        });

        poller = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshMessages()));
        poller.setCycleCount(Timeline.INDEFINITE);
        poller.play();
    }

    private void loadConversations() {
        try {
            long me = ServiceLocator.getCurrentUser().getId();
            List<Conversation> list = ServiceLocator.getApiClient().listConversations(me);
            conversations.setAll(list);
            if (!list.isEmpty()) conversationsList.getSelectionModel().select(0);
        } catch (Exception ex) {
            AlertUtils.error("Chats", ex.getMessage());
        }
    }

    private void openConversation(Conversation c) {
        currentConv = c;
        refreshMessages();
    }

    private void refreshMessages() {
        if (currentConv == null) return;
        try {
            List<ChatMessage> list = ServiceLocator.getApiClient().listMessages(currentConv.getId(), 100);
            messages.setAll(list);
            messagesList.scrollTo(messages.size() - 1);
        } catch (Exception ex) { /* ignore in polling */ }
    }

    @FXML
    public void onSend(ActionEvent e) {
        if (currentConv == null) { AlertUtils.error("Chat", "Select or create a conversation"); return; }
        String text = inputField.getText();
        if (text == null || text.isBlank()) return;
        try {
            ServiceLocator.getApiClient().sendMessage(currentConv.getId(), ServiceLocator.getCurrentUser().getId(), text.trim());
            inputField.clear();
            refreshMessages();
        } catch (Exception ex) { AlertUtils.error("Send", ex.getMessage()); }
    }

    @FXML
    public void onNewChat(ActionEvent e) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("New chat");
        dlg.setHeaderText("Enter partner email");
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(email -> {
            try {
                var opt = ServiceLocator.getApiClient().findUserByEmail(email.trim());
                if (opt.isEmpty()) { AlertUtils.error("New chat", "User not found"); return; }
                User other = opt.get();
                Conversation c = ServiceLocator.getApiClient().getOrCreateConversation(ServiceLocator.getCurrentUser().getId(), other.getId());
                loadConversations();
                conversationsList.getSelectionModel().select(c);
            } catch (Exception ex) { AlertUtils.error("New chat", ex.getMessage()); }
        });
    }
}
