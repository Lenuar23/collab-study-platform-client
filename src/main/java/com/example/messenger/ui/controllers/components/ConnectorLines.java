package com.example.messenger.ui.components;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class ConnectorLines {

    private final Pane canvas;
    private final Region centerNode;
    private final Button[] buttons;
    private final Line[] lines;
    private AnimationTimer timer;

    public ConnectorLines(Pane canvas, Region centerNode, Button[] buttons) {
        this.canvas = canvas;
        this.centerNode = centerNode;
        this.buttons = buttons;
        this.lines = new Line[buttons.length];
        createLines();
        startAnimation();
    }

    private void createLines() {
        for (int i = 0; i < buttons.length; i++) {
            Line line = new Line();

            line.setStroke(Color.web("#CCD0CF", 0.5)); 
            line.setStrokeWidth(2.5); 
            
            line.getStrokeDashArray().addAll(10d, 5d); 
            
            line.setMouseTransparent(true);
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#CCD0CF"));
            glow.setRadius(15);
            glow.setSpread(0.2);
            glow.setBlurType(BlurType.GAUSSIAN);
            line.setEffect(glow);

            line.setVisible(false);

            lines[i] = line;
            canvas.getChildren().add(0, line);
        }
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateLines();
            }
        };
        timer.start();
    }

    private void updateLines() {
        double centerX = centerNode.getLayoutX() + centerNode.getWidth() / 2;
        double centerY = centerNode.getLayoutY() + centerNode.getHeight() / 2;
        double centerRadius = centerNode.getWidth() / 2;

        for (int i = 0; i < buttons.length; i++) {
            Button btn = buttons[i];
            Line line = lines[i];

            if (!btn.isVisible() || btn.getOpacity() < 0.1) {
                line.setVisible(false);
                continue;
            }

            line.setVisible(true);
            line.setOpacity(btn.getOpacity()); 

            double btnX = btn.getLayoutX() + btn.getWidth() / 2;
            double btnY = btn.getLayoutY() + btn.getHeight() / 2;
            double btnRadius = btn.getWidth() / 2;

            double dx = btnX - centerX;
            double dy = btnY - centerY;
            double angle = Math.atan2(dy, dx);

            double startOffset = centerRadius + 2; 
            line.setStartX(centerX + Math.cos(angle) * startOffset);
            line.setStartY(centerY + Math.sin(angle) * startOffset);

            double endOffset = btnRadius + 2;
            line.setEndX(btnX - Math.cos(angle) * endOffset);
            line.setEndY(btnY - Math.sin(angle) * endOffset);
        }
    }
}