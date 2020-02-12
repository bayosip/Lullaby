package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

public class Splash extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final String TAG = Splash.class.getSimpleName();
    private List<AuthUI.IdpConfig> loginProviders;
    private String chosenProvider;

    private String profileDisplayName ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initiatePrequisites();
        GeneralUtil.getHandler().postDelayed(new Runnable() {


            @Override
            public void run() {

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(loginProviders)
                                .setTheme(R.style.AppTheme_Splash) // Set theme
                                .build(),
                        RC_SIGN_IN);
            }
        }, 3000);
    }

    private void initiatePrequisites(){
        loginProviders = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    // User is signed in to Firebase, but we can only get
                    // basic info like name, email, and profile photo url
                    String email = user.getEmail();
                    // Even a user's provider-specific profile information
                    // only reveals basic information
                    for (UserInfo profile : user.getProviderData()) {
                        // Name, email address, and profile photo Url

                        profileDisplayName = profile.getDisplayName();
                        String profileEmail = profile.getEmail();
                        //Uri profilePhotoUrl = profile.getPhotoUrl();
                        if(!TextUtils.isEmpty(email) && email.equalsIgnoreCase(profileEmail))break;

                    }
                    if (TextUtils.isEmpty(user.getPhotoUrl().toString())){

                    }else {
                        startHomeActivity();
                    }

                } else {
                    // User is signed out of Firebase
                }

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.e(TAG, "onActivityResult: ", response.getError());
                GeneralUtil.message("Error Signing In");
            }
        }
    }

    public void startHomeActivity(){
        Intent intent = new Intent(Splash.this, Home.class).putExtra(Constants.USER_DATA, profileDisplayName);
        GeneralUtil.transitionActivity(Splash.this, intent);
    }

}
