package org.example.cspclient.api;

import org.example.cspclient.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApiClient {
    Optional<User> login(String email, String password) throws Exception;
    User register(String name, String email, String password) throws Exception;

    List<Group> listGroups(long userId) throws Exception;
    Group createGroup(long userId, String name, String description) throws Exception;

    List<TaskItem> listTasks(long groupId) throws Exception;
    TaskItem createTask(long groupId, long createdBy, String title, String description, TaskStatus status, LocalDate deadline) throws Exception;
    TaskItem updateTaskStatus(long taskId, TaskStatus status) throws Exception;

    List<ResourceItem> listResources(long groupId) throws Exception;
    ResourceItem addResource(long groupId, long uploadedBy, String title, String type, String urlOrPath) throws Exception;

    List<ActivityLog> listActivity(long groupId) throws Exception;

    // --- Chat ---
    List<Conversation> listConversations(long userId) throws Exception;
    Conversation getOrCreateConversation(long userId, long otherUserId) throws Exception;
    List<ChatMessage> listMessages(long conversationId, int limit) throws Exception;
    ChatMessage sendMessage(long conversationId, long senderId, String content) throws Exception;
    Optional<User> findUserByEmail(String email) throws Exception;
}
