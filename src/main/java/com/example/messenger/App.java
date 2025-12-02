package com.example.messenger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// MUST HAVE:
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Messenger Client");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
