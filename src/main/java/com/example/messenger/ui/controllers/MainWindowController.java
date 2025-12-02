package com.example.messenger.ui.controllers;

import com.example.messenger.dto.UserDto;
import com.example.messenger.net.AuthService; // <--- Додано
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore; // <--- Додано для страховки
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene; // <--- Додано
import javafx.stage.Stage; // <--- Додано
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;

public class MainWindowController {

    @FXML private StackPane rootPane;
    @FXML private Pane canvas;

    @FXML private StackPane centerHalo;
    @FXML private Label greetingLabel;

    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Button chatsButton;
    @FXML private Button tasksButton;
    @FXML private Button groupsButton;
    @FXML private Button materialsButton;

    private Button[] orbitButtons;
    private Circle[] buttonGlows;
    private Circle centerGlow;

    private boolean expanded = false;
    private boolean animating = false;

    private Timeline pulseTimeline;

    // --- СЕРВІСИ ---
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService(); // <--- Додано сервіс авторизації

    private UserDto currentUser;
    private Parent profileView;
    private UserProfileController profileController;

    @FXML
    public void initialize() {
        orbitButtons = new Button[]{
                profileButton,
                logoutButton,
                tasksButton,
                materialsButton,
                groupsButton,
                chatsButton
        };

        for (Button b : orbitButtons) {
            b.setOpacity(0);
            b.setVisible(false);
            b.setMouseTransparent(true);
        }

        // ПРИВ'ЯЗКА ПОДІЙ
        profileButton.setOnAction(e -> openUserProfile());
        logoutButton.setOnAction(e -> onLogout()); // <--- ПРИВ'ЯЗКА КНОПКИ LOGOUT

        Platform.runLater(() -> {
            setupGlowEffects();
            setupLayout();
            setupResizeListeners();
            startPulseAnimation();
            fadeInScreen();
        });

        centerHalo.setOnMouseClicked(e -> {
            if (!animating) {
                toggleMenu();
            }
        });
    }

