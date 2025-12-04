package com.example.messenger.ui.controllers;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.TaskDto;
import com.example.messenger.dto.TaskProgressDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.GroupService;
import com.example.messenger.net.TaskService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class TasksController {

    @FXML private VBox mainCard;
    @FXML private Label headerTitle;
    @FXML private Button backButton;
    @FXML private Button createButton;
    @FXML private Label statusLabel;

    @FXML private VBox taskListPane;
    @FXML private VBox createTaskPane;
    @FXML private VBox taskDetailsPane;

    @FXML private ListView<TaskDto> tasksListView;

    @FXML private TextField newTitleField;
    @FXML private TextArea newDescArea;
    @FXML private DatePicker newDueDatePicker;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailDescLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ListView<String> progressListView;
    @FXML private HBox assignBox;

    private final TaskService taskService = new TaskService();
    private final GroupService groupService = new GroupService();
    private final UserService userService = new UserService();

    private Long currentGroupId;
    private boolean isGlobalMode = false;

    private TaskDto selectedTask;
    private Runnable closeCallback;

    private final Map<Long, String> userNameCache = new HashMap<>();

    @FXML
    private void initialize() {
        // УВАГА: В інтерфейсі "JOINED" замінює "OPEN"
        statusCombo.setItems(FXCollections.observableArrayList(
                "JOINED",      // UI: JOINED -> Server: OPEN
                "IN_PROGRESS",
                "DONE"
        ));

        tasksListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TaskDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setStyle("-fx-padding: 12; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-cursor: hand;");

                    VBox info = new VBox(4);
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                    // Отримуємо "сирий" статус від сервера
                    String rawStatus = item.getStatus() != null ? item.getStatus() : "OPEN";

                    // Конвертуємо для відображення
                    String displayStatus = rawStatus;
                    if ("OPEN".equals(rawStatus)) displayStatus = "JOINED";

                    String groupInfo = isGlobalMode ? "Group " + item.getGroupId() + " • " : "";
                    String dateInfo = (item.getDueDate() != null ? " • Due: " + item.getDueDate() : "");

                    String subText = groupInfo + "[" + displayStatus + "]" + dateInfo;
                    Label meta = new Label(subText);

                    // Стилі
                    if ("DONE".equals(rawStatus)) meta.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 11px;"); // Green
                    else if ("FAILED".equals(rawStatus)) meta.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;"); // Red
                    else if ("DEFERRED".equals(rawStatus)) meta.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;"); // Orange
                    else if ("IN_PROGRESS".equals(rawStatus)) meta.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11px;"); // Blue
                    else meta.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 11px;"); // Grey (Joined/Open)

                    info.getChildren().addAll(title, meta);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    Label arrow = new Label("›");
                    arrow.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 18px; -fx-font-weight: bold;");

                    box.getChildren().addAll(info, arrow);
                    box.setOnMouseClicked(e -> openTaskDetails(item));

                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0 5 0;");
                }
            }
        });
    }

    // --- SETUP ---

    public void setupGroupMode(Long groupId, String groupName) {
        this.currentGroupId = groupId;
        this.isGlobalMode = false;

        headerTitle.setText("Tasks: " + groupName);
        backButton.setVisible(false); backButton.setManaged(false);
        createButton.setVisible(true); createButton.setManaged(true);
        loadTasks();
    }

    public void setupGlobalMode(Runnable closeCallback) {
        this.isGlobalMode = true;
        this.currentGroupId = null;
        this.closeCallback = closeCallback;

        headerTitle.setText("All My Tasks");
        backButton.setVisible(true); backButton.setManaged(true);
        createButton.setVisible(false); createButton.setManaged(false);
        loadAllMyTasks();
    }

    // --- NAVIGATION ---

    @FXML private void onShowCreate() {
        taskListPane.setVisible(false); taskListPane.setManaged(false);
        taskDetailsPane.setVisible(false); taskDetailsPane.setManaged(false);
        createTaskPane.setVisible(true); createTaskPane.setManaged(true);
        createButton.setVisible(false);
    }

    @FXML private void onCancelCreate() {
        newTitleField.clear(); newDescArea.clear(); newDueDatePicker.setValue(null);
        returnToList();
    }

    @FXML private void onBackToList() {
        returnToList();
    }

    private void returnToList() {
        createTaskPane.setVisible(false); createTaskPane.setManaged(false);
        taskDetailsPane.setVisible(false); taskDetailsPane.setManaged(false);
        taskListPane.setVisible(true); taskListPane.setManaged(true);
        if (!isGlobalMode) createButton.setVisible(true);
    }

    @FXML private void onClose() {
        if (closeCallback != null) closeCallback.run();
    }

    // --- LOGIC ---

    private void openTaskDetails(TaskDto task) {
        this.selectedTask = task;

        detailTitleLabel.setText(task.getTitle());
        String desc = (task.getDescription() != null ? task.getDescription() : "No description.");
        if (task.getDueDate() != null) desc += "\nDue Date: " + task.getDueDate();
        detailDescLabel.setText(desc);

        // Скидання: "JOINED" у випадаючому списку, що відповідає "OPEN"
        statusCombo.setValue("JOINED");

        // Ховаємо кнопку "Assign" спочатку
        assignBox.setVisible(false);
        assignBox.setManaged(false);

        taskListPane.setVisible(false); taskListPane.setManaged(false);
        createTaskPane.setVisible(false); createTaskPane.setManaged(false);
        taskDetailsPane.setVisible(true); taskDetailsPane.setManaged(true);
        createButton.setVisible(false);

        loadProgress();
    }

    @FXML private void onCreateTask() {
        String title = newTitleField.getText();
        String desc = newDescArea.getText();
        String due = null;
        if (newDueDatePicker.getValue() != null) {
            due = newDueDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        if (title.isBlank()) { statusLabel.setText("Title required!"); return; }

        final String finalDue = due;
        new Thread(() -> {
            try {
                taskService.createTask(currentGroupId, title, desc, finalDue);
                Platform.runLater(() -> {
                    onCancelCreate();
                    loadTasks();
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void onUpdateStatus() {
        if (selectedTask == null) return;
        String uiStatus = statusCombo.getValue();

        // МАППІНГ: UI -> SERVER
        String serverStatus = uiStatus;
        if ("JOINED".equals(uiStatus)) {
            serverStatus = "OPEN";
        }

        final String statusToSend = serverStatus;

        new Thread(() -> {
            try {
                taskService.updateUserTaskStatus(selectedTask.getTaskId(), SessionStore.getUserId(), statusToSend);
                Platform.runLater(this::loadProgress);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML private void onAssignUser() {
        Long myId = SessionStore.getUserId();
        if (myId == null) return;

        new Thread(() -> {
            try {
                taskService.assignUserToTask(selectedTask.getTaskId(), myId);
                Platform.runLater(() -> {
                    loadProgress();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // --- DATA LOADING ---

    private void loadTasks() {
        new Thread(() -> {
            try {
                TaskDto[] tasks = taskService.getTasksForGroup(currentGroupId);
                Platform.runLater(() -> {
                    if (tasks != null) {
                        tasksListView.setItems(FXCollections.observableArrayList(tasks));
                    } else {
                        tasksListView.getItems().clear();
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadAllMyTasks() {
        new Thread(() -> {
            try {
                GroupDto[] groups = groupService.listGroups();
                List<TaskDto> allTasks = new ArrayList<>();
                if (groups != null) {
                    for (GroupDto g : groups) {
                        try {
                            TaskDto[] gTasks = taskService.getTasksForGroup(g.getGroupId());
                            if (gTasks != null) allTasks.addAll(Arrays.asList(gTasks));
                        } catch (Exception ignore) {}
                    }
                }
                Platform.runLater(() -> tasksListView.setItems(FXCollections.observableArrayList(allTasks)));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadProgress() {
        new Thread(() -> {
            try {
                TaskProgressDto[] prog = taskService.getTaskProgress(selectedTask.getTaskId());

                boolean amIAssigned = false;
                Long myId = SessionStore.getUserId();

                List<String> displayList = new ArrayList<>();

                if (prog != null) {
                    for (TaskProgressDto p : prog) {
                        Long uid = p.getUserId();


                        if (uid.equals(myId)) {
                            amIAssigned = true;

                           
                            String myRawStatus = p.getStatus();
                            String myUiStatus = myRawStatus;
                            if ("OPEN".equals(myRawStatus)) myUiStatus = "JOINED";

                            final String finalStatus = myUiStatus;
                            Platform.runLater(() -> statusCombo.setValue(finalStatus));
                        }

                        String name = "User " + uid;
                        if (userNameCache.containsKey(uid)) {
                            name = userNameCache.get(uid);
                        } else {
                            try {
                                UserDto u = userService.getUserById(uid);
                                if (u.getName() != null) {
                                    name = u.getName();
                                    userNameCache.put(uid, name);
                                }
                            } catch (Exception ignore) {}
                        }


                        String displayStatus = p.getStatus();
                        if ("OPEN".equals(displayStatus)) displayStatus = "JOINED";

                        displayList.add(name + ": " + displayStatus);
                    }
                }

                boolean finalAmIAssigned = amIAssigned;
                Platform.runLater(() -> {
                    progressListView.setItems(FXCollections.observableArrayList(displayList));

                    // Кнопка Assign з'являється, тільки якщо я ще не в таску і це режим групи
                    if (!isGlobalMode && !finalAmIAssigned) {
                        assignBox.setVisible(true);
                        assignBox.setManaged(true);
                    } else {
                        assignBox.setVisible(false);
                        assignBox.setManaged(false);
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}