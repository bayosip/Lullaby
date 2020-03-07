package com.clocktower.lullaby.view.fragments.home;

import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;

public class FullscreenFragment extends BaseFragment implements View.OnClickListener {

    private static final String SEEK_INFO = "Seek";
    private MediaController mediaController;
    VideoView video;
    ImageButton playVideoBtn;
    ImageButton exitFullscreen;
    ContentLoadingProgressBar buffering;
    String url;
    int seekTo;

    public static FullscreenFragment getInstance(String url, int resumeFrom){
        FullscreenFragment fragment = new FullscreenFragment();
        Bundle extra =  new Bundle();
        extra.putString(Constants.URI_DATA, url);
        extra.putInt(SEEK_INFO, resumeFrom);
        fragment.setArguments(extra);

        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = getArguments().getString(Constants.URI_DATA);
        seekTo = getArguments().getInt(SEEK_INFO);
        initialiseWidgets(view);
    }

    public void setMediaController(MediaController mediaController){
        this.mediaController = mediaController;
    }

    private void initialiseWidgets(View view) {
        video = view.findViewById(R.id.videoFullscreen);
        video.setOnClickListener(this);
        playVideoBtn = view.findViewById(R.id.buttonPlayVideo);
        playVideoBtn.setOnClickListener(this);
        buffering = view.findViewById(R.id.progress_video_loading);
        buffering.show();
        exitFullscreen = view.findViewById(R.id.buttonFullScreen);
        exitFullscreen.setOnClickListener(this);

        if (mediaController!=null){
            video.setMediaController(mediaController);
            mediaController.setAnchorView(video);
        }
        playVideoBtn.setVisibility(View.GONE);
        snapToFullScreen();
        playSelectedVideoFrom(url, seekTo);

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    buffering.show();
                    playSelectedVideoFrom(url, seekTo);
                }else {
                    seekTo = video.getCurrentPosition();
                    playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                    video.pause();
                }
                break;
            case R.id.videoFullscreen:
                // Crashlytics.getInstance().crash();
                if (video.isPlaying()){
                    playVideoBtn.setVisibility(View.VISIBLE);
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                }
                break;
            case R.id.buttonFullScreen:
                snapOutOfFullscreen();
                break;
    }
}

    private void playSelectedVideoFrom(String url, final int seekTo) {
        try {
            Uri uri = Uri.parse(url);
            video.setVideoURI(uri);
            video.setOnPreparedListener(mediaPlayer -> {
                buffering.show();
                mediaPlayer.setLooping(false);
                video.start();
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(seekTo);
                    buffering.hide();
                }
            });

            video.setOnCompletionListener(mediaPlayer -> {
                mediaPlayer.reset();
                mediaPlayer.release();
                playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                playVideoBtn.setVisibility(View.VISIBLE);
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
        video.requestFocus();
    }

    private void snapOutOfFullscreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getListenerContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = (int)(255*metrics.density);
        params.leftMargin = 0;
        video.setLayoutParams(params);
        listener.getListenerContext().onBackPressed();
    }

    private void snapToFullScreen(){
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getListenerContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        video.setLayoutParams(params);
    }



    @Override
    public void onStop() {
        super.onStop();
        if (video!=null && video.isPlaying()){
            video.stopPlayback();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(video.isPlaying())video.stopPlayback();
        listener.restoreViewsAfterLeavingCommentSection();
    }
}
