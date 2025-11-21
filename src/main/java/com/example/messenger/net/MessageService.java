package com.example.messenger.net;

import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.SendMessageRequest;
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
}
