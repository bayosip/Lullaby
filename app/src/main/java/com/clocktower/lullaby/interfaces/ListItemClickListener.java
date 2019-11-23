package com.clocktower.lullaby.interfaces;


import com.clocktower.lullaby.view.activities.Alarm;

public interface ListItemClickListener {

    void onMusicTrackClick(int position);
    Alarm getListenerContext();
}
