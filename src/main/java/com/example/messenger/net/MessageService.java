package com.example.messenger.net;

import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.convers.UpdateMessageRequest;
import com.example.messenger.store.SessionStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageService {

    public MessageDto[] listMessages(long conversationId) throws IOException, InterruptedException {
        String path = "/chat/" + conversationId + "/messages";
        return ApiClient.get(path, MessageDto[].class);
    }

    public MessageDto[] getGroupMessages(Long groupId) throws IOException, InterruptedException {
        return ApiClient.get("/groups/" + groupId + "/messages", MessageDto[].class);
    }

    public void sendMessage(long conversationId, String content) throws IOException, InterruptedException {
        Long senderId = SessionStore.getUserId();
        if (senderId == null) throw new IllegalStateException("User is not logged in");

        // ВИПРАВЛЕННЯ: Використовуємо Map замість класу SimpleMessageRequest
        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("senderUserId", senderId);
        body.put("content", content);

        ApiClient.post("/chat/messages", body, Void.class);
    }

    public void deleteMessage(long messageId) throws IOException, InterruptedException {
        String path = "/chat/messages/" + messageId;
        ApiClient.delete(path);
    }

    public MessageDto updateMessage(long messageId, String newContent) throws IOException, InterruptedException {
        UpdateMessageRequest request = new UpdateMessageRequest(newContent);
        String path = "/chat/messages/" + messageId;
        return ApiClient.patch(path, request, MessageDto.class);
    }
}