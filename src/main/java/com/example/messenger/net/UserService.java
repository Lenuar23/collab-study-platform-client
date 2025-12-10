package com.example.messenger.net;

import com.example.messenger.dto.dtuser.UpdateUserProfileRequest;
import com.example.messenger.dto.UserDto;

import java.io.File;
import java.io.IOException;

public class UserService {

    public UserDto getUserById(Long userId) throws IOException, InterruptedException {
        return ApiClient.get("/users/" + userId, UserDto.class);
    }

    // ПРИБРАЛИ параметр description
    public UserDto updateUserProfile(Long userId, String name, String avatarUrl)
            throws IOException, InterruptedException {
        // Створюємо запит без description
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(name, avatarUrl);
        return ApiClient.put("/users/" + userId + "/profile", request, UserDto.class);
    }

    public UserDto uploadAvatarFile(Long userId, File file) throws IOException, InterruptedException {
        return ApiClient.putMultipartFile("/users/" + userId + "/avatar/file", "file", file, UserDto.class);
    }
}