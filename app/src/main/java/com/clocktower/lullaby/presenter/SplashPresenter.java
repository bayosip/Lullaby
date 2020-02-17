package com.clocktower.lullaby.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.ProfilePicture;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.clocktower.lullaby.view.activities.Splash;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashPresenter {

    private FirebaseApp app;
    private volatile Splash activity;
    private List<AuthUI.IdpConfig> loginProviders;
    private FirebaseUser user;
    private String uuid;
    private FirebaseFirestore firestore;
    StorageReference storageRef;
    private Uri profilePhotoUrl;
    private ProfilePicture profile;
    private Bitmap imageBitmap;
    private static final String filename = "profile.png", filename_T = "profile_Thumb.png";

    private boolean firebaseSave = false;

    private String profileDisplayName;

    public SplashPresenter(Splash activity) {
        this.activity = activity;
        initiatePrequisites();
    }

    private void initiatePrequisites(){
        app = FirebaseApp.initializeApp(App.context);
        firestore = FirebaseUtil.getFirestore();
        storageRef = FirebaseUtil.getStorage();
        loginProviders = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
    }

    private void subscribeUserToNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("Coza_Family")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = activity.getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = activity.getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(Splash.TAG, msg);
                        GeneralUtil.message(msg);
                    }
                });
    }

    public void initialiseLogin() {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(loginProviders)
                        .setTheme(R.style.AppTheme_Login) // Set theme
                        .build(),
                Constants.RC_SIGN_IN);
    }

    public void checkIfLoginIsSuccessful(){
        user = FirebaseUtil.getmAuth().getCurrentUser();

        if (user != null) {
            subscribeUserToNotification();
            // User is signed in to Firebase, but we can only get
            // basic info like name, email, and profile photo url
            String email = user.getEmail();
            uuid = user.getUid();
            // Even a user's provider-specific profile information
            // only reveals basic information
            for (UserInfo profile : user.getProviderData()) {
                // Name, email address, and profile photo Url
                profileDisplayName = profile.getDisplayName();
                String profileEmail = profile.getEmail();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(profileEmail) &&
                        email.equalsIgnoreCase(profileEmail)){
                    Log.i(Splash.TAG, "User found");
                    break;
                }
            }

            if (TextUtils.isEmpty(user.getDisplayName())){
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                        .Builder().setDisplayName(profileDisplayName)
                        .build();
                user.updateProfile(profileUpdates);
            }
            profilePhotoUrl = user.getPhotoUrl();
            checkIfUserIsRegisteredOnFireStore();
        }
    }

    private void checkIfUserIsRegisteredOnFireStore(){
        DocumentReference docRef = firestore.collection(Constants.USERS).document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(Splash.TAG, "DocumentSnapshot data: " + document.getData());
                        GeneralUtil.getAppPref(App.context).edit().putString("PROFILE",
                                profilePhotoUrl.toString()).apply();
                    } else {
                        Log.d(Splash.TAG, "No such document");
                        if (profilePhotoUrl== null){
                            activity.startProfilePictureFragment(profileDisplayName);
                        }else {
                            savePictureOnFireBase(profilePhotoUrl);
                        }
                    }
                } else {
                    Log.d(Splash.TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setUserImage(Uri userImage){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                                                    .Builder()
                                                                    .setPhotoUri(userImage)
                                                                    .build();
        user.updateProfile(profileUpdates);
    }

    private void storeFirestore(@NonNull Task<Uri> task, String user_name) {
        Uri download_uri;
        if(task != null) {
            download_uri = task.getResult();
        } else {
            download_uri = profilePhotoUrl;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", download_uri.toString());

        firestore.collection(Constants.USERS).document(uuid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    activity.hidePB();
                    startHomeActivity();
                } else {
                    activity.hidePB();
                    String error = task.getException().getMessage();
                    GeneralUtil.showAlertMessage(activity, activity.getString(R.string.error), error);
                }
                //setupProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    public boolean savePictureOnFireBase(Uri uri) {
        // Create a reference to "profile_pic.jpg"
        GeneralUtil.getAppPref(activity).edit().putString(Constants.PROFILE,
                uri.toString()).apply();
        activity.showPB();
        final StorageReference profileRef = storageRef.child(Constants.USERS).child(
                user.getDisplayName().trim() + "_" + filename);
        byte[] img = GeneralUtil.compressImgFromUri(activity, uri);
        // Create a reference to 'images/profile_pic.jpg'
        //final StorageReference profileImagesRef = storageRef.child("images/"+filename);
        final UploadTask uploadTask;
            uploadTask = profileRef.putBytes(img);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return profileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        GeneralUtil.message("Profile Changed");
                        firebaseSave = true;
                        setUserImage(task.getResult());
                        storeFirestore(task, profileDisplayName);
                    } else {
                        activity.hidePB();
                        GeneralUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                //viewImplementation.hidePictureLoaderBar();
                                GeneralUtil.showAlertMessage(activity, activity.getString(R.string.error),
                                        activity.getString(R.string.error_image_msg));
                            }
                        });
                    }
                }
            });
        Log.e(Splash.TAG, "Saved: " + firebaseSave);
        return firebaseSave;
    }

    public void startHomeActivity(){
        Intent intent = new Intent(activity, Home.class).putExtra(Constants.USER_DATA, profileDisplayName);
        GeneralUtil.transitionActivity(activity, intent);
    }

    public void getImageFromIntent(Intent data) {
        Uri imageUri = data.getData();

        Log.d(Splash.TAG, "getImageFromIntent: "+ imageUri.toString());
        //savePictureOnFireBase(imageUri);
    }

    public void registerUserOnDbWith(String email, String pwd) {
       final FirebaseAuth auth = FirebaseUtil.getmAuth();
       auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            GeneralUtil.showAlertMessage(activity, "Success!!!",
                                    "Please on the link in your email to complete verification");
                        }else {
                            GeneralUtil.message("Registration Unsuccessful... Check and try again.");
                        }
                    }
                });
            }
        });
    }
}
