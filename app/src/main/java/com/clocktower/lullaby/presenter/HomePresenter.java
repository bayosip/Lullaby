package com.clocktower.lullaby.presenter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.service.WakeTimeReceiver;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.model.utilities.ServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

public class HomePresenter extends FirebaseToHomePresenter {

    private HomeViewInterFace interFace;

    private AlarmManager alarmManager;
    private MediaPlayer player;
    private Calendar calendar;
    private Date date;
    private static final String DATE_FORMAT = "HH:mm";
    private SharedPreferences.Editor editor;
    private SharedPreferences appPref;
    private final String message = "WAKE UP!!!!";
    private String dateTime;
    private long alarm = 0;
    private PendingIntent pendingIntent;
    private String trackurl;
    private boolean isTrackPlaying = false;
    private boolean isAlarmSet = false;


    private static final int REQUEST_CODE = 100;
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String ALARM_TIME = "ALARM_TIME";

    public HomePresenter(HomeViewInterFace interFace) {
        super(interFace);
        this.interFace = interFace;
        initialisePrequisites();
    }

    private void initialisePrequisites(){
        appPref = GeneralUtil.getAppPref(interFace.getListenerContext());
        editor = appPref.edit();
        calendar = Calendar.getInstance();
        alarmManager = (AlarmManager) this.interFace.getListenerContext()
                .getSystemService(Context.ALARM_SERVICE);
    }


    public List<File> retrieveAllAudioFilesFromPhone(File file){
        List<File> fileList = new ArrayList<>();
        File[] allFiles = file.listFiles();

        for (File afile: allFiles){
            if (afile!=null && afile.isDirectory() && !afile.isHidden()){
                fileList.addAll(retrieveAllAudioFilesFromPhone(afile));
            }else {
                if (afile.getName().endsWith(Constants.AAC)|| afile.getName().endsWith(Constants.MP3)
                        ||afile.getName().endsWith(Constants.WAV)){

                    fileList.add(afile);
                }
            }
        }

        return fileList.size()>0? fileList: null;
    }

    public Thread musicPlayerThread(final Handler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (player != null) {
                    try {
                        Message msg = new Message();
                        msg.what = player.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {

                    }
                }
            }
        });
        return thread;
    }

    public void startNewMusic(String trackurl){
        try {
            if (player!=null) {
                player.stop();
                player.reset();
                player.release();
                player = null;
            }
            player = new MediaPlayer();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.reset();
                        mp.release();
                        player=null;
                    }
                });
                player.setDataSource(trackurl);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        isTrackPlaying = true;
                        interFace.setTrackBarForMusic(mediaPlayer.getDuration());
                    }
                });



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playMusic(){
        if(player!=null && !isTrackPlaying){
            player.start();
            isTrackPlaying = true;
        }
    }

    public boolean musicIsPlaying(){
        return player!=null &&isTrackPlaying;
    }

    public void pauseMusic(){
        if(player !=null && isTrackPlaying){
            player.pause();
            isTrackPlaying = false;
        }
    }

    public void setAlarm(int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        String min = String.valueOf(minute);
        String hr = String.valueOf(hour);
        if(minute<10)min = "0"+minute;
        if(hour<10)hr =  "0"+hour;
       dateTime = hr+":"+min;
       alarm = calendar.getTimeInMillis();

       if(appPref.contains(Constants.TRACK_URL)){
           setLullabyAlarm();
       }else {
           interFace.goToMusicSetter();
           GeneralUtil.message("Please set Song For Schedule");
       }
        GeneralUtil.message("Schedule Set to - "+ hr +":" + min);
    }


    public void seekMusic(int progress){
        if(player!=null)player.seekTo(progress);
    }


    private void setLullabyAlarm() {
        isAlarmSet = true;
        Intent alarmIntent = new Intent(interFace.getListenerContext(), WakeTimeReceiver.class);
        alarmIntent.putExtra("Message", message);
        alarmIntent.putExtra("Home", alarm);
        alarmIntent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);

        pendingIntent = PendingIntent.getBroadcast(interFace.getListenerContext(), REQUEST_CODE,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);


        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    alarm, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
        }
    }


    public void cancelAlarm(){
        alarm = 0;
        isAlarmSet = false;
        alarmManager.cancel(pendingIntent);
        if(ServiceUtil.isServiceAlreadyRunningAPI16(interFace.getListenerContext()))
            ServiceUtil.stopService(interFace.getListenerContext());
        GeneralUtil.message("Home Cancelled!");
    }

    public List<SongInfo> loadSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = interFace.getListenerContext().getContentResolver().query(uri,
                null, selection, null, null);
        List<SongInfo> songInfoList = new ArrayList<>();
        if(cursor!=null){
            try{
                if(cursor.moveToFirst()){

                   while (cursor.moveToNext()){

                       String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                       Log.w("Songs", songName);
                       String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                       String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                       SongInfo info = new SongInfo(songName, artist, url);
                       songInfoList.add(info);
                   }
               }
            }finally {
                cursor.close();
            }
        }
        return songInfoList.size() >0? songInfoList: null;
    }

    public void stopMusic() {
        if(player!=null && isTrackPlaying){
            player.stop();
            isTrackPlaying = false;
        }
    }

    public void setAlarmTone(String path) {

        if(ServiceUtil.isServiceAlreadyRunningAPI16(interFace.getListenerContext()))
            ServiceUtil.stopService(interFace.getListenerContext());

        if(appPref.contains(Constants.TRACK_URL)){
            editor.remove(Constants.TRACK_URL);
        }
        editor.putString(Constants.TRACK_URL, path);
        editor.commit();
        if(alarm>0)setLullabyAlarm();
    }
}