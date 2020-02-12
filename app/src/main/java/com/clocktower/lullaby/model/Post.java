package com.clocktower.lullaby.model;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Post {

    String postTitle, mediaURL, mediaType, timeStamp;

    private Post(String postTitle, String mediaURL, String mediaType, String timeStamp) {
        this.postTitle = postTitle;
        this.mediaURL = mediaURL;
        this.mediaType = mediaType;
        this.timeStamp = timeStamp;
    }

    private Post(String postTitle, String mediaType, String timeStamp) {
        this.postTitle = postTitle;
        this.mediaType = mediaType;
        this.timeStamp = timeStamp;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public String getPostTitle() {
        return postTitle;
    }


    public class PostBuilder {

        String postTitle, mediaURL = null, mediaType, timeStamp;

        public PostBuilder setPostTitle(@NonNull String postTitle) {
            this.postTitle = postTitle;
            return this;
        }

        public PostBuilder setMediaURL(@Nullable String mediaURL) {
            this.mediaURL = mediaURL;
            return this;
        }

        public PostBuilder setMediaType(@NonNull String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public PostBuilder setTimeStamp(@NonNull String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public Post build(){
            if (TextUtils.isEmpty(this.mediaURL) && this.mediaType.equalsIgnoreCase("text")){
                return new Post(postTitle, mediaType, timeStamp);
            }else {
               return new Post(postTitle, mediaURL, mediaType, timeStamp);
            }
        }
    }
}
