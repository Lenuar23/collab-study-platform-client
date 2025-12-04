package com.example.messenger.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // Якщо використовується Jackson (не обов'язково, але корисно)

public class MaterialDto {
    private Long resourceId;
    private String filename;
    private String fileUrl;
    private String fileType;
    private Long uploaderId;
    private String uploaderName;
    private Long groupId;
    private String createdAt;

    public MaterialDto() {}

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    // --- ХАК ДЛЯ ВИПРАВЛЕННЯ ПОМИЛКИ ---
    // Якщо сервер надсилає "id", цей метод перехопить його і запише в resourceId
    public void setId(Long id) { this.resourceId = id; }
    public Long getId() { return resourceId; }
    // -----------------------------------

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    @Override
    public String toString() {
        return filename;
    }
}