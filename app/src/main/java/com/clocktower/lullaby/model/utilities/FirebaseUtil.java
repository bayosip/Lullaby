package com.clocktower.lullaby.model.utilities;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clocktower.lullaby.interfaces.LoginListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
                    loginListener.goStraightToHomePage();
                } else {
                    loginListener.initialiseLogin();
                }
            }
        });
    }
}
