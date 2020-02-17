package com.clocktower.lullaby.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class Post extends PostId{

    private String Title, Url;
    private Long MediaType;
    private Date TimeStamp;


    public Post() { }

    public Post(String Title, String Url, long MediaType, Date TimeStamp) {
        this.Title = Title;
        this.Url = Url;
        this.MediaType = MediaType;
        this.TimeStamp = TimeStamp;
    }

    public String getUrl() {
        return Url;
    }

    public String getTitle() {
        return Title;
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public Long getMediaType() {
        return MediaType;
    }
}
