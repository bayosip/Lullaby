package com.clocktower.lullaby.view.list.blog_list;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.Post;

import java.util.List;

public class BlogVH extends RecyclerView.ViewHolder implements View.OnClickListener {


    VideoView video;
    TextView elaspedTime, postTitle;
    TextView likeCount, commentCount;
    ImageView like, comment;
    ImageButton playVideoBtn;
    ContentLoadingProgressBar buffering;
    List<Post> posts;

    public BlogVH(@NonNull View itemView) {
        super(itemView);
        initialiseWidgets(itemView);
    }

    private void initialiseWidgets(View v){

        video = v.findViewById(R.id.videoViewPost);
        video.setOnClickListener(this);
        elaspedTime = v.findViewById(R.id.textElaspedTime);
        postTitle = v.findViewById(R.id.textPostTitle);
        likeCount = v.findViewById(R.id.text_blog_like_count);
        commentCount =v.findViewById(R.id.text_post_comment_count);
        like = v.findViewById(R.id.post_like_btn);
        comment = v.findViewById(R.id.post_comment_icon);
        playVideoBtn = v.findViewById(R.id.buttonPlayVideo);
        playVideoBtn.setOnClickListener(this);
        buffering = v.findViewById(R.id.progress_video_loading);
    }


    public void setBlogItems(List<Post> posts){
        this.posts = posts;
        postTitle.setText(posts.get(getAdapterPosition()).getPostTitle());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPlayVideo:
               if (!video.isPlaying()) {
                   playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                   playVideoBtn.setVisibility(View.GONE);
                   buffering.show();
                   playSelectedVideoFrom(posts.get(getAdapterPosition()).getVideoURL());
               }else {
                   playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                   playVideoBtn.setVisibility(View.VISIBLE);
                   video.pause();
               }
               break;
            case R.id.videoViewPost:
                playVideoBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void playSelectedVideoFrom(String url){

        try {
            Uri uri = Uri.parse(url);
            video.setVideoURI(uri);
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    buffering.hide();
                    mediaPlayer.setLooping(true);
                    video.start();
                }
            });
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                    playVideoBtn.setVisibility(View.VISIBLE);
                }
            });

        }catch (Exception ex){

        }
        video.requestFocus();

    }
}
