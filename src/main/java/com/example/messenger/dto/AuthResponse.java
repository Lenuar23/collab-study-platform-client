package com.example.messenger.dto;

public class AuthResponse {
    private Long userId;
    private String token;

    public AuthResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
