package com.clocktower.lullaby.interfaces;

import android.app.Activity;

import com.clocktower.lullaby.model.SongInfo;

import java.io.File;
import java.util.List;

public interface FragmentListener extends ListItemClickListener {

    void setAlarm(int hour, int minute);
    void stopAlarm();
    void playOrPauseMusic();
    void setAlarmMusic();
    void seekMusicToPosition(int time);
    Thread musicPlayerThread();

}
