package com.example.messenger.net;

import com.example.messenger.dto.ConversationSummary;
import com.example.messenger.dto.CreateConversationResponse;
import com.example.messenger.dto.CreateDirectConversationRequest;
import com.example.messenger.store.SessionStore;

import java.io.IOException;

public class ConversationService {

    public long createDirectConversation(long otherUserId) throws IOException, InterruptedException {
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User is not logged in");
        }

        CreateDirectConversationRequest request =
                new CreateDirectConversationRequest(currentUserId, otherUserId);

        CreateConversationResponse response =
                ApiClient.post("/chat/direct", request, CreateConversationResponse.class);

        if (response == null || response.getConversationId() == null) {
            throw new IOException("Server did not return conversationId");
        }

        return response.getConversationId();
    }

    public ConversationSummary[] listUserConversations() throws IOException, InterruptedException {
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User is not logged in");
        }
        String path = "/chat/conversations/of-user/" + currentUserId;
        return ApiClient.get(path, ConversationSummary[].class);
    }
}
