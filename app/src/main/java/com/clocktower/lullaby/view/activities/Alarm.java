package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.clocktower.lullaby.interfaces.AlarmViewInterFace;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.present.AlarmPresenter;
import com.clocktower.lullaby.view.fragments.AlarmPageAdapter;
import com.clocktower.lullaby.view.fragments.AlarmSetterFragment;
import com.clocktower.lullaby.view.fragments.BaseFragment;
import com.clocktower.lullaby.view.fragments.MusicSelectorDialog;
import com.clocktower.lullaby.view.fragments.TrackSetterFragment;
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
    private AlarmPageAdapter adapter;
    private List<BaseFragment> fragmentList;
    private MediaPlayer mp;
    private TextView title;
    private TrackSetterFragment trackFrag;
    private AlarmSetterFragment alarmFrag;
    private MusicSelectorDialog musicSelectorDialog;
    private AlarmPresenter presenter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisePrequisites();
        setupActionBar();

        initialiseWidgets();
    }

    private void initialisePrequisites() {
        presenter = new AlarmPresenter(this);
        fragmentList = new LinkedList<>();
        alarmFrag = AlarmSetterFragment.getInstance();
        trackFrag = TrackSetterFragment.getInstance();
        musicSelectorDialog = MusicSelectorDialog.getInstance();

        fragmentList.add(alarmFrag);
        fragmentList.add(trackFrag);

        adapter = new AlarmPageAdapter(getSupportFragmentManager(), fragmentList);
        //mp = MediaPlayer.
    }

    private void setupActionBar() {
        toolbar = findViewById(R.id.appbar_alarm);
        title = findViewById(R.id.alarm_toolbar_name);
    }


    private void setUpAlarmActionBar() {
        // Get the ActionBar here to configure the way it behaves.
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)
        ab.setDisplayShowHomeEnabled(false); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(false);
    }

    private void setupMusicActionBar() {

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();

        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)
    }

    private void initialiseWidgets() {
        setupActionBar();
        pager = findViewById(R.id.page_container);
        fab = findViewById(R.id.buttonMusicContent);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (flag) {
                    case Constants.SET_ALARM_TRACK_FLAG:
                        pager.setCurrentItem(1);
                        //musicPlayerThread();
                        break;
                    case Constants.TRACK_SELECTOR_FLAG:
                        accessFilesFromPhone();
                        break;
                }
            }
        });

        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(pageChangeListener);

    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (adapter.getPageTitle(position).equals(Constants.ALARM_SETTER)) {
                fab.setImageResource(R.drawable.ic_audio_24dp);
                flag = Constants.SET_ALARM_TRACK_FLAG;
                setUpAlarmActionBar();
                title.setText(Constants.ALARM_SETTER);

            } else if (adapter.getPageTitle(position).equals(Constants.MUSIC_SELECTOR)) {
                fab.setImageResource(R.drawable.ic_queue_music_24dp);
                flag = Constants.TRACK_SELECTOR_FLAG;
                setupMusicActionBar();
                title.setText(Constants.MUSIC_SELECTOR);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void accessFilesFromPhone() {
        Dexter.withActivity(Alarm.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        List<File> files = presenter.
                                retrieveAllAudioFilesFromPhone(getExternalFilesDir(Environment.DIRECTORY_MUSIC));
                        if (files != null)
                            musicSelectorDialog.show(getSupportFragmentManager(), files);
                        else GeneralUtil.showAlertMessage(Alarm.this, "Error!",
                                "No Audio Files Found");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onMusicTrackClick(int position) {

    }

    @Override
    public void setAlarm(int hour, int minute) {
        presenter.setAlarm(hour, minute);
    }

    @Override
    public void stopAlarm() {

    }

    @Override
    public void playOrPauseMusic() {
        if (!mp.isPlaying()) {
            mp.start();
            trackFrag.changePlayButtonRes(R.drawable.ic_pause_24dp);
        } else {
            mp.pause();
            trackFrag.changePlayButtonRes(R.drawable.ic_play_arrow_24dp);
        }
    }

    public Thread musicPlayerThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        trackFrag.getHandler().sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {

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

    public Fragment getCurrentFragmentInView() {
        return getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" +
                        R.id.page_container + ":" + pager.getCurrentItem());
    }

    @Override
    public void onBackPressed() {
        if ((getCurrentFragmentInView() instanceof AlarmSetterFragment))
            GeneralUtil.exitApp(Alarm.this);
        else pager.setCurrentItem(0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}