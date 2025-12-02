package com.example.messenger.ui.controllers.chat;

import com.example.messenger.dto.ConversationDetailsResponse;
import com.example.messenger.dto.ConversationSummary;
import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.MessageService;
import com.example.messenger.net.UserService;
import com.example.messenger.ui.controllers.UserProfileController;

import javafx.stage.Window;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class MainChatController {

    @FXML private ChatSidebarController sidebarController;
    @FXML private ChatHeaderController headerController;
    @FXML private ChatMessageController messagesController;

    private final AuthService authService = new AuthService();
    private final ConversationService conversationService = new ConversationService();
    private final MessageService messageService = new MessageService();
    private final UserService userService = new UserService();

    private final ObservableList<ConversationItem> conversations = FXCollections.observableArrayList();
    private List<MessageDto> currentMessages = new ArrayList<>();
    private Long activeConversationId = null;

    @FXML
    private void initialize() {
        sidebarController.setMain(this);
        headerController.setMain(this);
        messagesController.setMain(this);

        sidebarController.setList(conversations);

        loadCurrentUserLabel();
        loadUserConversations();
    }

    public void loadUserConversations() {
        try {
            ConversationSummary[] arr = conversationService.listUserConversations();
            conversations.clear();
            if (arr == null) return;

            for (ConversationSummary summary : arr) {
                if (summary == null) continue;

                Long convId = summary.getConversationId();
                if (convId == null) continue;

                ConversationDetailsResponse details = conversationService.getConversationDetails(convId);
                String title = computeConversationTitle(details);

                ConversationItem item = new ConversationItem(convId, title, summary.getType());

                try {
                    long unread = conversationService.getUnreadCount(convId);
                    item.setUnreadCount(unread);
                } catch (Exception ignored) {}

                conversations.add(item);
            }

            sidebarController.refreshList();
            if (!conversations.isEmpty()) sidebarController.selectFirst();

        } catch (Exception e) {
            showError("Failed to load conversations: " + e.getMessage());
        }
    }

    private String resolveDirectTitle(long conversationId) throws Exception {
        ConversationDetailsResponse details = conversationService.getConversationDetails(conversationId);
        return computeConversationTitle(details);
    }

    private String resolveGroupTitle(long conversationId) throws Exception {
        ConversationDetailsResponse details = conversationService.getConversationDetails(conversationId);
        return computeConversationTitle(details);
    }

    public void selectConversation(ConversationItem item) {
        if (item == null) return;

        activeConversationId = item.getConversationId();

        try {
            ConversationDetailsResponse details = conversationService.getConversationDetails(activeConversationId);
            headerController.setConversationTitle(computeConversationTitle(details));
        } catch (Exception e) {
            headerController.setConversationTitle(item.getTitle());
        }

        loadMessages(activeConversationId);
    }

    public void openDirectChat(long otherUserId) {
        try {
            long convId = conversationService.createDirectConversation(otherUserId);

            ConversationItem found = null;
            for (ConversationItem c : conversations) {
                if (c.getConversationId() == convId) {
                    found = c;
                    break;
                }
            }

            if (found == null) {
                ConversationDetailsResponse details = conversationService.getConversationDetails(convId);
                String title = computeConversationTitle(details);
                found = new ConversationItem(convId, title, "DIRECT");
                conversations.add(found);
            }

            sidebarController.selectConversation(found);

        } catch (Exception e) {
            showError("Failed to open direct chat: " + e.getMessage());
        }
    }

    public void createGroup(String name, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            showError("Participants list is empty.");
            return;
        }
        try {
            long convId = conversationService.createGroupConversation(
                    (name == null || name.isBlank()) ? null : name,
                    ids
            );

            ConversationDetailsResponse details = conversationService.getConversationDetails(convId);
            String title = computeConversationTitle(details);

            ConversationItem item = new ConversationItem(convId, title, "GROUP");
            conversations.add(item);
            sidebarController.selectConversation(item);

        } catch (Exception e) {
            showError("Failed to create group: " + e.getMessage());
        }
    }

    public void addParticipant(long userId) {
        if (activeConversationId == null) {
            showError("Select a conversation first.");
            return;
        }
        try {
            conversationService.addParticipant(activeConversationId, userId);
            showInfo("Participant added", "User " + userId + " added.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    public void removeParticipant(long userId) {
        if (activeConversationId == null) {
            showError("Select a conversation first.");
            return;
        }
        try {
            conversationService.removeParticipant(activeConversationId, userId);
            showInfo("Participant removed", "User " + userId + " removed.");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    public void showParticipants() {
        if (activeConversationId == null) {
            showError("Select a conversation first.");
            return;
        }
        try {
            ConversationDetailsResponse details = conversationService.getConversationDetails(activeConversationId);

            if (details.getParticipants() == null || details.getParticipants().isEmpty()) {
                showInfo("Participants", "No participants.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (var p : details.getParticipants()) {
                sb.append("User ").append(p.getUserId());
                if (p.getName() != null) sb.append(" - ").append(p.getName());
                if (p.getEmail() != null) sb.append(" <").append(p.getEmail()).append(">");
                sb.append("\n");
            }

            showInfo("Participants", sb.toString());

        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    void loadMessages(long conversationId) {
        try {
            MessageDto[] arr = messageService.listMessages(conversationId);

            if (arr == null) {
                currentMessages = new ArrayList<>();
                messagesController.showMessages(currentMessages);
                return;
            }

            currentMessages = Arrays.asList(arr);
            messagesController.showMessages(currentMessages);

            Long maxId = null;
            for (MessageDto m : arr) {
                Long id = m.getMessageId();
                if (id == null) continue;
                if (maxId == null || id > maxId) maxId = id;
            }

            try {
                conversationService.markAsRead(conversationId, maxId);
                for (ConversationItem c : conversations) {
                    if (c.getConversationId() == conversationId) {
                        c.setUnreadCount(0);
                    }
                }
                sidebarController.refreshList();
            } catch (Exception ignored) {}

        } catch (Exception e) {
            showError("Failed to load messages: " + e.getMessage());
        }
    }

    public Long getActiveConversationId() { return activeConversationId; }
    public List<MessageDto> getCurrentMessages() { return currentMessages; }

    public void showStats() {
        if (currentMessages == null || currentMessages.isEmpty()) {
            showInfo("Statistics", "No messages.");
            return;
        }

        Map<Long, Integer> counts = new HashMap<>();
        for (MessageDto m : currentMessages) {
            Long sender = m.getSenderUserId();
            if (sender == null) continue;
            counts.put(sender, counts.getOrDefault(sender, 0) + 1);
        }

        StringBuilder sb = new StringBuilder();
        for (var e : counts.entrySet()) {
            sb.append("User ").append(e.getKey())
                    .append(": ").append(e.getValue()).append(" messages\n");
        }

        showInfo("Statistics", sb.toString());
    }

    public void openProfile() {
        try {
            UserDto user = authService.getCurrentUser();
            if (user == null) {
                showError("Unable to load profile.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user_profile.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException ex) {
                showError("Failed to load profile window: " + ex.getMessage());
                return;
            }

            UserProfileController controller = loader.getController();
            controller.setUserAndServices(user, userService, updated -> {
                if (updated != null) headerController.setCurrentUser(updated);
            });

            Stage stage = new Stage();
            stage.setTitle("User profile");
            stage.setScene(new Scene(root));
            stage.initOwner(sidebarController.getRootSceneWindow());
            stage.show();

        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    public void logout() {
        try {
            authService.logout();
            openLoginScreen();
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    private void openLoginScreen() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            showError("Failed to load login window: " + ex.getMessage());
            return;
        }

        Window w;
        try {
            w = sidebarController.getRootSceneWindow();
        } catch (Exception e) {
            showError("Failed to access window: " + e.getMessage());
            return;
        }

        Stage stage = (Stage) w;
        stage.setScene(new Scene(root));
        stage.setTitle("Messenger - Login");
        stage.show();
    }

    private void loadCurrentUserLabel() {
        try {
            UserDto me = authService.getCurrentUser();
            if (me != null) headerController.setCurrentUser(me);
        } catch (Exception ignored) {}
    }

    public void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    // UPDATED: Added 'throws IOException, InterruptedException' to fix compilation error
    private String computeConversationTitle(ConversationDetailsResponse details) throws IOException, InterruptedException {
        if (details == null) return "Conversation";

        String type = details.getType();
        List<ConversationDetailsResponse.ParticipantInfo> parts = details.getParticipants();
        Long myId = authService.getCurrentUser().getUserId();

        if ("DIRECT".equalsIgnoreCase(type)) {
            if (parts != null) {
                for (var p : parts) {
                    if (!Objects.equals(p.getUserId(), myId)) {
                        if (p.getName() != null && !p.getName().isBlank()) return p.getName();
                        if (p.getEmail() != null && !p.getEmail().isBlank()) return p.getEmail();
                        return "User " + p.getUserId();
                    }
                }
            }
            return "Direct chat";
        }

        if (parts != null && parts.size() <= 4) {
            StringBuilder sb = new StringBuilder("Group: ");
            for (var p : parts) {
                if (sb.length() > 7) sb.append(", ");
                sb.append(
                    (p.getName() != null && !p.getName().isBlank())
                        ? p.getName()
                        : "User " + p.getUserId()
                );
            }
            return sb.toString();
        }

        return "Group " + details.getConversationId();
    }
}