package com.example.messenger.ui.navigation;

import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.io.IOException;
import java.util.function.Consumer;

public class OverlayNavigator {
    private final StackPane rootPane;
    private final Pane backgroundToBlur;
    private Parent activeOverlay;

    public OverlayNavigator(StackPane rootPane, Pane backgroundToBlur) {
        this.rootPane = rootPane;
        this.backgroundToBlur = backgroundToBlur;
    }

    public <T> void open(String fxmlPath, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Налаштовуємо контролер через лямбду
            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }

            activeOverlay = view;
            StackPane.setAlignment(view, javafx.geometry.Pos.CENTER);

            if (!rootPane.getChildren().contains(view)) {
                rootPane.getChildren().add(view);
            }

            applyEffectsAndAnimation(view);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("EROOR: " + fxmlPath);
        }
    }

    public void close() {
        if (activeOverlay == null) return;

        Timeline hideAnim = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(activeOverlay.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(activeOverlay.scaleXProperty(), 0.9, Interpolator.EASE_IN),
                        new KeyValue(activeOverlay.scaleYProperty(), 0.9, Interpolator.EASE_IN)
                )
        );

        hideAnim.setOnFinished(e -> {
            rootPane.getChildren().remove(activeOverlay);
            activeOverlay = null;
            backgroundToBlur.setEffect(null);
            backgroundToBlur.setMouseTransparent(false);
            rootPane.requestFocus();
        });

        hideAnim.play();
    }

    private void applyEffectsAndAnimation(Parent view) {
        backgroundToBlur.setEffect(new GaussianBlur(15));
        backgroundToBlur.setMouseTransparent(true);

        view.setOpacity(0);
        view.setScaleX(0.9);
        view.setScaleY(0.9);

        new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(view.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(view.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(view.scaleYProperty(), 1, Interpolator.EASE_OUT)
                )
        ).play();
    }
}