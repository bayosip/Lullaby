package com.clocktower.lullaby.model.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

public class RingToneService extends Service {
    private MediaPlayer myPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        GeneralUtil.message("Service Created");

        //myPlayer = MediaPlayer.create(this, R.raw.sun);
        myPlayer.setLooping(false); // Set looping
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        GeneralUtil.message("Service Started");
        myPlayer.start();
    }
    @Override
    public void onDestroy() {
        GeneralUtil.message("Service Stopped");
        myPlayer.stop();
    }
}
