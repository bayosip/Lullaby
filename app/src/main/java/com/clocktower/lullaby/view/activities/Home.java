package com.clocktower.lullaby.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.clocktower.lullaby.interfaces.HomeViewInterFace;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.interfaces.ProfileListener;
import com.clocktower.lullaby.model.Comments;
import com.clocktower.lullaby.model.Post;
import com.clocktower.lullaby.model.SongInfo;
import com.clocktower.lullaby.model.utilities.Constants;
import com.clocktower.lullaby.model.utilities.FirebaseUtil;
import com.clocktower.lullaby.model.utilities.GeneralUtil;
import com.clocktower.lullaby.presenter.HomePresenter;
import com.clocktower.lullaby.view.NonSwipeableViewPager;
import com.clocktower.lullaby.view.fragments.home.ChatFragment;
import com.clocktower.lullaby.view.fragments.home.CommentsFragment;
import com.clocktower.lullaby.view.fragments.home.CreatePostFragment;
import com.clocktower.lullaby.view.fragments.home.FullscreenFragment;
import com.clocktower.lullaby.view.fragments.home.HomePageFragmentAdapter;
import com.clocktower.lullaby.view.fragments.home.RandomFragment;
import com.clocktower.lullaby.view.fragments.home.SchedulerSetterFragment;
import com.clocktower.lullaby.view.fragments.home.BaseFragment;
import com.clocktower.lullaby.view.fragments.home.BlogFragment;
import com.clocktower.lullaby.view.fragments.home.MusicSelectorDialog;
import com.clocktower.lullaby.view.fragments.home.TrackSetterFragment;
import com.clocktower.lullaby.view.fragments.login.Profile_creation_frag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.clocktower.lullaby.model.utilities.Constants.COMMENTS;
import static com.clocktower.lullaby.model.utilities.Constants.CREATE_POST;
import static com.clocktower.lullaby.model.utilities.Constants.PROFILE;

public class Home extends AppCompatActivity implements HomeViewInterFace, ProfileListener {

