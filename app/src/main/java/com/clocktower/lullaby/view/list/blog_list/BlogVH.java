package com.clocktower.lullaby.view.list.blog_list;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.FragmentListener;
import com.clocktower.lullaby.model.CozaBlog;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.koushikdutta.ion.Ion;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BlogVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = "BlogVH";
    VideoView video;
    TextView elaspedTime, postTitle;
    TextView likeCount, commentCount;
    ImageView like, comment, imgPost;
    ImageButton playVideoBtn, fullscreen;
    ConstraintLayout mediaView, videoLayout;
    ContentLoadingProgressBar buffering;
    String url;
    String postId;
    Bitmap postBitMap;
    private FragmentListener listener;
    private String title;
    private boolean isPlayClicked = false;

    public BlogVH(@NonNull View itemView) {
        super(itemView);
        initialiseWidgets(itemView);
    }

    private void initialiseWidgets(View v){
        videoLayout = v.findViewById(R.id.layoutVideoPost);
        video = v.findViewById(R.id.videoViewPost);
        imgPost = v.findViewById(R.id.imageViewPost);
        elaspedTime = v.findViewById(R.id.textElaspedTime);
        postTitle = v.findViewById(R.id.textPostTitle);
        likeCount = v.findViewById(R.id.text_blog_like_count);
        commentCount =v.findViewById(R.id.text_post_comment_count);
        like = v.findViewById(R.id.post_like_btn);
        like.setOnClickListener(this);
        comment = v.findViewById(R.id.post_comment_icon);
        comment.setOnClickListener(this);
        playVideoBtn = v.findViewById(R.id.buttonPlayVideo);
        playVideoBtn.setOnClickListener(this);
        buffering = v.findViewById(R.id.progress_video_loading);
        buffering.hide();
        mediaView = v.findViewById(R.id.mediaView);
        fullscreen = v.findViewById(R.id.buttonExitFullScreen);
        fullscreen.setOnClickListener(this);
        imgPost.setOnClickListener(this);

    }

    public void setMediaController(MediaController mediaController){
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
    }

    public void resetViews(){
        mediaView.addView(null);

        mediaView.addView(imgPost);
        mediaView.addView(videoLayout);
    }

    public void setBlogItems(List<CozaBlog> posts){
        postId = posts.get(getAdapterPosition()).getPost().postId;
        url = posts.get(getAdapterPosition()).getPost().getUrl();
        Log.d(TAG, "setBlogItems: " + url);
        listener.updateLikesCount(postId);
        listener.updateCommentCount(postId);
        likeCount.setText(listener.getViewContext().getString(R.string.like_count,
                posts.get(getAdapterPosition()).getLikeCount()));
        commentCount.setText(listener.getViewContext().getString(R.string.comment_count,
                posts.get(getAdapterPosition()).getCommentCount()));
        if (posts.get(getAdapterPosition()).isLiked()){
            like.setImageResource(R.drawable.ic_like_on_24dp);
        }else {
            like.setImageResource(R.drawable.ic_like_off_24dp);
        }
        title = posts.get(getAdapterPosition()).getPost().getTitle();
        postTitle.setText(title);
        //Check Media type
        long type =posts.get(getAdapterPosition()).getPost().getMediaType();
        if (type>0){
            mediaView.setVisibility(View.VISIBLE);
            if(type ==1){
                imgPost.setVisibility(View.VISIBLE);
                loadAndResizeImage();
            }else if (type==2) {
                videoLayout.setVisibility(View.VISIBLE);
                playSelectedVideoFrom(url);
                snapOutOfFullscreen();
            }
        }else mediaView.setVisibility(View.GONE);

        try {
            long millisecond = posts.get(getAdapterPosition()).getPost().getTimeStamp().getTime();
            String dateString = DateFormat.format("MMM dd yyyy, HH:mm:ss", new Date(millisecond)).toString();
            elaspedTime.setText(dateString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    isPlayClicked = true;
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    buffering.show();
                    playSelectedVideoFrom(url);
                    snapOutOfFullscreen();
                }else {
                    video.pause();
                }
                break;
            case R.id.post_like_btn:
                listener.likeThisPost(postId);
                like.setImageResource(R.drawable.ic_like_on_24dp);
                break;
            case R.id.post_comment_icon: case R.id.text_post_comment_count:
                listener.openCommentSectionOnPostWithId(postId, title);
                break;
            case R.id.buttonExitFullScreen:
                if(video.isPlaying()) {
                    video.pause();
                    playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                    playVideoBtn.setVisibility(View.VISIBLE);
                    listener.makeVideoFullScreen(url, video.getCurrentPosition());
                }
                break;
            case R.id.imageViewPost:
                if(postBitMap!= null)
                    listener.makeFullPicture(postBitMap);
                break;
        }
    }

    private void playSelectedVideoFrom(String url){

        try {
            Uri uri = Uri.parse(url);
            video.setVideoURI(uri);
            video.setOnPreparedListener(mediaPlayer -> {

                mediaPlayer.setOnBufferingUpdateListener((mediaPlayer12, percent) -> {
                    if (isPlayClicked && (percent<=99))
                        buffering.show();
                    else buffering.hide();
                });

                mediaPlayer.setLooping(false);
                mediaPlayer.start();
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                if (!isPlayClicked) {
                    mediaPlayer.seekTo(100);
                    mediaPlayer.setOnSeekCompleteListener(mediaPlayer1 -> {
                        mediaPlayer1.pause();
                    });
                }else {
                    if (mediaPlayer.isPlaying()) buffering.hide();
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //video.requestFocus();
        video.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.reset();
            mediaPlayer.release();
            playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
            playVideoBtn.setVisibility(View.VISIBLE);
            if(isPlayClicked){
                isPlayClicked = false;
                playSelectedVideoFrom(url);
            }
        });
    }

    public void setListener(FragmentListener listener) {
        this.listener = listener;
        HashTagHelper mTextHashTagHelper = HashTagHelper.Creator
                .create(listener.getViewContext().getResources().getColor(R.color.lightColor), null);
        mTextHashTagHelper.handle(postTitle);
    }

    private void snapOutOfFullscreen(){
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getViewContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = (int)(255*metrics.density);
        params.leftMargin = 0;
        video.setLayoutParams(params);
    }

    private void loadAndResizeImage(){
        imgPost.setImageBitmap(null);
        try {
            Bitmap result = Ion.with(listener.getViewContext())
                    .load(url)
                    .withBitmap()
                    .asBitmap()
                    .get();
            postBitMap = result;

            imgPost.setImageBitmap(GeneralUtil.resizeBitmap(result, imgPost.getMeasuredWidth()));
        } catch (ExecutionException e) {
            e.printStackTrace();
            imgPost.setImageResource(R.drawable.ic_image_24dp);
        } catch (InterruptedException e) {
            e.printStackTrace();
            imgPost.setImageResource(R.drawable.ic_image_24dp);
        }
    }
}