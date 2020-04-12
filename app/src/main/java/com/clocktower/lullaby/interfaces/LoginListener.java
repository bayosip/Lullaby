package com.clocktower.lullaby.interfaces;

import android.app.Activity;

public interface LoginListener {

    void initialiseLogin();
    Activity getLoginActivity();
    void goStraightToHomePage(String name);
    void startProfilePictureFragment(String profileDisplayName);
    void hidePB();
    void showPB();
    void progressPB(long progress);
    void changeProfilePic(String url);
}
