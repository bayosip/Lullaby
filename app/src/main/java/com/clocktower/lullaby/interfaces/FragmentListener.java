package com.clocktower.lullaby.interfaces;

import android.os.Handler;
import android.widget.MediaController;

import androidx.fragment.app.FragmentManager;

public interface FragmentListener extends ListItemClickListener {

    void setAlarm(int hour, int minute);
    void stopAlarm();
    void playOrPauseMusic(FragmentManager manager);
    void stopMusic();
    void setAlarmMusic();
    void seekMusicToPosition(int time);
    void musicPlayerThread(Handler handler);
    MediaController getVideoMediaController();

}
