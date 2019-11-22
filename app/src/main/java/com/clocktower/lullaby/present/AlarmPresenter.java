package com.clocktower.lullaby.present;

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

    AlarmViewInterFace interFace;

    Calendar calendar;

    public AlarmPresenter(AlarmViewInterFace interFace) {
        this.interFace = interFace;
    }

    public void setAlarm(int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String min = String.valueOf(minute);
        if(minute<10)min = "0"+minute;
        GeneralUtil.message("Alarm Set to - "+ hour +":" + min);
    }

    public List<File> retrieveAllAudioFilesFromPhone(File file){
        List<File> fileList = new ArrayList<>();;
        File[] allFiles = file.listFiles();

        for (File afile: allFiles){
            if (afile.isDirectory() && !afile.isHidden()){
                retrieveAllAudioFilesFromPhone(afile);
            }else {
                if (afile.getName().endsWith(Constants.AAC)|| afile.getName().endsWith(Constants.MP3)
                        ||afile.getName().endsWith(Constants.WAV)){

                    fileList.add(afile);
                }
            }
        }

        return fileList.size()>0? fileList: null;
    }

    private void loadSongs(){
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
    }
}
