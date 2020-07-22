package com.clocktower.lullaby.view.fragments.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.model.utilities.RealPathUtil;
import com.crashlytics.android.Crashlytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;


import java.util.List;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;


public class CreatePostFragment extends AbstractAudioViewFragment{

    private static final String TAG = "CreatePostFragment";
    private Post newPost;
    private Button uploadVideo, uploadImage, uploadAudio, postToBlog;
    private View audioTestLayout;
    private ImageView image;
    private VideoView video;
    private EditText postTitle;
    ImageButton playVideoBtn;
    ImageCreator imgCreator;
    Uri postUri = null;
    long mediaType = 0;
    String videoPath;
    private SongInfo audio = null;

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
        listener.accessFilesFromPhone();
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
        uploadAudio = v.findViewById(R.id.btn_upload_music);
        postToBlog = v.findViewById(R.id.post_btn);

        audioTestLayout = v.findViewById(R.id.audioSampleLayout);
        songName = v.findViewById(R.id.textAudioName);
        songTime = v.findViewById(R.id.textDuration);
        play_pause = v.findViewById(R.id.buttonPlayPause);
        trackBar = v.findViewById(R.id.seekMusic);

        trackBar.setOnSeekBarChangeListener(this);
        play_pause.setOnClickListener(this);

        playVideoBtn.setOnClickListener(btnListener);
        uploadImage.setOnClickListener(btnListener);
        uploadVideo.setOnClickListener(btnListener);
        uploadAudio.setOnClickListener(btnListener);
        postToBlog.setOnClickListener(btnListener);

        MediaController mediaController = new MediaController(getActivity());
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
    }

    Button.OnClickListener btnListener = view -> {
        if (video.isPlaying())video.stopPlayback();
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
            case R.id.post_btn:
                if(listener.isUserAdmin()) {
                    String desc = postTitle.getText().toString();
                    String uri;
                    if (mediaType != Constants.TEXT){
                        if(mediaType == Constants._AUDIO && postUri ==null){
                            listener.saveNewAudioInDb(null);
                            return;
                        } else {
                            uri = (String) RealPathUtil.getMediaPath(listener.getViewContext(), postUri, mediaType);
                            if (TextUtils.isEmpty(uri))uri =(String) RealPathUtil
                                    .getMediaPathRetry(listener.getViewContext(),postUri, mediaType);
                        }
                    } else uri = "none";

                    if (postUri != null && TextUtils.isEmpty(uri))// check if uri real path was returned
                        uri = postUri.getLastPathSegment();

                    Log.w(TAG, "onClick: final uri - " + uri);
                    Crashlytics.log("Admin tried to upload media of type: " + mediaType);

                    if (TextUtils.isEmpty(uri))
                        GeneralUtil.message("Something went wrong! Please try again");

                    else if ((mediaType > 0 && !uri.equals("none")) ||
                            (mediaType == 0 && !TextUtils.isEmpty(desc))) {
                        if (mediaType != 3) {
                            newPost = new Post(desc, uri, mediaType, null);
                            listener.saveNewPostInDB(newPost, mediaType);
                        } else {
                            listener.saveNewAudioInDb(audio);
                        }
                    } else {
                        GeneralUtil.message("Post a Message, an Image or a Video.");
                    }
                }
                break;
            default:
                postUri = null;
            case R.id.btn_upload_photo:
                Crashlytics.log("Admin clicked on upload image");
                postTitle.setVisibility(View.VISIBLE);
                mediaView.setVisibility(View.VISIBLE);
                audioTestLayout.setVisibility(View.GONE);
                playVideoBtn.setVisibility(View.GONE);
                video.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                imgCreator.createAnImage(CreatePostFragment.this);
                postToBlog.setText("Post");
                break;
            case R.id.btn_upload_video:
                Crashlytics.log("Admin clicked on upload video");
                postTitle.setVisibility(View.VISIBLE);
                mediaView.setVisibility(View.VISIBLE);
                audioTestLayout.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                video.setVisibility(View.VISIBLE);
                selectAudioOrVideoMedia(Constants.VIDEO);
                postToBlog.setText("Post");
                break;
            case R.id.btn_upload_music:
                mediaType = Constants._AUDIO;
                Crashlytics.log("Admin clicked on upload audio");
                postToBlog.setText("Upload Audio");
                listener.showAudioFromDevice();
                audioTestLayout.setVisibility(View.VISIBLE);
                postTitle.setVisibility(View.INVISIBLE);
                break;
        }
    };

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
        listener.getViewContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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

    private void startMusicPicker(){
        Intent intent = new Intent();
        intent.setType("audio/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                .Video.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("audio/*");

        Intent chooserIntent = Intent.createChooser(intent, "Select Audio File");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(chooserIntent, Constants.PICK_AUDIO_REQUEST);
    }

    public void selectAudioOrVideoMedia(final long mediaType){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (mediaType==Constants.VIDEO){
                            startVideoPicker();
                        }
                        else if(mediaType == Constants._AUDIO) startMusicPicker();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == listener.getViewContext().RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.REQUEST_IMAGE_CAPTURE:
                    Bitmap bMap = (Bitmap) data.getExtras().get("data");
                    if(bMap!=null) {
                        image.setImageBitmap(bMap);
                        saveImage(bMap);
                        mediaType = Constants.IMAGE;
                    }
                    break;
                case Constants.PICK_IMAGE_REQUEST:
                    Uri imgUri = data.getData();
                    if (imgUri != null) {
                        image.setImageURI(imgUri);
                        postUri = imgUri;
                        mediaType = Constants.IMAGE;
                    }
                    break;

                case Constants.PICK_VIDEO_REQUEST:
                    postUri = data.getData();
                    mediaType = Constants.VIDEO;
                    if (data.getData()!= null) {
                        playSelectedVideoFrom();
                        Log.i(TAG, "onActivityResult: " +  RealPathUtil
                                .getMediaPathRetry(listener.getViewContext(), postUri,
                                        Constants.VIDEO));
                        playVideoBtn.setVisibility(View.VISIBLE);
                        adjustVideoSize();
                    }
                    break;
                case Constants.PICK_AUDIO_REQUEST:
                    Log.w(TAG, "onActivityResult: " + data.toString() );
                    Log.d(TAG, "onActivityResult: debugging music" );
                    mediaType = Constants._AUDIO;
                    postUri = data.getData();
                    RealPathUtil.getMediaPath(listener.getViewContext(), data.getData(), Constants._AUDIO);
                    if(audio==null){
                        audio = (SongInfo)RealPathUtil.getMediaPathRetry(listener.getViewContext(),postUri, mediaType);
                        if (audio==null)
                            audio = new SongInfo("unknown", "unknown",
                                    postUri.getLastPathSegment());
                    }
                    listener.playSelectedAudio(audio);
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
                        postUri = GeneralUtil.getImageUri(listener.getViewContext(), bMap,
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
