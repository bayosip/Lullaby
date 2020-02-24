package com.clocktower.lullaby.model.utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.LoginListener;
import com.clocktower.lullaby.view.activities.Splash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseUtil {

    static FirebaseFirestore firestore;
    static StorageReference storage;
    private static FirebaseAuth mAuth;
    private static String profileDisplayName;
    private static final String filename = "profile.png", filename_T = "profile_Thumb.png";

    private static final String TAG = "FirebaseUtil";
    private static boolean firebaseSave = false;

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

    public static void subscribeUserToNotification(Activity activity) {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.APP_TOPIC)
                .addOnCompleteListener(task -> {
                    String msg = activity.getString(R.string.msg_subscribed);
                    if (!task.isSuccessful()) {
                        msg = activity.getString(R.string.msg_subscribe_failed);
                    }
                    Log.d(TAG, msg);
                    GeneralUtil.message(msg);
                });
    }

    public static void checkIfUserIsSignedIn(final LoginListener loginListener) {

        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            verifyUserFBAcc(user, loginListener);
        });
    }
    public static void verifyUserFBAcc(final FirebaseUser user, final LoginListener loginListener){
        if (user != null){
            if(!user.isEmailVerified()) {
                GeneralUtil.message("Please Verify With Link In Email");
                return;
            }else {
                FirebaseUtil.subscribeUserToNotification(loginListener.getLoginActivity());
                // User is signed in to Firebase, but we can only get
                // basic info like name, email, and profile photo url
                String email = user.getEmail();
                // Even a user's provider-specific profile information
                // only reveals basic information
                for (UserInfo profile : user.getProviderData()) {
                    // Name, email address, and profile photo Url
                    profileDisplayName = profile.getDisplayName();
                    String profileEmail = profile.getEmail();

                    if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(profileEmail) &&
                            email.equalsIgnoreCase(profileEmail)) {
                        Log.i(Splash.TAG, "User found");
                        break;
                    }
                }

                if (TextUtils.isEmpty(user.getDisplayName())) {
                    if (!TextUtils.isEmpty(profileDisplayName)) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                .Builder().setDisplayName(profileDisplayName)
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "verifyUserFBAcc: Profile name successfully updated");
                                checkIfUserIsRegisteredOnFireStore(user, loginListener);
                            }
                        });
                    } else {
                        Log.w(TAG, "verifyUserFBAcc: No User name found");
                        loginListener.startProfilePictureFragment(profileDisplayName);
                    }
                }else {
                    Log.d(TAG, "verifyUserFBAcc: User name alreadt in FB auth");
                    checkIfUserIsRegisteredOnFireStore(user, loginListener);
                }
            }
        }else {
            loginListener.initialiseLogin();
        }
    }

    public static void checkIfUserIsRegisteredOnFireStore(final FirebaseUser user,
                                                          final LoginListener listener){
        DocumentReference docRef = firestore.collection(Constants.USERS).document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    GeneralUtil.getAppPref(App.context).edit().putString("PROFILE",
                            user.getPhotoUrl().toString()).apply();
                    listener.goStraightToHomePage(user.getDisplayName());
                } else {
                    Log.w(TAG, "No such document");
                    if (user.getPhotoUrl()== null || TextUtils.isEmpty(user.getPhotoUrl().toString())){
                        listener.startProfilePictureFragment(user.getDisplayName());
                    }else {
                        Bitmap bitmap = null;
                        try {
                        bitmap = Ion.with(listener.getLoginActivity())
                                .load(user.getPhotoUrl().toString()).withBitmap().asBitmap().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        savePictureOnFireBase(bitmap, user, listener);
                    }
                }
            } else {
                Log.e(TAG, "get failed with ", task.getException());
            }
        });
    }

    private static void storeFirestore(@NonNull Task<Uri> task, final FirebaseUser user, LoginListener loginListener) {
        Uri download_uri;
        if(task != null) {
            download_uri = task.getResult();
        } else {
            download_uri = user.getPhotoUrl();
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user.getDisplayName());
        userMap.put("image", download_uri.toString());

        firestore.collection(Constants.USERS).document(user.getUid()).set(userMap).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                loginListener.hidePB();
                loginListener.goStraightToHomePage(user.getDisplayName());
            } else {
                loginListener.hidePB();
                String error = task1.getException().getMessage();
                GeneralUtil.showAlertMessage(loginListener.getLoginActivity(),
                        loginListener.getLoginActivity().getString(R.string.error), error);
            }
            //setupProgress.setVisibility(View.INVISIBLE);
        });
    }

    public static boolean savePictureOnFireBase(Bitmap bitmap, FirebaseUser user, LoginListener listener) {
        // Create a reference to "profile_pic.jpg"
        if (bitmap == null)return false;
        listener.showPB();
        final StorageReference profileRef = storage.child(Constants.USERS).child(
                user.getUid().trim() + "_" + filename);
        byte[] img = GeneralUtil.compressImgFromUri(bitmap);
        Log.d(TAG, "savePictureOnFireBase: " + img.toString());
        // Create a reference to 'images/profile_pic.jpg'
        //final StorageReference profileImagesRef = storageRef.child("images/"+filename);
        final UploadTask uploadTask;
        uploadTask = profileRef.putBytes(img);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return profileRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GeneralUtil.getAppPref(listener.getLoginActivity()).edit().putString(Constants.PROFILE,
                        task.getResult().toString()).apply();
                GeneralUtil.message("Profile Changed");
                firebaseSave = true;
                setUserImage(task.getResult(), user);
                storeFirestore(task, user, listener);
            } else {
                firebaseSave = false;
                listener.hidePB();
                GeneralUtil.getHandler().post(() -> {
                    //viewImplementation.hidePictureLoaderBar();
                    GeneralUtil.showAlertMessage(listener.getLoginActivity(),
                            listener.getLoginActivity().getString(R.string.error),
                            listener.getLoginActivity().getString(R.string.error_image_msg));
                });
            }
        });
        Log.e(Splash.TAG, "Saved: " + firebaseSave);
        return firebaseSave;
    }

    private static void setUserImage(Uri userImage, FirebaseUser user){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                .Builder()
                .setPhotoUri(userImage)
                .build();
        user.updateProfile(profileUpdates);
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
