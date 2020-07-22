package com.clocktower.lullaby.presenter;

import android.Manifest;
import android.net.Uri;
import android.util.Log;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.FirebaseDataToHomeInteractor;
import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.Users;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

abstract public class FirebaseToHomePresenter implements FirebaseDataToHomeInteractor {

    private static final String TAG = "FirebaseToHomePresenter";
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private HomeViewInterFace interFace;
    private DocumentSnapshot lastVisible;
    private Home activity;

    private Boolean isFirstPageFirstLoad = true;
    private StorageReference storage;
    private ListenerRegistration listenerRegistration;

    public FirebaseToHomePresenter(HomeViewInterFace interFace) {
        this.interFace = interFace;
        this.activity = interFace.getViewContext();
        initialisePrequisites();
    }

    private void initialisePrequisites(){
        user = FirebaseUtil.getmAuth().getCurrentUser();
        firestore = FirebaseUtil.getFirestore();
        storage = FirebaseUtil.getStorage();
        //Source source = Source.CACHE;
    }

    @Override
    public void checkIfUserIsAdmin(String uuid) {
        DocumentReference docRef = firestore.collection(Constants.USERS).document(uuid);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
           Users user = documentSnapshot.toObject(Users.class);
           if(user!=null) {
               Log.w(TAG, "checkIfUserIsAdmin: " + user.toString());
               interFace.removeBNBItemIfNoAdmin(user.isUserIsAdmin());
           }
        });

    }

    @Override
    public void firstPageFirstLoad(){
        Query firstQuery = firestore.collection(Constants.POSTS)
                .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
                .limit(5);
        listenerRegistration = firstQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (documentSnapshots!= null && !documentSnapshots.isEmpty()) {
                if (isFirstPageFirstLoad) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    interFace.clearBlogList();
                }
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String blogPostId = doc.getDocument().getId();
                        final Post blogPost = doc.getDocument().toObject(Post.class).withId(blogPostId);
                        interFace.updateBlogWith(blogPost);
                    }
                }
                isFirstPageFirstLoad = false;
            }
        });
    }

    @Override
    public void loadMorePost(){
        if(user!= null) {
            Query nextQuery = firestore.collection(Constants.POSTS)
                    .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
            listenerRegistration = nextQuery.addSnapshotListener((documentSnapshots, e) -> {
                if (documentSnapshots!= null && !documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = doc.getDocument().getId();
                            final Post blogPost = doc.getDocument().toObject(Post.class)
                                    .withId(blogPostId);
                           interFace.updateBlogWith(blogPost);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void likePost(final String postId){
        firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                .document(user.getUid()).get().addOnCompleteListener(task -> {
                    if(!task.getResult().exists()){
                        Map<String, Object> likesMap = new HashMap<>();
                        likesMap.put("TimeStamp", FieldValue.serverTimestamp());
                        firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                                .document(user.getUid()).set(likesMap);
                    }else {
                        firestore.collection("Posts/" + postId + "/Likes")
                                .document(user.getUid()).delete();
                    }
                });
    }

    @Override
    public void getLikePostForPost(final String postId){
        firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                .addSnapshotListener((documentSnapshots, e) -> {
                    int count =0;
                        if (documentSnapshots!= null && !documentSnapshots.isEmpty()) {
                            count = documentSnapshots.size();
                        }
                        interFace.updatePostLikesCount(postId, count);
                });

        //Get Likes
        listenerRegistration = firestore.collection("Posts/" + postId + "/Likes").document(user.getUid())
                .addSnapshotListener((documentSnapshot, e) ->{
                        if(documentSnapshot!=null)
                            interFace.updateLikeBtnImg(postId, documentSnapshot.exists());});

    }

    @Override
    public void getNumberOfComments(final String postID){
        listenerRegistration = firestore.collection(Constants.POSTS).document(postID).collection(Constants.COMMENTS)
                .addSnapshotListener((documentSnapshots, e) -> {
                    if(documentSnapshots !=null) {
                        int count = 0;
                        if (!documentSnapshots.isEmpty()) {
                            count = documentSnapshots.size();
                        }
                        interFace.updatePostCommentsCount(postID, count);
                    }
                });
    }

    @Override
    public void loadCommentsForPostWithID(String postID){
        listenerRegistration = firestore.collection("Posts/" + postID+ "/Comments")
                .addSnapshotListener((documentSnapshots, e) -> {

                    if (documentSnapshots!= null && !documentSnapshots.isEmpty()) {

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String commentId = doc.getDocument().getId();
                                Comments comments = doc.getDocument().toObject(Comments.class);
                                interFace.updatePostComments(comments);
                            }
                        }
                    }
                });
    }

    @Override
    public void makeCommentOnPostWithID(String postID, String comment_message){
        Map<String, Object> commentsMap = new HashMap<>();
        commentsMap.put("comment", comment_message);
        commentsMap.put("username", user.getDisplayName());
        if (user.getPhotoUrl()!=null)
            commentsMap.put("url", user.getPhotoUrl().toString());
        else commentsMap.put("url", "");
        commentsMap.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("Posts/" + postID+ "/Comments").add(commentsMap)
                .addOnCompleteListener(task -> {
                    if(task==null || !task.isSuccessful()){
                        GeneralUtil.message( "Error Posting Comment : " + task.getException().getMessage());
                    }
                });
    }

    //Save A Post
    @Override
    public void saveMediaPostAttachmentInStorage(final Post newPost){

        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                       storeMediaPostAttachmentInStorage(newPost);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        GeneralUtil.showAlertMessage(activity,
                                activity.getString(R.string.error),"Internal Storage Permission Denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }

    private void storeMediaPostAttachmentInStorage(final Post newPost){
        Uri file = Uri.fromFile(new File(newPost.getUrl()));
        String fileExt = newPost.getMediaType() ==1? Constants.PNG:Constants.MP4;
        final StorageReference newPostRef = storage.child(Constants.POSTS)
                .child(GeneralUtil.randomName()+ "_" + fileExt);
        final UploadTask uploadTask;
        uploadTask = newPostRef.putFile(file);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            activity.showPB();
            activity.progressPB(FirebaseUtil.calculateFileUploadProgress(taskSnapshot));
        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return newPostRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GeneralUtil.message("Post uploaded successfully!");
                storePostDataInFirestore(task, newPost);
            } else {
                activity.hidePB();
                GeneralUtil.getHandler().post(() -> {
                    task.getException().printStackTrace();
                    //viewImplementation.hidePictureLoaderBar();
                    GeneralUtil.showAlertMessage(activity,
                            activity.getString(R.string.error),
                            activity.getString(R.string.error_post_msg));
                });
            }
        });
    }

    @Override
    public void storePostDataInFirestore(Task<Uri> task, Post newPost) {
        String download_uri;
        if(task != null) {
            download_uri = task.getResult().toString();
        }else {
            download_uri = "none";
        }

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("Title", newPost.getTitle());
        postMap.put("Url", download_uri);
        postMap.put("MediaType", newPost.getMediaType());
        postMap.put("TimeStamp", FieldValue.serverTimestamp());

        firestore.collection(Constants.POSTS).document("PID-" +GeneralUtil.randomName())
                .set(postMap).addOnCompleteListener(task1 -> {

            if(task1.isSuccessful()){
                GeneralUtil.message("Post was added");
                activity.hidePB();
                activity.goStraightToHomePage("");
            } else {

                GeneralUtil.showAlertMessage(activity, activity.getString(R.string.error),
                        "Something Went Wrong, Please Try Again!");
            }
            activity.hidePB();
        });
    }

    @Override
    public void loadMusicTracks() {
        Query audioQuery = firestore.collection(Constants.AUDIO);
        listenerRegistration = audioQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (documentSnapshots!= null && !documentSnapshots.isEmpty()) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        SongInfo audio = doc.getDocument().toObject(SongInfo.class);
                        interFace.updateTrackList(audio);
                    }
                }
            }else {
                GeneralUtil.message("No data to load...!");
            }
        });
    }

    @Override
    public void saveAudioInStorage(SongInfo newAudio) {

        Uri file = Uri.fromFile(new File(newAudio.getUrl()));
        String fileExt = ".mp3";
        final StorageReference newAudioRef = storage.child(Constants.AUDIO)
                .child(GeneralUtil.randomName()+ "_" + fileExt);
        final UploadTask uploadTask;
        uploadTask = newAudioRef.putFile(file);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            activity.showPB();
            activity.progressPB(FirebaseUtil.calculateFileUploadProgress(taskSnapshot));
        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return newAudioRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GeneralUtil.message("Post uploaded successfully!");
                storeAudioDataInFirestore(task, newAudio);
            } else {
                activity.hidePB();
                GeneralUtil.getHandler().post(() -> {
                    task.getException().printStackTrace();
                    //viewImplementation.hidePictureLoaderBar();
                    GeneralUtil.showAlertMessage(activity,
                            activity.getString(R.string.error),
                            activity.getString(R.string.error_post_msg));
                });
            }
        });
    }

    private void storeAudioDataInFirestore(Task<Uri> task, SongInfo newAudio) {
        String download_uri;
        if(task != null) {
            download_uri = task.getResult().toString();
        }else {
            download_uri = "none";
        }

        Map<String, Object> audioMap = new HashMap<>();
        audioMap.put("SongName", newAudio.getTrackName());
        audioMap.put("Artiste", newAudio.getArtiste());
        audioMap.put("Url", download_uri);
        //audioMap.put("TimeStamp", FieldValue.serverTimestamp());

        firestore.collection(Constants.AUDIO).document("AID-" +GeneralUtil.randomName())
                .set(audioMap).addOnCompleteListener(task1 -> {

            if(task1.isSuccessful()){
                GeneralUtil.message("New audio was added");
                activity.hidePB();
                activity.goStraightToHomePage("");
            } else {

                GeneralUtil.showAlertMessage(activity, activity.getString(R.string.error),
                        "Something Went Wrong, Please Try Again!");
            }
            activity.hidePB();
        });
    }

    @Override
    public void removeListenerRegistration(){
        if(listenerRegistration!=null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

}
