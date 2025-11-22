package com.example.messenger.net;

import com.example.messenger.dto.*;
import com.example.messenger.store.SessionStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public long createGroupConversation(String name, List<Long> participantIds) throws IOException, InterruptedException {
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User is not logged in");
        }

        List<Long> ids = new ArrayList<>();
        if (participantIds != null) {
            ids.addAll(participantIds);
        }
        if (!ids.contains(currentUserId)) {
            ids.add(currentUserId);
        }

        CreateGroupConversationRequest request = new CreateGroupConversationRequest(name, ids);
        CreateConversationResponse response =
                ApiClient.post("/chat/group", request, CreateConversationResponse.class);

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

    public ConversationDetailsResponse getConversationDetails(long conversationId) throws IOException, InterruptedException {
        String path = "/chat/conversations/" + conversationId;
        return ApiClient.get(path, ConversationDetailsResponse.class);
    }

    public void addParticipant(long conversationId, long userId) throws IOException, InterruptedException {
        AddParticipantRequest request = new AddParticipantRequest(userId, null);
        String path = "/chat/conversations/" + conversationId + "/participants";
        ApiClient.post(path, request, Void.class);
    }

    public long getUnreadCount(long conversationId) throws IOException, InterruptedException {
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User is not logged in");
        }
        String path = "/chat/conversations/" + conversationId + "/unread-count?userId=" + currentUserId;
        @SuppressWarnings("unchecked")
        Map<String, Object> res = ApiClient.get(path, Map.class);
        if (res == null) {
            return 0L;
        }
        Object value = res.get("count");
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

public ConversationSummary[] listUserConversationsForUser(long userId) throws IOException, InterruptedException {
    String path = "/chat/user/" + userId;
    return ApiClient.get(path, ConversationSummary[].class);
}

public ConversationDetailsResponse.ParticipantInfo[] listConversationParticipants(long conversationId)
        throws IOException, InterruptedException {
    String path = "/chat/conversations/" + conversationId + "/participants";
    return ApiClient.get(path, ConversationDetailsResponse.ParticipantInfo[].class);
}

    public void markAsRead(long conversationId, Long lastReadMessageId) throws IOException, InterruptedException {
        if (lastReadMessageId == null) {
            return;
        }
        Long currentUserId = SessionStore.getUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User is not logged in");
        }
        UpdateReadReceiptRequest request = new UpdateReadReceiptRequest(currentUserId, lastReadMessageId);
        String path = "/chat/conversations/" + conversationId + "/read-receipts";
        ApiClient.post(path, request, Void.class);
    }
}