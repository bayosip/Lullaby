package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.clocktower.lullaby.interfaces.AlarmViewInterFace;
import com.clocktower.lullaby.interfaces.FragmentListener;
import com.clocktower.lullaby.interfaces.ListItemClickListener;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.present.AlarmPresenter;
import com.clocktower.lullaby.view.Fragments.AlarmSetterFragment;
import com.clocktower.lullaby.view.Fragments.BaseFragment;
import com.clocktower.lullaby.view.Fragments.FragmentPageAdapter;
import com.clocktower.lullaby.view.Fragments.MusicSelectorDialog;
import com.clocktower.lullaby.view.Fragments.TrackSetterFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Alarm extends AppCompatActivity implements AlarmViewInterFace {

    private ViewPager pager;
    private FloatingActionButton fab;
    private int flag;
    private FragmentPageAdapter adapter;
    private List<BaseFragment> fragmentList;
    private MediaPlayer mp;
    private TrackSetterFragment trackFrag;
    private AlarmSetterFragment alarmFrag;
    private MusicSelectorDialog musicSelectorDialog;
    private AlarmPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisePrequisites();
        initialiseWidgets();
    }

    private void initialisePrequisites(){
        presenter = new AlarmPresenter(this);
        fragmentList = new LinkedList<>();
        alarmFrag = AlarmSetterFragment.getInstance();
        trackFrag = TrackSetterFragment.getInstance();
        musicSelectorDialog  = MusicSelectorDialog.getInstance();

        fragmentList.add(alarmFrag);
        fragmentList.add(trackFrag);

        adapter = new FragmentPageAdapter(getSupportFragmentManager(),  fragmentList);
        mp = MediaPlayer.
    }



    private void initialiseWidgets(){
        pager = findViewById(R.id.pager);
        fab = findViewById(R.id.buttonMusicContent);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (flag){
                    case Constants.SET_ALARM_TRACK_FLAG:
                        pager.setCurrentItem(1, true);
                        break;
                    case Constants.TRACK_SELECTOR_FLAG:

                        break;
                }
            }
        });
        pager.addOnPageChangeListener(pageChangeListener);
        pager.setAdapter(adapter);
        musicPlayerThread();
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (adapter.getPageTitle(position).equals(Constants.ALARM_SETTER)){
                fab.setImageResource(R.drawable.ic_audio_24dp);
                flag = Constants.SET_ALARM_TRACK_FLAG;

            }else if(adapter.getPageTitle(position).equals(Constants.MUSIC_SELECTOR)){
                fab.setImageResource(R.drawable.ic_queue_music_24dp);
                flag = Constants.TRACK_SELECTOR_FLAG;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void accessFilesFromPhone(){
            Dexter.withActivity(Alarm.this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {

                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            List<File> files = presenter.retrieveAllAudioFilesFromPhone(getExternalFilesDir());
                            musicSelectorDialog.show(getSupportFragmentManager(), files);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                                 PermissionToken token) {

                        }
                    }).check();
        }



    @Override
    public void onMusicTrackClick(int position) {

    }

    @Override
    public void setAlarm() {

    }

    @Override
    public void stopAlarm() {

    }

    @Override
    public void playOrPauseMusic() {
        if (!mp.isPlaying()){
            mp.start();
            trackFrag.changePlayButtonRes(R.drawable.ic_pause_24dp);
        }else {
            mp.pause();
            trackFrag.changePlayButtonRes(R.drawable.ic_play_arrow_24dp);
        }
    }

    private Thread musicPlayerThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp!=null){
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        trackFrag.getHandler().sendMessage(msg);
                        Thread.sleep(1000);
                    }catch (InterruptedException ie){

                    }
                }
            }
        });
        return thread;
    }

    @Override
    public void setAlarmMusic() {

    }

    @Override
    public void seekMusicToPosition(long time) {

    }

    @Override
    public Context getListenerContext() {
        return getApplicationContext();
    }


    @Override
    public void retrieveAllAudioFilesFromPhone(List<File> audioFiles) {

    }

    @Override
    public void retrieveAllMusicFilesFromPhone(List<SongInfo> audioFiles) {

    }
}
