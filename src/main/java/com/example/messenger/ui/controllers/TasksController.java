package com.example.messenger.ui.controllers;

import com.example.messenger.dto.TaskDto;
import com.example.messenger.dto.TaskProgressDto;
import com.example.messenger.net.TaskService;
import com.example.messenger.store.SessionStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TasksController {

    @FXML
    private Label groupTitleLabel;

    @FXML
    private TextField groupIdField;

    @FXML
    private ListView<String> tasksList;

    @FXML
    private TextField newTitleField;

    @FXML
    private TextField newDueDateField;

    @FXML
    private TextArea newDescriptionArea;

    @FXML
    private TextField assignUserIdField;

    @FXML
    private TextField statusUserIdField;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private ListView<String> progressList;

    private final TaskService taskService = new TaskService();
    private Long currentGroupId;
    private Long groupId;
    private List<TaskDto> currentTasks = new ArrayList<>();
    private Long selectedTaskId;

    @FXML
    private void initialize() {
        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList("OPEN", "IN_PROGRESS", "DONE"));
        }

        if (tasksList != null) {
            tasksList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;

                int idx = newVal.intValue();
                if (idx < 0 || idx >= currentTasks.size()) return;

                TaskDto task = currentTasks.get(idx);
                selectedTaskId = task.getTaskId();
                progressList.setItems(FXCollections.observableArrayList());
            });
        }
    }

    // ----------------------------
    // AUTOMATIC GROUP ID INJECTION
    // ----------------------------
    public void setGroup(Long groupId, String groupName) {
        this.groupId = groupId;
        this.currentGroupId = groupId;

        if (groupTitleLabel != null) {
            if (groupName != null && !groupName.isBlank()) {
                groupTitleLabel.setText("Tasks for group: " + groupName + " (ID " + groupId + ")");
            } else {
                groupTitleLabel.setText("Tasks for group ID " + groupId);
            }
        }

        loadTasks();
    }


    // ----------------------------
    // LOAD TASKS
    // ----------------------------
    @FXML
    private void onLoadTasks(ActionEvent event) {
        if (currentGroupId == null) {
            showError("No group ID provided.");
            return;
        }
        loadTasks();
    }

    private void loadTasks() {
        if (currentGroupId == null) return;

        try {
            TaskDto[] arr = taskService.getTasksForGroup(currentGroupId);
            currentTasks = (arr == null) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(arr));

            ObservableList<String> items = FXCollections.observableArrayList();
            for (TaskDto t : currentTasks) {
                StringBuilder sb = new StringBuilder();
                sb.append("Task ").append(t.getTaskId()).append(": ");
                sb.append(t.getTitle() != null ? t.getTitle() : "(no title)");
                if (t.getStatus() != null) sb.append(" [").append(t.getStatus()).append("]");
                if (t.getDueDate() != null) sb.append(" (due ").append(t.getDueDate()).append(")");
                items.add(sb.toString());
            }

            tasksList.setItems(items);
            selectedTaskId = null;
            progressList.setItems(FXCollections.observableArrayList());

        } catch (IOException | InterruptedException e) {
            showError("Failed to load tasks: " + e.getMessage());
        }
    }

    // ----------------------------
    // CREATE TASK
    // ----------------------------
    @FXML
    private void onCreateTask(ActionEvent event) {
        if (currentGroupId == null) {
            showError("No group selected.");
            return;
        }

        String title = newTitleField.getText();
        String dueDate = newDueDateField.getText();
        String description = newDescriptionArea.getText();

        if (title == null || title.isBlank()) {
            showError("Task title cannot be empty.");
            return;
        }

        if (dueDate != null && dueDate.isBlank()) dueDate = null;
        if (description != null && description.isBlank()) description = null;

        try {
            taskService.createTask(currentGroupId, title.trim(), description, dueDate);

            newTitleField.clear();
            newDueDateField.clear();
            newDescriptionArea.clear();

            showInfo("Success", "Task created.");
            loadTasks();
        } catch (IOException | InterruptedException e) {
            showError("Failed to create task: " + e.getMessage());
        }
    }

    // ----------------------------
    // ASSIGN USER
    // ----------------------------
    @FXML
    private void onAssignUser(ActionEvent event) {
        if (selectedTaskId == null) {
            showError("Select a task first.");
            return;
        }

        String text = assignUserIdField.getText();
        if (text == null || text.isBlank()) {
            showError("Enter User ID.");
            return;
        }

        Long userId;
        try {
            userId = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            showError("User ID must be a number.");
            return;
        }

        try {
            taskService.assignUserToTask(selectedTaskId, userId);
            showInfo("Success", "User assigned.");
            assignUserIdField.clear();
            loadProgressInternal();
        } catch (IOException | InterruptedException e) {
            showError("Failed: " + e.getMessage());
        }
    }

    // ----------------------------
    // LOAD PROGRESS
    // ----------------------------
    @FXML
    private void onLoadProgress(ActionEvent event) {
        if (selectedTaskId == null) {
            showError("Select a task.");
            return;
        }
        loadProgressInternal();
    }

    private void loadProgressInternal() {
        if (selectedTaskId == null) return;

        try {
            TaskProgressDto[] arr = taskService.getTaskProgress(selectedTaskId);
            ObservableList<String> items = FXCollections.observableArrayList();

            if (arr != null) {
                for (TaskProgressDto p : arr) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("User ").append(p.getUserId())
                            .append(": ").append(p.getStatus());
                    if (p.getUpdatedAt() != null)
                        sb.append(" (updated ").append(p.getUpdatedAt()).append(")");
                    if (p.getCompletedAt() != null)
                        sb.append(", completed ").append(p.getCompletedAt());
                    items.add(sb.toString());
                }
            }

            progressList.setItems(items);

        } catch (IOException | InterruptedException e) {
            showError("Failed: " + e.getMessage());
        }
    }

    // ----------------------------
    // UPDATE STATUS
    // ----------------------------
    @FXML
    private void onUpdateStatus(ActionEvent event) {
        if (selectedTaskId == null) {
            showError("Select a task.");
            return;
        }

        String status = statusCombo.getValue();
        if (status == null || status.isBlank()) {
            showError("Select status.");
            return;
        }

        Long userId;
        String text = statusUserIdField.getText();

        if (text == null || text.isBlank()) {
            userId = SessionStore.getUserId();
            if (userId == null) {
                showError("User ID missing.");
                return;
            }
        } else {
            try {
                userId = Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                showError("User ID must be a number.");
                return;
            }
        }

        try {
            taskService.updateUserTaskStatus(selectedTaskId, userId, status);
            showInfo("Success", "Status updated.");
            loadProgressInternal();
        } catch (IOException | InterruptedException e) {
            showError("Failed: " + e.getMessage());
        }
    }

    // ----------------------------
    // ALERT HELPERS
    // ----------------------------
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
