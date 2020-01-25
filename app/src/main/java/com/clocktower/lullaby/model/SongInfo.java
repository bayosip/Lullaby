package com.clocktower.lullaby.model;

import javax.inject.Inject;

public class SongInfo {

    String songName;
    String artiste;
    String songUrl;

    @Inject
    public SongInfo(String songName, String artiste, String songUrl) {
        this.songName = songName;
        this.artiste = artiste;
        this.songUrl = songUrl;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtiste() {
        return artiste;
    }

    public String getSongUrl() {
        return songUrl;
    }
}
