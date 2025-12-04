package com.example.messenger.ui.components;

import javafx.animation.*;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class OrbitMenuAnimator {

    private final Pane canvas;
    private final StackPane centerHalo;
    private final Button[] orbitButtons;
    
    private final Circle centerGlow;
    private final Circle[] buttonGlows;

    private boolean expanded = false;
    private boolean animating = false;
    private Timeline pulseTimeline;

    public OrbitMenuAnimator(Pane canvas, StackPane centerHalo, Button[] orbitButtons) {
        this.canvas = canvas;
        this.centerHalo = centerHalo;
        this.orbitButtons = orbitButtons;
        this.buttonGlows = new Circle[orbitButtons.length];
        
        this.centerGlow = new Circle();
        setupGlowEffects();
        setupListeners();
        startPulseAnimation();
    }

    public void initialLayout() {
        layoutCenter();
    }

    public void toggle() {
        if (animating) return;
        if (!expanded) expandMenu();
        else collapseMenu();
        expanded = !expanded;
    }

    private void setupListeners() {
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!animating) layoutCenter();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!animating) layoutCenter();
        });
    }

    private void setupGlowEffects() {
        centerGlow.setFill(null);
        centerGlow.setStroke(Color.web("#CCD0CF", 0.3));
        centerGlow.setStrokeWidth(3);
        centerGlow.setMouseTransparent(true);
        centerGlow.setCache(true);
        centerGlow.setCacheHint(CacheHint.SPEED);
        
        if (!canvas.getChildren().contains(centerGlow)) {
            canvas.getChildren().add(0, centerGlow);
        }

        centerHalo.setCache(true);
        centerHalo.setCacheHint(CacheHint.QUALITY);

        for (int i = 0; i < orbitButtons.length; i++) {
            Circle glow = new Circle();
            glow.setFill(null);
            glow.setStroke(Color.web("#9BA8AB", 0.4));
            glow.setStrokeWidth(2);
            glow.setMouseTransparent(true);
            glow.setOpacity(0);
            glow.setVisible(false);
            glow.setCache(true);
            glow.setCacheHint(CacheHint.SPEED);
            
            buttonGlows[i] = glow;
            canvas.getChildren().add(0, glow);

            orbitButtons[i].setCache(true);
            orbitButtons[i].setCacheHint(CacheHint.QUALITY);
            
            orbitButtons[i].setOpacity(0);
            orbitButtons[i].setVisible(false);
            orbitButtons[i].setMouseTransparent(true);
        }
    }

    private void startPulseAnimation() {
        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(centerGlow.opacityProperty(), 0.4, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(2), new KeyValue(centerGlow.opacityProperty(), 0.15, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(4), new KeyValue(centerGlow.opacityProperty(), 0.4, Interpolator.EASE_BOTH))
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.play();
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

        centerGlow.setRadius(haloSize / 2.0);
        centerGlow.setCenterX(w / 2.0);
        centerGlow.setCenterY(h / 2.0);

        double btnSize = clamp(min * 0.11, 85, 150);

        for (int i = 0; i < orbitButtons.length; i++) {
            Button b = orbitButtons[i];
            b.setPrefSize(btnSize, btnSize);
            b.setMinSize(btnSize, btnSize);
            b.setMaxSize(btnSize, btnSize);

            if (buttonGlows[i] != null) {
                buttonGlows[i].setRadius(btnSize / 2.0);
            }
        }

        if (expanded) {
            positionButtonsExpanded();
        } else {
            positionButtonsCollapsed();
        }
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
            
            // ЛОГІКА ДИСТАНЦІЇ:
            // 0 (Profile) і 3 (Materials) залишаємо як є (множник 1.0)
            // Інші відлітають далі (множник 1.6)
            double distFactor = (i == 0 || i == 3) ? 1.0 : 1.6;
            double currentRadius = radius * distFactor;

            double targetX = cx + currentRadius * Math.cos(a) - b.getPrefWidth() / 2.0;
            double targetY = cy + currentRadius * Math.sin(a) - b.getPrefHeight() / 2.0;

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
                new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(glow.scaleXProperty(), 1.2, Interpolator.EASE_OUT),
                                new KeyValue(glow.scaleYProperty(), 1.2, Interpolator.EASE_OUT),
                                new KeyValue(glow.opacityProperty(), 0.8, Interpolator.EASE_OUT)
                        )
                ).play();
            });

            b.setOnMouseExited(ev -> {
                new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(glow.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                                new KeyValue(glow.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                                new KeyValue(glow.opacityProperty(), 1.0, Interpolator.EASE_OUT)
                        )
                ).play();
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

            // Приховування: повертаємося з правильних позицій (враховуючи множник)
            double distFactor = (i == 0 || i == 3) ? 1.0 : 1.6;
            
            // Ціль - центр
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
            
            // ТА Ж САМА ЛОГІКА ПРИ РЕСАЙЗІ (Positioning)
            double distFactor = (i == 0 || i == 3) ? 1.0 : 1.6;
            double currentRadius = radius * distFactor;
            
            double x = cx + currentRadius * Math.cos(a) - b.getPrefWidth() / 2.0;
            double y = cy + currentRadius * Math.sin(a) - b.getPrefHeight() / 2.0;

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

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}