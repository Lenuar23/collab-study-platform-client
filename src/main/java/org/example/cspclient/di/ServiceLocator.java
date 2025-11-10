package org.example.cspclient.di;

import javafx.stage.Stage;
import org.example.cspclient.api.ApiClient;
import org.example.cspclient.model.User;
import org.example.cspclient.view.ViewManager;

public class ServiceLocator {
    private static ApiClient apiClient;
    private static ViewManager viewManager;
    private static Stage stage;
    private static User currentUser;

    public static ApiClient getApiClient() {
        return apiClient;
    }

    public static void setApiClient(ApiClient apiClient) {
        ServiceLocator.apiClient = apiClient;
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }

    public static void setViewManager(ViewManager viewManager) {
        ServiceLocator.viewManager = viewManager;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        ServiceLocator.stage = stage;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        ServiceLocator.currentUser = currentUser;
    }

    public static void logout() {
        currentUser = null;
        try {
            getStage().setScene(viewManager.loadLoginScene());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
