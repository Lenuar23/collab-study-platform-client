package com.example.messenger.ui.controllers.groupacctions;

import com.example.messenger.dto.GroupDto;
import com.example.messenger.net.GroupService;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;

public class GroupSettingsManager {

    private final TextField editNameField;
    private final TextField editDescField;
    private final ImageView groupAvatarHeader;
    private final Label groupNameLabel;
    private final Label groupIdLabel;
    private final Canvas matrixCanvas;
    private final Label quoteLabel;
    private final GroupService groupService;
    private final BiConsumer<String, Boolean> notificationCallback;

    private GroupDto group;
    private Long conversationId;
    private AnimationTimer matrixTimer;

    private static final String[] QUOTES = {
            "\"It works on my machine.\"", "\"Code is poetry.\"", "\"Java is life.\"",
            "\"Hello World!\"", "\"404 Wisdom Not Found\""
    };

    public GroupSettingsManager(TextField name, TextField desc, ImageView avatar, Label gName, Label gId,
                                Canvas canvas, Label quote, BiConsumer<String, Boolean> callback) {
        this.editNameField = name;
        this.editDescField = desc;
        this.groupAvatarHeader = avatar;
        this.groupNameLabel = gName;
        this.groupIdLabel = gId;
        this.matrixCanvas = canvas;
        this.quoteLabel = quote;
        this.notificationCallback = callback;
        this.groupService = new GroupService();
    }

    public void setup(GroupDto group, Long conversationId) {
        this.group = group;
        this.conversationId = conversationId;

        updateHeader();
        String cleanDesc = group.getDescription() != null ? group.getDescription().replaceAll("\\[CHAT:\\d+\\]", "").trim() : "";
        editNameField.setText(group.getName());
        editDescField.setText(cleanDesc);
    }

    private void updateHeader() {
        groupNameLabel.setText(group.getName());
        groupIdLabel.setText("#" + group.getGroupId());
        if (groupAvatarHeader != null) {
            groupAvatarHeader.setClip(new Circle(20, 20, 20));
            loadAvatar(group.getAvatarUrl(), groupAvatarHeader);
        }
    }

    public void saveSettings() {
        String newName = editNameField.getText();
        String newDesc = editDescField.getText();
        String finalDesc = newDesc + (conversationId != null ? " [CHAT:" + conversationId + "]" : "");
        new Thread(() -> {
            try {
                GroupDto updated = groupService.updateGroup(group.getGroupId(), newName, finalDesc);
                Platform.runLater(() -> {
                    this.group = updated;
                    updateHeader();
                    notificationCallback.accept("Settings Updated!", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> notificationCallback.accept("Error updating settings", true));
            }
        }).start();
    }

    public void changeAvatar(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Group Avatar");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            new Thread(() -> {
                try {
                    groupService.uploadGroupAvatar(group.getGroupId(), file);
                    Platform.runLater(() -> {
                        notificationCallback.accept("Avatar updated!", false);
                        if (groupAvatarHeader != null) groupAvatarHeader.setImage(new Image(file.toURI().toString()));
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> notificationCallback.accept("Failed to upload: " + e.getMessage(), true));
                }
            }).start();
        }
    }

    // --- VISUALS ---
    public void startVisuals() {
        showRandomQuote();
        startMatrix();
    }

    public void stopVisuals() {
        if (matrixTimer != null) matrixTimer.stop();
    }

    private void showRandomQuote() {
        if (quoteLabel != null) quoteLabel.setText(QUOTES[new Random().nextInt(QUOTES.length)]);
    }

    private void startMatrix() {
        if (matrixTimer != null) matrixTimer.stop();
        if (matrixCanvas == null) return;
        GraphicsContext gc = matrixCanvas.getGraphicsContext2D();
        int fontSize = 14;
        gc.setFont(new Font("Monospace", fontSize));
        final int[] columns = new int[1];
        final int[][] drops = new int[1][];
        matrixTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = matrixCanvas.getWidth();
                double h = matrixCanvas.getHeight();
                if (drops[0] == null || drops[0].length != (int)(w / fontSize)) {
                    columns[0] = (int) (w / fontSize);
                    drops[0] = new int[columns[0]];
                    Arrays.fill(drops[0], 1);
                }
                gc.setFill(Color.rgb(0, 0, 0, 0.1));
                gc.fillRect(0, 0, w, h);
                gc.setFill(Color.web("#00FF00"));
                for (int i = 0; i < drops[0].length; i++) {
                    String text = String.valueOf((char) (0x30A0 + Math.random() * 96));
                    double x = i * fontSize;
                    double y = drops[0][i] * fontSize;
                    gc.fillText(text, x, y);
                    if (y > h && Math.random() > 0.975) { drops[0][i] = 0; }
                    drops[0][i]++;
                }
            }
        };
        matrixTimer.start();
    }

    private void loadAvatar(String url, ImageView target) {
        if (url == null || url.isBlank()) { target.setImage(null); return; }
        if (!url.startsWith("http")) url = "http://localhost:8080" + (url.startsWith("/") ? "" : "/") + url;
        try { target.setImage(new Image(url, true)); } catch (Exception e) {}
    }
}