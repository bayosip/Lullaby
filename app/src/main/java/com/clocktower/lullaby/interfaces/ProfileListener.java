package com.clocktower.lullaby.interfaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

public interface ProfileListener extends LoginListener {
    void goStraightToHomePage(String getName);
    void saveUserNameintoDb(String name);
    boolean savePictureInDb(Bitmap bitmap);
    boolean savePictureInDb(Uri uri);
}
