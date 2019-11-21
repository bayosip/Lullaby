package com.clocktower.lullaby.interfaces;

import android.content.Context;

public interface ListItemClickListener {

    void onMusicTrackClick(int position);
    Context getListenerContext();
}
