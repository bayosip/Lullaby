package com.clocktower.lullaby.view.fragments.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.clocktower.lullaby.R;

import at.markushi.ui.CircleButton;

public class TrackSetterFragment extends AbstractAudioViewFragment {

    private static final String TAG = "TrackSetterFragment";
    private ImageView musicArt;
    private Button setMusic;
    private ContentLoadingProgressBar progressBar;


    public static TrackSetterFragment getInstance(){
        TrackSetterFragment fragment = new TrackSetterFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_selector, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseWidgets(view);
    }

    private void initialiseWidgets(View view){
        musicArt = view.findViewById(R.id.imageTrackImg);
        songName = view.findViewById(R.id.textSongName);
        songTime = view.findViewById(R.id.textDuration);
        play_pause = view.findViewById(R.id.buttonPlayPause);
        trackBar = view.findViewById(R.id.seekMusic);
        setMusic = view.findViewById(R.id.buttonSetAlarmMusic);
        progressBar = view.findViewById(R.id.musicLoading);
        progressBar.hide();

        trackBar.setOnSeekBarChangeListener(this);

        play_pause.setOnClickListener(this);
        setMusic.setOnClickListener(btnListener);
    }

    Button.OnClickListener btnListener = view -> {
        switch (view.getId()){
            case R.id.buttonSetAlarmMusic:
                listener.setAlarmMusic();
                break;
        }
    };

    public void show(){
        progressBar.show();
    }

    public void hide(){
        progressBar.hide();
    }
}
