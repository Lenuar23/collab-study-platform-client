package org.example.cspclient.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.cspclient.MainApp;

import java.io.IOException;
import java.net.URL;

public class ViewManager {

    public Scene loadLoginScene() throws IOException { return loadScene("/org/example/cspclient/view/login.fxml"); }
    public Scene loadRegisterScene() throws IOException { return loadScene("/org/example/cspclient/view/register.fxml"); }
    public Scene loadHomeScene() throws IOException { return loadScene("/org/example/cspclient/view/home.fxml"); }
    public Scene loadGroupDetailsScene() throws IOException { return loadScene("/org/example/cspclient/view/group_details.fxml"); }
    public Scene loadUserSettingsScene() throws IOException { return loadScene("/org/example/cspclient/view/user_settings.fxml"); }
    public Scene loadGroupSettingsScene() throws IOException { return loadScene("/org/example/cspclient/view/group_settings.fxml"); }

    private Scene loadScene(String resource) throws IOException {
        try {
            URL url = MainApp.class.getResource(resource);
            if (url == null) throw new IOException("FXML not found: " + resource);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL css = MainApp.class.getResource("/org/example/cspclient/application.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            return scene;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            // bubble up the best message for AlertUtils
            IOException io = new IOException("Failed to load " + resource + ": " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            io.initCause(e);
            throw io;
        }
    }
}
