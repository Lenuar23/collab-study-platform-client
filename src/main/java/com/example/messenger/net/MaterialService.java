package com.example.messenger.net;

import com.example.messenger.dto.MaterialDto;
import com.example.messenger.store.SessionStore;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaterialService {

    private final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();

    public MaterialDto[] getMyMaterials() throws IOException, InterruptedException {
        Long uid = SessionStore.getUserId();
        String query = (uid != null) ? "?userIdParam=" + uid : "";
        try {
            return ApiClient.get("/resources/my" + query, MaterialDto[].class);
        } catch (Exception e) {
            return new MaterialDto[0];
        }
    }

    public MaterialDto[] getGroupMaterials(Long groupId) throws IOException, InterruptedException {
        Long uid = SessionStore.getUserId();
        String query = (uid != null) ? "?userIdParam=" + uid : "";
        try {
            return ApiClient.get("/groups/" + groupId + "/resources" + query, MaterialDto[].class);
        } catch (Exception e) {
            return new MaterialDto[0];
        }
    }

    public MaterialDto uploadFile(Long groupId, File file) throws IOException, InterruptedException {
        if (groupId == null) throw new IOException("Group ID is required.");
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) throw new IOException("User not logged in.");

        String url = BASE_URL + "/groups/" + groupId + "/resources?userIdParam=" + currentUserId;
        String boundary = new BigInteger(256, new Random()).toString();

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        List<byte[]> byteArrays = new ArrayList<>();
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        byteArrays.add(header.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(fileBytes);
        byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        int totalSize = byteArrays.stream().mapToInt(b -> b.length).sum();
        byte[] body = new byte[totalSize];
        int currentPos = 0;
        for (byte[] b : byteArrays) {
            System.arraycopy(b, 0, body, currentPos, b.length);
            currentPos += b.length;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));

        String token = SessionStore.getToken();
        if (token != null) builder.header("Authorization", "Bearer " + token);

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String jsonResponse = response.body();

            // ВИТЯГУЄМО ID З JSON (Ручний парсинг)
            Long resourceId = extractIdFromJson(jsonResponse);

            MaterialDto dto = new MaterialDto();
            dto.setResourceId(resourceId); // Зберігаємо реальний ID!
            dto.setFilename(file.getName());
            dto.setFileType("File");

            // Формуємо правильне посилання для скачування
            if (resourceId != null) {
                dto.setFileUrl(BASE_URL + "/resources/" + resourceId + "/download");
            }

            return dto;
        } else {
            throw new IOException("Upload failed: " + response.statusCode());
        }
    }

    // Допоміжний метод для парсингу ID
    private Long extractIdFromJson(String json) {
        try {
            // Шукаємо "resourceId":123 або "id":123
            Pattern p = Pattern.compile("\"(resourceId|id)\"\\s*:\\s*(\\d+)");
            Matcher m = p.matcher(json);
            if (m.find()) {
                return Long.parseLong(m.group(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void downloadFile(Long resourceId, File destination) throws IOException, InterruptedException {
        if (resourceId == null) throw new IOException("Resource ID is missing.");

        Long uid = SessionStore.getUserId();
        String query = (uid != null) ? "?userIdParam=" + uid : "";
        String url = BASE_URL + "/resources/" + resourceId + "/download" + query;

        performDownload(url, destination);
    }

    public void downloadFileFromUrl(String url, File destination) throws IOException, InterruptedException {
        if (url == null || url.isBlank()) throw new IOException("URL is missing.");

        // Додаємо userIdParam якщо його немає
        if (!url.contains("userIdParam")) {
            Long uid = SessionStore.getUserId();
            if (url.contains("?")) url += "&userIdParam=" + uid;
            else url += "?userIdParam=" + uid;
        }
        performDownload(url, destination);
    }

    private void performDownload(String url, File destination) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        String token = SessionStore.getToken();
        if (token != null) builder.header("Authorization", "Bearer " + token);

        HttpResponse<Path> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofFile(destination.toPath()));

        if (response.statusCode() != 200) {
            throw new IOException("Download failed: " + response.statusCode());
        }
    }
}