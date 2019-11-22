package com.clocktower.lullaby.present;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.clocktower.lullaby.interfaces.AlarmViewInterFace;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmPresenter {

    private AlarmViewInterFace interFace;

    private AlarmManager alarmManager;
    private Calendar calendar;

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
        List<File> fileList;
        fileList = new ArrayList<>();
        File[] allFiles = file.listFiles();

        for (File afile: allFiles){
            if (afile.isDirectory() && !afile.isHidden()){
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

    public void setAlarm(int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String min = String.valueOf(minute);
        if(minute<10)min = "0"+minute;
        GeneralUtil.message("Alarm Set to - "+ hour +":" + min);
    }

    public List<SongInfo> loadSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = interFace.getListenerContext().getContentResolver().query(uri,
                null, selection, null, null);
        List<SongInfo> songInfoList = new ArrayList<>();
        if(cursor!=null){
            if(cursor.moveToFirst()){
                while (cursor.moveToNext()){
                    String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    SongInfo info = new SongInfo(songName, artist, url);
                    songInfoList.add(info);
                }
            }
            cursor.close();
        }
        return songInfoList.size() >0? songInfoList: null;
    }
}
