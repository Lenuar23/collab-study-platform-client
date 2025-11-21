package com.example.messenger.ui.controllers;

import com.example.messenger.dto.ConversationDetailsResponse;
import com.example.messenger.dto.ConversationSummary;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.MessageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

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
    private TextField groupNameField;

    @FXML
    private TextField groupParticipantsField;

    @FXML
    private TextField addParticipantField;

    @FXML
    private TextField searchField;

    @FXML
    private Label activeConversationLabel;

    private final MessageService messageService = new MessageService();
    private final ConversationService conversationService = new ConversationService();

    private final ObservableList<ConversationItem> conversations = FXCollections.observableArrayList();

    private Long activeConversationId = null;
    private List<MessageDto> currentMessages = new ArrayList<>();

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
                String baseTitle = "Conversation " + convId;
                if (s.getType() != null) {
                    baseTitle += " (" + s.getType() + ")";
                }
                ConversationItem item = new ConversationItem(convId, baseTitle);
                try {
                    long unread = conversationService.getUnreadCount(convId);
                    item.setUnreadCount(unread);
                } catch (Exception ignored) {
                }
                conversations.add(item);
            }

            if (!conversations.isEmpty()) {
                conversationsList.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showError("Failed to load conversations: " + e.getMessage());
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
            ConversationItem existing = null;
            for (ConversationItem c : conversations) {
                if (c.getConversationId() == conversationId) {
                    existing = c;
                    break;
                }
            }
            if (existing == null) {
                ConversationItem item = new ConversationItem(conversationId, "Direct with user " + otherUserId);
                conversations.add(item);
                existing = item;
            }
            conversationsList.getSelectionModel().select(existing);
            activeConversationId = conversationId;
            activeConversationLabel.setText(existing.getTitle());
            loadMessages(conversationId);
        } catch (Exception e) {
            showError("Failed to open direct chat: " + e.getMessage());
        }
    }

    @FXML
    protected void onCreateGroup(ActionEvent event) {
        String name = groupNameField.getText();
        String participantsText = groupParticipantsField.getText();

        if (participantsText == null || participantsText.isBlank()) {
            showError("Please enter participant IDs (comma separated).");
            return;
        }

        List<Long> participantIds = new ArrayList<>();
        String[] parts = participantsText.split(",");
        try {
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    participantIds.add(Long.parseLong(trimmed));
                }
            }
        } catch (NumberFormatException e) {
            showError("Participant IDs must be numbers separated by commas.");
            return;
        }

        try {
            long convId = conversationService.createGroupConversation(
                    (name == null || name.isBlank()) ? null : name,
                    participantIds
            );
            String title = (name != null && !name.isBlank())
                    ? "Group: " + name
                    : "Group conversation " + convId;
            ConversationItem item = new ConversationItem(convId, title);
            conversations.add(item);
            conversationsList.getSelectionModel().select(item);
            activeConversationId = convId;
            activeConversationLabel.setText(item.getTitle());
            loadMessages(convId);
        } catch (Exception e) {
            showError("Failed to create group: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddParticipant(ActionEvent event) {
        if (activeConversationId == null) {
            showError("Please select a conversation.");
            return;
        }

        String userIdText = addParticipantField.getText();
        if (userIdText == null || userIdText.isBlank()) {
            showError("Please enter user ID to add.");
            return;
        }

        long userId;
        try {
            userId = Long.parseLong(userIdText);
        } catch (NumberFormatException e) {
            showError("User ID must be a number.");
            return;
        }

        try {
            conversationService.addParticipant(activeConversationId, userId);
            showInfo("Participant added", "User " + userId + " was added to conversation.");
            addParticipantField.clear();
        } catch (Exception e) {
            showError("Failed to add participant: " + e.getMessage());
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

    @FXML
    protected void onEditMessage(ActionEvent event) {
        if (activeConversationId == null) {
            showError("Please select a conversation.");
            return;
        }

        int selectedIndex = messagesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentMessages.size()) {
            showError("Please select a message to edit.");
            return;
        }

        MessageDto msg = currentMessages.get(selectedIndex);
        TextInputDialog dialog = new TextInputDialog(msg.getContent());
        dialog.setTitle("Edit message");
        dialog.setHeaderText("Edit selected message");
        dialog.setContentText("New text:");

        dialog.getEditor().setText(msg.getContent() == null ? "" : msg.getContent());
        dialog.getEditor().selectAll();

        var result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String newContent = result.get();
        if (newContent == null || newContent.isBlank()) {
            showError("Message cannot be empty.");
            return;
        }
        if (newContent.equals(msg.getContent())) {
            return;
        }

        try {
            messageService.updateMessage(msg.getMessageId(), newContent);
            loadMessages(activeConversationId);
        } catch (Exception e) {
            showError("Failed to edit message: " + e.getMessage());
        }
    }

    @FXML
    protected void onShowStats(ActionEvent event) {
        if (currentMessages == null || currentMessages.isEmpty()) {
            showInfo("Statistics", "No messages in this conversation.");
            return;
        }

        Map<Long, Integer> counts = new HashMap<>();
        for (MessageDto m : currentMessages) {
            Long sender = m.getSenderUserId();
            if (sender == null) {
                continue;
            }
            counts.put(sender, counts.getOrDefault(sender, 0) + 1);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
            sb.append("User ").append(entry.getKey())
              .append(": ").append(entry.getValue()).append(" messages").append("\n");
        }

        showInfo("Statistics", sb.toString());
    }

    @FXML
    protected void onDeleteMessage(ActionEvent event) {
        if (activeConversationId == null) {
            showError("Please select a conversation.");
            return;
        }

        int selectedIndex = messagesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentMessages.size()) {
            showError("Please select a message to delete.");
            return;
        }

        MessageDto msg = currentMessages.get(selectedIndex);
        if (msg.getMessageId() == null) {
            showError("Selected message has no id.");
            return;
        }

        try {
            messageService.deleteMessage(msg.getMessageId());
            loadMessages(activeConversationId);
        } catch (Exception e) {
            showError("Failed to delete message: " + e.getMessage());
        }
    }

    @FXML
    protected void onShowParticipants(ActionEvent event) {
        if (activeConversationId == null) {
            showError("Please select a conversation.");
            return;
        }

        try {
            ConversationDetailsResponse details = conversationService.getConversationDetails(activeConversationId);
            if (details == null || details.getParticipants() == null || details.getParticipants().isEmpty()) {
                showInfo("Participants", "No participants found.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (ConversationDetailsResponse.ParticipantInfo p : details.getParticipants()) {
                sb.append("User ").append(p.getUserId());
                if (p.getName() != null) {
                    sb.append(" - ").append(p.getName());
                }
                if (p.getEmail() != null) {
                    sb.append(" <").append(p.getEmail()).append(">");
                }
                sb.append("\n");
            }
            showInfo("Participants", sb.toString());
        } catch (Exception e) {
            showError("Failed to load participants: " + e.getMessage());
        }
    }

    @FXML
    protected void onSearch(ActionEvent event) {
        if (currentMessages == null || currentMessages.isEmpty()) {
            showInfo("Search", "No messages in this conversation.");
            return;
        }
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            showInfo("Search", "Please enter search text.");
            return;
        }
        String lower = query.toLowerCase();

        for (int i = 0; i < currentMessages.size(); i++) {
            MessageDto m = currentMessages.get(i);
            String content = m.getContent();
            if (content != null && content.toLowerCase().contains(lower)) {
                messagesList.getSelectionModel().select(i);
                messagesList.scrollTo(i);
                return;
            }
        }

        showInfo("Search", "No messages found.");
    }

    @FXML
    protected void onClearSearch(ActionEvent event) {
        searchField.clear();
        messagesList.getSelectionModel().clearSelection();
    }

    private void loadMessages(long conversationId) {
        try {
            MessageDto[] messages = messageService.listMessages(conversationId);
            if (messages == null) {
                messagesList.setItems(FXCollections.observableArrayList());
                currentMessages = new ArrayList<>();
                return;
            }
            currentMessages = Arrays.asList(messages);
            ObservableList<String> items = FXCollections.observableArrayList();
            Long maxId = null;
            for (MessageDto m : messages) {
                String line = "[" + m.getSenderUserId() + "] " + m.getContent();
                items.add(line);
                if (m.getMessageId() != null) {
                    if (maxId == null || m.getMessageId() > maxId) {
                        maxId = m.getMessageId();
                    }
                }
            }
            messagesList.setItems(items);

            try {
                conversationService.markAsRead(conversationId, maxId);
                for (ConversationItem c : conversations) {
                    if (c.getConversationId() == conversationId) {
                        c.setUnreadCount(0);
                        break;
                    }
                }
                conversationsList.refresh();
            } catch (Exception ignored) {
            }

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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ConversationItem {
        private final long conversationId;
        private final String baseTitle;
        private long unreadCount;

        ConversationItem(long conversationId, String baseTitle) {
            this.conversationId = conversationId;
            this.baseTitle = baseTitle;
        }

        long getConversationId() {
            return conversationId;
        }

        String getTitle() {
            if (unreadCount > 0) {
                return baseTitle + " [" + unreadCount + " unread]";
            }
            return baseTitle;
        }

        void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        @Override
        public String toString() {
            return getTitle();
        }
    }
}
