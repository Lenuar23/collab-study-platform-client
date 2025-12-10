package com.example.messenger.dto.convers;

public class CreateConversationResponse {

    private Long conversationId;

    public CreateConversationResponse() {
    }

    public CreateConversationResponse(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
