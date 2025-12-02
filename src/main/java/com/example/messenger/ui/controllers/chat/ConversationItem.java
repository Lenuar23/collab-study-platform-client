package com.example.messenger.ui.controllers.chat;

public class ConversationItem {

    private final long conversationId;
    private final String baseTitle;
    private final String type;
    private long unreadCount;

    public ConversationItem(long id, String title, String type) {
        this.conversationId = id;
        this.baseTitle = title;
        this.type = type;
    }

    public long getConversationId() {
        return conversationId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        if (unreadCount > 0) return baseTitle + " [" + unreadCount + " unread]";
        return baseTitle;
    }

    public void setUnreadCount(long unread) {
        this.unreadCount = unread;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
