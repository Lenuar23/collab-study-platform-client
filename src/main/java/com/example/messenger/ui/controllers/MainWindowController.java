package com.example.messenger.ui.controllers;

import com.example.messenger.dto.UserDto;
import com.example.messenger.dto.convers.ConversationSummary;
import com.example.messenger.dto.GroupDto;
import com.example.messenger.net.AuthService;
import com.example.messenger.net.ChatStompClient;
import com.example.messenger.net.ConversationService;
import com.example.messenger.net.GroupService;
import com.example.messenger.net.UserService;
import com.example.messenger.store.SessionStore;
import com.example.messenger.ui.components.*;
import com.example.messenger.ui.navigation.OverlayNavigator;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    // --- Badges ---
    @FXML private Circle tasksBadge;
    @FXML private Circle groupsBadge;
    @FXML private Circle chatsBadge;

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private final ConversationService conversationService = new ConversationService();
    private final GroupService groupService = new GroupService();

    private final ChatStompClient notificationClient = new ChatStompClient();
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
        bindBadgesToButtons();

        Platform.runLater(() -> {
            menuAnimator.initialLayout();
            fadeInScreen();
            startBackgroundNotifications();
        });
    }

    private void bindBadgesToButtons() {
        if (chatsBadge != null && chatsButton != null) bindBadge(chatsBadge, chatsButton);
        if (tasksBadge != null && tasksButton != null) bindBadge(tasksBadge, tasksButton);
        if (groupsBadge != null && groupsButton != null) bindBadge(groupsBadge, groupsButton);
    }

    private void bindBadge(Circle badge, Button targetBtn) {
        // Координати
        badge.layoutXProperty().bind(targetBtn.layoutXProperty().add(targetBtn.widthProperty()).subtract(15));
        badge.layoutYProperty().bind(targetBtn.layoutYProperty().add(10));
        // Анімація
        badge.translateXProperty().bind(targetBtn.translateXProperty());
        badge.translateYProperty().bind(targetBtn.translateYProperty());
    }

    private void startBackgroundNotifications() {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("DEBUG: Connecting to WebSocket for Notifications...");
                notificationClient.connect();

                Long myId = SessionStore.getUserId();
                if (myId == null) return;

                // --- ЧАТИ ---
                try {
                    ConversationSummary[] myConvs = conversationService.getMyConversations();
                    if (myConvs != null) {
                        for (ConversationSummary conv : myConvs) {
                            System.out.println("DEBUG: Subscribing to chat " + conv.getConversationId());
                            notificationClient.subscribeToConversation(conv.getConversationId(), msg -> {
                                System.out.println("DEBUG: Message received! Type: " + conv.getType());
                                Platform.runLater(() -> {
                                    if ("GROUP".equalsIgnoreCase(conv.getType())) {
                                        groupsBadge.setVisible(true);
                                        groupsBadge.toFront();
                                    } else {
                                        chatsBadge.setVisible(true);
                                        chatsBadge.toFront();
                                    }
                                });
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error subscribing to chats: " + e.getMessage());
                }

                // --- ЗАВДАННЯ ---
                try {
                    GroupDto[] myGroups = groupService.listGroups();
                    if (myGroups != null) {
                        for (GroupDto group : myGroups) {
                            notificationClient.subscribeToGroupTasks(group.getGroupId(), task -> {
                                System.out.println("DEBUG: Task received!");
                                Platform.runLater(() -> {
                                    tasksBadge.setVisible(true);
                                    tasksBadge.toFront();
                                    groupsBadge.setVisible(true);
                                    groupsBadge.toFront();
                                });
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error subscribing to tasks: " + e.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void bindMenuActions() {
        centerHalo.setOnMouseClicked(e -> menuAnimator.toggle());

        profileButton.setOnAction(e -> navigator.open("/ui/user_profile.fxml", (UserProfileController controller) -> {
            controller.setUserAndServices(currentUser, userService, this::setCurrentUser, navigator::close);
        }));

        groupsButton.setOnAction(e -> {
            groupsBadge.setVisible(false);
            navigator.open("/ui/groups.fxml", (GroupsController controller) -> {
                controller.setup(rootPane, navigator::close);
            });
        });

        chatsButton.setOnAction(e -> {
            chatsBadge.setVisible(false);
            navigator.open("/ui/chat.fxml", (ChatController controller) -> {
                controller.setup(rootPane, navigator::close);
            });
        });

        tasksButton.setOnAction(e -> {
            tasksBadge.setVisible(false);
            navigator.open("/ui/tasks.fxml", (TasksController controller) -> {
                controller.setupGlobalMode(navigator::close);
            });
        });

        materialsButton.setOnAction(e -> navigator.open("/ui/materials.fxml", (MaterialsController controller) -> {
            controller.setup(navigator::close);
        }));

        logoutButton.setOnAction(e -> onLogout());
    }

    private void onLogout() {
        if (notificationClient != null) notificationClient.disconnect();
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