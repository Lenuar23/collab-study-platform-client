package com.example.messenger.ui.controllers.chat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class ChatSidebarController {

    @FXML
    private ListView<ConversationItem> conversationsList;

    @FXML
    private TextField directUserIdField;

    @FXML
    private TextField groupNameField;

    @FXML
    private TextField groupParticipantsField;

    @FXML
    private TextField addParticipantField;

    private MainChatController main;
    private List<ConversationItem> backingList = new ArrayList<>();

    public void setMain(MainChatController main) {
        this.main = main;
    }

    public void setList(javafx.collections.ObservableList<ConversationItem> list) {
        conversationsList.setItems(list);
        this.backingList = list;
        setupSelectionListener();
    }

    public void refreshList() {
        conversationsList.refresh();
    }

    public void selectFirst() {
        if (!conversationsList.getItems().isEmpty()) {
            conversationsList.getSelectionModel().selectFirst();
        }
    }

    public void selectConversation(ConversationItem item) {
        conversationsList.getSelectionModel().select(item);
        if (main != null) {
            main.selectConversation(item);
        }
    }

    private void setupSelectionListener() {
        conversationsList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && main != null) {
                        main.selectConversation(newVal);
                    }
                });
    }

    // ---------------------------------------------------------
    //   DIRECT CHAT
    // ---------------------------------------------------------

    @FXML
    private void onOpenDirectChat() {
        if (main == null) return;

        String userIdText = directUserIdField.getText();
        if (userIdText == null || userIdText.isBlank()) {
            main.showError("Please enter other user ID.");
            return;
        }

        long userId;
        try {
            userId = Long.parseLong(userIdText);
        } catch (Exception e) {
            main.showError("User ID must be a number.");
            return;
        }

        main.openDirectChat(userId);
        directUserIdField.clear();
    }

    // ---------------------------------------------------------
    //   CREATE GROUP
    // ---------------------------------------------------------

    @FXML
    private void onCreateGroup() {
        if (main == null) return;

        String name = groupNameField.getText();
        String participantsText = groupParticipantsField.getText();

        if (participantsText == null || participantsText.isBlank()) {
            main.showError("Please enter participant IDs.");
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (String p : participantsText.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) {
                try {
                    ids.add(Long.parseLong(t));
                } catch (NumberFormatException e) {
                    main.showError("Participant IDs must be numbers.");
                    return;
                }
            }
        }

        main.createGroup(name, ids);

        groupNameField.clear();
        groupParticipantsField.clear();
    }

    // ---------------------------------------------------------
    //   ADD / REMOVE PARTICIPANT
    // ---------------------------------------------------------

    @FXML
    private void onAddParticipant() {
        if (main == null) return;

        String userIdText = addParticipantField.getText();
        if (userIdText == null || userIdText.isBlank()) {
            main.showError("Please enter user ID.");
            return;
        }

        try {
            long id = Long.parseLong(userIdText.trim());
            main.addParticipant(id);
            addParticipantField.clear();
        } catch (Exception e) {
            main.showError("User ID must be a number.");
        }
    }

    @FXML
    private void onRemoveParticipant() {
        if (main == null) return;

        String userIdText = addParticipantField.getText();
        if (userIdText == null || userIdText.isBlank()) {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Remove participant");
            dlg.setHeaderText("Enter user ID to remove:");
            dlg.setContentText("User ID:");
            var res = dlg.showAndWait();
            if (res.isEmpty()) return;
            userIdText = res.get();
        }

        try {
            long id = Long.parseLong(userIdText.trim());
            main.removeParticipant(id);
            addParticipantField.clear();
        } catch (Exception e) {
            main.showError("User ID must be a number.");
        }
    }

    // ---------------------------------------------------------
    //   PARTICIPANTS LIST
    // ---------------------------------------------------------

    @FXML
    private void onShowParticipants() {
        if (main != null) {
            main.showParticipants();
        }
    }

    // ---------------------------------------------------------
    //   UTILS
    // ---------------------------------------------------------

    public Window getRootSceneWindow() {
        return conversationsList.getScene().getWindow();
    }
}
