package com.clocktower.lullaby.interfaces;

import android.widget.SeekBar;

public interface AudioPlayerView extends SeekBar.OnSeekBarChangeListener {

    void calibrateTrackBarForMusic(int duration);
    void changePlayButtonRes(int resID);
    void setTrackBarProgress(int progress);
    void selectMusic(String song);
}
