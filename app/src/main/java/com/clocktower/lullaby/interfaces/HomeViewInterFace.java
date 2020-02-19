package com.clocktower.lullaby.interfaces;

import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.view.activities.Home;

import java.io.File;
import java.util.List;

public interface HomeViewInterFace extends FragmentListener{

    void setTrackBarForMusic(int duration);
    void goToMusicSetter();
    Home getActivity();
    void updateBlogWith(Post post);
    void updatePostLikesCount(String id, int count);
    void updateLikeBtnImg(String id, boolean exists);
    void updatePostComments(Comments comments);
    void updatePostCommentsCount(String postID, int count);
}