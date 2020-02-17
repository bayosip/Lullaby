package com.clocktower.lullaby.model;

import java.util.Date;

public class Comments {

    private String username, comment, url;
    private Date timestamp;

    public Comments(String username, String url, String comment, Date timestamp) {
        this.username = username;
        this.url = url;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getComment() {
        return comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
