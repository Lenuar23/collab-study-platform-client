package com.example.messenger.net;

import com.example.messenger.config.Env;
import com.example.messenger.store.SessionStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String buildUrl(String path) {
        String base = Env.API_BASE_URL; // e.g. http://localhost:8080/api
        if (path.startsWith("/")) {
            return base + path;
        }
        return base + "/" + path;
    }

    private static HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        String token = SessionStore.getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private static <T> T handleJsonResponse(HttpResponse<String> response, Class<T> responseType)
            throws IOException {
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

    public static <T> T post(String path, Object body, Class<T> responseType)
            throws IOException, InterruptedException {
        String url = buildUrl(path);
        String jsonBody = (body == null) ? "" : objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleJsonResponse(response, responseType);
    }

    public static <T> T put(String path, Object body, Class<T> responseType)
            throws IOException, InterruptedException {
        String url = buildUrl(path);
        String jsonBody = (body == null) ? "" : objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleJsonResponse(response, responseType);
    }

    public static <T> T patch(String path, Object body, Class<T> responseType)
            throws IOException, InterruptedException {
        String url = buildUrl(path);
        String jsonBody = (body == null) ? "" : objectMapper.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleJsonResponse(response, responseType);
    }

    public static <T> T get(String path, Class<T> responseType)
            throws IOException, InterruptedException {
        String url = buildUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleJsonResponse(response, responseType);
    }

    public static void delete(String path) throws IOException, InterruptedException {
        String url = buildUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String responseBody = response.body();

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + ": " + responseBody);
        }
    }

    /**
     * Simple GET that returns raw text instead of JSON.
     */
    public static String getText(String path) throws IOException, InterruptedException {
        String url = buildUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String responseBody = response.body();

        if (status >= 200 && status < 300) {
            return responseBody;
        } else {
            throw new IOException("HTTP " + status + ": " + responseBody);
        }
    }

    /**
     * Upload a single file using multipart/form-data with PUT.
     *
     * @param path         relative API path (e.g. "/users/1/avatar/file")
     * @param fieldName    form field name (for avatar it must be "file")
     * @param file         file to upload
     * @param responseType class of expected JSON response
     */
    public static <T> T putMultipartFile(String path, String fieldName, File file, Class<T> responseType)
            throws IOException, InterruptedException {

        String url = buildUrl(path);
        String boundary = "----JavaClientBoundary" + System.currentTimeMillis();
        String lineBreak = "\r\n";

        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append(lineBreak);
        sb.append("Content-Disposition: form-data; name=\"")
          .append(fieldName)
          .append("\"; filename=\"")
          .append(file.getName())
          .append("\"")
          .append(lineBreak);
        sb.append("Content-Type: application/octet-stream").append(lineBreak);
        sb.append(lineBreak);

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] preBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] endBytes = (lineBreak + "--" + boundary + "--" + lineBreak)
                .getBytes(StandardCharsets.UTF_8);

        List<byte[]> parts = new ArrayList<>();
        parts.add(preBytes);
        parts.add(fileBytes);
        parts.add(endBytes);

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArrays(parts);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .method("PUT", bodyPublisher);

        withAuth(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleJsonResponse(response, responseType);
    }
}