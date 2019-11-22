package com.clocktower.lullaby.model.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.view.activities.Alarm;

public class AlarmService extends IntentService {

    private static final int REQUEST_CODE = 100;
    private long alarm;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        alarm = intent.getLongExtra("Alarm", 0);
        sendNotification(intent.getStringExtra("Message"));
    }

    public AlarmService() {
        super("AlarmNotificationService");
    }


    private void sendNotification(String message){

        Intent wakeIntent = new Intent(getApplicationContext(), Alarm.class);
        wakeIntent.putExtra("Alarm",alarm);
        wakeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE,
                wakeIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder lullabyNotifyBuilder;

        if (Build.VERSION.SDK_INT <= 25) {
            lullabyNotifyBuilder = new NotificationCompat.Builder(getApplicationContext());
        } else {
            lullabyNotifyBuilder = new NotificationCompat.Builder(getApplicationContext(),
                    Constants.CHANNEL_ID);
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);

        lullabyNotifyBuilder.setContentTitle("Lullaby");
        lullabyNotifyBuilder.setDefaults(Notification.DEFAULT_ALL);
        lullabyNotifyBuilder.setContentText(message);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lullabyNotifyBuilder.setSmallIcon(R.drawable.ic_access_alarm_24dp);
        } else {
            lullabyNotifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }

        lullabyNotifyBuilder.setWhen(System.currentTimeMillis());
        lullabyNotifyBuilder.setAutoCancel(true);
        lullabyNotifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
        lullabyNotifyBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(REQUEST_CODE, lullabyNotifyBuilder.build());
        Log.e("AlarmService", "Notification sent.");
    }
}
