package org.example.cspclient.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.model.Group;
import org.example.cspclient.util.AlertUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class GroupsController {

    @FXML private ListView<Group> groupsList;

    @FXML
    public void initialize() {
        groupsList.setCellFactory(list -> new ListCell<>() {
            private final HBox box = new HBox(10);
            private final ImageView avatar = new ImageView();
            private final Label name = new Label();
            {
                box.setAlignment(Pos.CENTER_LEFT);
                avatar.setFitWidth(28); avatar.setFitHeight(28); avatar.setPreserveRatio(true);
                box.getChildren().addAll(avatar, name);
            }
            @Override protected void updateItem(Group g, boolean empty) {
                super.updateItem(g, empty);
                if (empty || g == null) { setGraphic(null); return; }
                name.setText(g.getName());
                String url = g.getAvatarUrl();
                try {
                    if (url == null || url.isBlank()) {
                        avatar.setImage(new Image(GroupsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
                    } else if (url.startsWith("resource:/")) {
                        avatar.setImage(new Image(GroupsController.class.getResourceAsStream(url.replace("resource:", ""))));
                    } else {
                        avatar.setImage(new Image(new File(url).toURI().toString()));
                    }
                } catch (Exception ex) {
                    avatar.setImage(new Image(GroupsController.class.getResourceAsStream("/org/example/cspclient/icon.png")));
                }
                setGraphic(box);
            }
        });
        refreshGroups();
    }

    private void refreshGroups() {
        try {
            List<Group> groups = ServiceLocator.getApiClient().listGroups(ServiceLocator.getCurrentUser().getId());
            ObservableList<Group> items = FXCollections.observableArrayList(groups);
            groupsList.setItems(items);
        } catch (Exception e) {
            AlertUtils.error("Groups", e.getMessage());
        }
    }

    @FXML
    public void onCreateGroup(ActionEvent e) {
        Dialog<Group> dialog = new Dialog<>();
        dialog.setTitle("New group");

        Label nameL = new Label("Name:");
        TextField nameF = new TextField();
        Label descL = new Label("Description:");
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
                    AlertUtils.error("Create group", ex.getMessage());
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
        if (g == null) { AlertUtils.error("Groups", "Select a group"); return; }
        try {
            ServiceLocator.getStage().getProperties().put("selectedGroup", g);
            ServiceLocator.setScenePreserveBounds(ServiceLocator.getViewManager().loadGroupDetailsScene());
        } catch (Exception ex) {
            AlertUtils.error("Navigation", ex.getMessage());
        }
    }
}
