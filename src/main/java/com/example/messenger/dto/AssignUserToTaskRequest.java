package com.example.messenger.dto;

public class AssignUserToTaskRequest {
    private Long userId;

    public AssignUserToTaskRequest() {
    }

    public AssignUserToTaskRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}