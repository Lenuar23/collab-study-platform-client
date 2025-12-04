package com.example.messenger.ui.controllers;

import com.example.messenger.dto.MaterialDto;
import com.example.messenger.net.MaterialService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class MaterialsController {

    @FXML private ListView<MaterialDto> materialsList;
    @FXML private VBox detailsPane;
    @FXML private Label fileNameLabel;
    @FXML private Label fileTypeLabel;

    // Notification components
    @FXML private VBox notificationPane;
    @FXML private Label notificationLabel;

    private final MaterialService materialService = new MaterialService();
    private Runnable closeAction;
    private MaterialDto selectedMaterial;

    public void setup(Runnable closeAction) {
        this.closeAction = closeAction;
        loadMaterials();
    }

    @FXML
    private void initialize() {
        materialsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showDetails(newV);
        });

        materialsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MaterialDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setStyle("-fx-padding: 10; -fx-cursor: hand;");

                    Label icon = new Label("📄");
                    icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

                    VBox info = new VBox(2);
                    Label name = new Label(item.getFilename());
                    name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    Label sub = new Label(item.getFileType() != null ? item.getFileType() : "FILE");
                    sub.setStyle("-fx-text-fill: #9BA8AB; -fx-font-size: 10px;");

                    info.getChildren().addAll(name, sub);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    box.getChildren().addAll(icon, info);
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    // --- CUSTOM NOTIFICATION ---
    private void showNotification(String message, boolean isError) {
        notificationLabel.setText(message);
        if (isError) {
            notificationPane.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 10; -fx-padding: 10;");
        } else {
            notificationPane.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 10; -fx-padding: 10;");
        }

        notificationPane.setVisible(true);
        notificationPane.setManaged(true);

        // Auto-hide after 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            notificationPane.setVisible(false);
            notificationPane.setManaged(false);
        });
        delay.play();
    }

    private void loadMaterials() {
        new Thread(() -> {
            try {
                MaterialDto[] materials = materialService.getMyMaterials();
                Platform.runLater(() -> {
                    if (materials != null) {
                        materialsList.setItems(FXCollections.observableArrayList(materials));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showDetails(MaterialDto mat) {
        this.selectedMaterial = mat;
        fileNameLabel.setText(mat.getFilename());
        fileTypeLabel.setText(mat.getFileType() != null ? mat.getFileType() : "Unknown");
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
    }

    @FXML private void onCloseDetails() {
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);
        materialsList.getSelectionModel().clearSelection();
        selectedMaterial = null;
    }

    @FXML private void onRefresh() { loadMaterials(); }
    @FXML private void onClose() { if (closeAction != null) closeAction.run(); }

    @FXML private void onDownload() {
        if (selectedMaterial == null) return;

        String originalName = selectedMaterial.getFilename();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(originalName);

        // Додаємо фільтр "Всі файли", але це лише для відображення
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        File dest = fileChooser.showSaveDialog(materialsList.getScene().getWindow());

        if (dest != null) {
            // --- ВИПРАВЛЕННЯ: ПЕРЕВІРКА РОЗШИРЕННЯ ---
            String destPath = dest.getAbsolutePath();
            String ext = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) {
                ext = originalName.substring(i); // наприклад ".pdf"
            }

            // Якщо збережений файл не має розширення, а оригінал мав - додаємо
            if (!destPath.endsWith(ext) && !ext.isEmpty()) {
                dest = new File(destPath + ext);
            }
            // ------------------------------------------

            final File finalDest = dest;
            new Thread(() -> {
                try {
                    if (selectedMaterial.getResourceId() != null) {
                        materialService.downloadFile(selectedMaterial.getResourceId(), finalDest);
                    } else if (selectedMaterial.getFileUrl() != null) {
                        materialService.downloadFileFromUrl(selectedMaterial.getFileUrl(), finalDest);
                    } else {
                        throw new Exception("File ID missing.");
                    }
                    Platform.runLater(() -> showNotification("File downloaded successfully!", false));
                } catch (Exception e) {
                    Platform.runLater(() -> showNotification("Download failed: " + e.getMessage(), true));
                }
            }).start();
        }
    }

}