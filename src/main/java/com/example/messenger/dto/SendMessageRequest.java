package com.example.messenger.dto;

public class SendMessageRequest {
    private Long conversationId;
    private Long senderUserId;
    private String content;

    public SendMessageRequest() {
    }

    public SendMessageRequest(Long conversationId, Long senderUserId, String content) {
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.content = content;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
