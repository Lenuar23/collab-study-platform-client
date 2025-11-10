package org.example.cspclient.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.cspclient.MainApp;

import java.io.IOException;

public class ViewManager {

    public Scene loadLoginScene() throws IOException { return loadScene("/org/example/cspclient/view/login.fxml"); }
    public Scene loadRegisterScene() throws IOException { return loadScene("/org/example/cspclient/view/register.fxml"); }
    public Scene loadDashboardScene() throws IOException { return loadScene("/org/example/cspclient/view/dashboard.fxml"); }
    public Scene loadGroupDetailsScene() throws IOException { return loadScene("/org/example/cspclient/view/group_details.fxml"); }
    public Scene loadChatScene() throws IOException { return loadScene("/org/example/cspclient/view/chat.fxml"); }

    private Scene loadScene(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(resource));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        // attach theme
        String css = MainApp.class.getResource("/org/example/cspclient/application.css").toExternalForm();
        scene.getStylesheets().add(css);
        return scene;
    }
}
