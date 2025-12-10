package com.example.messenger.net;

import com.example.messenger.dto.dtuser.AssignUserToTaskRequest;
import com.example.messenger.dto.dttask.CreateTaskRequest;
import com.example.messenger.dto.TaskDto;
import com.example.messenger.dto.dttask.TaskProgressDto;
import com.example.messenger.dto.dttask.UpdateUserTaskStatusRequest;
import com.example.messenger.store.SessionStore;

import java.io.IOException;

public class TaskService {

    public TaskDto createTask(Long groupId, String title, String description, String dueDate)
            throws IOException, InterruptedException {
        Long creatorUserId = SessionStore.getUserId();
        if (creatorUserId == null) {
            throw new IllegalStateException("User is not logged in.");
        }

        CreateTaskRequest request = new CreateTaskRequest();
        request.setGroupId(groupId);
        request.setCreatorUserId(creatorUserId);
        request.setTitle(title);
        request.setDescription(description);
        request.setDueDate(dueDate);

        return ApiClient.post("/tasks", request, TaskDto.class);
    }

    public TaskDto[] getTasksForGroup(Long groupId) throws IOException, InterruptedException {
        return ApiClient.get("/groups/" + groupId + "/tasks", TaskDto[].class);
    }

    public void assignUserToTask(Long taskId, Long userId) throws IOException, InterruptedException {
        AssignUserToTaskRequest request = new AssignUserToTaskRequest(userId);
        ApiClient.post("/tasks/" + taskId + "/assign", request, Void.class);
    }

    public TaskProgressDto updateUserTaskStatus(Long taskId, Long userId, String status)
            throws IOException, InterruptedException {
        UpdateUserTaskStatusRequest request = new UpdateUserTaskStatusRequest(userId, status);
        return ApiClient.patch("/tasks/" + taskId + "/progress", request, TaskProgressDto.class);
    }

    public TaskProgressDto[] getTaskProgress(Long taskId) throws IOException, InterruptedException {
        return ApiClient.get("/tasks/" + taskId + "/progress", TaskProgressDto[].class);
    }
}