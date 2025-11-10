package org.example.cspclient.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.*;
import org.example.cspclient.util.AlertUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GroupDetailsController {

    @FXML private Label groupTitleLabel;
    @FXML private TableView<TaskItem> tasksTable;
    @FXML private TableColumn<TaskItem, String> colTitle;
    @FXML private TableColumn<TaskItem, String> colDesc;
    @FXML private TableColumn<TaskItem, TaskStatus> colStatus;
    @FXML private TableColumn<TaskItem, LocalDate> colDeadline;

    @FXML private ListView<ResourceItem> resourcesList;
    @FXML private ListView<String> activityList;

    private Group group;

    @FXML
    public void initialize() {
        Object g = ServiceLocator.getStage().getProperties().get("selectedGroup");
        if (!(g instanceof Group)) {
            AlertUtils.error("Група", "Групу не знайдено");
            return;
        }
        this.group = (Group) g;
        groupTitleLabel.setText("Група: " + group.getName());

        // Setup table columns
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getStatus()));
        colDeadline.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDeadline()));

        refreshAll();
    }

    private void refreshAll() {
        try {
            List<TaskItem> tasks = ServiceLocator.getApiClient().listTasks(group.getId());
            tasksTable.setItems(FXCollections.observableArrayList(tasks));

            List<ResourceItem> resources = ServiceLocator.getApiClient().listResources(group.getId());
            resourcesList.setItems(FXCollections.observableArrayList(resources));

            // Activity
            List<ActivityLog> logs = ServiceLocator.getApiClient().listActivity(group.getId());
            ObservableList<String> lines = FXCollections.observableArrayList();
            for (ActivityLog log : logs) {
                lines.add(log.getTimestamp() + " | " + log.getAction() + " | " + log.getDetails());
            }
            activityList.setItems(lines);

        } catch (Exception ex) {
            AlertUtils.error("Оновлення", ex.getMessage());
        }
    }

    @FXML
    public void backToDashboard(ActionEvent e) {
        try {
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadDashboardScene());
        } catch (Exception ex) {
            AlertUtils.error("Навігація", ex.getMessage());
        }
    }

    @FXML
    public void onAddTask(ActionEvent e) {
        Dialog<TaskItem> dialog = new Dialog<>();
        dialog.setTitle("Нове завдання");

        TextField title = new TextField();
        TextField desc = new TextField();
        ComboBox<TaskStatus> status = new ComboBox<>();
        status.getItems().setAll(TaskStatus.values());
        status.getSelectionModel().select(TaskStatus.OPEN);
        DatePicker deadline = new DatePicker();

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, new Label("Заголовок:"), title);
        gp.addRow(1, new Label("Опис:"), desc);
        gp.addRow(2, new Label("Статус:"), status);
        gp.addRow(3, new Label("Дедлайн:"), deadline);
        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    LocalDate dl = deadline.getValue();
                    return ServiceLocator.getApiClient().createTask(group.getId(), ServiceLocator.getCurrentUser().getId(),
                            title.getText(), desc.getText(), status.getValue(), dl);
                } catch (Exception ex) {
                    AlertUtils.error("Створення завдання", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<TaskItem> res = dialog.showAndWait();
        res.ifPresent(t -> refreshAll());
    }

    @FXML
    public void onChangeStatus(ActionEvent e) {
        TaskItem selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.error("Завдання", "Оберіть завдання");
            return;
        }
        ChoiceDialog<TaskStatus> dlg = new ChoiceDialog<>(selected.getStatus(), TaskStatus.values());
        dlg.setTitle("Змінити статус");
        dlg.setHeaderText(selected.getTitle());
        dlg.setContentText("Новий статус:");
        Optional<TaskStatus> res = dlg.showAndWait();
        res.ifPresent(st -> {
            try {
                ServiceLocator.getApiClient().updateTaskStatus(selected.getId(), st);
                refreshAll();
            } catch (Exception ex) {
                AlertUtils.error("Оновлення", ex.getMessage());
            }
        });
    }

    @FXML
    public void onAddResource(ActionEvent e) {
        Dialog<ResourceItem> dialog = new Dialog<>();
        dialog.setTitle("Новий матеріал");

        TextField title = new TextField();
        TextField url = new TextField();
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("link", "file");
        type.getSelectionModel().select("link");

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, new Label("Назва:"), title);
        gp.addRow(1, new Label("URL/Path:"), url);
        gp.addRow(2, new Label("Тип:"), type);
        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    return ServiceLocator.getApiClient().addResource(group.getId(), ServiceLocator.getCurrentUser().getId(),
                            title.getText(), type.getValue(), url.getText());
                } catch (Exception ex) {
                    AlertUtils.error("Додавання матеріалу", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<ResourceItem> res = dialog.showAndWait();
        res.ifPresent(r -> refreshAll());
    }
}
