package com.example.messenger.dto;

public class ConversationSummary {
    private Long conversationId;
    private String type;

    public ConversationSummary() {
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
