package com.example.messenger.net;

import com.example.messenger.dto.CreateGroupRequest;
import com.example.messenger.dto.GroupDto;
import com.example.messenger.dto.UpdateGroupAvatarRequest;
import com.example.messenger.store.SessionStore;

import java.io.IOException;

public class GroupService {

    public GroupDto[] listGroups() throws IOException, InterruptedException {
        // GET /api/groups
        return ApiClient.get("/groups", GroupDto[].class);
    }

    public GroupDto createGroup(String name, String description, String avatarUrl)
            throws IOException, InterruptedException {
        Long ownerId = SessionStore.getUserId();
        if (ownerId == null) {
            throw new IllegalStateException("User is not logged in");
        }
        CreateGroupRequest request = new CreateGroupRequest(ownerId, name, description, avatarUrl);
        return ApiClient.post("/groups", request, GroupDto.class);
    }

    public GroupDto updateGroup(Long groupId, String name, String description)
            throws IOException, InterruptedException {
        // Server's updateGroup uses CreateGroupRequest but only reads name + description
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName(name);
        request.setDescription(description);
        return ApiClient.put("/groups/" + groupId, request, GroupDto.class);
    }

    public Long[] getGroupMembers(Long groupId) throws IOException, InterruptedException {
        // GET /api/groups/{groupId}/members
        return ApiClient.get("/groups/" + groupId + "/members", Long[].class);
    }

    public void addMember(Long groupId, Long userId) throws IOException, InterruptedException {
        // POST /api/groups/{groupId}/members/{userId}
        ApiClient.post("/groups/" + groupId + "/members/" + userId, null, Void.class);
    }

    public GroupDto updateAvatar(Long groupId, String avatarUrl)
            throws IOException, InterruptedException {
        UpdateGroupAvatarRequest request = new UpdateGroupAvatarRequest(avatarUrl);
        return ApiClient.put("/groups/" + groupId + "/avatar", request, GroupDto.class);
    }
}
