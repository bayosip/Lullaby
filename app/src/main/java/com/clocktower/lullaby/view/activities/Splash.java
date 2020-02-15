package com.clocktower.lullaby.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.LoginListener;
import com.clocktower.lullaby.model.ProfilePicture;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.present.SplashPresenter;
import com.clocktower.lullaby.view.fragments.login.Profile_creation_frag;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Splash extends AppCompatActivity implements LoginListener {

    public static final int RC_SIGN_IN = 101;
    public static final String TAG = Splash.class.getSimpleName();
    private List<AuthUI.IdpConfig> loginProviders;
    private String chosenProvider;
    private SplashPresenter presenter;
    private Profile_creation_frag fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GeneralUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUtil.checkIfUserIsSignedIn(Splash.this);
            }
        }, 1000);
    }

    public void startProfilePictureFragment(String name){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragment = Profile_creation_frag.getInstance(name);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    IdpResponse response = IdpResponse.fromResultIntent(data);
                    presenter.checkIfLoginIsSuccessful();
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri uri = result.getUri();
                    if (presenter.savePictureOnFireBase(uri))
                        fragment.setImageURI(uri);
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    CropImage.ActivityResult mResult = CropImage.getActivityResult(data);
                    Exception error = mResult.getError();
                    error.printStackTrace();
                    break;
                    case Constants.PICK_IMAGE_REQUEST:
                        break;
                }
        }else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            //Log.e(TAG, "onActivityResult: ", response.getError());
            GeneralUtil.message("Error Occured");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter==null)
            presenter = new SplashPresenter(this);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context
                .NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void showPB(){
        fragment.showProgressBar();
    }

    public void hidePB(){
        fragment.hideProgressBar();
    }

    @Override
    public void initialiseLogin() {
       presenter.initialiseLogin();
    }

    @Override
    public void goStraightToHomePage() {
        presenter.startHomeActivity();
    }
}
