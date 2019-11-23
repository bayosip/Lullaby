package com.clocktower.lullaby.interfaces;

import com.clocktower.lullaby.model.SongInfo;

import java.io.File;
import java.util.List;

public interface AlarmViewInterFace  extends FragmentListener{

    void setTrackBarForMusic(int duration);
    void goToMusicSetter();
}
