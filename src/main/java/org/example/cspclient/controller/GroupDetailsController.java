
package org.example.cspclient.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.*;
import org.example.cspclient.util.AlertUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GroupDetailsController {

    @FXML private Label groupTitle;
    @FXML private ListView<TaskItem> tasksList;
    @FXML private ListView<ResourceItem> resourcesList;
    @FXML private ListView<ActivityLog> activityList;
    @FXML private HBox taskButtonsBox;

    private Group currentGroup;

    @FXML
    public void initialize() {
        Object g = ServiceLocator.getStage().getProperties().get("selectedGroup");
        if (g instanceof Group) currentGroup = (Group) g;
        if (currentGroup != null) {
            groupTitle.setText(currentGroup.getName());
            // Pretty renderers
            tasksList.setCellFactory(list -> new ListCell<>() {
                @Override protected void updateItem(TaskItem t, boolean empty) {
                    super.updateItem(t, empty);
                    setText(empty || t==null ? null : t.getTitle());
                }
            });
            resourcesList.setCellFactory(list -> new ListCell<>() {
                @Override protected void updateItem(ResourceItem r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r==null) { setText(null); return; }
                    // Defensive: try to extract a reasonable label without knowing exact model getters
                    String label = tryStringGetter(r, "getTitle");
                    if (label == null) label = tryStringGetter(r, "getName");
                    if (label == null) label = tryStringGetter(r, "getUrl");
                    if (label == null) label = tryStringGetter(r, "getPath");
                    if (label == null) label = r.toString();
                    setText(label);
                }
            });
            activityList.setCellFactory(list -> new ListCell<>() {
                @Override protected void updateItem(ActivityLog a, boolean empty) {
                    super.updateItem(a, empty);
                    setText(empty || a==null ? null : a.getAction()+" • "+a.getTimestamp());
                }
            });
            refreshAll();
        }
    }

    private String tryStringGetter(Object obj, String method) {
        try {
            Method m = obj.getClass().getMethod(method);
            Object v = m.invoke(obj);
            return v != null ? v.toString() : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private void refreshAll() {
        try {
            tasksList.setItems(FXCollections.observableArrayList(
                    ServiceLocator.getApiClient().listTasks(currentGroup.getId())));
            resourcesList.setItems(FXCollections.observableArrayList(
                    ServiceLocator.getApiClient().listResources(currentGroup.getId())));
            activityList.setItems(FXCollections.observableArrayList(
                    ServiceLocator.getApiClient().listActivity(currentGroup.getId())));
        } catch (Exception e) {
            AlertUtils.error("Оновлення", e.getMessage());
        }
    }

    @FXML
    public void onAddTask(ActionEvent e) {
        Dialog<TaskItem> dialog = new Dialog<>();
        dialog.setTitle("Нове завдання");

        TextField titleF = new TextField();
        TextField descF = new TextField();
        DatePicker deadline = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Назва:"), titleF);
        grid.addRow(1, new Label("Опис:"), descF);
        grid.addRow(2, new Label("Дедлайн:"), deadline);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    return ServiceLocator.getApiClient().createTask(
                            currentGroup.getId(),
                            ServiceLocator.getCurrentUser().getId(),
                            titleF.getText(),
                            descF.getText(),
                            TaskStatus.OPEN,
                            deadline.getValue() != null ? deadline.getValue() : LocalDate.now().plusDays(3)
                    );
                } catch (Exception ex) {
                    AlertUtils.error("Створення завдання", ex.getMessage());
                }
            }
            return null;
        });

        Optional.ofNullable(dialog.showAndWait().orElse(null)).ifPresent(t -> refreshAll());
    }

    @FXML
    public void onChangeStatus(ActionEvent e) {
        TaskItem t = tasksList.getSelectionModel().getSelectedItem();
        if (t == null) { AlertUtils.error("Завдання", "Оберіть завдання"); return; }
        ChoiceDialog<TaskStatus> dlg = new ChoiceDialog<>(t.getStatus(), TaskStatus.values());
        dlg.setTitle("Статус");
        dlg.showAndWait().ifPresent(st -> {
            try {
                ServiceLocator.getApiClient().updateTaskStatus(t.getId(), st);
                refreshAll();
            } catch (Exception ex) {
                AlertUtils.error("Статус", ex.getMessage());
            }
        });
    }

    @FXML
    public void onAddResource(ActionEvent e) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Новий матеріал");
        dlg.setHeaderText("Введіть посилання або шлях");
        dlg.showAndWait().ifPresent(url -> {
            try {
                ServiceLocator.getApiClient().addResource(currentGroup.getId(), ServiceLocator.getCurrentUser().getId(), "Material", "link", url);
                refreshAll();
            } catch (Exception ex) { AlertUtils.error("Матеріали", ex.getMessage());}
        });
    }

    @FXML
    public void onBack(ActionEvent e) {
        try {
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadHomeScene());
        } catch (Exception ex) { AlertUtils.error("Навігація", ex.getMessage()); }
    }
}
