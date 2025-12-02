package com.example.messenger.ui.controllers;

import com.example.messenger.dto.UserDto;
import com.example.messenger.net.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

public class UserProfileController {

    @FXML private VBox profileRoot;
    @FXML private ImageView avatarImageView;
    @FXML private TextField nameField;
    @FXML private Label emailLabel;
    @FXML private TextField idField;

    // ГРА
    @FXML private GridPane gameGrid;
    @FXML private Label gameStatusLabel;

    @FXML private Button editButton;
    @FXML private Button backButton;
    @FXML private Button chooseAvatarButton;

    @FXML private HBox actionButtonsBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private UserDto user;
    private UserService userService;
    private Consumer<UserDto> onUserUpdated;
    private Runnable onCloseRequest;

    private File selectedAvatarFile;

    private static final int ROWS = 8;
    private static final int COLS = 12;
    private static final int MINES = 15;
    private Cell[][] cells = new Cell[ROWS][COLS];
    private boolean gameOver = false;

    // --- ФІКС ПОМИЛКИ: Додано перевантажений метод для сумісності з MainChatController ---
    public void setUserAndServices(UserDto user, UserService userService, Consumer<UserDto> onUserUpdated) {
        this.setUserAndServices(user, userService, onUserUpdated, null);
    }
    // -------------------------------------------------------------------------------------

    public void setUserAndServices(UserDto user, UserService userService, Consumer<UserDto> onUserUpdated, Runnable onCloseRequest) {
        this.user = user;
        this.userService = userService;
        this.onUserUpdated = onUserUpdated;
        this.onCloseRequest = onCloseRequest;

        updateUI();
        setEditMode(false);

        if (gameGrid != null) {
            startNewGame();
        }
    }

    private void updateUI() {
        if (user != null) {
            nameField.setText(user.getName() != null ? user.getName() : "");
            emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
            idField.setText(user.getUserId() != null ? "#" + user.getUserId() : "");
            loadAvatarFromUrl(user.getAvatarUrl());
        } else {
            nameField.setText("");
            emailLabel.setText("");
            idField.setText("");
            avatarImageView.setImage(null);
        }
    }

    private void setEditMode(boolean enable) {
        nameField.setEditable(enable);
        nameField.setStyle(enable
                ? "-fx-background-color: #253745; -fx-text-fill: white; -fx-background-radius: 4;"
                : "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-width: 0 0 1 0; -fx-border-color: #4A5C6A;");

        if (chooseAvatarButton != null) {
            chooseAvatarButton.setVisible(enable);
            chooseAvatarButton.setManaged(enable);
        }
        if (actionButtonsBox != null) {
            actionButtonsBox.setVisible(enable);
            actionButtonsBox.setManaged(enable);
        }
        if (editButton != null) editButton.setVisible(!enable);
        if (backButton != null) backButton.setDisable(enable);
    }

    // --- GAME LOGIC ---

    @FXML
    private void startNewGame() {
        if (gameGrid == null) return;

        gameGrid.getChildren().clear();
        gameStatusLabel.setText("");
        gameOver = false;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = new Cell(r, c);
                cells[r][c] = cell;
                gameGrid.add(cell, c, r);
            }
        }

        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < MINES) {
            int r = random.nextInt(ROWS);
            int c = random.nextInt(COLS);
            if (!cells[r][c].hasMine) {
                cells[r][c].hasMine = true;
                minesPlaced++;
            }
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!cells[r][c].hasMine) {
                    cells[r][c].neighborMines = countNeighbors(r, c);
                }
            }
        }
    }

    private int countNeighbors(int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nr = r + i, nc = c + j;
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) {
                    if (cells[nr][nc].hasMine) count++;
                }
            }
        }
        return count;
    }

    private void openCell(Cell cell) {
        if (gameOver || cell.isOpen || cell.isFlagged) return;
        cell.isOpen = true;
        cell.setDisable(true);

        if (cell.hasMine) {
            cell.setText("💣");
            cell.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-opacity: 1;");
            gameStatusLabel.setText("GAME OVER!");
            gameOver = true;
            revealAllMines();
        } else {
            cell.setStyle("-fx-background-color: #9BA8AB; -fx-opacity: 0.8;");
            if (cell.neighborMines > 0) {
                cell.setText(String.valueOf(cell.neighborMines));
                String color = switch(cell.neighborMines) {
                    case 1 -> "#2980b9"; case 2 -> "#27ae60"; case 3 -> "#c0392b"; default -> "#8e44ad";
                };
                cell.setStyle("-fx-background-color: #BDC3C7; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
            } else {
                cell.setText("");
                cell.setStyle("-fx-background-color: #7f8c8d;");
                openNeighbors(cell);
            }
        }
    }

    private void openNeighbors(Cell cell) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = cell.row + i, nc = cell.col + j;
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) {
                    openCell(cells[nr][nc]);
                }
            }
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c].hasMine) {
                    cells[r][c].setText("💣");
                    cells[r][c].setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
                }
            }
        }
    }

    private class Cell extends Button {
        int row, col;
        boolean hasMine = false, isOpen = false, isFlagged = false;
        int neighborMines = 0;

        public Cell(int r, int c) {
            this.row = r; this.col = c;
            setPrefSize(30, 30);
            setStyle("-fx-background-color: #34495e; -fx-border-color: #2c3e50;");
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) openCell(this);
                else if (e.getButton() == MouseButton.SECONDARY && !isOpen && !gameOver) {
                    isFlagged = !isFlagged;
                    setText(isFlagged ? "🚩" : "");
                    setStyle(isFlagged ? "-fx-background-color: #34495e; -fx-text-fill: #f1c40f;" : "-fx-background-color: #34495e;");
                }
            });
        }
    }

    // --- ACTIONS ---

    @FXML private void onEnableEdit() { setEditMode(true); }
    @FXML private void onCancelEdit() { selectedAvatarFile = null; updateUI(); setEditMode(false); }
    @FXML private void onBack() { if (onCloseRequest != null) onCloseRequest.run(); }
    @FXML private void onChooseAvatar(ActionEvent event) {
        Window window = profileRoot.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            selectedAvatarFile = file;
            avatarImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (user == null) return;
        String newName = nameField.getText() != null ? nameField.getText().trim() : user.getName();
        if (newName.isEmpty()) newName = user.getName();

        try {
            UserDto updated = userService.updateUserProfile(
                    user.getUserId(),
                    newName,
                    user.getAvatarUrl()
            );

            if (selectedAvatarFile != null) {
                updated = userService.uploadAvatarFile(updated.getUserId(), selectedAvatarFile);
            }
            this.user = updated;
            if (onUserUpdated != null) onUserUpdated.accept(updated);
            setEditMode(false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadAvatarFromUrl(String url) {
        if (url == null || url.isBlank()) { avatarImageView.setImage(null); return; }
        if (!url.startsWith("http")) url = "http://localhost:8080" + (url.startsWith("/") ? "" : "/") + url;
        try { avatarImageView.setImage(new Image(url, true)); } catch (Exception e) {}
    }
}