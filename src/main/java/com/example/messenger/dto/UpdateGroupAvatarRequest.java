package com.example.messenger.dto;

public class UpdateGroupAvatarRequest {
    private String avatarUrl;

    public UpdateGroupAvatarRequest() {
    }

    public UpdateGroupAvatarRequest(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
    return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
