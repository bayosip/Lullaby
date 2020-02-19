package com.clocktower.lullaby.model.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.clocktower.lullaby.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class CozaFirebaseMessagingService extends FirebaseMessagingService {

    private static final int REQUEST_CODE = 3000;
    private NotificationCompat.Builder notifyBuilder;
    private Notification notification;

    private static final String TAG = "CozaFirebaseMessagingService";
    private NotificationManager notificationManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notificationManager = (NotificationManager) getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        if(remoteMessage.getData()!=null){
                sendNotificationAPI26(remoteMessage);
        }
    }


    private void sendNotificationAPI26(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String content = data.get("content");

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        createNotification(soundUri, title, content);
    }

    private void createNotification(Uri uri, String... data) {

        if (Build.VERSION.SDK_INT <= 25) {
            notifyBuilder = new NotificationCompat.Builder(getApplicationContext());
        } else {
            notifyBuilder = new NotificationCompat.Builder(getApplicationContext(),
                    getString(R.string.default_notification_channel_id));
            createNotificationChannel();
        }
        notifyBuilder.setContentTitle(data[0]);
        notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
        notifyBuilder.setContentText(data[1]);

        notifyBuilder.setWhen(System.currentTimeMillis());
        notifyBuilder.setAutoCancel(true);
        notifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notifyBuilder.setOngoing(false).setOnlyAlertOnce(true);

        notification = notifyBuilder.build();
        notificationManager.notify(REQUEST_CODE, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = this.getString(R.string.coza_channel_name);
            String description = this.getString(R.string.coza_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);

        }
    }
}
