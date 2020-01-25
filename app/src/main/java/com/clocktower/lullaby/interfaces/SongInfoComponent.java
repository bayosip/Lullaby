package com.clocktower.lullaby.interfaces;

import com.clocktower.lullaby.model.SongInfo;

import dagger.Component;

@Component
public interface SongInfoComponent {

    SongInfo getSongInfo();
    void inject(String songName, String artiste, String songUrl);
}
