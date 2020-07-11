package com.clocktower.lullaby.model;


import com.google.firebase.firestore.PropertyName;

public class SongInfo {

    private String TrackName, Artiste, Url;

    public SongInfo() {}

    public SongInfo(String TrackName, String Artiste, String Url) {
        this.TrackName = TrackName;
        this.Artiste = Artiste;
        this.Url = Url;
    }

    public String getTrackName() {
        return TrackName;
    }

    @PropertyName("artiste")
    public String getArtiste() {
        return Artiste;
    }

    public String getUrl() {
        return Url;
    }
}