    // --- ЛОГІКА LOGOUT (ВИХІД) ---
    private void onLogout() {
        try {
            // 1. Спроба зробити logout на сервері
            authService.logout();
        } catch (Exception e) {
            System.err.println("Logout server error: " + e.getMessage());
            // Якщо сервер не відповів, все одно чистимо сесію локально
            SessionStore.clear();
        }

        // 2. Перехід на екран логіну
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Messenger - Login");
            // Можна додати центрування, якщо потрібно: stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load login screen.");
        }
    }

    // --- ЛОГІКА ВІДКРИТТЯ ПРОФІЛЮ ---
    private void openUserProfile() {
        try {
            if (profileView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user_profile.fxml"));
                profileView = loader.load();
                profileController = loader.getController();
                StackPane.setAlignment(profileView, javafx.geometry.Pos.CENTER);
            }

            profileController.setUserAndServices(
                    currentUser,
                    userService,
                    updatedUser -> setCurrentUser(updatedUser),
                    this::closeUserProfile
            );

            // Початковий стан анімації
            profileView.setOpacity(0);
            profileView.setScaleX(0.9);
            profileView.setScaleY(0.9);

            if (!rootPane.getChildren().contains(profileView)) {
                rootPane.getChildren().add(profileView);
            }

            canvas.setEffect(new GaussianBlur(15));

            Timeline showAnim = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(profileView.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(profileView.scaleXProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(profileView.scaleYProperty(), 1, Interpolator.EASE_OUT)
                    )
            );
            showAnim.play();

            if (expanded) {
                collapseMenu();
                expanded = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeUserProfile() {
        if (profileView == null) return;

        Timeline hideAnim = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(profileView.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(profileView.scaleXProperty(), 0.9, Interpolator.EASE_IN),
                        new KeyValue(profileView.scaleYProperty(), 0.9, Interpolator.EASE_IN)
                )
        );

        hideAnim.setOnFinished(e -> {
            rootPane.getChildren().remove(profileView);
            canvas.setEffect(null);
        });

        hideAnim.play();
    }

    private void setupGlowEffects() {
        centerGlow = new Circle();
        centerGlow.setFill(null);
        centerGlow.setStroke(javafx.scene.paint.Color.web("#CCD0CF", 0.3));
        centerGlow.setStrokeWidth(3);
        centerGlow.setMouseTransparent(true);
        centerGlow.setCache(true);
        centerGlow.setCacheHint(javafx.scene.CacheHint.SPEED);
        canvas.getChildren().add(0, centerGlow);

        centerHalo.setCache(true);
        centerHalo.setCacheHint(javafx.scene.CacheHint.QUALITY);

        buttonGlows = new Circle[orbitButtons.length];
        for (int i = 0; i < orbitButtons.length; i++) {
            Circle glow = new Circle();
            glow.setFill(null);
            glow.setStroke(javafx.scene.paint.Color.web("#9BA8AB", 0.4));
            glow.setStrokeWidth(2);
            glow.setMouseTransparent(true);
            glow.setOpacity(0);
            glow.setVisible(false);
            glow.setCache(true);
            glow.setCacheHint(javafx.scene.CacheHint.SPEED);
            buttonGlows[i] = glow;
            canvas.getChildren().add(0, glow);

            orbitButtons[i].setCache(true);
            orbitButtons[i].setCacheHint(javafx.scene.CacheHint.QUALITY);
        }
    }

    private void startPulseAnimation() {
        centerGlow.setCache(true);
        centerGlow.setCacheHint(javafx.scene.CacheHint.SPEED);

        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(centerGlow.opacityProperty(), 0.4, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(centerGlow.opacityProperty(), 0.15, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(4),
                        new KeyValue(centerGlow.opacityProperty(), 0.4, Interpolator.EASE_BOTH)
                )
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.play();
    }

    private void setupLayout() {
        if (rootPane.getScene() != null) {
            layoutCenter();
        } else {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(this::layoutCenter);
                }
            });
        }

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!animating) layoutCenter();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!animating) layoutCenter();
        });
    }

    public void setCurrentUser(UserDto u) {
        this.currentUser = u;

        if (u == null) {
            greetingLabel.setText("NATE HIGERS");
            return;
        }

        String display;
        if (u.getName() != null && !u.getName().isBlank())
            display = u.getName();
        else if (u.getEmail() != null && !u.getEmail().isBlank())
            display = u.getEmail();
        else display = "User " + u.getUserId();

        greetingLabel.setText("NATE HIGERS, " + display);
    }

    private void setupResizeListeners() {
        if (rootPane.getScene() != null) {
            rootPane.getScene().widthProperty().addListener((o, ov, nv) -> {
                if (!animating) layoutCenter();
            });
            rootPane.getScene().heightProperty().addListener((o, ov, nv) -> {
                if (!animating) layoutCenter();
            });
        }
    }

    private void layoutCenter() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        if (w <= 0 || h <= 0) return;

        double min = Math.min(w, h);
        double haloSize = clamp(min * 0.35, 240, 480);

        centerHalo.setPrefSize(haloSize, haloSize);
        centerHalo.setMinSize(haloSize, haloSize);
        centerHalo.setMaxSize(haloSize, haloSize);
        centerHalo.setLayoutX((w - haloSize) / 2.0);
        centerHalo.setLayoutY((h - haloSize) / 2.0);

        if (centerGlow != null) {
            centerGlow.setRadius(haloSize / 2.0);
            centerGlow.setCenterX(w / 2.0);
            centerGlow.setCenterY(h / 2.0);
        }

        double btnSize = clamp(min * 0.11, 85, 150);

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            b.setPrefSize(btnSize, btnSize);
            b.setMinSize(btnSize, btnSize);
            b.setMaxSize(btnSize, btnSize);

            if (buttonGlows != null && buttonGlows[i] != null) {
                Circle glow = buttonGlows[i];
                glow.setRadius(btnSize / 2.0);
            }
        }

        if (expanded) {
            positionButtonsExpanded();
        } else {
            positionButtonsCollapsed();
        }
    }

    private void toggleMenu() {
        if (!expanded) expandMenu();
        else collapseMenu();
        expanded = !expanded;
    }

    private void expandMenu() {
        animating = true;

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = centerHalo.getPrefWidth() * 0.95;
        double[] angles = {-90, -30, 30, 90, 150, -150};

        for (int i = 0; i < orbitButtons.length; i++) {
            orbitButtons[i].toBack();
            buttonGlows[i].toBack();
        }
        centerGlow.toBack();
        centerHalo.toFront();

        ParallelTransition parallel = new ParallelTransition();

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            Circle glow = buttonGlows[i];

            b.setVisible(true);
            b.setMouseTransparent(true);
            glow.setVisible(true);

            double a = Math.toRadians(angles[i]);
            double targetX = cx + radius * Math.cos(a) - b.getPrefWidth() / 2.0;
            double targetY = cy + radius * Math.sin(a) - b.getPrefHeight() / 2.0;

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(b.layoutXProperty(), cx - b.getPrefWidth() / 2.0, Interpolator.EASE_OUT),
                            new KeyValue(b.layoutYProperty(), cy - b.getPrefHeight() / 2.0, Interpolator.EASE_OUT),
                            new KeyValue(b.opacityProperty(), 0, Interpolator.EASE_OUT),
                            new KeyValue(b.scaleXProperty(), 0.5, Interpolator.EASE_OUT),
                            new KeyValue(b.scaleYProperty(), 0.5, Interpolator.EASE_OUT),
                            new KeyValue(b.rotateProperty(), -180, Interpolator.EASE_OUT)
                    ),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(b.layoutXProperty(), targetX, Interpolator.EASE_OUT),
                            new KeyValue(b.layoutYProperty(), targetY, Interpolator.EASE_OUT),
                            new KeyValue(b.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(b.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(b.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(b.rotateProperty(), 0, Interpolator.EASE_OUT)
                    )
            );

            Timeline glowTl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(glow.opacityProperty(), 0, Interpolator.EASE_OUT),
                            new KeyValue(glow.scaleXProperty(), 0.5, Interpolator.EASE_OUT),
                            new KeyValue(glow.scaleYProperty(), 0.5, Interpolator.EASE_OUT)
                    ),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(glow.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(glow.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                            new KeyValue(glow.scaleYProperty(), 1.0, Interpolator.EASE_OUT)
                    )
            );

            parallel.getChildren().addAll(tl, glowTl);

            final Button btn = b;
            final Circle btnGlow = glow;

            tl.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                if (newStatus == Animation.Status.RUNNING) {
                    AnimationTimer timer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (tl.getStatus() != Animation.Status.RUNNING) {
                                stop();
                                return;
                            }
                            btnGlow.setCenterX(btn.getLayoutX() + btn.getPrefWidth() / 2.0);
                            btnGlow.setCenterY(btn.getLayoutY() + btn.getPrefHeight() / 2.0);
                        }
                    };
                    timer.start();
                }
            });

            b.setOnMouseEntered(ev -> {
                Timeline hoverGlow = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(glow.scaleXProperty(), 1.2, Interpolator.EASE_OUT),
                                new KeyValue(glow.scaleYProperty(), 1.2, Interpolator.EASE_OUT),
                                new KeyValue(glow.opacityProperty(), 0.8, Interpolator.EASE_OUT)
                        )
                );
                hoverGlow.play();
            });

            b.setOnMouseExited(ev -> {
                Timeline hoverGlow = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(glow.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                                new KeyValue(glow.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                                new KeyValue(glow.opacityProperty(), 1.0, Interpolator.EASE_OUT)
                        )
                );
                hoverGlow.play();
            });
        }

        parallel.setOnFinished(e -> {
            animating = false;
            for (Button b : orbitButtons) {
                b.setMouseTransparent(false);
            }
        });

        parallel.play();
    }

    private void collapseMenu() {
        animating = true;

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;

        for (Button b : orbitButtons) {
            b.setMouseTransparent(true);
        }

        ParallelTransition parallel = new ParallelTransition();

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            Circle glow = buttonGlows[i];

            double targetX = cx - b.getPrefWidth() / 2.0;
            double targetY = cy - b.getPrefHeight() / 2.0;

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(b.layoutXProperty(), b.getLayoutX(), Interpolator.EASE_IN),
                            new KeyValue(b.layoutYProperty(), b.getLayoutY(), Interpolator.EASE_IN),
                            new KeyValue(b.opacityProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(b.scaleXProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(b.scaleYProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(b.rotateProperty(), 0, Interpolator.EASE_IN)
                    ),
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(b.layoutXProperty(), targetX, Interpolator.EASE_IN),
                            new KeyValue(b.layoutYProperty(), targetY, Interpolator.EASE_IN),
                            new KeyValue(b.opacityProperty(), 0.0, Interpolator.EASE_IN),
                            new KeyValue(b.scaleXProperty(), 0.5, Interpolator.EASE_IN),
                            new KeyValue(b.scaleYProperty(), 0.5, Interpolator.EASE_IN),
                            new KeyValue(b.rotateProperty(), 180, Interpolator.EASE_IN)
                    )
            );

            Timeline glowTl = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(glow.opacityProperty(), 0.0, Interpolator.EASE_IN),
                            new KeyValue(glow.scaleXProperty(), 0.5, Interpolator.EASE_IN),
                            new KeyValue(glow.scaleYProperty(), 0.5, Interpolator.EASE_IN)
                    )
            );

            parallel.getChildren().addAll(tl, glowTl);

            final Button btn = b;
            final Circle btnGlow = glow;

            tl.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                if (newStatus == Animation.Status.RUNNING) {
                    AnimationTimer timer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (tl.getStatus() != Animation.Status.RUNNING) {
                                stop();
                                return;
                            }
                            btnGlow.setCenterX(btn.getLayoutX() + btn.getPrefWidth() / 2.0);
                            btnGlow.setCenterY(btn.getLayoutY() + btn.getPrefHeight() / 2.0);
                        }
                    };
                    timer.start();
                }
            });
        }

        parallel.setOnFinished(e -> {
            animating = false;
            for (int i = 0; i < orbitButtons.length; i++) {
                orbitButtons[i].setVisible(false);
                buttonGlows[i].setVisible(false);
            }
        });

        parallel.play();
    }

    private void positionButtonsExpanded() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = centerHalo.getPrefWidth() * 0.95;
        double[] angles = {-90, -30, 30, 90, 150, -150};

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            Circle glow = buttonGlows[i];

            double a = Math.toRadians(angles[i]);
            double x = cx + radius * Math.cos(a) - b.getPrefWidth() / 2.0;
            double y = cy + radius * Math.sin(a) - b.getPrefHeight() / 2.0;

            b.setLayoutX(x);
            b.setLayoutY(y);
            b.setOpacity(1);
            b.setVisible(true);

            glow.setCenterX(x + b.getPrefWidth() / 2.0);
            glow.setCenterY(y + b.getPrefHeight() / 2.0);
            glow.setOpacity(1);
            glow.setVisible(true);
        }
    }

    private void positionButtonsCollapsed() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            Circle glow = buttonGlows[i];

            double x = cx - b.getPrefWidth() / 2.0;
            double y = cy - b.getPrefHeight() / 2.0;

            b.setLayoutX(x);
            b.setLayoutY(y);
            b.setOpacity(0);
            b.setVisible(false);

            glow.setOpacity(0);
            glow.setVisible(false);
        }
    }

    private void fadeInScreen() {
        rootPane.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), rootPane);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}