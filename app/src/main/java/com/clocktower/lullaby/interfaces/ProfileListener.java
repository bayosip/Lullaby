package com.clocktower.lullaby.interfaces;

import android.content.Intent;
import android.net.Uri;

public interface ProfileListener {
    void goStraightToHomePage(String getName);
    void saveUserNameintoDb(String name);
    boolean savePictureInDb(Uri uri);
    void getImageFromIntent(Intent data);
}
