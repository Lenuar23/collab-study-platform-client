package com.example.messenger.dto;

import java.util.List;

public class ConversationDetailsResponse {

    private Long conversationId;
    private String type;
    private List<ParticipantInfo> participants;
    private List<MessageDto> messages;

    public static class ParticipantInfo {
        private Long userId;
        private String name;
        private String email;

        public ParticipantInfo() {
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public ConversationDetailsResponse() {
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

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
