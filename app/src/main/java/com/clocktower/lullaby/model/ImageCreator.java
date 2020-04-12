package com.clocktower.lullaby.model;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

public class ImageCreator {
    private static Uri profileUri;
    private static String username, imageURL;


    public ImageCreator() {
    }

    public ImageCreator(String username) {
        if(!TextUtils.isEmpty(username.trim())) {
            //this.profileUri = profileUri;
            this.username = username;
        }
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        ImageCreator.imageURL = imageURL;
    }

    public Uri getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(Uri profileUri) {
        ImageCreator.profileUri = profileUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        ImageCreator.username = username;
    }



    public void createAnImage(final Fragment fragment){
        final Dialog change = new Dialog(fragment.getContext());
        change.requestWindowFeature(Window.FEATURE_NO_TITLE);
        change.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        change.setContentView(R.layout.picture_option_popup);
        change.setCancelable(true);

        Button camera = change.findViewById(R.id.buttonCam);
        Button gallery = change.findViewById(R.id.buttonGallery);

        camera.setOnClickListener(view -> {
            if (fragment.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                if (Build.VERSION.SDK_INT >=23) {
                    dispatchTakePictureIntent(fragment);
                }
                else dispatchTakePictureIntentAPILow(fragment);
            }
            else {
                GeneralUtil.message("Oops! No camera found.");
            }
            change.dismiss();
        });

        gallery.setOnClickListener(view -> {
            openImageFileChooser(fragment);
            change.dismiss();
        });
        change.show();
    }

    private void startImagePicker(final Fragment fragment) {

        Intent intent = new Intent();
        intent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                .Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(intent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(chooserIntent, Constants.PICK_IMAGE_REQUEST);

    }

   /* private void startImagePicker(Fragment fragment){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(fragment.getContext(), fragment);
    }*/


    private void openImageFileChooser(final Fragment fragment){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Dexter.withActivity(fragment.getActivity())
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            startImagePicker(fragment);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            GeneralUtil.showAlertMessage(fragment.getActivity(),
                                    fragment.getString(R.string.error),"Internal Storage Permission Denied");
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                       PermissionToken token) {
                            token.cancelPermissionRequest();
                        }
                    }).check();
        }
    }

    private void dispatchTakePictureIntent(final Fragment fragment) {
        Uri imageUri = getImageUri(fragment);

        Dexter.withActivity(fragment.getActivity())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            //intent.putExtra("android.intent.extra.quickCapture", true);
                            fragment.startActivityForResult(intent, Constants.REQUEST_IMAGE_CAPTURE);
                            fragment.getActivity().sendBroadcast(new Intent(Constants.IMAGE_CAPTURE_URI)
                                    .putExtra(Constants.URI_DATA, imageUri.toString()));
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        GeneralUtil.showAlertMessage(fragment.getActivity(),
                                fragment.getString(R.string.error),"Camera Permission Denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }

    private Uri getImageUri(Fragment fragment){
        Uri m_imgUri = null;
        File m_file;
        try {
            m_file = getOutputMediaFile(fragment);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                m_imgUri = Uri.fromFile(m_file);
            else {
                m_imgUri = FileProvider.getUriForFile(App.context,
                        App.context.getPackageName() + ".provider", m_file);
            }
        } catch (IOException p_e) {
        }
        return m_imgUri;
    }

    private void dispatchTakePictureIntentAPILow(Fragment fragment) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            fragment.startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    public static File getOutputMediaFile(Fragment fragment) throws IOException {
        File mediaStorageDir = fragment.getActivity().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File image = File.createTempFile("COZA_IMG", ".png", mediaStorageDir);
        return image;
    }

}
