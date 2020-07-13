package com.clocktower.lullaby.interfaces;

public interface AudioItemClickListener extends ListItemClickListener {
    interface RefreshItem {
        void setSelectedPosition(int position);
    }
    void onMusicTrackClick(int position);
}
