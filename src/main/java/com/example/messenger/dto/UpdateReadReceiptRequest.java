package com.example.messenger.dto;

public class UpdateReadReceiptRequest {
    private Long userId;
    private Long lastReadMessageId;

    public UpdateReadReceiptRequest() {
    }

    public UpdateReadReceiptRequest(Long userId, Long lastReadMessageId) {
        this.userId = userId;
        this.lastReadMessageId = lastReadMessageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}
