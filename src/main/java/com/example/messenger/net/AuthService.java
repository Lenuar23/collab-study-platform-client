package com.example.messenger.net;

import com.example.messenger.dto.AuthRequest;
import com.example.messenger.dto.AuthResponse;
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
}
