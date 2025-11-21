package com.example.messenger.ui.controllers;

import com.example.messenger.dto.ConversationSummary;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.MessageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChatController {

    @FXML
    private ListView<ConversationItem> conversationsList;

    @FXML
    private ListView<String> messagesList;

    @FXML
    private TextField messageField;

    @FXML
    private TextField directUserIdField;

    @FXML
    private Label activeConversationLabel;

    private final MessageService messageService = new MessageService();
    private final ConversationService conversationService = new ConversationService();

    private final ObservableList<ConversationItem> conversations = FXCollections.observableArrayList();

    private Long activeConversationId = null;

    @FXML
    private void initialize() {
        conversationsList.setItems(conversations);
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeConversationId = newVal.getConversationId();
                activeConversationLabel.setText(newVal.getTitle());
                loadMessages(activeConversationId);
            }
        });

        loadUserConversations();
    }

    private void loadUserConversations() {
        try {
            ConversationSummary[] summaries = conversationService.listUserConversations();
            conversations.clear();
            if (summaries == null) {
                return;
            }

            for (ConversationSummary s : summaries) {
                Long convId = s.getConversationId();
                if (convId == null) {
                    continue;
                }
                String title = "Conversation " + convId;
                if (s.getType() != null) {
                    title += " (" + s.getType() + ")";
                }
                conversations.add(new ConversationItem(convId, title));
            }

            if (!conversations.isEmpty()) {
                conversationsList.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            // show error once, but do not break UI
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load conversations");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onOpenDirectChat(ActionEvent event) {
        String userIdText = directUserIdField.getText();
        if (userIdText == null || userIdText.isBlank()) {
            showError("Please enter other user ID.");
            return;
        }

        long otherUserId;
        try {
            otherUserId = Long.parseLong(userIdText);
        } catch (NumberFormatException e) {
            showError("User ID must be a number.");
            return;
        }

        try {
            long conversationId = conversationService.createDirectConversation(otherUserId);
            ConversationItem item = new ConversationItem(conversationId, "Direct with user " + otherUserId);
            boolean exists = conversations.stream().anyMatch(c -> c.getConversationId() == conversationId);
            if (!exists) {
                conversations.add(item);
            }
            conversationsList.getSelectionModel().select(item);
            activeConversationId = conversationId;
            activeConversationLabel.setText(item.getTitle());
            loadMessages(conversationId);
        } catch (Exception e) {
            showError("Failed to open direct chat: " + e.getMessage());
        }
    }

    @FXML
    protected void onSendMessage(ActionEvent event) {
        if (activeConversationId == null) {
            showError("Please select a conversation.");
            return;
        }

        String content = messageField.getText();
        if (content == null || content.isBlank()) {
            showError("Please enter a message.");
            return;
        }

        try {
            messageService.sendMessage(activeConversationId, content);
            messageField.clear();
            loadMessages(activeConversationId);
        } catch (Exception e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }

    private void loadMessages(long conversationId) {
        try {
            MessageDto[] messages = messageService.listMessages(conversationId);
            if (messages == null) {
                messagesList.setItems(FXCollections.observableArrayList());
                return;
            }
            ObservableList<String> items = FXCollections.observableArrayList();
            for (MessageDto m : messages) {
                String line = "[" + m.getSenderUserId() + "] " + m.getContent();
                items.add(line);
            }
            messagesList.setItems(items);
        } catch (Exception e) {
            showError("Failed to load messages: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ConversationItem {
        private final long conversationId;
        private final String title;

        ConversationItem(long conversationId, String title) {
            this.conversationId = conversationId;
            this.title = title;
        }

        long getConversationId() {
            return conversationId;
        }

        String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
