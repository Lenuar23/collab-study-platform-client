package org.example.cspclient.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private long id;
    private long userId;
    private String action;
    private LocalDateTime timestamp;
    private String details;

    public ActivityLog() {}

    public ActivityLog(long id, long userId, String action, LocalDateTime timestamp, String details) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
