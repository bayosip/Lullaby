package com.clocktower.lullaby.model.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

import java.io.IOException;

public class RingToneService extends Service {
    private MediaPlayer myPlayer;
    private static final String CHANNEL_ID = "Lullaby_Alarm";

    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder notifyBuilder;
    private Thread backgroundThread;

    private SharedPreferences.Editor editor;
    private SharedPreferences appPref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        GeneralUtil.message("Service Created");

        appPref = GeneralUtil.getAppPref();
        editor = appPref.edit();
        notificationManager = (NotificationManager) getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        backgroundThread = new Thread(lullabyTask);

        myPlayer = new MediaPlayer();
        myPlayer.setLooping(true);// Set looping
        myPlayer.setOnCompletionListener(mp -> {
            mp.reset();
            mp.release();
            myPlayer=null;
        });
        createNotification();
    }

    private Runnable lullabyTask = new Runnable() {
        @Override
        public void run() {
            // do something in here
            String path = appPref.getString(Constants.TRACK_URL, null);
            if (!TextUtils.isEmpty(path)) {
                try {
                    myPlayer.setDataSource(RingToneService.this, Uri.parse(path));
                    myPlayer.prepareAsync();
                    myPlayer.setOnPreparedListener(mediaPlayer -> mediaPlayer.start());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            startForeground(2, notification);
        this.backgroundThread.start();
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

    private void createNotification() {

        if (Build.VERSION.SDK_INT <= 25) {
            notifyBuilder = new NotificationCompat.Builder(getApplicationContext());
        } else {
            notifyBuilder = new NotificationCompat.Builder(getApplicationContext(),
                    CHANNEL_ID);
        }
        createNotificationChannel();
        notifyBuilder.setOngoing(false).setOnlyAlertOnce(true);

        notification = notifyBuilder.build();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = this.getString(R.string.channel_name);
            String description = this.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }
}
