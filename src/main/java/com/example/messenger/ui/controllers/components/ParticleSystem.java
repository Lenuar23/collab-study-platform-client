package com.example.messenger.ui.components;

import javafx.animation.AnimationTimer;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

    private final Pane container;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private AnimationTimer timer;
    private final int count;

    public ParticleSystem(Pane container, int count) {
        this.container = container;
        this.count = count;
        createParticles();
    }

    private void createParticles() {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            particles.add(p);
            container.getChildren().add(0, p); // Додаємо на задній план (індекс 0)
        }
    }

    public void start() {
        if (timer != null) return;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateParticles();
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void updateParticles() {
        double width = container.getWidth();
        double height = container.getHeight();

        // Якщо контейнер ще не має розмірів, пропускаємо кадр
        if (width == 0 || height == 0) return;

        for (Particle p : particles) {
            p.update(width, height);
        }
    }

    // Внутрішній клас для однієї частинки
    private class Particle extends Circle {
        private double velocityX;
        private double velocityY;

        public Particle() {
            // Випадковий радіус від 1 до 3 пікселів
            super(random.nextDouble() * 2 + 1);
            
            // Колір: блідо-блакитний, дуже прозорий
            setFill(Color.web("#CCD0CF"));
            setOpacity(random.nextDouble() * 0.3 + 0.1);
            
            // Важливо для продуктивності:
            setMouseTransparent(true); // Щоб не блокувати кліки
            setCache(true);
            setCacheHint(CacheHint.SPEED);

            // Початкова позиція (випадкова)
            // Використовуємо великі значення, щоб розкидати їх за межі екрану спочатку
            setTranslateX(random.nextDouble() * 2000); 
            setTranslateY(random.nextDouble() * 2000);

            // Швидкість (дуже повільна)
            velocityX = (random.nextDouble() - 0.5) * 0.5; 
            velocityY = (random.nextDouble() - 0.5) * 0.5;
        }

        public void update(double width, double height) {
            double newX = getTranslateX() + velocityX;
            double newY = getTranslateY() + velocityY;

            // Логіка "телепортації" при виході за межі екрану
            if (newX < -50) newX = width + 50;
            if (newX > width + 50) newX = -50;
            if (newY < -50) newY = height + 50;
            if (newY > height + 50) newY = -50;

            setTranslateX(newX);
            setTranslateY(newY);
        }
    }
}