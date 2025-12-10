package com.example.messenger.net;

import com.example.messenger.config.Env;
import com.example.messenger.store.SessionStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApiClient {

    public static final CookieManager cookieManager = new CookieManager();

    static {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    private static String buildUrl(String path) {
        String base = Env.API_BASE_URL;
        if (path.startsWith("/")) {
            return base + path;
        }
        return base + "/" + path;
    }

    // --- ОНОВЛЕНИЙ МЕТОД АВТОРИЗАЦІЇ ---
    private static HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        // 1. Шукаємо JSESSIONID у куках
        Optional<HttpCookie> sessionCookie = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> "JSESSIONID".equals(c.getName()))
                .findFirst();

        if (sessionCookie.isPresent()) {
            // ВАРІАНТ A: Якщо є сесія від Google -> використовуємо її
            // Додаємо заголовок вручну, щоб HttpClient точно його відправив
            builder.header("Cookie", "JSESSIONID=" + sessionCookie.get().getValue());
            System.out.println("DEBUG: Sending JSESSIONID manually: " + sessionCookie.get().getValue());
        } else {
            // ВАРІАНТ B: Якщо сесії немає -> пробуємо Bearer Token (для звичайного входу)
            String token = SessionStore.getToken();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        return builder;
    }
    // ------------------------------------

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
            // Додамо логування помилки для ясності
            System.err.println("API Error " + status + ": " + responseBody);
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