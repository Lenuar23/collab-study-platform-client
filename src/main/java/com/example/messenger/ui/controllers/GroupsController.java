package com.example.messenger.ui.controllers;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.net.GroupService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupsController {

    @FXML
    private ListView<String> groupsList;

    // Selected group details
    @FXML
    private Label selectedGroupLabel;
    @FXML
    private Label groupIdLabel;
    @FXML
    private Label groupOwnerLabel;
    @FXML
    private Label groupCreatedAtLabel;

    @FXML
    private TextField editNameField;
    @FXML
    private TextField editDescriptionField;
    @FXML
    private TextField editAvatarField;

    @FXML
    private ListView<String> membersList;
    @FXML
    private TextField addMemberField;

    // Create group form
    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField avatarField;

    private final GroupService groupService = new GroupService();
    private List<GroupDto> currentGroups = new ArrayList<>();
    private GroupDto selectedGroup = null;

    @FXML
    private void initialize() {
        groupsList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                return;
            }
            int idx = newVal.intValue();
            if (idx >= 0 && idx < currentGroups.size()) {
                showGroupDetails(currentGroups.get(idx));
            }
        });

        refreshGroups();
    }

    @FXML
    private void onRefreshGroups() {
        refreshGroups();
    }

    @FXML
    private void onCreateGroup() {
        String name = nameField.getText();
        String desc = descriptionField.getText();
        String avatar = avatarField.getText();

        if (name == null || name.isBlank()) {
            showError("Group name is required.");
            return;
        }

        try {
            groupService.createGroup(name, desc, avatar);
            nameField.clear();
            descriptionField.clear();
            avatarField.clear();
            showInfo("Success", "Group created.");
            refreshGroups();
        } catch (Exception e) {
            showError("Failed to create group: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveGroupChanges() {
        if (selectedGroup == null) {
            showError("No group selected.");
            return;
        }
        String name = editNameField.getText();
        String desc = editDescriptionField.getText();
        if (name == null || name.isBlank()) {
            showError("Group name cannot be empty.");
            return;
        }
        try {
            groupService.updateGroup(selectedGroup.getGroupId(), name, desc);
            showInfo("Success", "Group updated.");
            // refresh groups and keep selection
            Long keepId = selectedGroup.getGroupId();
            refreshGroups(keepId);
        } catch (Exception e) {
            showError("Failed to update group: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateAvatar() {
        if (selectedGroup == null) {
            showError("No group selected.");
            return;
        }
        String avatar = editAvatarField.getText();
        if (avatar == null || avatar.isBlank()) {
            showError("Avatar URL cannot be empty.");
            return;
        }
        try {
            groupService.updateAvatar(selectedGroup.getGroupId(), avatar);
            showInfo("Success", "Avatar updated.");
            Long keepId = selectedGroup.getGroupId();
            refreshGroups(keepId);
        } catch (Exception e) {
            showError("Failed to update avatar: " + e.getMessage());
        }
    }

    @FXML
    private void onAddMember() {
        if (selectedGroup == null) {
            showError("No group selected.");
            return;
        }
        String text = addMemberField.getText();
        if (text == null || text.isBlank()) {
            showError("Please enter user ID.");
            return;
        }
        long userId;
        try {
            userId = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            showError("User ID must be a number.");
            return;
        }

        try {
            groupService.addMember(selectedGroup.getGroupId(), userId);
            addMemberField.clear();
            loadMembers(selectedGroup);
            showInfo("Success", "User added to group.");
        } catch (Exception e) {
            showError("Failed to add member: " + e.getMessage());
        }
    }

    private void refreshGroups() {
        refreshGroups(null);
    }

    private void refreshGroups(Long keepSelectedId) {
        try {
            GroupDto[] arr = groupService.listGroups();
            currentGroups = (arr == null) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(arr));

            ObservableList<String> items = FXCollections.observableArrayList();
            for (GroupDto g : currentGroups) {
                items.add(g.toString());
            }
            groupsList.setItems(items);

            if (currentGroups.isEmpty()) {
                clearGroupDetails();
                return;
            }

            int indexToSelect = 0;
            if (keepSelectedId != null) {
                for (int i = 0; i < currentGroups.size(); i++) {
                    GroupDto g = currentGroups.get(i);
                    if (g.getGroupId() != null && g.getGroupId().equals(keepSelectedId)) {
                        indexToSelect = i;
                        break;
                    }
                }
            }

            groupsList.getSelectionModel().select(indexToSelect);
            showGroupDetails(currentGroups.get(indexToSelect));
        } catch (Exception e) {
            showError("Failed to load groups: " + e.getMessage());
        }
    }

    private void showGroupDetails(GroupDto group) {
        selectedGroup = group;
        if (group == null) {
            clearGroupDetails();
            return;
        }

        selectedGroupLabel.setText(group.getName() != null ? group.getName() : "Group");
        groupIdLabel.setText("ID: " + group.getGroupId());
        groupOwnerLabel.setText("Owner: " + group.getOwnerUserId());
        groupCreatedAtLabel.setText("Created: " + (group.getCreatedAt() != null ? group.getCreatedAt() : "-"));

        editNameField.setText(group.getName() != null ? group.getName() : "");
        editDescriptionField.setText(group.getDescription() != null ? group.getDescription() : "");
        editAvatarField.setText(group.getAvatarUrl() != null ? group.getAvatarUrl() : "");

        loadMembers(group);
    }

    private void loadMembers(GroupDto group) {
        if (group == null) {
            membersList.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            Long[] memberIds = groupService.getGroupMembers(group.getGroupId());
            ObservableList<String> items = FXCollections.observableArrayList();
            if (memberIds != null) {
                for (Long id : memberIds) {
                    items.add("User " + id);
                }
            }
            membersList.setItems(items);
        } catch (Exception e) {
            showError("Failed to load members: " + e.getMessage());
        }
    }

    private void clearGroupDetails() {
        selectedGroup = null;
        selectedGroupLabel.setText("No group selected");
        groupIdLabel.setText("ID: -");
        groupOwnerLabel.setText("Owner: -");
        groupCreatedAtLabel.setText("Created: -");
        editNameField.clear();
        editDescriptionField.clear();
        editAvatarField.clear();
        membersList.setItems(FXCollections.observableArrayList());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
