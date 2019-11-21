package com.clocktower.lullaby.present;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.clocktower.lullaby.interfaces.AlarmViewInterFace;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlarmPresenter {

    AlarmViewInterFace interFace;
    List<File> fileList;

    public AlarmPresenter(AlarmViewInterFace interFace) {
        this.interFace = interFace;
        fileList = new ArrayList<>();
    }

    public List<File> retrieveAllAudioFilesFromPhone(File file){

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

        return fileList;
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
