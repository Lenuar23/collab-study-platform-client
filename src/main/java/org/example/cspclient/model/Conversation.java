package org.example.cspclient.model;

import java.time.LocalDateTime;

public class Conversation {
    private long id;
    private long userAId;
    private long userBId;
    private LocalDateTime lastMessageAt;

    public Conversation() {}

    public Conversation(long id, long userAId, long userBId, LocalDateTime lastMessageAt) {
        this.id = id;
        this.userAId = userAId;
        this.userBId = userBId;
        this.lastMessageAt = lastMessageAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserAId() { return userAId; }
    public void setUserAId(long userAId) { this.userAId = userAId; }

    public long getUserBId() { return userBId; }
    public void setUserBId(long userBId) { this.userBId = userBId; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
