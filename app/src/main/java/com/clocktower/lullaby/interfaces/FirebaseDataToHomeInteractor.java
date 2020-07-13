package com.clocktower.lullaby.interfaces;

import android.net.Uri;

import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.SongInfo;
import com.google.android.gms.tasks.Task;

public interface FirebaseDataToHomeInteractor {
   void checkIfUserIsAdmin(String uuid);
   void firstPageFirstLoad();
   void loadMorePost();
   void likePost(final String postId);
   void getLikePostForPost(final String postId);
   void getNumberOfComments(final String postID);
   void loadCommentsForPostWithID(String postID);
   void makeCommentOnPostWithID(String postID, String comment_message);
   void saveMediaPostAttachmentInStorage(final Post newPost);
   void storePostDataInFirestore(Task<Uri> task, Post newPost);
   void removeListenerRegistration();

   void loadMusicTracks();
   void saveAudioInStorage(final SongInfo newAudio);

}
