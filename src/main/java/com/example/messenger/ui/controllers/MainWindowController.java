package com.example.messenger.ui.controllers;

import com.example.messenger.dto.UserDto;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import com.example.messenger.ui.components.*;
import com.example.messenger.ui.navigation.OverlayNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;

import java.io.IOException;

public class MainWindowController {

    @FXML private StackPane rootPane;
    @FXML private Pane canvas;
    @FXML private StackPane centerHalo;
    @FXML private Label greetingLabel;

    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Button chatsButton;
    @FXML private Button tasksButton;
    @FXML private Button groupsButton;
    @FXML private Button materialsButton;

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private UserDto currentUser;

    private OrbitMenuAnimator menuAnimator;
    private OverlayNavigator navigator;
    private ParticleSystem particleSystem;
    private ParallaxEffect parallaxEffect;
    private ConnectorLines connectorLines;

    @FXML
    public void initialize() {
        Button[] orbitButtons = { profileButton, logoutButton, tasksButton, materialsButton, groupsButton, chatsButton };

        navigator = new OverlayNavigator(rootPane, canvas);
        menuAnimator = new OrbitMenuAnimator(canvas, centerHalo, orbitButtons);

        particleSystem = new ParticleSystem(canvas, 200);
        particleSystem.start();

        connectorLines = new ConnectorLines(canvas, centerHalo, orbitButtons);

        parallaxEffect = new ParallaxEffect(rootPane);
        parallaxEffect.addNode(centerHalo, 0.03, 600);
        for (Button btn : orbitButtons) {
            parallaxEffect.addNode(btn, 0.1, 200);
        }

        bindMenuActions();

        Platform.runLater(() -> {
            menuAnimator.initialLayout();
            fadeInScreen();
        });
    }

    private void bindMenuActions() {
        centerHalo.setOnMouseClicked(e -> menuAnimator.toggle());

        profileButton.setOnAction(e -> navigator.open("/ui/user_profile.fxml", (UserProfileController controller) -> {
            controller.setUserAndServices(currentUser, userService, this::setCurrentUser, navigator::close);
        }));

        groupsButton.setOnAction(e -> navigator.open("/ui/groups.fxml", (GroupsController controller) -> {
            controller.setup(rootPane, navigator::close);
        }));

        chatsButton.setOnAction(e -> navigator.open("/ui/chat.fxml", (ChatController controller) -> {
            controller.setup(rootPane, navigator::close);
        }));

        // --- NEW: GLOBAL TASKS ---
        tasksButton.setOnAction(e -> navigator.open("/ui/tasks.fxml", (TasksController controller) -> {
            controller.setupGlobalMode(navigator::close);
        }));

        materialsButton.setOnAction(e -> navigator.open("/ui/materials.fxml", (MaterialsController controller) -> {
            controller.setup(navigator::close);
        }));

        logoutButton.setOnAction(e -> onLogout());
    }

    private void onLogout() {
        try {
            authService.logout();
        } catch (Exception e) {
            SessionStore.clear();
        }

        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/ui/login.fxml"))));
            stage.setTitle("Messenger - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUser(UserDto u) {
        this.currentUser = u;
        if (u == null) {
            greetingLabel.setText("Messenger");
            return;
        }
        String name = (u.getName() != null && !u.getName().isBlank()) ? u.getName() : u.getEmail();
        greetingLabel.setText("Hi, " + name);
    }

    private void fadeInScreen() {
        rootPane.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), rootPane);
        ft.setToValue(1);
        ft.play();
    }
}