package org.example.cspclient.model;

import java.time.LocalDateTime;

public class Group {
    private long id;
    private String name;
    private String description;
    private long createdBy;
    private LocalDateTime createdAt;

    public Group() {}

    public Group(long id, String name, String description, long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
