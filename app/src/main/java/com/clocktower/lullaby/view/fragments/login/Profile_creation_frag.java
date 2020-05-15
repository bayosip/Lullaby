package com.clocktower.lullaby.view.fragments.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.ProfileListener;
import com.clocktower.lullaby.model.ImageCreator;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;

public class Profile_creation_frag extends Fragment implements View.OnClickListener {

    private static final String TAG = "Profile_creation_frag";
    private static final String NAME = "Name";
    private ImageButton addPicture;
    private CircularImageView profilePic;
    private Button mContinue, saveName;
    private EditText enterName;
    private ContentLoadingProgressBar progressBar;
    private String getName;
    private boolean hasName;
    private ImageCreator profile;
    private ProfileListener listener;
    private Intent mImgUri;
    

    public static Profile_creation_frag getInstance(String name){
        Profile_creation_frag fragment = new Profile_creation_frag();
        Bundle extra =  new Bundle();
        extra.putString(NAME, name);
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
        getName = getArguments().getString(NAME, null);
        profile = new ImageCreator();
        initialiseWidgets(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ProfileListener) context;
    }

    private void initialiseWidgets(View view){
        addPicture = view.findViewById(R.id.buttonAddImage);
        addPicture.setOnClickListener(this);
        enterName = view.findViewById(R.id.editTextEnterName);

        profilePic= view.findViewById(R.id.imageViewID);
        mContinue = view.findViewById(R.id.buttonContinueHome);
        saveName = view.findViewById(R.id.buttonSaveName);
        progressBar = view.findViewById(R.id.progressBarProfileUpload);
        progressBar.hide();
        mContinue.setOnClickListener(this);
        saveName.setOnClickListener(this);
        if (!TextUtils.isEmpty(getName)){
            enterName.setText(getName);
            enterName.setEnabled(false);
            saveName.setVisibility(View.GONE);
        }
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
                profile.createAnImage(Profile_creation_frag.this);
                break;
            case R.id.buttonContinueHome:
                if (!TextUtils.isEmpty(getName))
                    listener.goStraightToHomePage(getName);
                else{
                    GeneralUtil.message("Please Enter Name");
                }
                break;
            case R.id.buttonSaveName:
                String name = enterName.getText().toString();
                listener.saveUserNameintoDb(name);
                enterName.setEnabled(false);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == listener.getLoginActivity().RESULT_OK && data != null) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri uri = result.getUri();
                    if (listener.saveProfilePictureInDb(result.getBitmap()))
                        setImageURI(uri);
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    CropImage.ActivityResult mResult = CropImage.getActivityResult(data);
                    Exception error = mResult.getError();
                    error.printStackTrace();
                    break;
                case Constants.REQUEST_IMAGE_CAPTURE:
                    Log.d(TAG, "onActivityResult: "+ data.toString());
                    Bitmap bMap = (Bitmap)data.getExtras().get("data");
                    profilePic.setImageBitmap(bMap);
                    listener.saveProfilePictureInDb(bMap);
                    break;
                case Constants.PICK_IMAGE_REQUEST:
                    Uri imgUri = data.getData();
                    if(imgUri!= null && listener.saveProfilePictureInDb(imgUri))
                        setImageURI(imgUri);
                    break;
            }
        }else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Log.e(TAG, "onActivityResult: " + resultCode);
            GeneralUtil.message("Error Occured " + resultCode);
        }
    }

    public void setImageURI(Uri uri) {
        profilePic.setImageURI(uri);
    }

    public void progressPB(long progress) {
        getActivity().runOnUiThread(() -> {
            progressBar.setProgress((int) progress);
        });
    }
}
