package com.clocktower.lullaby.view.fragments.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.loader.content.CursorLoader;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.ImageCreator;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.view.activities.Splash;
import com.crashlytics.android.Crashlytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.util.List;

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
    String videoPath;

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

        MediaController mediaController = new MediaController(getActivity());
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    isPlayClicked = true;
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    playSelectedVideoFrom();
                } else {
                    video.pause();
                }
                break;
            case R.id.btn_upload_photo:
                Crashlytics.log("Admin selected image to upload");
                mediaView.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
               imgCreator.createAnImage(CreatePostFragment.this);
               break;
            case R.id.btn_upload_video:
                Crashlytics.log("Admin selected video to upload");
                mediaView.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                video.setVisibility(View.VISIBLE);
                selectAVideo();
                break;
            case R.id.post_btn:
                String desc = postTitle.getText().toString();
                String uri;
                if (mediaType ==1)uri = getImageURI(postUri);

                else if(mediaType ==2) {
                    uri = getVideoPath(postUri);
                }

                else uri = "none";

                if(postUri!=null && TextUtils.isEmpty(uri)) uri = postUri.toString();
                Log.w(TAG, "onClick: " + uri );
                Crashlytics.log("Admin tried to upload media of type: " + mediaType);

                if(TextUtils.isEmpty(uri)) GeneralUtil.message("Something went wrong! Please try again");

                else if((mediaType>0 && !uri.equals("none"))||
                        (mediaType==0 && !TextUtils.isEmpty(desc))) {
                    newPost = new Post(desc, uri, mediaType, null);
                    listener.saveNewPostInDB(newPost, mediaType);
                }else {
                    GeneralUtil.message("Post a Message, an Image or a Video.");
                }
                break;
        }
    }

    private void playSelectedVideoFrom(){
        try {
            video.setVideoURI(postUri);
            video.setOnPreparedListener(mediaPlayer -> {
                if (mediaPlayer.isPlaying()) {
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
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

    private void adjustVideoSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        listener.getListenerContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) video.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = (int)(300*metrics.density);
        params.leftMargin = 0;
        video.setLayoutParams(params);
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

    private void selectAVideo(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                      /*new VideoPicker.Builder(listener.getListenerContext())
                                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                                .directory(VideoPicker.Directory.DEFAULT)
                                .extension(VideoPicker.Extension.MP4)
                                .enableDebuggingMode(false)
                                .build();*/
                      startVideoPicker();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();

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
            else path = uri.getLastPathSegment();
        }
        return path;
    }

    @SuppressLint("ObsoleteSdkInt")
    private String getVideoPath(Uri uri) {

            String realPath="";
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                String[] proj = { MediaStore.Video.Media.DATA };

                @SuppressLint("Recycle")
                Cursor cursor = listener.getListenerContext()
                        .getContentResolver().query(uri, proj, null, null, null);
                int column_index = 0;
                String result="";
                if (cursor != null) {
                    column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    realPath=cursor.getString(column_index);
                }
            }
            // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19){
                String[] proj = { MediaStore.Video.Media.DATA };
                CursorLoader cursorLoader = new CursorLoader(listener.getListenerContext(),
                        uri, proj, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                if(cursor != null){
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    cursor.moveToFirst();
                    realPath = cursor.getString(column_index);
                }
            }
            // SDK > 19 (Android 4.4)
            else{
                String wholeID = DocumentsContract.getDocumentId(uri);
                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];
                String[] column = { MediaStore.Video.Media.DATA };
                // where id is equal to
                String sel = MediaStore.Video.Media._ID + "=?";
                Cursor cursor = listener.getListenerContext()
                        .getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{ id }, null);
                int columnIndex = 0;
                if (cursor != null) {
                    columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        realPath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            }
            return realPath;
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
                    if (data.getData()!= null) {
                        playSelectedVideoFrom();
                        playVideoBtn.setVisibility(View.VISIBLE);
                        adjustVideoSize();
                    }
                    break;
                case VideoPicker.VIDEO_PICKER_REQUEST_CODE:
                    mediaType = 2;
                    List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                    videoPath = mPaths.get(0);
                    Log.w(TAG, "onActivityResult: " + videoPath );
                    postUri = Uri.parse(videoPath);
                    playVideoBtn.setVisibility(View.VISIBLE);
                    playSelectedVideoFrom();
                    break;
            }
        } else {
            //Log.e(TAG, "onActivityResult: " + resultCode);
            GeneralUtil.message("Error Occured " + resultCode);
            playVideoBtn.setVisibility(View.GONE);
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
