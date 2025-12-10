package com.example.messenger.net;

import com.example.messenger.dto.dtgroup.CreateGroupRequest;
import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.dtgroup.UpdateGroupRequest;
import com.example.messenger.store.SessionStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroupService {

    public GroupDto[] listGroups() throws IOException, InterruptedException {
        Long userId = SessionStore.getUserId();
        if (userId != null) {
            return ApiClient.get("/groups?userId=" + userId, GroupDto[].class);
        }
        return ApiClient.get("/groups", GroupDto[].class);
    }

    public GroupDto createGroup(String name, String description, String avatarUrl) throws IOException, InterruptedException {
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) throw new IOException("User is not logged in.");

        CreateGroupRequest request = new CreateGroupRequest(currentUserId, name, description, avatarUrl);
        return ApiClient.post("/groups", request, GroupDto.class);
    }

    public GroupDto updateGroup(Long groupId, String name, String description) throws IOException, InterruptedException {
        UpdateGroupRequest request = new UpdateGroupRequest(name, description);
        return ApiClient.put("/groups/" + groupId, request, GroupDto.class);
    }

    public void addMember(Long groupId, Long userId) throws IOException, InterruptedException {
        ApiClient.post("/groups/" + groupId + "/members/" + userId, null, Void.class);
    }

    public Long[] getGroupMembers(Long groupId) throws IOException, InterruptedException {
        return ApiClient.get("/groups/" + groupId + "/members", Long[].class);
    }

    // --- АВАТАРИ ---

    // ПРАВИЛЬНИЙ МЕТОД (БЕЗ ХАКУ)
    public void uploadGroupAvatar(Long groupId, File file) throws IOException, InterruptedException {
        // Тепер ми не чіпаємо UserService!
        // Відправляємо файл напряму в групу
        String path = "/groups/" + groupId + "/avatar/file";

        // Використовуємо putMultipartFile
        ApiClient.putMultipartFile(path, "file", file, GroupDto.class);
    }

    public void deleteGroup(Long groupId) throws IOException, InterruptedException {
        // Відправляємо запит DELETE на сервер
        ApiClient.delete("/groups/" + groupId);
    }

    public void updateAvatar(Long groupId, String avatarUrl) throws IOException, InterruptedException {
        // ВИКОРИСТОВУЄМО MAP, ЩОБ УНИКНУТИ ПОМИЛКИ SERIALIZER
        Map<String, String> body = new HashMap<>();
        body.put("avatarUrl", avatarUrl);

        ApiClient.put("/groups/" + groupId + "/avatar", body, Void.class);
    }
    public void removeMember(Long groupId, Long userId) throws IOException, InterruptedException {

        ApiClient.delete("/groups/" + groupId + "/members/" + userId);
    }
}