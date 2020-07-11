package com.clocktower.lullaby.presenter;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.service.WakeTimeReceiver;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.model.utilities.ServiceUtil;
import com.clocktower.lullaby.view.fragments.home.MusicSelectorDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
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
    private Handler handler;
    private String track_name = null;


    private static final int REQUEST_CODE = 100;
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String ALARM_TIME = "ALARM_TIME";

    public HomePresenter(HomeViewInterFace interFace) {
        super(interFace);
        this.interFace = interFace;
        initialisePrequisites();
    }

    private void initialisePrequisites(){
        handler = new Handler();
        appPref = GeneralUtil.getAppPref(interFace.getViewContext());
        editor = appPref.edit();
        calendar = Calendar.getInstance();
        alarmManager = (AlarmManager) this.interFace.getViewContext()
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

    public void changeTrackBarProgress(){
        interFace.updateTrackBar(player.getCurrentPosition());
        if(player.isPlaying()) {
            Runnable runnable = () -> {
                changeTrackBarProgress();
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    public void playMusic(){
        isTrackPlaying = true;
        if(player!=null) {
            if (TextUtils.isEmpty(track_name)) {
                player.setOnPreparedListener(mediaPlayer -> {
                   mediaPlayer.setOnBufferingUpdateListener((mediaPlayer1, percent) -> {
                        if (percent<=99)interFace.showMusicBuffer();

                        else interFace.hideMusicBuffer();
                    });
                    interFace.setTrackDuration(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    changeTrackBarProgress();
                });
                player.setOnCompletionListener(mp -> {
                    mp.reset();
                    mp.release();
                    player = null;
                });
                track_name = "play";
            }
            else {
                player.start();
                changeTrackBarProgress();
                isTrackPlaying = true;
            }
        }else {
            startNewMusic("");
        }
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
            player.setDataSource(interFace.getViewContext(),
                    Uri.parse(trackurl));
           player.prepareAsync();
        playMusic();
        } catch (IOException e) {
            e.printStackTrace();
            //player = MediaPlayer.create(interFace.getViewContext(), R.raw.test_track);
           //player.setDataSource(trackurl);
        }
    }

    public void seekMusic(int progress){
        if(player!=null && isTrackPlaying){
            player.seekTo(progress);
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
       alarm = calendar.getTimeInMillis() - 60;

       if(appPref.contains(Constants.TRACK_URL)){
           setLullabyAlarm();
       }else {
           interFace.goToMusicSetter();
           GeneralUtil.message("Please set Song For Schedule");
       }
        GeneralUtil.message("Schedule Set to - "+ hr +":" + min);
    }

    private void setLullabyAlarm() {
        isAlarmSet = true;
        Intent alarmIntent = new Intent(interFace.getViewContext(), WakeTimeReceiver.class);
        alarmIntent.putExtra("Message", message);
        alarmIntent.putExtra("Home", alarm);
        alarmIntent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);

        pendingIntent = PendingIntent.getBroadcast(interFace.getViewContext(), REQUEST_CODE,
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
        if(ServiceUtil.isServiceAlreadyRunningAPI16(interFace.getViewContext()))
            ServiceUtil.stopService(interFace.getViewContext());
        GeneralUtil.message("Home Cancelled!");
    }

    public List<SongInfo> loadSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = interFace.getViewContext().getContentResolver().query(uri,
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
            isTrackPlaying = false;
            //interFace.updateTrackBar(0);
            player.stop();
            player.reset();
        }
    }

    public void setAlarmTone(String path) {

        if(ServiceUtil.isServiceAlreadyRunningAPI16(interFace.getViewContext()))
            ServiceUtil.stopService(interFace.getViewContext());

        if(appPref.contains(Constants.TRACK_URL)){
            editor.remove(Constants.TRACK_URL);
        }
        editor.putString(Constants.TRACK_URL, path);
        editor.commit();
        if(alarm>0)setLullabyAlarm();
    }
}
