package com.clocktower.lullaby.interfaces;


import android.graphics.Bitmap;

import com.clocktower.lullaby.view.activities.Home;

public interface ListItemClickListener {

    void onMusicTrackClick(int position);
    Home getListenerContext();
    void likeThisPost(String postID);

    void makeVideoFullScreen(String url, int currentPosition);
    void makeFullPicture(Bitmap bitmap);
}
