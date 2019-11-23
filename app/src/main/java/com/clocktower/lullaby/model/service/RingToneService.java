package com.clocktower.lullaby.model.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.text.TextUtils;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

import java.io.IOException;

public class RingToneService extends Service {
    private MediaPlayer myPlayer;

    private SharedPreferences.Editor editor;
    private SharedPreferences appPref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        GeneralUtil.message("Service Created");

        appPref = GeneralUtil.getAppPref(getApplicationContext());
        editor = appPref.edit();
        myPlayer = new MediaPlayer();
        myPlayer.setLooping(true);// Set looping
        myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                myPlayer=null;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String path = appPref.getString(Constants.TRACK_URL, null);
        if (!TextUtils.isEmpty(path)) {
            try {
                myPlayer.setDataSource(path);
                myPlayer.prepareAsync();
                myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        GeneralUtil.message("Service Started");
    }

    @Override
    public void onDestroy() {
        GeneralUtil.message("Service Stopped");
        editor.remove(Constants.TRACK_URL);
        editor.commit();
        myPlayer.stop();
        myPlayer.reset();
        myPlayer.release();
        myPlayer=null;
    }
}
