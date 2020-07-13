package com.clocktower.lullaby.view.fragments.home;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.AudioPlayerView;

import at.markushi.ui.CircleButton;

public abstract class AbstractAudioViewFragment extends BaseFragment implements AudioPlayerView, Button.OnClickListener {

    private static final String TAG = "AudioViewFragment";
    protected SeekBar trackBar;
    protected TextView songName, songTime;
    protected CircleButton play_pause;
    protected boolean isPlayClicked = false;
    protected boolean isAudioLoaded = false;

    @Override
    public void calibrateTrackBarForMusic(int duration) {
        trackBar.setMax(duration);
        calculateTime(duration);
    }

    protected void calculateTime(int milliseconds) {
        int tru_secs = milliseconds/1000;
        int min = tru_secs/60;
        int sec = tru_secs%60;
        String seperator = ":";
        if (sec<=9)seperator = ":0";
        String time = min + seperator + sec;
        songTime.setText(time);
    }

    @Override
    public void setTrackBarProgress(int progress) {
        Log.w(TAG, "setTrackBarProgress: " + progress );
        trackBar.setProgress(progress);
        calculateTime(progress);
    }

    @Override
    public void selectMusic(String song) {
        isAudioLoaded = true;
        songName.setText(song);
    }

    @Override
    public void changePlayButtonRes(int resID) {
        play_pause.setImageResource(resID);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()== R.id.buttonPlayPause) {
            listener.playOrPauseMusic(getChildFragmentManager(), isPlayClicked);
            isPlayClicked = !isPlayClicked;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && listener.musicPlaying()) {
            listener.seekMusicToPosition(progress);
            seekBar.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
