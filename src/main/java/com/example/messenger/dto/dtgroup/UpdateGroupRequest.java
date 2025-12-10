package com.example.messenger.dto.dtgroup;

public class UpdateGroupRequest {
    private String name;
    private String description;

    public UpdateGroupRequest() {
    }

    public UpdateGroupRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}