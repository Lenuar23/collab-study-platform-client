package org.example.cspclient.api;

import org.example.cspclient.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MockApiClient implements ApiClient {

    private final AtomicLong userSeq = new AtomicLong(100);
    private final AtomicLong groupSeq = new AtomicLong(200);
    private final AtomicLong taskSeq = new AtomicLong(300);
    private final AtomicLong resSeq = new AtomicLong(400);
    private final AtomicLong logSeq = new AtomicLong(500);
    private final AtomicLong convSeq = new AtomicLong(600);
    private final AtomicLong msgSeq = new AtomicLong(700);

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Group> groups = new HashMap<>();
    private final Map<Long, TaskItem> tasks = new HashMap<>();
    private final Map<Long, ResourceItem> resources = new HashMap<>();
    private final List<ActivityLog> activity = new ArrayList<>();

    private final Map<Long, Conversation> conversations = new HashMap<>();
    private final Map<Long, List<ChatMessage>> messages = new HashMap<>();

    public MockApiClient() {
        // Users
        User demo = new User(1L, "Demo User", "demo@example.com");
        users.put(1L, demo);
        User alice = new User(2L, "Alice Green", "alice@example.com");
        users.put(2L, alice);
        User bob = new User(3L, "Bob Lime", "bob@example.com");
        users.put(3L, bob);

        // Group & tasks
        Group g = new Group(1L, "Algorithms", "Study group for Algorithms", 1L, LocalDateTime.now().minusDays(2));
        groups.put(1L, g);

        TaskItem t1 = new TaskItem(1L, 1L, 1L, "Read Chapter 1", "Big-O notation", TaskStatus.OPEN, LocalDate.now().plusDays(3), LocalDateTime.now().minusDays(1));
        TaskItem t2 = new TaskItem(2L, 1L, 1L, "Solve 10 exercises", "From the end of chapter", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(5), LocalDateTime.now().minusHours(12));
        tasks.put(1L, t1);
        tasks.put(2L, t2);

        ResourceItem r1 = new ResourceItem(1L, 1L, 1L, "Course Slides", "link", "https://example.com/slides", LocalDateTime.now().minusDays(1));
        resources.put(1L, r1);

        activity.add(new ActivityLog(nextLogId(), 1L, "GROUP_CREATED", LocalDateTime.now().minusDays(2), "Algorithms group created"));
        activity.add(new ActivityLog(nextLogId(), 1L, "TASK_CREATED", LocalDateTime.now().minusDays(1), "Task 'Read Chapter 1' created"));

        // Seed a conversation Demo <-> Alice
        Conversation c = new Conversation(nextConvId(), 1L, 2L, LocalDateTime.now().minusMinutes(1));
        conversations.put(c.getId(), c);
        List<ChatMessage> list = new ArrayList<>();
        list.add(new ChatMessage(nextMsgId(), c.getId(), 2L, 1L, "Привіт! Як просувається навчання?", LocalDateTime.now().minusMinutes(5)));
        list.add(new ChatMessage(nextMsgId(), c.getId(), 1L, 2L, "Гей! Непогано, думаю додати нові задачі до групи.", LocalDateTime.now().minusMinutes(3)));
        list.add(new ChatMessage(nextMsgId(), c.getId(), 2L, 1L, "Клас, напиши коли будеш в онлайні.", LocalDateTime.now().minusMinutes(1)));
        messages.put(c.getId(), list);
    }

    private long nextGroupId() { return groupSeq.incrementAndGet(); }
    private long nextTaskId() { return taskSeq.incrementAndGet(); }
    private long nextResId() { return resSeq.incrementAndGet(); }
    private long nextLogId() { return logSeq.incrementAndGet(); }
    private long nextUserId() { return userSeq.incrementAndGet(); }
    private long nextConvId() { return convSeq.incrementAndGet(); }
    private long nextMsgId() { return msgSeq.incrementAndGet(); }

    @Override
    public Optional<User> login(String email, String password) {
        return users.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        if (users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            throw new IllegalArgumentException("Email already registered");
        }
        User u = new User(nextUserId(), name, email);
        users.put(u.getId(), u);
        activity.add(new ActivityLog(nextLogId(), u.getId(), "USER_REGISTERED", LocalDateTime.now(), "New user " + email));
        return u;
    }

    @Override
    public List<Group> listGroups(long userId) {
        return groups.values().stream()
                .sorted(Comparator.comparing(Group::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Group createGroup(long userId, String name, String description) {
        Group g = new Group(nextGroupId(), name, description, userId, LocalDateTime.now());
        groups.put(g.getId(), g);
        activity.add(new ActivityLog(nextLogId(), userId, "GROUP_CREATED", LocalDateTime.now(), "Group '" + name + "' created"));
        return g;
    }

    @Override
    public List<TaskItem> listTasks(long groupId) {
        return tasks.values().stream().filter(t -> t.getGroupId() == groupId)
                .sorted(Comparator.comparing(TaskItem::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public TaskItem createTask(long groupId, long createdBy, String title, String description, TaskStatus status, LocalDate deadline) {
        TaskItem t = new TaskItem(nextTaskId(), groupId, createdBy, title, description, status, deadline, LocalDateTime.now());
        tasks.put(t.getId(), t);
        activity.add(new ActivityLog(nextLogId(), createdBy, "TASK_CREATED", LocalDateTime.now(), "Task '" + title + "' created"));
        return t;
    }

    @Override
    public TaskItem updateTaskStatus(long taskId, TaskStatus status) {
        TaskItem t = tasks.get(taskId);
        if (t == null) throw new IllegalArgumentException("Task not found");
        t.setStatus(status);
        activity.add(new ActivityLog(nextLogId(), t.getCreatedBy(), "TASK_STATUS", LocalDateTime.now(), "Task '" + t.getTitle() + "' -> " + status));
        return t;
    }

    @Override
    public List<ResourceItem> listResources(long groupId) {
        return resources.values().stream().filter(r -> r.getGroupId() == groupId)
                .sorted(Comparator.comparing(ResourceItem::getUploadedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ResourceItem addResource(long groupId, long uploadedBy, String title, String type, String urlOrPath) {
        ResourceItem r = new ResourceItem(nextResId(), groupId, uploadedBy, title, type, urlOrPath, LocalDateTime.now());
        resources.put(r.getId(), r);
        activity.add(new ActivityLog(nextLogId(), uploadedBy, "RESOURCE_ADDED", LocalDateTime.now(), "Resource '" + title + "' added"));
        return r;
    }

    @Override
    public List<ActivityLog> listActivity(long groupId) {
        return activity.stream().sorted(Comparator.comparing(ActivityLog::getTimestamp).reversed()).collect(Collectors.toList());
    }

    // --- Chat ---

    @Override
    public List<Conversation> listConversations(long userId) {
        return conversations.values().stream()
                .filter(c -> c.getUserAId() == userId || c.getUserBId() == userId)
                .sorted(Comparator.comparing(Conversation::getLastMessageAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Conversation getOrCreateConversation(long userId, long otherUserId) {
        Optional<Conversation> existing = conversations.values().stream()
                .filter(c -> (c.getUserAId() == userId && c.getUserBId() == otherUserId) ||
                             (c.getUserAId() == otherUserId && c.getUserBId() == userId))
                .findFirst();
        if (existing.isPresent()) return existing.get();
        Conversation c = new Conversation(nextConvId(), userId, otherUserId, LocalDateTime.now());
        conversations.put(c.getId(), c);
        messages.put(c.getId(), new ArrayList<>());
        return c;
    }

    @Override
    public List<ChatMessage> listMessages(long conversationId, int limit) {
        List<ChatMessage> list = messages.getOrDefault(conversationId, new ArrayList<>());
        return list.stream().sorted(Comparator.comparing(ChatMessage::getTimestamp)).collect(Collectors.toList());
    }

    @Override
    public ChatMessage sendMessage(long conversationId, long senderId, String content) {
        Conversation c = conversations.get(conversationId);
        if (c == null) throw new IllegalArgumentException("Conversation not found");
        long receiver = c.getUserAId() == senderId ? c.getUserBId() : c.getUserAId();
        ChatMessage m = new ChatMessage(nextMsgId(), conversationId, senderId, receiver, content, LocalDateTime.now());
        messages.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(m);
        c.setLastMessageAt(m.getTimestamp());
        return m;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }
}
