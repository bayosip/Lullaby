package com.clocktower.lullaby.interfaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

public interface ProfileListener extends LoginListener {
    void disableScreen();
    void enableScreen();
    void saveUserNameintoDb(String name);
    boolean saveProfilePictureInDb(Bitmap bitmap);
    boolean saveProfilePictureInDb(Uri uri);

}
