package com.example.messenger.dto;

public class CreateDirectConversationRequest {
    private Long user1Id;
    private Long user2Id;

    public CreateDirectConversationRequest() {
    }

    public CreateDirectConversationRequest(Long user1Id, Long user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
}
