package org.example.cspclient.model;

import java.time.LocalDateTime;

public class ResourceItem {
    private long id;
    private long groupId;
    private long uploadedBy;
    private String title;
    private String type; // link | file
    private String pathOrUrl;
    private LocalDateTime uploadedAt;

    public ResourceItem() {}

    public ResourceItem(long id, long groupId, long uploadedBy, String title, String type, String pathOrUrl, LocalDateTime uploadedAt) {
        this.id = id;
        this.groupId = groupId;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
        this.uploadedAt = uploadedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getGroupId() { return groupId; }
    public void setGroupId(long groupId) { this.groupId = groupId; }

    public long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(long uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPathOrUrl() { return pathOrUrl; }
    public void setPathOrUrl(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    @Override
    public String toString() {
        return title + " (" + type + ")";
    }
}
