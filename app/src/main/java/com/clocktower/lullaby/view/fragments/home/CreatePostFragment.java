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

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.util.List;

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
    Uri postUri;
    long mediaType = 0;
    String videoPath;
    private SongInfo audio;

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
        playVideoBtn.setOnClickListener(this);

        uploadImage.setOnClickListener(btnListener);
        uploadVideo.setOnClickListener(btnListener);
        uploadAudio.setOnClickListener(btnListener);
        postToBlog.setOnClickListener(btnListener);

        MediaController mediaController = new MediaController(getActivity());
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
    }

    Button.OnClickListener btnListener = view -> {
        switch (view.getId()) {
            case R.id.buttonPlayVideo:
                if (!video.isPlaying()) {
                    isPlayClicked = true;
                    playVideoBtn.setImageResource(R.drawable.ic_pause_video_24dp);
                    playVideoBtn.setVisibility(View.GONE);
                    audioTestLayout.setVisibility(View.GONE);
                    playSelectedVideoFrom();
                } else {
                    video.pause();
                }
                break;
            case R.id.btn_upload_photo:
                Crashlytics.log("Admin clicked on upload image");
                postTitle.setVisibility(View.VISIBLE);
                mediaView.setVisibility(View.VISIBLE);
                audioTestLayout.setVisibility(View.GONE);
                playVideoBtn.setVisibility(View.GONE);
                video.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                imgCreator.createAnImage(CreatePostFragment.this);
                break;
            case R.id.btn_upload_video:
                Crashlytics.log("Admin clicked on upload video");
                postTitle.setVisibility(View.VISIBLE);
                mediaView.setVisibility(View.VISIBLE);
                audioTestLayout.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                video.setVisibility(View.VISIBLE);
                selectAudioOrVideoMedia(Constants.VIDEO);
                break;
            case R.id.btn_upload_music:
                Crashlytics.log("Admin clicked on upload audio");
                listener.showAudioFromDevice();
                audioTestLayout.setVisibility(View.VISIBLE);
                postTitle.setVisibility(View.INVISIBLE);
                break;
            case R.id.post_btn:
                String desc = postTitle.getText().toString();
                String uri;
                if (mediaType != Constants.TEXT)
                    uri = getMediaPath(postUri, mediaType);
                else uri = "none";

                if(postUri!=null && TextUtils.isEmpty(uri)) uri = postUri.toString();
                Log.w(TAG, "onClick: " + uri );
                Crashlytics.log("Admin tried to upload media of type: " + mediaType);

                if(TextUtils.isEmpty(uri)) GeneralUtil.message("Something went wrong! Please try again");


                else if((mediaType>0 && !uri.equals("none"))||
                        (mediaType==0 && !TextUtils.isEmpty(desc))) {
                    if(mediaType!=3) {
                        newPost = new Post(desc, uri, mediaType, null);
                        listener.saveNewPostInDB(newPost, mediaType);
                    }else{
                        listener.saveNewAudioInDb(audio);
                    }
                }else {
                    GeneralUtil.message("Post a Message, an Image or a Video.");
                }
                break;
        }
        if (mediaType == 3){
            postToBlog.setText("Upload Audio");
        }else postToBlog.setText("Post");
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
                      /*new VideoPicker.Builder(listener.getViewContext())
                                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                                .directory(VideoPicker.Directory.DEFAULT)
                                .extension(VideoPicker.Extension.MP4)
                                .enableDebuggingMode(false)
                                .build();*/
                        if (mediaType==Constants.VIDEO)
                            startVideoPicker();
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



    @SuppressLint("ObsoleteSdkInt")
    private String getMediaPath(Uri uri, long mediaType) {

        String metadata, meta_id;
        Uri extUri;
        if (mediaType == Constants.IMAGE){
            metadata = MediaStore.Images.Media.DATA;
            extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Images.Media._ID;
        } else if (mediaType == Constants.VIDEO) {
            metadata =  MediaStore.Video.Media.DATA;
            extUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Video.Media._ID;
        }else {
            metadata =  MediaStore.Audio.Media.DATA;
            extUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Audio.Media._ID;
        }

        String realPath = null;

        // SDK < API11
        if(mediaType ==1 || Build.VERSION.SDK_INT < 19) {
            String[] proj = {metadata};
            CursorLoader cursorLoader = new CursorLoader(listener.getViewContext(),
                    uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(metadata);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
                cursor.close();
            }
        }

        // SDK > 19 (Android 4.4)

        if (TextUtils.isEmpty(realPath)){
            getRealPathFromURI_API19(listener.getViewContext(), uri);
        }
        return realPath;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public String getRealPathFromURI_API19(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (RealPathUtil.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (RealPathUtil.isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads",
                        "content://downloads/public_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        if (mediaType ==Constants._AUDIO)
                         audio = RealPathUtil.geSongInfo(context, contentUri, null, null);
                       return RealPathUtil.getDataColumn(context, contentUri, null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "getRealPathFromURI_API19: wrong folder - " + contentUriPrefix);
                    }
                }
            }
            // MediaProvider
            else if (RealPathUtil.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };


                audio = RealPathUtil.geSongInfo(context, contentUri, null, null);

                return RealPathUtil.getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (RealPathUtil.isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            audio = RealPathUtil.geSongInfo(context, uri, null, null);

            return RealPathUtil.getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            audio = new SongInfo("", "", uri.getPath());
            return uri.getPath();
        }

        return null;
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
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri uri = result.getUri();
                    if (uri != null){
                        image.setImageURI(uri);
                        postUri= uri;
                        mediaType = Constants.IMAGE;
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    CropImage.ActivityResult mResult = CropImage.getActivityResult(data);
                    Exception error = mResult.getError();
                    error.printStackTrace();
                    break;
                case Constants.PICK_VIDEO_REQUEST:
                    postUri = data.getData();
                    mediaType = Constants.VIDEO;
                    if (data.getData()!= null) {
                        playSelectedVideoFrom();
                        Log.i(TAG, "onActivityResult: " +getMediaPath(postUri, Constants.VIDEO));
                        playVideoBtn.setVisibility(View.VISIBLE);
                        adjustVideoSize();
                    }
                    break;
                case VideoPicker.VIDEO_PICKER_REQUEST_CODE:
                    mediaType = Constants.VIDEO;
                    List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                    videoPath = mPaths.get(0);
                    Log.w(TAG, "onActivityResult: " + videoPath );
                    postUri = Uri.parse(videoPath);
                    playVideoBtn.setVisibility(View.VISIBLE);
                    playSelectedVideoFrom();
                    break;
                case Constants.PICK_AUDIO_REQUEST:
                    Log.w(TAG, "onActivityResult: " + data.toString() );
                    Log.d(TAG, "onActivityResult: debuging music" );
                    mediaType = Constants._AUDIO;
                    getMediaPath(data.getData(), Constants._AUDIO);
                    if(audio!=null)
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
