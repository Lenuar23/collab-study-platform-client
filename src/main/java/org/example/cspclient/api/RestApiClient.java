
package org.example.cspclient.api;

import org.example.cspclient.model.*;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Temporary REST client that delegates to a local fallback (MockApiClient)
 * until the real server endpoints are ready. This ensures the app compiles
 * after ApiClient interface changes.
 */
public class RestApiClient implements ApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final ApiClient fallback; // delegates to mock for now

    public RestApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newHttpClient();
        this.fallback = new MockApiClient();
    }

    @Override
    public Optional<User> login(String email, String password) throws Exception {
        // TODO: replace with real HTTP call when server is ready
        return fallback.login(email, password);
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        // TODO: replace with real HTTP call when server is ready
        return fallback.register(name, email, password);
    }

    @Override
    public List<Group> listGroups(long userId) throws Exception {
        return fallback.listGroups(userId);
    }

    @Override
    public Group createGroup(long userId, String name, String description) throws Exception {
        return fallback.createGroup(userId, name, description);
    }

    @Override
    public List<TaskItem> listTasks(long groupId) throws Exception {
        return fallback.listTasks(groupId);
    }

    @Override
    public TaskItem createTask(long groupId, long createdBy, String title, String description, TaskStatus status, LocalDate deadline) throws Exception {
        return fallback.createTask(groupId, createdBy, title, description, status, deadline);
    }

    @Override
    public TaskItem updateTaskStatus(long taskId, TaskStatus status) throws Exception {
        return fallback.updateTaskStatus(taskId, status);
    }

    @Override
    public List<ResourceItem> listResources(long groupId) throws Exception {
        return fallback.listResources(groupId);
    }

    @Override
    public ResourceItem addResource(long groupId, long uploadedBy, String title, String type, String urlOrPath) throws Exception {
        return fallback.addResource(groupId, uploadedBy, title, type, urlOrPath);
    }

    @Override
    public List<ActivityLog> listActivity(long groupId) throws Exception {
        return fallback.listActivity(groupId);
    }

    @Override
    public List<Conversation> listConversations(long userId) throws Exception {
        return fallback.listConversations(userId);
    }

    @Override
    public Conversation getOrCreateConversation(long userId, long otherUserId) throws Exception {
        return fallback.getOrCreateConversation(userId, otherUserId);
    }

    @Override
    public List<ChatMessage> listMessages(long conversationId, int limit) throws Exception {
        return fallback.listMessages(conversationId, limit);
    }

    @Override
    public ChatMessage sendMessage(long conversationId, long senderId, String content) throws Exception {
        return fallback.sendMessage(conversationId, senderId, content);
    }

    @Override
    public Optional<User> findUserByEmail(String email) throws Exception {
        return fallback.findUserByEmail(email);
    }

    @Override
    public User getUserById(long id) throws Exception {
        return fallback.getUserById(id);
    }

    @Override
    public Group getGroupById(long id) throws Exception {
        return fallback.getGroupById(id);
    }

    @Override
    public User updateUserProfile(long id, String name, String about, String avatarUrl) throws Exception {
        return fallback.updateUserProfile(id, name, about, avatarUrl);
    }

    @Override
    public Group updateGroupProfile(long id, String name, String description, String avatarUrl) throws Exception {
        return fallback.updateGroupProfile(id, name, description, avatarUrl);
    }
}
