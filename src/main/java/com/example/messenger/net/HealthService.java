package com.example.messenger.net;

import java.io.IOException;

public class HealthService {

    /**
     * GET /api/health
     *
     * @return raw response body (expected "OK")
     */
    public String checkHealth() throws IOException, InterruptedException {
        // Env.API_BASE_URL already points to ".../api"
        return ApiClient.getText("/health");
    }
}