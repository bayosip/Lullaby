package com.clocktower.lullaby.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.widget.MediaController;

import androidx.fragment.app.FragmentManager;

import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.SongInfo;

public interface FragmentListener extends BlogItemClickListener {

    boolean isUserAdmin();
    void setAlarm(int hour, int minute);
    void stopAlarm();
    void playSelectedAudio(SongInfo audio);
    void playOrPauseMusic(FragmentManager manager, boolean isClicked);
    void changePlayButtonIcon(int resID);
    void accessFilesFromPhone();
    void showAudioFromDevice();
    void stopMusic();
    void setAlarmMusic();
    void seekMusicToPosition(int position);
    void updateTrackBar(int time);
    void setTrackDuration(int duration);
    boolean musicPlaying();
    MediaController getVideoMediaController();
    void startLoadingPostsFromFirebase();

    void loadMorePost();

    void clearBlogList();

    //Post upload Interface Method

    //Blog interface functions
    void updateLikesCount(String postID);
    void updateCommentCount(String postId);
    void openCommentSectionOnPostWithId(String postID, String title);
    void retrieveAllComments(String postId);
    void postACommentOnPostWithId(String postId, String msg);
    void restoreViewsAfterLeavingCommentSection();
    void removeToolbars();

    void saveNewPostInDB(Post post, long type);
    void saveNewAudioInDb(SongInfo audio);

    void updateTrackList(SongInfo audio);

    void showMusicBuffer();
    void hideMusicBuffer();
}
