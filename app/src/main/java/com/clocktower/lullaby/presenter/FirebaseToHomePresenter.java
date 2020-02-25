package com.clocktower.lullaby.presenter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

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
        commentsMap.put("url", user.getPhotoUrl().toString());
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
        firestore.collection(Constants.POSTS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
            }
        });
    }

}
