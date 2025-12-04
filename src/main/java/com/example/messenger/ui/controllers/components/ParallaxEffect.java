package com.example.messenger.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class ParallaxEffect {

    private final Pane inputSource;
    private final List<ParallaxItem> items = new ArrayList<>();

    public ParallaxEffect(Pane inputSource) {
        this.inputSource = inputSource;
        setupListener();
    }

    // Тепер передаємо triggerRadius сюди
    public void addNode(Node node, double intensity, double triggerRadius) {
        items.add(new ParallaxItem(node, intensity, triggerRadius));
    }

    private void setupListener() {
        inputSource.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            for (ParallaxItem item : items) {
                // Центр об'єкта
                double nodeCenterX = item.node.getLayoutX() + item.node.getBoundsInLocal().getWidth() / 2;
                double nodeCenterY = item.node.getLayoutY() + item.node.getBoundsInLocal().getHeight() / 2;

                double dx = mouseX - nodeCenterX;
                double dy = mouseY - nodeCenterY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // Використовуємо персональний радіус для кожного об'єкта
                if (distance < item.triggerRadius) {
                    double factor = (item.triggerRadius - distance) / item.triggerRadius;

                    // Рух ВІД курсора
                    double moveX = -dx * factor * item.intensity;
                    double moveY = -dy * factor * item.intensity;

                    item.node.setTranslateX(moveX);
                    item.node.setTranslateY(moveY);
                } else {
                    item.node.setTranslateX(0);
                    item.node.setTranslateY(0);
                }
            }
        });
    }

    private static class ParallaxItem {
        Node node;
        double intensity;
        double triggerRadius;

        public ParallaxItem(Node node, double intensity, double triggerRadius) {
            this.node = node;
            this.intensity = intensity;
            this.triggerRadius = triggerRadius;
        }
    }
}