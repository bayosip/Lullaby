package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.LoginListener;
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

public class Splash extends AppCompatActivity implements LoginListener, View.OnClickListener {


    public static final String TAG = Splash.class.getSimpleName();
    private SplashPresenter presenter;
    private Profile_creation_frag fragment;
    private Button signIn, register;
    private View loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialiseWidgets();
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
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void startEmailRegFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        RegisterationFragment fragment = RegisterationFragment.getInstance();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void initialiseWidgets(){
        signIn = findViewById(R.id.buttonSignIn);
        signIn.setOnClickListener(this);
        register = findViewById(R.id.buttonRegister);
        register.setOnClickListener(this);
        loginView = findViewById(R.id.loginOptionsView);

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

    public void showPB(){
        fragment.showProgressBar();
    }

    public void hidePB(){
        fragment.hideProgressBar();
    }

    @Override
    public void initialiseLogin() {
       loginView.setVisibility(View.VISIBLE);
    }

    @Override
    public void goStraightToHomePage() {
        presenter.startHomeActivity();
    }

    public boolean savePictureInDb(Uri uri) {
        return presenter.savePictureOnFireBase(uri);
    }

    public void getImageFromIntent(Intent data) {
        presenter.getImageFromIntent(data);
    }

    public void registerUserWith(String email, String pwd) {
        presenter.registerUserOnDbWith(email, pwd);
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
}