    private static final String TAG = "Home";
    private NonSwipeableViewPager pager;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;
    private int flag;
    private HomePageFragmentAdapter adapter;
    private List<BaseFragment> fragmentList;
    private TextView title;
    private BlogFragment blogFrag;
    private TrackSetterFragment trackFrag;
    private SchedulerSetterFragment alarmFrag;
    private CommentsFragment commentFrag;
    private FullscreenFragment fullFrag;
    private Profile_creation_frag profileFrag;
    private CreatePostFragment createFrag;
    private ChatFragment chatFrag;
    private MusicSelectorDialog musicSelectorDialog;
    private MediaController mediaController;
    private HomePresenter presenter;
    private Toolbar toolbar;
    private List<SongInfo> audioFiles = new ArrayList<>();
    private SongInfo chosenSong;
    private ImageView homeProfile;
    private TextView user_name;
    private View commentView;
    private String mUsername;
    FirebaseUser user;
    private View simple, profile;
    private ContentLoadingProgressBar progressBar;
    private boolean isUserAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisePrequisites();
        setupActionBar();
        initialiseWidgets();
    }

    private void initialisePrequisites() {
        user = FirebaseUtil.getmAuth().getCurrentUser();
        if (user!=null)mUsername = user.getDisplayName();
        presenter = new HomePresenter(this);
        fragmentList = new LinkedList<>();
        blogFrag = BlogFragment.getInstance(mUsername);
        chatFrag = ChatFragment.getInstance(mUsername);
        alarmFrag = SchedulerSetterFragment.getInstance();
        trackFrag = TrackSetterFragment.getInstance();
        musicSelectorDialog = MusicSelectorDialog.getInstance();
        mediaController = new MediaController(this);

        fragmentList.add(blogFrag);
        fragmentList.add(alarmFrag);
        fragmentList.add(trackFrag);
        fragmentList.add(chatFrag);
        fragmentList.add(new RandomFragment());

        adapter = new HomePageFragmentAdapter(getSupportFragmentManager(), fragmentList);
    }

    private void setupActionBar() {
        toolbar = findViewById(R.id.appbar_home);
        title = findViewById(R.id.frag_toolbar_name);
        user_name = findViewById(R.id.textHomeUserName);
        user_name.setText(mUsername);
        homeProfile = findViewById(R.id.imageHomeProfile);
        progressBar = findViewById(R.id.loading_progress);
        progressBar.hide();

        if (user.getPhotoUrl()!=null) {
            Ion.with(this)
                    .load(user.getPhotoUrl().toString())
                    .withBitmap()
                    .placeholder(R.drawable.ic_person_24dp)
                    .intoImageView(homeProfile);

            Log.w(TAG, "setupActionBar: " + user.getPhotoUrl().toString() + " // "
                    + GeneralUtil.getAppPref(this).getString(Constants.PROFILE, null));
        }
        bottomNavigationView = findViewById(R.id.navigationView);
        simple = findViewById(R.id.simpleToolbarView);
        profile = findViewById(R.id.profileToolbarView);

        presenter.checkIfUserIsAdmin(FirebaseUtil.getmAuth().getCurrentUser().getUid());
        setupHomeActionBar();
    }

    @Override
    public void removeBNBItemIfNoAdmin(boolean isAdmin) {
        isUserAdmin = isAdmin;
        if(!isAdmin)
            bottomNavigationView.getMenu().removeItem(R.id.navigation_create);
    }

    @Override
    public boolean isUserAdmin() {
        return isUserAdmin;
    }

    private void setupHomeActionBar() {

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        simple.setVisibility(View.GONE);
        profile.setVisibility(View.VISIBLE);

        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(false); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);
    }


    private void setupOtherActionBar() {
        // Get the ActionBar here to configure the way it behaves.
        setSupportActionBar(toolbar);
        simple.setVisibility(View.VISIBLE);
        profile.setVisibility(View.GONE);
        // Get the ActionBar here to configure the way it behaves.
        final ActionBar ab = getSupportActionBar();

//        ab.setDisplayOptions();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)
    }



    private void initialiseWidgets() {
        setupActionBar();
        commentView = findViewById(R.id.home_fragment_container);
        pager = findViewById(R.id.page_container);
        fab = findViewById(R.id.buttonMusicContent);
        fab.hide();
        fab.setOnClickListener(view -> {
            if (audioFiles != null) {
                MusicSelectorDialog.setAudioFiles(audioFiles);
                musicSelectorDialog.show(getSupportFragmentManager());
            } else GeneralUtil.showAlertMessage(Home.this, "Error!",
                    "No Audio Files Found");
        });
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(pageChangeListener);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener  navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.navigation_home:
                            pager.setCurrentItem(0);
                            return true;
                        case R.id.navigation_create:
                            startPostCreationPage();
                            return  true;
                        case R.id.navigation_alarm:
                            pager.setCurrentItem(1);
                            return true;
                        case R.id.navigation_music:
                            pager.setCurrentItem(2);
                            return true;
                        case R.id.navigation_forum:
                            pager.setCurrentItem(3);
                            return true;
                    }
                    return false;
                }
            };

    private void startPostCreationPage() {
        fab.hide();
        if(isUserAdmin) {
            pager.setCurrentItem(4);
            GeneralUtil.getHandler().postDelayed(() -> {
                pager.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);

                createFrag = CreatePostFragment.newInstance();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.add(R.id.home_fragment_container, createFrag, Constants.CREATE_POST);
                fragmentTransaction.commitAllowingStateLoss();
                commentView.setVisibility(View.VISIBLE);
                title.setText("Create Post");
                setupOtherActionBar();
            }, 300);

        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {
            if (adapter.getPageTitle(position).equals(Constants.HOME)) {
                blogFrag.clearList();
                setupHomeActionBar();
                fab.hide();
            }else if (adapter.getPageTitle(position).equals(Constants.ALARM_SETTER)){
                fab.hide();
                flag = Constants.SET_ALARM_TRACK_FLAG;
                title.setText(Constants.ALARM_SETTER);
                setupOtherActionBar();
            } else if (adapter.getPageTitle(position).equals(Constants.MUSIC_SELECTOR)) {
                fab.setImageResource(R.drawable.ic_queue_music_24dp);
                flag = Constants.TRACK_SELECTOR_FLAG;
                setupOtherActionBar();
                if(audioFiles!=null)
                    audioFiles.clear();
                else {
                    audioFiles = new ArrayList<>();
                }
                presenter.loadMusicTracks();
                title.setText(Constants.MUSIC_SELECTOR);
                fab.show();
            }else if (adapter.getPageTitle(position).equals(Constants.FORUM)){
                fab.hide();
                setupHomeActionBar();
            }else if (adapter.getPageTitle(position).equals(Constants.RANDOM)){
                fab.hide();
                setupOtherActionBar();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    public void updateTrackList(SongInfo audio) {
        audioFiles.add(audio);
    }


    public void accessFilesFromPhone() {
        Dexter.withActivity(Home.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        audioFiles= presenter.loadSongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }

    @Override
    public void showAudioFromDevice() {
        if (audioFiles != null) {
            MusicSelectorDialog.setAudioFiles(audioFiles);
            musicSelectorDialog.show(getSupportFragmentManager());

        } else{
            createFrag.selectAudioOrVideoMedia(Constants._AUDIO);
        }
    }

    @Override
    public void changePlayButtonIcon(int resID) {
        if(createFrag == null) {
            trackFrag.changePlayButtonRes(resID);
        }else {
            createFrag.changePlayButtonRes(resID);
        }
    }

    @Override
    public void playSelectedAudio(SongInfo audio) {
        chosenSong = audio;
        if(createFrag == null) {
            trackFrag.selectMusic(chosenSong.getTrackName());
        }else {
            createFrag.selectMusic(audio.getTrackName());
        }
        changePlayButtonIcon(R.drawable.ic_pause_24dp);
        presenter.startNewMusic(chosenSong.getUrl());
        musicSelectorDialog.dismiss();
    }

    @Override
    public void setAlarm(int hour, int minute) {
        presenter.setAlarm(hour, minute);
    }

    @Override
    public void stopAlarm() {
        presenter.cancelAlarm();
    }

    @Override
    public void playOrPauseMusic(FragmentManager manager, boolean isClicked) {
        if (!presenter.musicIsPlaying()) {
            if (chosenSong != null) {
                presenter.playMusic(chosenSong.getUrl());
                changePlayButtonIcon(R.drawable.ic_pause_24dp);
            }else {
                if (audioFiles != null) {
                    musicSelectorDialog.show(manager);
                }
            }
        } else {
            presenter.pauseMusic();
            changePlayButtonIcon(R.drawable.ic_play_arrow_24dp);
        }
    }

    @Override
    public void showMusicBuffer() {
        trackFrag.show();
    }

    @Override
    public void hideMusicBuffer() {
        trackFrag.hide();
    }

    @Override
    public void stopMusic() {
        presenter.stopMusic();
        trackFrag.changePlayButtonRes(R.drawable.ic_play_arrow_24dp);
    }

    @Override
    public boolean musicPlaying() {
        return presenter.musicIsPlaying();
    }

    @Override
    public MediaController getVideoMediaController() {
        return mediaController;
    }

    @Override
    public void setAlarmMusic() {
        if(chosenSong!=null)
            presenter.setAlarmTone(chosenSong.getUrl());
        else GeneralUtil.message("Select A Song First...");
    }

    @Override
    public void seekMusicToPosition(int position) {
        presenter.seekMusic(position);
    }

    @Override
    public void updateTrackBar(int time) {
        if(createFrag==null)
            trackFrag.setTrackBarProgress(time);
        else
            createFrag.setTrackBarProgress(time);
    }

    @Override
    public Home getViewContext() {
        return Home.this;
    }

    @Override
    public void updateLikesCount(String id) {
        presenter.getLikePostForPost(id);
    }

    @Override
    public void likeThisPost(String id) {
        presenter.likePost(id);
    }

    @Override
    public void setTrackDuration(int duration) {
        if(createFrag==null)
            trackFrag.calibrateTrackBarForMusic(duration);
        else
            createFrag.calibrateTrackBarForMusic(duration);
    }

    @Override
    public void goToMusicSetter() {
        pager.setCurrentItem(2);
    }

    @Override
    public void startLoadingPostsFromFirebase() {
        presenter.firstPageFirstLoad();
    }

    @Override
    public void loadMorePost() {
        presenter.loadMorePost();
    }

    @Override
    public void updateBlogWith(Post post) {
        runOnUiThread(()->
        blogFrag.updateAdapter(post));
    }

    @Override
    public void updatePostLikesCount(String id, int count) {
        runOnUiThread(()->
        blogFrag.updateLikeCount(id, count));
    }

    @Override
    public void updateLikeBtnImg(String id, boolean exists) {
        runOnUiThread(()->
        blogFrag.updateLikeBtnImg(id, exists));
    }

    @Override
    public void updatePostComments(final Comments comments) {
        if (commentFrag!=null) {
            runOnUiThread(()->
            commentFrag.updateAdapter(comments));
        }
    }

    @Override
    public void openCommentSectionOnPostWithId(String postID, String title) {
        pager.setVisibility(View.GONE);
        bottomNavigationView.setVisibility(View.GONE);

        commentFrag = CommentsFragment.newInstance(postID, title);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.home_fragment_container, commentFrag, COMMENTS);
        fragmentTransaction.commitAllowingStateLoss();
        commentView.setVisibility(View.VISIBLE);
        this.title.setText("Comments");
        setupOtherActionBar();
    }

    private void startFullScreenFragment(){
        fab.hide();
        pager.setVisibility(View.GONE);
        removeToolbars();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.home_fragment_container, fullFrag, Constants.FULLSCREEN);
        fragmentTransaction.commitAllowingStateLoss();
        commentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void makeVideoFullScreen(String url, int currentPosition) {

        fullFrag = FullscreenFragment.getVideoInstance(url, currentPosition);
        fullFrag.setTargetFragment(blogFrag, Constants.PLAY_BACK_CODE);
        fullFrag.setMediaController(mediaController);
        startFullScreenFragment();
    }

    @Override
    public void makeFullPicture(Bitmap bitmap) {
        fullFrag = FullscreenFragment.getImgInstance(bitmap);
        startFullScreenFragment();
    }

    @Override
    public void updateCommentCount(String postId) {
        presenter.getNumberOfComments(postId);
    }

    @Override
    public void retrieveAllComments(String postId) {
        presenter.loadCommentsForPostWithID(postId);
    }

    @Override
    public void postACommentOnPostWithId(String postId, String msg) {
        presenter.makeCommentOnPostWithID(postId, msg);
    }

    @Override
    public void updatePostCommentsCount(String postID, int count) {
        blogFrag.updateCommentCount(postID, count);
    }

    @Override
    public void clearBlogList() {
        blogFrag.clearList();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public Fragment getCurrentFragmentInView() {
        return getSupportFragmentManager().findFragmentById(R.id.page_container );
    }

    @Override
    public void restoreViewsAfterLeavingCommentSection() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void removeToolbars() {
        bottomNavigationView.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
    }

    @Override
    public void startProfilePictureFragment(String name){
        pager.setVisibility(View.GONE);
        setupOtherActionBar();
        title.setText("Profile");
        bottomNavigationView.setVisibility(View.GONE);
        commentView.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        profileFrag = Profile_creation_frag.getInstance(name);
        fragmentTransaction.add(R.id.home_fragment_container, profileFrag, PROFILE);
        fragmentTransaction.commit();
    }

    private void logOut() {
        FirebaseUtil.getmAuth().signOut();
        sendToLogin();
        presenter.removeListenerRegistration();
    }

    private void sendToLogin() {
        GeneralUtil.transitionActivity(Home.this,
                Splash.class);
    }

    @Override
    public void goStraightToHomePage(String getName) {
       onBackPressed();
    }

    @Override
    public void saveUserNameintoDb(String name) {
    }

    @Override
    public boolean saveProfilePictureInDb(Bitmap bitmap) {
        return FirebaseUtil.saveProfilePictureOnFireBase(bitmap, user, Home.this);
    }

    @Override
    public void disableScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void changeProfilePic(String url) {
        Ion.with(this)
                .load(url)
                .withBitmap()
                .intoImageView(homeProfile);
    }

    @Override
    public void retryLogin() {
    }

    @Override
    public void enableScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean saveProfilePictureInDb(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = Ion.with(Home.this)
                    .load(uri.toString()).withBitmap().asBitmap().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return FirebaseUtil.saveProfilePictureOnFireBase(bitmap, user, Home.this);
    }

    @Override
    public void saveNewPostInDB(Post post, long type) {
        if (type>0) {
            presenter.saveMediaPostAttachmentInStorage(post);
        }else {
            presenter.storePostDataInFirestore(null, post);
        }
    }

    @Override
    public void saveNewAudioInDb(SongInfo audio) {
        if (audio!=null)chosenSong = audio;
        presenter.saveAudioInStorage(chosenSong);
    }

    @Override
    public void initialiseLogin() {
    }

    @Override
    public Activity getLoginActivity() {
        return Home.this;
    }

    @Override
    public void hidePB() {
        progressBar.hide();
        enableScreen();
    }

    @Override
    public void showPB() {
        progressBar.show();
        disableScreen();
    }

    @Override
    public void progressPB(long progress) {
        runOnUiThread(() -> {
            progressBar.setProgress((int) progress);
        });
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.removeListenerRegistration();
    }


    @Override
    public void onBackPressed() {
        Fragment fragment= getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
        if(fragment!=null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.remove(fragment).commitAllowingStateLoss();
            restoreViewsAfterLeavingCommentSection();
            commentView.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
            setupHomeActionBar();

            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            String tag = fragment.getTag();
            if (!TextUtils.isEmpty(tag) && tag.equals(PROFILE)){
                blogFrag.clearList();
                Ion.with(Home.this)
                        .load(user.getPhotoUrl().toString())
                        .withBitmap()
                        .placeholder(R.drawable.ic_person_24dp)
                        .intoImageView(homeProfile);
            }else if(!TextUtils.isEmpty(tag) && tag.equals(CREATE_POST)){
                blogFrag.clearList();
                createFrag = null;
            }

        }else if ( pager.getCurrentItem() == 0) {
            GeneralUtil.exitApp(Home.this);
        }
        else {
            pager.setCurrentItem(0);
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        MenuInflater inflater = getMenuInflater () ;
        inflater.inflate (R.menu.home_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_account:
                startProfilePictureFragment(mUsername);
                return true;
            case R.id.home_log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}