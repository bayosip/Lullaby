package com.clocktower.lullaby.view.fragments.home;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.labo.kaji.fragmentanimations.PushPullAnimation;

public class FullscreenFragment extends BaseFragment implements View.OnClickListener {

    private static final String SEEK_INFO = "Seek";
    private static final long DURATION = 1000;
    private static final String BITMAP = "Bitmap";
    private static final String TYPE = "Type";
    private MediaController mediaController;
    View videoPostView, imgPostView;
    VideoView video;
    ImageButton playVideoBtn, exitFullscreen, cancel;
    ImageView fullImg;
    ContentLoadingProgressBar buffering;
    Bitmap fullBitmap;
    String url;
    int seekTo;
    Long mediaType;

    public static FullscreenFragment getVideoInstance(String url, int resumeFrom){
        FullscreenFragment fragment = new FullscreenFragment();
        Bundle extra =  new Bundle();
        extra.putString(Constants.URI_DATA, url);
        extra.putInt(SEEK_INFO, resumeFrom);
        extra.putLong(TYPE, Constants.VIDEO);
        fragment.setArguments(extra);

        return  fragment;
    }

    public static FullscreenFragment getImgInstance (Bitmap bitmap){
        FullscreenFragment fragment = new FullscreenFragment();
        Bundle extra =  new Bundle();
        extra.putParcelable(BITMAP, bitmap);
        extra.putLong(TYPE, Constants.IMAGE);
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
        mediaType = getArguments().getLong(TYPE);
        if ( mediaType== Constants.VIDEO) {
            url = getArguments().getString(Constants.URI_DATA);
            seekTo = getArguments().getInt(SEEK_INFO);
        }else {
            fullBitmap = (Bitmap)getArguments().getParcelable(BITMAP);
        }
        initialiseWidgets(view);
    }

    public void setMediaController(MediaController mediaController){
        this.mediaController = mediaController;
    }

    private void initialiseWidgets(View view) {
        videoPostView = view.findViewById(R.id.layoutFullVid);
        imgPostView = view.findViewById(R.id.layoutFullImg);
        video = view.findViewById(R.id.videoFullscreen);
        video.setOnClickListener(this);
        playVideoBtn = view.findViewById(R.id.buttonPlayVideo);
        playVideoBtn.setOnClickListener(this);
        buffering = view.findViewById(R.id.progress_video_loading);
        buffering.show();
        exitFullscreen = view.findViewById(R.id.buttonExitFullScreen);
        exitFullscreen.setOnClickListener(this);
        cancel = view.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(this);
        fullImg = view.findViewById(R.id.imgViewFull);

        if (mediaType == 2) {
            videoPostView.setVisibility(View.VISIBLE);
            if (mediaController != null) {
                video.setMediaController(mediaController);
                mediaController.setAnchorView(video);
            }
            playVideoBtn.setVisibility(View.GONE);
            snapToFullScreen();
            playSelectedVideoFrom(url, seekTo);
        }else {
            imgPostView.setVisibility(View.VISIBLE);

            fullImg.setImageBitmap(GeneralUtil.resizeBitmap(fullBitmap, fullImg.getMeasuredWidth()));
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return PushPullAnimation.create(PushPullAnimation.LEFT, enter, DURATION);
        } else {
            return PushPullAnimation.create(PushPullAnimation.RIGHT, enter, DURATION);
        }
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
                // Crashlytics.getVideoInstance().crash();
                if (video.isPlaying()){
                    playVideoBtn.setVisibility(View.VISIBLE);
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                }
                break;
            case R.id.buttonExitFullScreen: case R.id.btn_cancel:
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
        listener.getViewContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = (int)(255*metrics.density);
        params.leftMargin = 0;
        video.setLayoutParams(params);
        listener.getViewContext().onBackPressed();
    }

    private void snapToFullScreen(){
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getViewContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
