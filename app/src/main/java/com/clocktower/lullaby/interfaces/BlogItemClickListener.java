package com.clocktower.lullaby.interfaces;

import android.graphics.Bitmap;

public interface BlogItemClickListener extends ListItemClickListener {

    void likeThisPost(String postID);
    void makeVideoFullScreen(String url, int currentPosition);
    void makeFullPicture(Bitmap bitmap);
}
