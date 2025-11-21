package com.example.messenger.net;

import com.example.messenger.config.Env;
import com.example.messenger.store.SessionStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String buildUrl(String path) {
        String base = Env.API_BASE_URL;
        if (path.startsWith("/")) {
            return base + path;
        }
        return base + "/" + path;
    }

    public static <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String url = buildUrl(path);
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

        String token = SessionStore.getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String responseBody = response.body();

        if (status >= 200 && status < 300) {
            if (responseType == Void.class || responseBody == null || responseBody.isBlank()) {
                return null;
            }
            return objectMapper.readValue(responseBody, responseType);
        } else {
            throw new IOException("HTTP " + status + ": " + responseBody);
        }
    }

    public static <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        String url = buildUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        String token = SessionStore.getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String responseBody = response.body();

        if (status >= 200 && status < 300) {
            if (responseType == Void.class || responseBody == null || responseBody.isBlank()) {
                return null;
            }
            return objectMapper.readValue(responseBody, responseType);
        } else {
            throw new IOException("HTTP " + status + ": " + responseBody);
        }
    }
}
