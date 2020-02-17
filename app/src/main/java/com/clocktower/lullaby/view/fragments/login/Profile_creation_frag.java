package com.clocktower.lullaby.view.fragments.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.ProfilePicture;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Splash;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_creation_frag extends Fragment implements View.OnClickListener {

    private static final String TAG = "Profile_creation_frag";
    private static final String NAME = "Name";
    private Splash activity;
    private ImageButton addPicture;
    private CircleImageView profilePic;
    private Button mContinue;
    private TextView username;
    private ContentLoadingProgressBar progressBar;
    private String getName;
    private ProfilePicture profile;
    

    public static Profile_creation_frag getInstance(String name){
        Profile_creation_frag fragment = new Profile_creation_frag();
        Bundle extra =  new Bundle();
        extra.putString(NAME, name );
        fragment.setArguments(extra);
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_creation, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getName = getArguments().getString(NAME);
        profile = new ProfilePicture();
        initialiseWidgets(view);
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Splash)context;
    }

    private void initialiseWidgets(View view){
        addPicture = view.findViewById(R.id.buttonAddImage);
        addPicture.setOnClickListener(this);
        username = view.findViewById(R.id.text_user_name_crtn);
        username.setText(getName);
        profilePic= view.findViewById(R.id.imageViewID);
        mContinue = view.findViewById(R.id.buttonContinueHome);
        progressBar = view.findViewById(R.id.progressBarProfileUpload);
        progressBar.hide();
        mContinue.setOnClickListener(this);
    }

    public void showProgressBar(){
        progressBar.show();
    }

    public void hideProgressBar(){
        progressBar.hide();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonAddImage:
                progressBar.show();
                profile.changeProfilePic(Profile_creation_frag.this);
                break;
            case R.id.buttonContinueHome:
                activity.goStraightToHomePage();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Splash.RESULT_OK && data != null) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri uri = result.getUri();
                    if (activity.savePictureInDb(uri))
                        setImageURI(uri);
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    CropImage.ActivityResult mResult = CropImage.getActivityResult(data);
                    Exception error = mResult.getError();
                    error.printStackTrace();
                    break;
                case Constants.REQUEST_IMAGE_CAPTURE:
                    Log.d(TAG, "onActivityResult: "+ data.toString());
                    setImageURI(data.getData());
                    activity.getImageFromIntent(data);
                    break;
                case Constants.PICK_IMAGE_REQUEST:
                    Uri imgUri = data.getData();
                    if(imgUri!= null && activity.savePictureInDb(imgUri))
                        setImageURI(imgUri);
                    break;
            }
        }else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            //Log.e(TAG, "onActivityResult: ", response.getError());
            GeneralUtil.message("Error Occured" + resultCode);
        }
    }

    public void setImageURI(Uri uri) {
        profilePic.setImageURI(uri);
    }
}
