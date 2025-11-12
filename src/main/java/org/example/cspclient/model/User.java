package org.example.cspclient.model;

public class User {
    private long id;
    private String name;
    private String email;
    private String avatarUrl;  // new optional
    private String about;      // new optional

    public User() {}

    public User(long id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    @Override public String toString() { return name != null ? name : email; }
}
