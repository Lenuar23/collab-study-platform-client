package org.example.cspclient.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskItem {
    private long id;
    private long groupId;
    private long createdBy;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    public TaskItem() {}

    public TaskItem(long id, long groupId, long createdBy, String title, String description, TaskStatus status, LocalDate deadline, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getGroupId() { return groupId; }
    public void setGroupId(long groupId) { this.groupId = groupId; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
