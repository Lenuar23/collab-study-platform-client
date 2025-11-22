package com.example.messenger.dto;

public class GroupDto {
    private Long groupId;
    private String name;
    private String description;
    private Long ownerUserId;
    private String avatarUrl;
    private String createdAt;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        if (groupId == null && name == null) {
            return "Group";
        }
        String base = (name != null) ? name : ("Group " + groupId);
        if (groupId != null) {
            return "#" + groupId + " " + base;
        }
        return base;
    }
}
