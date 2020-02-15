package com.clocktower.lullaby.present;

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
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clocktower.lullaby.view.activities.Splash.RC_SIGN_IN;

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
        FirebaseMessaging.getInstance().subscribeToTopic("Coza Family")
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
                        .setTheme(R.style.AppTheme_Splash) // Set theme
                        .build(),
                RC_SIGN_IN);
    }

    public void checkIfLoginIsSuccessful(){
        user = FirebaseUtil.getmAuth().getCurrentUser();

        if (user != null) {
            subscribeUserToNotification();
            // User is signed in to Firebase, but we can only get
            // basic info like name, email, and profile photo url
            String email = user.getEmail();
            // Even a user's provider-specific profile information
            // only reveals basic information
            for (UserInfo profile : user.getProviderData()) {
                // Name, email address, and profile photo Url

                profileDisplayName = profile.getDisplayName();
                String profileEmail = profile.getEmail();
                profilePhotoUrl = profile.getPhotoUrl();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(profileEmail) &&
                        email.equalsIgnoreCase(profileEmail))break;

            }
            if (TextUtils.isEmpty(user.getDisplayName())){
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                        .Builder().setDisplayName(profileDisplayName)
                        .build();
                user.updateProfile(profileUpdates);
            }
            if (user.getPhotoUrl()== null){
                if(profilePhotoUrl==null){
                    activity.startProfilePictureFragment(profileDisplayName);
                }else {
                    GeneralUtil.getAppPref(activity).edit().putString("PROFILE",
                            profilePhotoUrl.toString()).apply();
                    savePictureOnFireBase(profilePhotoUrl);
                }
            }else {

            }
        } else {
            // User is signed out of Firebase
        }
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

        firestore.collection("Users").document(uuid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                profilePhotoUrl.toString()).apply();
        activity.showPB();
        final StorageReference profileRef = storageRef.child(Constants.USERS).child(
                user.getDisplayName().trim() + "_" + filename);
        byte[] img = GeneralUtil.compressImg(activity, uri);
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
}
