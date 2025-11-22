package com.example.messenger.net;

import com.example.messenger.dto.AuthRequest;
import com.example.messenger.dto.AuthResponse;
import com.example.messenger.dto.RegisterRequest;
import com.example.messenger.dto.UserDto;
import com.example.messenger.store.SessionStore;

import java.io.IOException;

public class AuthService {

    public AuthResponse login(String email, String password) throws IOException, InterruptedException {
        AuthRequest request = new AuthRequest(email, password);
        AuthResponse response = ApiClient.post("/auth/login", request, AuthResponse.class);

        if (response != null && response.getToken() != null) {
            SessionStore.setSession(response.getUserId(), response.getToken());
        }

        return response;
    }

    public void register(String name, String email, String password) throws IOException, InterruptedException {
        RegisterRequest request = new RegisterRequest(name, email, password);
        ApiClient.post("/auth/register", request, Void.class);
    }

    /**
     * GET /api/auth/me
     */
    public UserDto getCurrentUser() throws IOException, InterruptedException {
        return ApiClient.get("/auth/me", UserDto.class);
    }

    /**
     * POST /api/auth/logout
     */
    public void logout() throws IOException, InterruptedException {
        ApiClient.post("/auth/logout", null, Void.class);
        SessionStore.clear();
    }
}