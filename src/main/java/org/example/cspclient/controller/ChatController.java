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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.ChatMessage;
import org.example.cspclient.model.Conversation;
import org.example.cspclient.model.User;
import org.example.cspclient.util.AlertUtils;

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

        // Pretty cells for conversations
        conversationsList.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                long me = ServiceLocator.getCurrentUser().getId();
                long otherId = c.getUserAId() == me ? c.getUserBId() : c.getUserAId();
                String otherName = ("User " + otherId);
                // (In mock/api we don't have direct resolver; show id. Optionally can fetch by email if available)
                setText(otherName + "\n" + (c.getLastMessageAt() != null ? c.getLastMessageAt().toString() : ""));
                getStyleClass().add("conv-cell");
            }
        });

        // Pretty bubble cells for messages
        messagesList.setCellFactory(list -> new ListCell<>() {
            private final HBox box = new HBox();
            private final Label bubble = new Label();
            private final Label time = new Label();
            private final Region spacer = new Region();
            {
                bubble.setWrapText(true);
                bubble.setMaxWidth(420);
                time.getStyleClass().add("msg-time");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                box.setPadding(new Insets(6,10,6,10));
            }
            @Override protected void updateItem(ChatMessage m, boolean empty) {
                super.updateItem(m, empty);
                box.getChildren().clear();
                if (empty || m == null) {
                    setGraphic(null);
                    return;
                }
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

        // Polling every 2 seconds (mock replacement for WebSocket)
        poller = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshMessages()));
        poller.setCycleCount(Timeline.INDEFINITE);
        poller.play();
    }

    private void loadConversations() {
        try {
            long me = ServiceLocator.getCurrentUser().getId();
            List<Conversation> list = ServiceLocator.getApiClient().listConversations(me);
            conversations.setAll(list);
            if (!list.isEmpty()) {
                conversationsList.getSelectionModel().select(0);
            }
        } catch (Exception ex) {
            AlertUtils.error("Чати", ex.getMessage());
        }
    }

    private void openConversation(Conversation c) {
        currentConv = c;
        chatTitle.setText("Діалог #" + c.getId());
        refreshMessages();
    }

    private void refreshMessages() {
        if (currentConv == null) return;
        try {
            List<ChatMessage> list = ServiceLocator.getApiClient().listMessages(currentConv.getId(), 100);
            messages.setAll(list);
            messagesList.scrollTo(messages.size() - 1);
        } catch (Exception ex) {
            // silent in polling
        }
    }

    @FXML
    public void onSend(ActionEvent e) {
        if (currentConv == null) {
            AlertUtils.error("Чат", "Оберіть діалог або створіть новий");
            return;
        }
        String text = inputField.getText();
        if (text == null || text.isBlank()) return;
        try {
            ServiceLocator.getApiClient().sendMessage(currentConv.getId(), ServiceLocator.getCurrentUser().getId(), text.trim());
            inputField.clear();
            refreshMessages();
        } catch (Exception ex) {
            AlertUtils.error("Надсилання", ex.getMessage());
        }
    }

    @FXML
    public void onNewChat(ActionEvent e) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Новий чат");
        dlg.setHeaderText("Введи email співрозмовника");
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(email -> {
            try {
                var opt = ServiceLocator.getApiClient().findUserByEmail(email.trim());
                if (opt.isEmpty()) {
                    AlertUtils.error("Новий чат", "Користувача не знайдено");
                    return;
                }
                User other = opt.get();
                Conversation c = ServiceLocator.getApiClient().getOrCreateConversation(ServiceLocator.getCurrentUser().getId(), other.getId());
                loadConversations();
                conversationsList.getSelectionModel().select(c);
            } catch (Exception ex) {
                AlertUtils.error("Новий чат", ex.getMessage());
            }
        });
    }

    @FXML
    public void backToDashboard(ActionEvent e) {
        try {
            if (poller != null) poller.stop();
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadDashboardScene());
        } catch (Exception ex) {
            AlertUtils.error("Навігація", ex.getMessage());
        }
    }
}
