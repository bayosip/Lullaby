package com.clocktower.lullaby.presenter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class FirebaseToHomePresenter {

    private static final String TAG = "FirebaseToHomePresenter";
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private HomeViewInterFace interFace;
    private DocumentSnapshot lastVisible;
    private Home activity;

    private Boolean isFirstPageFirstLoad = true;
    private StorageReference storage;

    public FirebaseToHomePresenter(HomeViewInterFace interFace) {
        this.interFace = interFace;
        this.activity = interFace.getActivity();
        initialisePrequisites();
    }

    private void initialisePrequisites(){
        user = FirebaseUtil.getmAuth().getCurrentUser();
        firestore = FirebaseUtil.getFirestore();
        //Source source = Source.CACHE;
    }

    public void firstPageFirstLoad(){
        Query firstQuery = firestore.collection(Constants.POSTS).orderBy(Constants.TIMESTAMP,
                Query.Direction.DESCENDING).limit(3);
        firstQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                if (isFirstPageFirstLoad) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    interFace.clearList();
                    //blog_list.clear();
                }
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String blogPostId = doc.getDocument().getId();
                        Post blogPost = doc.getDocument().toObject(Post.class).withId(blogPostId);
                        interFace.updateBlogWith(blogPost);
                    }
                }
                isFirstPageFirstLoad = false;
            }
        });
    }

    public void likePost(final String postId){
        firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                .document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists()){
                    Map<String, Object> likesMap = new HashMap<>();
                    likesMap.put("TimeStamp", FieldValue.serverTimestamp());
                    firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                            .document(user.getUid()).set(likesMap);
                }else {
                    firestore.collection("Posts/" + postId + "/Likes")
                            .document(user.getUid()).delete();
                }
            }
        });
    }

    public void getLikePostForPost(final String postId){
        firestore.collection(Constants.POSTS).document(postId).collection(Constants.LIKES)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        int count =0;
                        if(!documentSnapshots.isEmpty()){
                            count = documentSnapshots.size();
                        }
                        interFace.updatePostLikesCount(postId, count);
                    }
                });

        //Get Likes
        firestore.collection("Posts/" + postId + "/Likes").document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        interFace.updateLikeBtnImg(postId, documentSnapshot.exists());
                    }
                });

    }

    public void getNumberOfComments(final String postID){
        firestore.collection(Constants.POSTS).document(postID).collection(Constants.COMMENTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        int count =0;
                        if(!documentSnapshots.isEmpty()){
                            count = documentSnapshots.size();
                        }
                        interFace.updatePostCommentsCount(postID, count);
                    }
                });
    }

    public void loadCommentsForPostWithID(String postID){
        firestore.collection("Posts/" + postID+ "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    interFace.updatePostComments(comments);
                                }
                            }
                        }
                    }
                });
    }

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

    public void loadMorePost(){

        if(user!= null) {

            Query nextQuery = firestore.collection(Constants.POSTS)
                    .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener((documentSnapshots, e) -> {

                if (!documentSnapshots.isEmpty()) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = doc.getDocument().getId();
                            Post blogPost = doc.getDocument().toObject(Post.class).withId(blogPostId);
                            interFace.updateBlogWith(blogPost);
                        }
                    }
                }

            });
        }
    }

    public void getPostsFromFirebase(){
        firestore.collection(Constants.POSTS).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if(queryDocumentSnapshots!=null){
                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()) {
                    // try {
                    Log.i(TAG, "onEvent: " +doc.getDocument().getData().get("MediaType").getClass().getSimpleName());
                    Post aPost = doc.getDocument().toObject(Post.class);
                    interFace.updateBlogWith(aPost);
                    /*}catch (NullPointerException ex){
                        ex.printStackTrace();
                    }*/
                }
            }
        });
    }

    //Save A Post
    public void savePostImageInStorage(Bitmap bitmap, final Post post) {
        // Create a reference to "profile_pic.jpg"
        if (bitmap == null)return;
        activity.showPB();
        final StorageReference imgPostRef = storage.child(Constants.POSTS)
                .child(GeneralUtil.randomName()+ "_" + ".png");
        byte[] img = GeneralUtil.compressImgFromBitmap(bitmap);
        Log.d(TAG, "saveProfilePictureOnFireBase: " + img.toString());
        // Create a reference to 'images/profile_pic.jpg'
        //final StorageReference profileImagesRef = storageRef.child("images/"+filename);
        final UploadTask uploadTask;
        uploadTask = imgPostRef.putBytes(img);
        uploadTask.addOnProgressListener(taskSnapshot -> {


        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return imgPostRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GeneralUtil.message("Post uploaded successfully!");
                storePostDataInFirestore(task, post);
            } else {
                activity.hidePB();
                GeneralUtil.getHandler().post(() -> {
                    //viewImplementation.hidePictureLoaderBar();
                    GeneralUtil.showAlertMessage(activity,
                            activity.getString(R.string.error),
                            activity.getString(R.string.error_image_msg));
                });
            }
        });
    }



    private void storePostDataInFirestore(Task<Uri> task, Post newPost) {
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

        firestore.collection("Posts").document("PID-" +GeneralUtil.randomName())
                .set(postMap).addOnCompleteListener(task1 -> {

            if(task1.isSuccessful()){
                GeneralUtil.message("Post was added");
                activity.goStraightToHomePage("");
            } else {

                GeneralUtil.showAlertMessage(activity, activity.getString(R.string.error),
                        "Something Went Wrong, Please Try Again!");
            }

            activity.hidePB();

        });

    }

}
