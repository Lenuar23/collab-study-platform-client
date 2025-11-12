package org.example.cspclient.di;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.cspclient.view.ViewManager;
import org.example.cspclient.api.ApiClient;
import org.example.cspclient.api.MockApiClient;
import org.example.cspclient.model.User;

public class ServiceLocator {

    private static Stage stage;
    private static ViewManager viewManager;
    private static ApiClient api = new MockApiClient();
    private static User currentUser;

    public static void init(Stage s, ViewManager vm) {
        stage = s;
        viewManager = vm;
    }

    public static Stage getStage() { return stage; }
    public static ViewManager getViewManager() { return viewManager; }
    public static ApiClient getApiClient() { return api; }
    public static void setApiClient(ApiClient a) { api = a; }

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }

    /** Prefer reusing the same Scene to keep maximized state. */
    public static void setScenePreserveBounds(Scene newScene) {
        if (stage == null) return;
        final boolean wasMax = stage.isMaximized();

        if (stage.getScene() != null) {
            // Reuse existing scene and detach incoming root to avoid "already set as root of another scene"
            copyScene(stage.getScene(), newScene);
        } else {
            stage.setScene(newScene);
        }

        if (wasMax) {
            stage.setMaximized(true);
            Platform.runLater(() -> stage.setMaximized(true));
        }
    }

    /** Copy root and stylesheets from newScene into existing scene. */
    private static void copyScene(Scene existing, Scene incoming) {
        Parent incomingRoot = incoming.getRoot();
        // Detach the root from its original scene before reusing
        incoming.setRoot(new Group());
        existing.setRoot(incomingRoot);
        existing.getStylesheets().setAll(incoming.getStylesheets());
        existing.getRoot().applyCss();
    }

    public static void logout() {
        currentUser = null;
        try {
            setScenePreserveBounds(viewManager.loadLoginScene());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
