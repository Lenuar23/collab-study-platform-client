package com.example.messenger.dto;

public class CreateGroupRequest {
    private Long ownerUserId;
    private String name;
    private String description;
    private String avatarUrl;

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(Long ownerUserId, String name, String description, String avatarUrl) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
