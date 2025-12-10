package com.example.messenger.dto.dttask;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- Імпорт

@JsonIgnoreProperties(ignoreUnknown = true) // <--- Додати це
public class TaskProgressDto {
    private Long id;
    private Long taskId;
    private Long userId;
    private String status;
    private String updatedAt;
    private String completedAt;

    public TaskProgressDto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
