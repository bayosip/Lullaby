package com.clocktower.lullaby.model;

public class Profile {

    String username, url;

    public Profile(String username, String url) {
        this.username = username;
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }
}
