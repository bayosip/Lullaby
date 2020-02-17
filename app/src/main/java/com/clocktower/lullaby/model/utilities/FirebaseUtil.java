package com.clocktower.lullaby.model.utilities;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.interfaces.LoginListener;
import com.clocktower.lullaby.view.activities.Splash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class FirebaseUtil {

    static FirebaseFirestore firestore;
    static StorageReference storage;
    private static FirebaseAuth mAuth;

    private static final String TAG = "FirebaseUtil";

    static {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
    }

    public static FirebaseAuth getmAuth() {
        if (mAuth == null) {

        }
        return mAuth;
    }


    public static FirebaseFirestore getFirestore() {
        return firestore;
    }

    public static StorageReference getStorage() {
        return storage;
    }

    public static void checkIfUserIsSignedIn(final LoginListener loginListener) {

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "User Signed In");
                    String username = user.getDisplayName();
                    Log.e(TAG, "User is: " + username);
                    if(TextUtils.isEmpty(username)){
                        for (UserInfo profile : user.getProviderData()) {
                            // Name, email address, and profile photo Url
                            String profileDisplayName = profile.getDisplayName();
                            String profileEmail = profile.getEmail();

                            if(!TextUtils.isEmpty(user.getEmail()) && !TextUtils.isEmpty(profileEmail) &&
                                    user.getEmail().equalsIgnoreCase(profileEmail)){
                                if (!TextUtils.isEmpty(profileDisplayName) && TextUtils.isEmpty(username)){
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                            .Builder().setDisplayName(profileDisplayName)
                                            .build();
                                    user.updateProfile(profileUpdates);
                                }
                                break;
                            }
                        }
                    }
                    if (user.getPhotoUrl() != null){
                        GeneralUtil.getAppPref(App.context).edit().putString("PROFILE",
                                user.getPhotoUrl().toString()).apply();
                    loginListener.goStraightToHomePage();
                    }
                    else {
                        loginListener.startProfilePictureFragment(username);
                    }
                } else {
                    loginListener.initialiseLogin();
                }
            }
        });
    }

    public static void downloadMusicTrack(String audioName) throws IOException {
        StorageReference audioRef = storage.child("audio/"+audioName);

        File localFile = File.createTempFile(audioName, "mp3");

        audioRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }


}
