package org.example.cspclient.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cspclient.model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

public class RestApiClient implements ApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public RestApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.http = HttpClient.newHttpClient();
    }

    private HttpRequest.Builder req(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json");
    }

    private <T> T parse(HttpResponse<String> resp, Class<T> clazz) throws Exception {
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readValue(resp.body(), clazz);
        }
        throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
    }

    private <T> T parseList(HttpResponse<String> resp, TypeReference<T> ref) throws Exception {
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readValue(resp.body(), ref);
        }
        throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
    }

    @Override
    public Optional<User> login(String email, String password) throws Exception {
        Map<String, String> payload = Map.of("email", email, "password", password);
        HttpRequest r = req("/api/auth/login").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        User user = parse(resp, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        Map<String, String> payload = Map.of("name", name, "email", email, "password", password);
        HttpRequest r = req("/api/auth/register").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, User.class);
    }

    @Override
    public List<Group> listGroups(long userId) throws Exception {
        HttpRequest r = req("/api/groups").GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        Group[] arr = parse(resp, Group[].class);
        return Arrays.asList(arr);
    }

    @Override
    public Group createGroup(long userId, String name, String description) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("description", description);
        payload.put("createdBy", userId);
        HttpRequest r = req("/api/groups").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, Group.class);
    }

    @Override
    public List<TaskItem> listTasks(long groupId) throws Exception {
        HttpRequest r = req("/api/groups/"+groupId+"/tasks").GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        TaskItem[] arr = parse(resp, TaskItem[].class);
        return Arrays.asList(arr);
    }

    @Override
    public TaskItem createTask(long groupId, long createdBy, String title, String description, TaskStatus status, LocalDate deadline) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("status", status.toString());
        payload.put("deadline", deadline != null ? deadline.toString() : null);
        payload.put("createdBy", createdBy);
        HttpRequest r = req("/api/groups/"+groupId+"/tasks").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, TaskItem.class);
    }

    @Override
    public TaskItem updateTaskStatus(long taskId, TaskStatus status) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status.toString());
        HttpRequest r = req("/api/tasks/"+taskId).PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, TaskItem.class);
    }

    @Override
    public List<ResourceItem> listResources(long groupId) throws Exception {
        HttpRequest r = req("/api/groups/"+groupId+"/resources").GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        ResourceItem[] arr = parse(resp, ResourceItem[].class);
        return Arrays.asList(arr);
    }

    @Override
    public ResourceItem addResource(long groupId, long uploadedBy, String title, String type, String urlOrPath) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("type", type);
        payload.put("pathOrUrl", urlOrPath);
        payload.put("uploadedBy", uploadedBy);
        HttpRequest r = req("/api/groups/"+groupId+"/resources").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, ResourceItem.class);
    }

    @Override
    public List<ActivityLog> listActivity(long groupId) throws Exception {
        HttpRequest r = req("/api/groups/"+groupId+"/activity").GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        ActivityLog[] arr = parse(resp, ActivityLog[].class);
        return Arrays.asList(arr);
    }

    // --- Chat ---
    @Override
    public List<Conversation> listConversations(long userId) throws Exception {
        HttpRequest r = req("/api/chat/conversations?userId=" + userId).GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parseList(resp, new TypeReference<List<Conversation>>(){});
    }

    @Override
    public Conversation getOrCreateConversation(long userId, long otherUserId) throws Exception {
        Map<String, Object> payload = Map.of("userAId", userId, "userBId", otherUserId);
        HttpRequest r = req("/api/chat/conversations").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, Conversation.class);
    }

    @Override
    public List<ChatMessage> listMessages(long conversationId, int limit) throws Exception {
        HttpRequest r = req("/api/chat/conversations/" + conversationId + "/messages?limit=" + limit).GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parseList(resp, new TypeReference<List<ChatMessage>>(){});
    }

    @Override
    public ChatMessage sendMessage(long conversationId, long senderId, String content) throws Exception {
        Map<String, Object> payload = Map.of("senderId", senderId, "content", content);
        HttpRequest r = req("/api/chat/conversations/" + conversationId + "/messages").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        return parse(resp, ChatMessage.class);
    }

    @Override
    public Optional<User> findUserByEmail(String email) throws Exception {
        HttpRequest r = req("/api/users/by-email?email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8)).GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 404) return Optional.empty();
        return Optional.of(parse(resp, User.class));
    }
}
