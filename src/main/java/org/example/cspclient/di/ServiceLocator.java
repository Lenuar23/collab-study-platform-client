package org.example.cspclient.di;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.cspclient.api.ApiClient;
import org.example.cspclient.model.User;
import org.example.cspclient.view.ViewManager;

public class ServiceLocator {
    private static ApiClient apiClient;
    private static Stage stage;
    private static ViewManager viewManager;
    private static User currentUser;

    public static ApiClient getApiClient() { return apiClient; }
    public static void setApiClient(ApiClient api) { apiClient = api; }

    public static Stage getStage() { return stage; }
    public static void setStage(Stage st) { stage = st; }

    public static ViewManager getViewManager() { return viewManager; }
    public static void setViewManager(ViewManager vm) { viewManager = vm; }

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }

    /** Switch scene but keep window size/position and maximized state (Windows-safe) */
    public static void setScenePreserveBounds(Scene scene) {
        if (stage == null) return;
        boolean wasMax = stage.isMaximized();
        double w = stage.getWidth();
        double h = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        // Switch scene first
        stage.setScene(scene);

        if (wasMax) {
            // If it was maximized, do NOT touch size/position — just restore maximized
            stage.setMaximized(true);
        } else {
            // Restore previous bounds for windowed mode
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(w);
            stage.setHeight(h);
        }
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
