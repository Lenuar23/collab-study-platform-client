package org.example.cspclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.cspclient.api.ApiClient;
import org.example.cspclient.api.MockApiClient;
import org.example.cspclient.api.RestApiClient;
import org.example.cspclient.di.ServiceLocator;
import org.example.cspclient.view.ViewManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load configuration
        Properties cfg = loadConfig();
        String apiMode = cfg.getProperty("api.mode", "mock");
        String baseUrl = cfg.getProperty("api.baseUrl", "http://localhost:8080");

        ApiClient api = "rest".equalsIgnoreCase(apiMode)
                ? new RestApiClient(baseUrl)
                : new MockApiClient();

        ServiceLocator.setApiClient(api);
        ServiceLocator.setStage(stage);

        ViewManager vm = new ViewManager();
        ServiceLocator.setViewManager(vm);
        Scene scene = vm.loadLoginScene();

        stage.setTitle("Collaborative Study Platform - Client");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    private Properties loadConfig() throws IOException {
        Properties p = new Properties();
        try (InputStream is = MainApp.class.getResourceAsStream("/org/example/cspclient/config.properties")) {
            if (is != null) {
                p.load(is);
            }
        }
        return p;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
