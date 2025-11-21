package com.example.messenger.net;

import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.SendMessageRequest;
import com.example.messenger.dto.UpdateMessageRequest;
import com.example.messenger.store.SessionStore;

import java.io.IOException;

public class MessageService {

    public MessageDto[] listMessages(long conversationId) throws IOException, InterruptedException {
        String path = "/chat/" + conversationId + "/messages";
        return ApiClient.get(path, MessageDto[].class);
    }

    public void sendMessage(long conversationId, String content) throws IOException, InterruptedException {
        Long senderId = SessionStore.getUserId();
        if (senderId == null) {
            throw new IllegalStateException("User is not logged in");
        }
        SendMessageRequest request = new SendMessageRequest(conversationId, senderId, content);
        ApiClient.post("/chat/messages", request, Void.class);
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
