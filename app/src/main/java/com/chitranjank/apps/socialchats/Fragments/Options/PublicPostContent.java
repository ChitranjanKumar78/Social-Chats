package com.chitranjank.apps.socialchats.Fragments.Options;

public class PublicPostContent {
    private String imageUrl;
    private String captionMessage;
    private String profileUrl;
    private String userNamePerson;
    private int likeCount;

    String userId;

    public PublicPostContent() {
    }

    public PublicPostContent(String profileUrl, String userNamePerson, String imageUrl,
                             String captionMessage, String userId, int likeCount) {
        this.imageUrl = imageUrl;
        this.captionMessage = captionMessage;
        this.profileUrl = profileUrl;
        this.userNamePerson = userNamePerson;
        this.userId = userId;
        this.likeCount = likeCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public String getUserId() {
        return userId;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getUserNamePerson() {
        return userNamePerson;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaptionMessage() {
        return captionMessage;
    }
}
