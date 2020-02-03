package com.clocktower.lullaby.model;

public class Post {

    String postTitle, videoURL;

    public Post(String postTitle, String videoURL) {
        this.postTitle = postTitle;
        this.videoURL = videoURL;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public String getPostTitle() {
        return postTitle;
    }
}
