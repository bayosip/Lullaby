package com.clocktower.lullaby.present;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.clocktower.lullaby.interfaces.AlarmViewInterFace;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.service.WakeTimeReceiver;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

public class AlarmPresenter {

    private AlarmViewInterFace interFace;

    private AlarmManager alarmManager;
    private MediaPlayer player;
    private Calendar calendar;
    private Date date;
    private static final String DATE_FORMAT = "HH:mm";
    private static SharedPreferences.Editor editor;
    private SharedPreferences appPref;
    private String message;
    private String dateTime;
    private long alarm;
    private PendingIntent pendingIntent;
    private String trackurl;


    private static final int REQUEST_CODE = 100;
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String ALARM_TIME = "ALARM_TIME";

    public AlarmPresenter(AlarmViewInterFace interFace) {
        this.interFace = interFace;
        initialisePrequisites();
    }

    private void initialisePrequisites(){

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

    public void playMusic(String trackurl){
        try {
            if (player ==null){
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
                        player.start();
                    }
                });
            }else {
                if(!player.isPlaying())player.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean musicIsPlaying(){
        return player!=null && player.isPlaying();
    }

    public void pauseMusic(){
        if(player !=null && player.isPlaying())player.pause();
    }

    public void setAlarm(int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String min = String.valueOf(minute);
        String hr = String.valueOf(hour);
        if(minute<10)min = "0"+minute;
        if(hour<10)hr =  "0"+hour;
       dateTime = hr+":"+min;
       setGameAlarm(calendar.getTimeInMillis());
        GeneralUtil.message("Alarm Set to - "+ hour +":" + min);
    }


    public void seekMusic(int progress){
        if(player!=null)player.seekTo(progress);
    }


    private void setGameAlarm( long triggerTime) {

        Intent alarmIntent = new Intent(interFace.getListenerContext(), WakeTimeReceiver.class);
        alarmIntent.putExtra("Message", message);
        alarmIntent.putExtra("Alarm", triggerTime);
        alarmIntent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);

        pendingIntent = PendingIntent.getBroadcast(interFace.getListenerContext(), REQUEST_CODE,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);


        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void timeParser() {
// create date formatter, set time zone to UTC and parse
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        TimeZone phoneTimeZone = TimeZone.getTimeZone(TimeZone.getDefault().getID());
        formatter.setTimeZone(phoneTimeZone);
        try {
            date = formatter.parse(dateTime);
            if(appPref.contains(ALARM_TIME)){
                alarm = appPref.getLong(ALARM_TIME, 0);
                if (alarm < date.getTime()){
                    editor.remove(ALARM_TIME);
                    editor.putLong(ALARM_TIME, date.getTime());
                    editor.commit();
                    setGameAlarm(date.getTime());
                }
            }
            else {
                editor.putLong(ALARM_TIME, date.getTime());
                editor.commit();
                setGameAlarm(date.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    public void cancelAlarm(){
        alarmManager.cancel(pendingIntent);
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
}
