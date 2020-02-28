package com.clocktower.lullaby.view.list.blog_list;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Size;
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
import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class BlogVH extends RecyclerView.ViewHolder implements View.OnClickListener {


    VideoView video;
    TextView elaspedTime, postTitle;
    TextView likeCount, commentCount;
    ImageView like, comment, imgPost;
    ImageButton playVideoBtn, fullscreen;
    View mediaView;
    ContentLoadingProgressBar buffering;
    String url;
    String postId;
    private FragmentListener listener;
    private String title;
    private boolean isFullScreen;


    public BlogVH(@NonNull View itemView) {
        super(itemView);
        initialiseWidgets(itemView);
    }

    private void initialiseWidgets(View v){

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
        fullscreen = v.findViewById(R.id.buttonFullScreen);
        fullscreen.setOnClickListener(this);

    }

    public void setMediaController(MediaController mediaController){
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
    }

    public void setBlogItems(List<CozaBlog> posts){

        postId = posts.get(getAdapterPosition()).getPost().postId;
        listener.updateLikesCount(postId);
        listener.updateCommentCount(postId);
        likeCount.setText(listener.getListenerContext().getString(R.string.like_count,
                posts.get(getAdapterPosition()).getLikeCount()));
        commentCount.setText(listener.getListenerContext().getString(R.string.comment_count,
                posts.get(getAdapterPosition()).getCommentCount()));
        if (posts.get(getAdapterPosition()).isLiked()){
            like.setImageResource(R.drawable.ic_like_on_24dp);
        }else {
            like.setImageResource(R.drawable.ic_like_off_24dp);
        }
        title = posts.get(getAdapterPosition()).getPost().getTitle();
        postTitle.setText(title);

        mediaView.setVisibility(posts.get(getAdapterPosition()).getPost().getMediaType()==0?
                View.GONE: View.VISIBLE);
        video.setVisibility(posts.get(getAdapterPosition()).getPost().getMediaType()==2?
                View.VISIBLE: View.GONE);

        if(posts.get(getAdapterPosition()).getPost().getMediaType()==2) {
            playVideoBtn.setVisibility(View.VISIBLE);
            fullscreen.setVisibility(View.VISIBLE);
        }
        url = posts.get(getAdapterPosition()).getPost().getUrl();

        if(posts.get(getAdapterPosition()).getPost().getMediaType()==1)
            Ion.with(imgPost)
                    .placeholder(R.drawable.ic_image_24dp)
                    .load( url);
        try {
            long millisecond = posts.get(getAdapterPosition()).getPost().getTimeStamp().getTime();
            String dateString = DateFormat.format("MMM dd yyyy, HH:mm:ss", new Date(millisecond)).toString();
            elaspedTime.setText(dateString);
        }catch (Exception e){
            e.printStackTrace();
        }
        Bitmap bitmap = null;
        try {
            bitmap = GeneralUtil.retriveVideoFrameFromVideo(url);
            imgPost.setImageBitmap(bitmap);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            imgPost.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    imgPost.setVisibility(View.GONE);
                    buffering.show();
                    playSelectedVideoFrom(url);
                    snapOutOfFullscreen();
                }else {
                    video.pause();
                }
                break;
            case R.id.post_like_btn:
                listener.likeThisPost(postId);
                break;
            case R.id.post_comment_icon:
                listener.openCommentSectionOnPostWithId(postId, title);
                break;
            case R.id.buttonFullScreen:
                if(video.isPlaying()) {
                    video.pause();
                    listener.makeVideoFullScreen(url, video.getCurrentPosition());
                }
                break;
        }
    }

    private void playSelectedVideoFrom(String url){

        try {
            Uri uri = Uri.parse(url);
            video.setVideoURI(uri);
            video.setOnPreparedListener(mediaPlayer -> {
                buffering.show();
                mediaPlayer.setLooping(false);
                video.start();
                if(mediaPlayer.isPlaying())buffering.hide();
            });

            if (video.isPlaying()){
                buffering.hide();
            }

            video.setOnCompletionListener(mediaPlayer -> {
                mediaPlayer.reset();
                mediaPlayer.release();
                playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                playVideoBtn.setVisibility(View.VISIBLE);
                imgPost.setVisibility(View.VISIBLE);
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
        //video.requestFocus();
    }

    public void setListener(FragmentListener listener) {
        this.listener = listener;
    }

    private void snapOutOfFullscreen(){
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getListenerContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = (int)(255*metrics.density);
        params.leftMargin = 0;
        video.setLayoutParams(params);
    }
}