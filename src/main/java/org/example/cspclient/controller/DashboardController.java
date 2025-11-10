package org.example.cspclient.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.Group;
import org.example.cspclient.util.AlertUtils;

import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Group> groupsList;

    @FXML
    public void initialize() {
        var user = ServiceLocator.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Вітаю, " + user.getName());
        }
        refreshGroups();
    }

    private void refreshGroups() {
        try {
            List<Group> groups = ServiceLocator.getApiClient().listGroups(ServiceLocator.getCurrentUser().getId());
            ObservableList<Group> items = FXCollections.observableArrayList(groups);
            groupsList.setItems(items);
        } catch (Exception e) {
            AlertUtils.error("Групи", e.getMessage());
        }
    }

    @FXML
    public void onCreateGroup(ActionEvent e) {
        Dialog<Group> dialog = new Dialog<>();
        dialog.setTitle("Нова група");

        Label nameL = new Label("Назва:");
        TextField nameF = new TextField();
        Label descL = new Label("Опис:");
        TextField descF = new TextField();

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, nameL, nameF);
        gp.addRow(1, descL, descF);
        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    return ServiceLocator.getApiClient().createGroup(ServiceLocator.getCurrentUser().getId(), nameF.getText(), descF.getText());
                } catch (Exception ex) {
                    AlertUtils.error("Створення групи", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Group> res = dialog.showAndWait();
        res.ifPresent(g -> refreshGroups());
    }

    @FXML
    public void onOpenSelected(ActionEvent e) {
        Group g = groupsList.getSelectionModel().getSelectedItem();
        if (g == null) {
            AlertUtils.error("Групи", "Оберіть групу");
            return;
        }
        try {
            ServiceLocator.getStage().getProperties().put("selectedGroup", g);
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadGroupDetailsScene());
        } catch (Exception ex) {
            AlertUtils.error("Навігація", ex.getMessage());
        }
    }

    @FXML
    public void openChats(ActionEvent e) {
        try {
            ServiceLocator.getStage().setScene(ServiceLocator.getViewManager().loadChatScene());
        } catch (Exception ex) {
            AlertUtils.error("Навігація", ex.getMessage());
        }
    }

    @FXML
    public void onLogout(ActionEvent e) {
        ServiceLocator.logout();
    }
}
