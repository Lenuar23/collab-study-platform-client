package com.example.messenger.dto.convers;

public class UpdateMessageRequest {

    private String content;

    public UpdateMessageRequest() {
    }

    public UpdateMessageRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
