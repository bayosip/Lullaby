package com.clocktower.lullaby.view.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clocktower.lullaby.R;

import at.markushi.ui.CircleButton;

public class TrackSetterFragment extends BaseFragment {

    private ImageView musicArt;
    private CircleButton play_pause;
    private SeekBar trackBar;
    private Button setMusic;
    private TextView songName;


    public static TrackSetterFragment getInstance(){
        TrackSetterFragment fragment = new TrackSetterFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_selector, container);
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
        play_pause = view.findViewById(R.id.buttonPlayPause);
        trackBar = view.findViewById(R.id.seekMusic);
        setMusic = view.findViewById(R.id.buttonSetAlarmMusic);

        trackBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    listener.seekMusicToPosition(progress);
                    trackBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        play_pause.setOnClickListener(btnListener);
    }

    Button.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            listener.playOrPauseMusic();
        }
    };

    public void changePlayButtonRes(int resID){
        play_pause.setImageResource(resID);
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;
            trackBar.setProgress(currentPosition);
        }
    };

    public Handler getHandler() {
        return handler;
    }
}
