package com.chitranjank.apps.socialchats;

public class User {
    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String email;
    private String about;

    public User() {
    }

    public User(String id, String username, String imageURL, String status, String email,
                String about) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.email = email;
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public String getAbout() {
        return about;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

}
