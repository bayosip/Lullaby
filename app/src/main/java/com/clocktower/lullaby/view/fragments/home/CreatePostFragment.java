package com.clocktower.lullaby.view.fragments.home;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.ImageCreator;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Splash;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

public class CreatePostFragment extends BaseFragment implements View.OnClickListener{

    private static final String TAG = "CreatePostFragment";
    private Post newPost;
    private Button uploadVideo, uploadImage, postToBlog;
    private ImageView image;
    private VideoView video;
    private EditText postTitle;
    ImageButton playVideoBtn;
    ImageCreator imgCreator;
    Uri postUri;
    long mediaType = 0;

    View mediaView;
    private boolean isPlayClicked = false;

    public static CreatePostFragment newInstance(){

        CreatePostFragment fragment = new CreatePostFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgCreator = new ImageCreator();
        initialiseWidgets(view);
    }

    private void initialiseWidgets(View v) {

        video = v.findViewById(R.id.new_post_video);
        image = v.findViewById(R.id.new_post_image);
        mediaView = v.findViewById(R.id.new_post_media);
        postTitle = v.findViewById(R.id.new_post_desc);
        playVideoBtn = v.findViewById(R.id.buttonPlayVideo);
        uploadImage = v.findViewById(R.id.btn_upload_photo);
        uploadVideo = v.findViewById(R.id.btn_upload_video);
        postToBlog = v.findViewById(R.id.post_btn);

        playVideoBtn.setOnClickListener(this);
        uploadImage.setOnClickListener(this);
        uploadVideo.setOnClickListener(this);
        postToBlog.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    isPlayClicked = true;
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    //playSelectedVideoFrom(url);
                } else {
                    video.pause();
                }
                break;
            case R.id.btn_upload_photo:
                mediaView.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
               imgCreator.createAnImage(CreatePostFragment.this);
               break;
            case R.id.btn_upload_video:
                mediaView.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                video.setVisibility(View.VISIBLE);
                startVideoPicker();
                break;
            case R.id.post_btn:
                String desc = postTitle.getText().toString();
                String uri;
                if (mediaType ==1)uri = getImageURI(postUri);

                else if(mediaType ==2) uri = getVideoPath(postUri);

                else uri = "none";

                Log.w(TAG, "onClick: " + uri );

                if((mediaType>0 && !uri.equals("none"))||
                        (mediaType==0 && !TextUtils.isEmpty(desc))) {
                    newPost = new Post(desc, uri, mediaType, null);
                    listener.saveNewPostInDB(newPost, mediaType);
                }else {
                    GeneralUtil.message("Post a Message, an Image or a Video.");
                }
                break;
        }
    }

    private void startVideoPicker() {

        Intent intent = new Intent();
        intent.setType("video/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                .Video.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("video/*");

        Intent chooserIntent = Intent.createChooser(intent, "Select Video");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(chooserIntent, Constants.PICK_VIDEO_REQUEST);

    }

    private String getImageURI(Uri uri) {
        String path = "";
        String[] projection = { MediaStore.Images.Media.DATA };
        if (listener.getListenerContext().getContentResolver() != null) {
            Cursor cursor = listener.getListenerContext().getContentResolver()
                    .query(uri, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
            else path = uri.toString();
        }
        return path;
    }

    private String getVideoPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = listener.getListenerContext().getContentResolver()
                .query(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

    private void playSelectedVideoFrom(){
        try {
            video.setVideoURI(postUri);
            video.setOnPreparedListener(mediaPlayer -> {
                if (!mediaPlayer.isPlaying()) {
                    playVideoBtn.setImageResource(R.drawable.ic_play_video_24dp);
                    playVideoBtn.setVisibility(View.VISIBLE);
                }
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                if (!isPlayClicked) {
                    mediaPlayer.seekTo(100);
                    mediaPlayer.setOnSeekCompleteListener(mediaPlayer1 -> {
                        mediaPlayer1.pause();
                    });
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //video.requestFocus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == listener.getListenerContext().RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.REQUEST_IMAGE_CAPTURE:
                    Bitmap bMap = (Bitmap) data.getExtras().get("data");
                    if(bMap!=null) {
                        image.setImageBitmap(bMap);
                        saveImage(bMap);
                        mediaType = 1;
                    }
                    break;
                case Constants.PICK_IMAGE_REQUEST:
                    Uri imgUri = data.getData();
                    if (imgUri != null) {
                        image.setImageURI(imgUri);
                        postUri = imgUri;
                        mediaType = 1;
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri uri = result.getUri();
                    if (uri != null){
                        image.setImageURI(uri);
                        postUri= uri;
                        mediaType = 1;
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    CropImage.ActivityResult mResult = CropImage.getActivityResult(data);
                    Exception error = mResult.getError();
                    error.printStackTrace();
                    break;
                case Constants.PICK_VIDEO_REQUEST:
                    postUri = data.getData();
                    mediaType = 2;
                    if (data.getData()!= null)
                        playSelectedVideoFrom();
                    break;
            }
        } else {
            //Log.e(TAG, "onActivityResult: " + resultCode);
            GeneralUtil.message("Error Occured " + resultCode);
            mediaView.setVisibility(View.GONE);
        }
    }

    private void saveImage(Bitmap bMap){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        postUri = GeneralUtil.getImageUri(listener.getListenerContext(), bMap,
                                GeneralUtil.randomName());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        GeneralUtil.showAlertMessage(getActivity(),
                                getString(R.string.error),"Internal Storage Permission Denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }
}
