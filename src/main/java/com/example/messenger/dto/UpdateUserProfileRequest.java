package com.example.messenger.dto;

public class UpdateUserProfileRequest {
    private String name;
    private String avatarUrl;

    public UpdateUserProfileRequest() {
    }

    public UpdateUserProfileRequest(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}