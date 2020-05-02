package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.LoginListener;
import com.clocktower.lullaby.interfaces.ProfileListener;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.presenter.SplashPresenter;
import com.clocktower.lullaby.view.fragments.login.Profile_creation_frag;
import com.clocktower.lullaby.view.fragments.login.RegisterationFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;

public class Splash extends AppCompatActivity implements ProfileListener, View.OnClickListener {


    public static final String TAG = Splash.class.getSimpleName();
    private SplashPresenter presenter;
    private Profile_creation_frag fragment;
    private RegisterationFragment regFragment;
    private Button signIn, register;
    private View loginView, frame;
    private TextView version;
    public static final String PROFILE = "Profile Creation";
    public static final String REGISTRATION = "Register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialiseWidgets();
        GeneralUtil.getHandler().post(() -> FirebaseUtil.checkIfUserIsSignedIn(Splash.this));
    }

    @Override
    public void startProfilePictureFragment(String name){

        frame.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragment = Profile_creation_frag.getInstance(name);
        fragmentTransaction.add(R.id.fragment_container, fragment, PROFILE);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void startEmailRegFragment(){
        frame.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        regFragment = RegisterationFragment.getInstance();
        fragmentTransaction.add(R.id.fragment_container, regFragment, REGISTRATION);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void initialiseWidgets(){
        signIn = findViewById(R.id.buttonSignIn);
        signIn.setOnClickListener(this);
        register = findViewById(R.id.buttonRegister);
        register.setOnClickListener(this);
        loginView = findViewById(R.id.loginOptionsView);
        frame = findViewById(R.id.fragment_container);
        version = findViewById(R.id.textAppVersion);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(getString(R.string.version, info.versionName ));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version.setText("");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.RC_SIGN_IN:
                    IdpResponse response = IdpResponse.fromResultIntent(data);
                    presenter.checkIfLoginIsSuccessful();
                    break;
            }
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

    @Override
    public void progressPB(long progress) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if(fragment!= null) {
            String tag = getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                    .getTag();
            Profile_creation_frag knownfrag = (Profile_creation_frag)getSupportFragmentManager()
                    .findFragmentByTag(tag);
            knownfrag.progressPB(progress);
        }
    }

    public void showPB(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if(fragment!= null) {
            String tag = getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                    .getTag();
            Profile_creation_frag knownfrag = (Profile_creation_frag)getSupportFragmentManager()
                    .findFragmentByTag(tag);
            knownfrag.showProgressBar();
        }
    }

    public void hidePB(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if(fragment!= null) {
            String tag = getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                    .getTag();
            Profile_creation_frag knownfrag = (Profile_creation_frag) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            knownfrag.hideProgressBar();
        }
    }

    @Override
    public void initialiseLogin() {
       loginView.setVisibility(View.VISIBLE);
    }

    @Override
    public void goStraightToHomePage(String name) {
        presenter.startHomeActivity(name);
    }

    @Override
    public void disableScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void enableScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public boolean saveProfilePictureInDb(Bitmap bitmap) {
        return presenter.saveImgInUserProfile(bitmap, Splash.this);//
    }

    @Override
    public boolean saveProfilePictureInDb(Uri uri) {
        return presenter.saveImgInUserProfile(uri, Splash.this);
    }

    public void registerUserWith(String email, String pwd) {
        presenter.registerUserOnDbWith(email, pwd);
    }

    @Override
    public void changeProfilePic(String url) {}

    @Override
    public Activity getLoginActivity() {
        return Splash.this;
    }

    @Override
    public void onClick(View view) {
            switch (view.getId()){
            case R.id.buttonSignIn:
                presenter.initialiseLogin();
                break;
            case R.id.buttonRegister:
                startEmailRegFragment();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment= getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment!=null) {
            String fragTag = getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                    .getTag();
            if (fragTag.equals(REGISTRATION)) {
                frame.setVisibility(View.GONE);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.remove(fragment).commitAllowingStateLoss();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void saveUserNameintoDb(String name) {
        presenter.saveNameinFBAuth(name);
    }
}
