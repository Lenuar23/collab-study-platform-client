package com.example.messenger.ui.controllers.chat;

import com.example.messenger.dto.MessageDto;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.MessageService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageController {

    @FXML
    private ListView<MessageDto> messagesList;

    @FXML
    private TextField messageField;

    private final MessageService messageService = new MessageService();
    private final AuthService authService = new AuthService();

    private MainChatController main;
    private List<MessageDto> currentMessages = new ArrayList<>();

    public void setMain(MainChatController main) {
        this.main = main;
    }

    @FXML
    private void initialize() {
        setupCellFactory();
    }

    public void showMessages(List<MessageDto> messages) {
        if (messages == null) {
            currentMessages = new ArrayList<>();
            messagesList.getItems().clear();
            return;
        }
        currentMessages = messages;
        messagesList.getItems().setAll(messages);
        if (!messages.isEmpty()) {
            messagesList.scrollTo(messages.size() - 1);
        }
    }

    private void setupCellFactory() {
        messagesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(MessageDto msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                long myId;
                try {
                    myId = authService.getCurrentUser().getUserId();
                } catch (Exception e) {
                    myId = -1;
                }

                boolean isMine =
                        msg.getSenderUserId() != null &&
                        msg.getSenderUserId() == myId;

                Label bubble = new Label(msg.getContent());
                bubble.setWrapText(true);
                bubble.setMaxWidth(500);
                bubble.getStyleClass().add(isMine ? "msg-bubble-mine" : "msg-bubble-other");

                HBox box = new HBox(bubble);
                box.setPadding(new Insets(4, 8, 4, 8));
                box.setFillHeight(true);
                box.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setGraphic(box);
                setText(null);
            }
        });

        messagesList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                editSelectedMessage();
            }
        });
    }

    @FXML
    private void onSendMessage() {
        if (main == null) return;

        Long convId = main.getActiveConversationId();
        if (convId == null) {
            main.showError("Please select a conversation.");
            return;
        }

        String content = messageField.getText();
        if (content == null || content.isBlank()) {
            return;
        }

        try {
            messageService.sendMessage(convId, content);
            messageField.clear();
            main.loadMessages(convId);
        } catch (Exception e) {
            main.showError("Failed to send message: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteMessage() {
        if (main == null) return;

        Long convId = main.getActiveConversationId();
        if (convId == null) {
            main.showError("Please select a conversation first.");
            return;
        }

        int index = messagesList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentMessages.size()) {
            main.showError("Select message to delete.");
            return;
        }

        MessageDto msg = currentMessages.get(index);
        if (msg.getMessageId() == null) {
            main.showError("Message has no id.");
            return;
        }

        try {
            messageService.deleteMessage(msg.getMessageId());
            main.loadMessages(convId);
        } catch (Exception e) {
            main.showError("Failed to delete message: " + e.getMessage());
        }
    }

    @FXML
    private void onEditMessage() {
        editSelectedMessage();
    }

    private void editSelectedMessage() {
        if (main == null) return;

        Long convId = main.getActiveConversationId();
        if (convId == null) {
            main.showError("Select a conversation first.");
            return;
        }

        int index = messagesList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentMessages.size()) {
            main.showError("Select message to edit.");
            return;
        }

        MessageDto msg = currentMessages.get(index);

        javafx.scene.control.TextInputDialog dialog =
                new javafx.scene.control.TextInputDialog(msg.getContent());

        dialog.setTitle("Edit message");
        dialog.setHeaderText("Edit selected message");
        dialog.setContentText("New text:");

        var result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String newContent = result.get();
        if (newContent == null || newContent.isBlank()) return;
        if (newContent.equals(msg.getContent())) return;

        try {
            messageService.updateMessage(msg.getMessageId(), newContent);
            main.loadMessages(convId);
        } catch (Exception e) {
            main.showError("Failed to edit message: " + e.getMessage());
        }
    }
}
