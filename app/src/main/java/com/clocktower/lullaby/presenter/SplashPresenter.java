package com.clocktower.lullaby.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.LoginListener;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Home;
import com.clocktower.lullaby.view.activities.Splash;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.koushikdutta.ion.Ion;

public class SplashPresenter {

    private FirebaseApp app;
    private LoginListener loginListener;
    private volatile Activity activity;
    private List<AuthUI.IdpConfig> loginProviders;
    private FirebaseUser user;
    StorageReference storageRef;

    private String profileDisplayName;

    public SplashPresenter(LoginListener listener) {
        this.loginListener = listener;
        this.activity = listener.getLoginActivity();
        initiatePrequisites();
    }

    private void initiatePrequisites(){
        app = FirebaseApp.initializeApp(App.context);
        storageRef = FirebaseUtil.getStorage();
        loginProviders = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        user = FirebaseUtil.getmAuth().getCurrentUser();
    }

    public void initialiseLogin() {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(loginProviders)
                        .setLogo(R.mipmap.ic_coza_logo_round)
                        .setTheme(R.style.AppTheme_Login) // Set theme
                        .build(),
                Constants.RC_SIGN_IN);
    }

    public void checkIfLoginIsSuccessful(){

        if (user != null){
            if(!user.isEmailVerified()) {
                GeneralUtil.message("Please Verify With Link In Email");
                return;
            }else {
                FirebaseUtil.subscribeUserToNotification(activity);
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
                                FirebaseUtil.checkIfUserIsRegisteredOnFireStore(user, loginListener);
                            }
                        });
                    } else {
                        loginListener.startProfilePictureFragment(profileDisplayName);
                    }
                }else {
                    FirebaseUtil.checkIfUserIsRegisteredOnFireStore(user, loginListener);
                }
            }
        }
    }

    public void startHomeActivity(String name){
        Intent intent = new Intent(activity, Home.class).putExtra(Constants.USER_DATA, name);
        GeneralUtil.transitionActivity(activity, intent);
    }

    public void registerUserOnDbWith(String email, String pwd) {
       final FirebaseAuth auth = FirebaseUtil.getmAuth();
       auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(task ->
               auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task1 -> {
                   if (task1.isSuccessful()){
                       GeneralUtil.showAlertMessage(activity, "Success!!!",
                               "Please on the link in your email to complete verification");
                       activity.onBackPressed();
                   }else {
                       GeneralUtil.message("Registration Unsuccessful... Check and try again.");
                   }
               }));
    }

    public void saveNameinFBAuth(final String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                .Builder().setDisplayName(name)
                .build();
        if (user!=null) {
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startHomeActivity(name);
                }
            });
        }
    }

    public boolean saveImgInUserProfile(Bitmap bitmap, LoginListener listener) {
        return FirebaseUtil.saveProfilePictureOnFireBase(bitmap, user, listener);
    }

    public boolean saveImgInUserProfile(Uri uri, LoginListener listener){
        Bitmap bitmap = null;
        try {
            bitmap = Ion.with(listener.getLoginActivity())
                    .load(uri.toString()).withBitmap().asBitmap().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return FirebaseUtil.saveProfilePictureOnFireBase(bitmap, user, listener);
    }
}
