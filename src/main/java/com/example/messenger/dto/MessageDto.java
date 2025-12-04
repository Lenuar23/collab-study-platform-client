package com.example.messenger.dto;

public class MessageDto {
    private Long messageId;
    private Long conversationId; // або groupId, залежить від API
    private Long senderUserId;
    private String senderName;   // <--- ДОДАНО
    private String content;

    public MessageDto() {}

    public MessageDto(Long senderId, String senderName, String content) {
        this.senderUserId = senderId;
        this.senderName = senderName;
        this.content = content;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    // Геттер, який шукає контролер
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return (senderName != null ? senderName : "User " + senderUserId) + ": " + content;
    }
}