package com.example.messenger.ui.controllers;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.TaskDto;
import com.example.messenger.dto.dttask.TaskProgressDto;
import com.example.messenger.dto.UserDto;
import com.example.messenger.net.ChatStompClient;
import com.example.messenger.net.GroupService;
import com.example.messenger.net.TaskService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
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
    @FXML private Button updateTaskStatusButton;
    @FXML private HBox assignBox;

    @FXML private ListView<ProgressItem> progressListView;

    private final TaskService taskService = new TaskService();
    private final GroupService groupService = new GroupService();
    private final UserService userService = new UserService();
    private final ChatStompClient stompClient = new ChatStompClient();

    private Long currentGroupId;
    private boolean isGlobalMode = false;
    private TaskDto selectedTask;
    private Runnable closeCallback;
    private final Map<Long, String> userNameCache = new HashMap<>();

    private static class ProgressItem {
        String userName;
        String status;
        String time;
        public ProgressItem(String userName, String status, String time) {
            this.userName = userName;
            this.status = status;
            this.time = time;
        }
    }

    @FXML
    private void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("OPEN", "IN_PROGRESS", "DONE"));

        statusCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
            }
        });
        statusCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            }
        });

        progressListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProgressItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-padding: 5 10 5 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 5;");

                    Label nameLbl = new Label(item.userName);
                    nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                    nameLbl.setMinWidth(80);

                    Label statusBadge = new Label(item.status);
                    String bg = "#95a5a6";
                    if ("DONE".equals(item.status)) bg = "#27ae60";
                    else if ("IN_PROGRESS".equals(item.status)) bg = "#2980b9";

                    statusBadge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 8 3 8; -fx-font-size: 10px; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label timeLbl = new Label(item.time);
                    timeLbl.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;");

                    row.getChildren().addAll(nameLbl, statusBadge, spacer, timeLbl);
                    setGraphic(row);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
                }
            }
        });

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

                    String displayStatus = item.getStatus() != null ? item.getStatus() : "OPEN";
                    String subText = "[" + displayStatus + "]" + (item.getDueDate() != null ? " • Due: " + item.getDueDate() : "");
                    Label meta = new Label(subText);

                    if ("DONE".equals(displayStatus)) meta.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 11px;");
                    else if ("IN_PROGRESS".equals(displayStatus)) meta.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11px;");
                    else meta.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 11px;");

                    info.getChildren().addAll(title, meta);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Label arrow = new Label("›");
                    arrow.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 18px;");

                    box.getChildren().addAll(info, arrow);
                    box.setOnMouseClicked(e -> openTaskDetails(item));
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0 5 0;");
                }
            }
        });
    }

    public void setupGroupMode(Long groupId, String groupName) {
        this.currentGroupId = groupId;
        this.isGlobalMode = false;
        headerTitle.setText("Tasks: " + groupName);
        backButton.setVisible(false); backButton.setManaged(false);
        createButton.setVisible(true); createButton.setManaged(true);

        loadTasks();

        Thread wsThread = new Thread(() -> {
            stompClient.connect();
            stompClient.subscribeToGroupTasks(groupId, (updatedTask) -> {
                Platform.runLater(() -> updateTaskInList(updatedTask));
            });
        });
        wsThread.setDaemon(true);
        wsThread.start();
    }

    public void setupGlobalMode(Runnable closeCallback) {
        this.isGlobalMode = true;
        this.currentGroupId = null;
        this.closeCallback = closeCallback;
        headerTitle.setText("All My Tasks");
        backButton.setVisible(true); backButton.setManaged(true);
        createButton.setVisible(false); createButton.setManaged(false);

        loadAllMyTasks();

        Thread wsThread = new Thread(() -> {
            try {
                stompClient.connect();
                GroupDto[] myGroups = groupService.listGroups();
                if (myGroups != null) {
                    for (GroupDto group : myGroups) {
                        stompClient.subscribeToGroupTasks(group.getGroupId(), (updatedTask) -> {
                            Platform.runLater(() -> updateTaskInList(updatedTask));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        wsThread.setDaemon(true);
        wsThread.start();
    }

    private void updateTaskInList(TaskDto task) {
        ObservableList<TaskDto> items = tasksListView.getItems();
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getTaskId().equals(task.getTaskId())) {
                items.set(i, task);
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(task);
        }

        if (selectedTask != null && selectedTask.getTaskId().equals(task.getTaskId())) {
            this.selectedTask = task;
            loadProgress();
        }
    }

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

    @FXML private void onBackToList() { returnToList(); }

    private void returnToList() {
        createTaskPane.setVisible(false); createTaskPane.setManaged(false);
        taskDetailsPane.setVisible(false); taskDetailsPane.setManaged(false);
        taskListPane.setVisible(true); taskListPane.setManaged(true);
        if (!isGlobalMode) createButton.setVisible(true);
        selectedTask = null;
    }

    @FXML private void onClose() {
        stompClient.disconnect();
        if (closeCallback != null) closeCallback.run();
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
            } catch (Exception e) { Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage())); }
        }).start();
    }

    @FXML private void onUpdateStatus() {
        if (selectedTask == null) return;
        String statusToSend = statusCombo.getValue();
        new Thread(() -> {
            try {
                taskService.updateUserTaskStatus(selectedTask.getTaskId(), SessionStore.getUserId(), statusToSend);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML private void onAssignUser() {
        Long myId = SessionStore.getUserId();
        if (myId == null) return;
        new Thread(() -> {
            try {
                taskService.assignUserToTask(selectedTask.getTaskId(), myId);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadTasks() {
        new Thread(() -> {
            try {
                TaskDto[] tasks = taskService.getTasksForGroup(currentGroupId);
                Platform.runLater(() -> {
                    if (tasks != null) tasksListView.setItems(FXCollections.observableArrayList(tasks));
                    else tasksListView.getItems().clear();
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

    private void openTaskDetails(TaskDto task) {
        this.selectedTask = task;
        detailTitleLabel.setText(task.getTitle());
        String desc = (task.getDescription() != null ? task.getDescription() : "No description.");
        if (task.getDueDate() != null) desc += "\nDue Date: " + task.getDueDate();
        detailDescLabel.setText(desc);

        statusCombo.setValue("OPEN");
        updateTaskStatusButton.setVisible(true); updateTaskStatusButton.setManaged(true); statusCombo.setDisable(false);
        assignBox.setVisible(false); assignBox.setManaged(false);

        taskListPane.setVisible(false); taskListPane.setManaged(false);
        createTaskPane.setVisible(false); createTaskPane.setManaged(false);
        taskDetailsPane.setVisible(true); taskDetailsPane.setManaged(true);
        createButton.setVisible(false);

        loadProgress();
    }

    private void loadProgress() {
        new Thread(() -> {
            try {
                TaskProgressDto[] prog = taskService.getTaskProgress(selectedTask.getTaskId());
                boolean amIAssigned = false;
                Long myId = SessionStore.getUserId();
                String myCurrentStatus = "OPEN";

                List<ProgressItem> displayList = new ArrayList<>();

                if (prog != null) {
                    for (TaskProgressDto p : prog) {
                        Long uid = p.getUserId();
                        String displayStatus = p.getStatus() != null ? p.getStatus() : "OPEN";

                        String timeStr = "";
                        if (p.getUpdatedAt() != null) {
                            try {
                                LocalDateTime dt = LocalDateTime.parse(p.getUpdatedAt());
                                timeStr = dt.format(DateTimeFormatter.ofPattern("HH:mm dd.MM"));
                            } catch (Exception e) {
                                timeStr = p.getUpdatedAt();
                            }
                        }

                        if (uid.equals(myId)) {
                            amIAssigned = true;
                            myCurrentStatus = displayStatus;
                            final String finalStatus = displayStatus;
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
                        displayList.add(new ProgressItem(name, displayStatus, timeStr));
                    }
                }

                boolean finalAmIAssigned = amIAssigned;
                String finalMyStatus = myCurrentStatus;

                Platform.runLater(() -> {
                    progressListView.setItems(FXCollections.observableArrayList(displayList));

                    if (!finalAmIAssigned) {
                        assignBox.setVisible(true); assignBox.setManaged(true);
                    } else {
                        assignBox.setVisible(false); assignBox.setManaged(false);
                    }
                    if (finalAmIAssigned && "DONE".equals(finalMyStatus)) {
                        updateTaskStatusButton.setVisible(false); updateTaskStatusButton.setManaged(false);
                        statusCombo.setDisable(true);
                    } else {
                        updateTaskStatusButton.setVisible(true); updateTaskStatusButton.setManaged(true);
                        statusCombo.setDisable(false);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}