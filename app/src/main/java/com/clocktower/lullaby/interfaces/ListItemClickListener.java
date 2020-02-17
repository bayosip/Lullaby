package com.clocktower.lullaby.interfaces;


import com.clocktower.lullaby.view.activities.Home;

public interface ListItemClickListener {

    void onMusicTrackClick(int position);
    Home getListenerContext();
    void likeThisPost(String postID);
}
