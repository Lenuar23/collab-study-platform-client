package org.example.cspclient;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.cspclient.view.ViewManager;
import org.example.cspclient.di.ServiceLocator;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewManager vm = new ViewManager();
        ServiceLocator.init(primaryStage, vm);

        primaryStage.setTitle("Collaborative Study Platform - Client");
        primaryStage.setScene(vm.loadLoginScene());

        // Start maximized (windowed fullscreen)
        primaryStage.setMaximized(true);

        try {
            Image icon = new Image(MainApp.class.getResourceAsStream("/org/example/cspclient/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception ignore) {}

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
