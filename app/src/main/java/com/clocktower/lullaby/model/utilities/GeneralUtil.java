package com.clocktower.lullaby.model.utilities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.clocktower.lullaby.App;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.view.activities.AppFinish;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import id.zelory.compressor.Compressor;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class GeneralUtil {
    private Context context;
    private Toast toast;
    private static final String APP_PREFS_NAME = "com.clocktower.lullaby.app_pref";
    private static SharedPreferences appPref;

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public static void message(String message) {
        Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show();
    }

    public static SharedPreferences getAppPref(Context context) {
        if (appPref == null) appPref = context.getSharedPreferences(APP_PREFS_NAME,
                Context.MODE_PRIVATE);

        return appPref;
    }

    public static Handler getHandler() {
        return uiHandler;
    }

    public static Bitmap getImageFromURL(String urlString){
        URL url = null;
        Bitmap bmp = null;
        try {
            url = new URL(urlString);
           bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }catch (NetworkOnMainThreadException e){
            e.printStackTrace();
        }
        return  bmp;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage, String title) {

        String path = CapturePhotoUtils.insertImage(inContext.getContentResolver(), inImage, title, null);
        return Uri.parse(path);
    }

    public static void exitApp(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) activity.finishAndRemoveTask();
        else activity.finish();
        Intent intent = new Intent(activity, AppFinish.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        activity.startActivity(intent);
    }

    public static String randomName() {
        String randomString = RandomStringUtils.randomAlphanumeric(20);
        if (!TextUtils.isEmpty(randomString))
            return randomString;
        else {
            Random generator = new Random();
            StringBuilder randomStringBuilder = new StringBuilder();
            int randomLength = generator.nextInt(20);
            char tempChar;
            for (int i = 0; i < randomLength; i++){
                tempChar = (char) (generator.nextInt(96) + 32);
                randomStringBuilder.append(tempChar);
            }
            return randomStringBuilder.toString();
        }
    }

    public static byte[] compressImgFromBitmap(Bitmap bitmap){


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] thumbData = baos.toByteArray();
            return thumbData;
        }
        else return null;
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath)
            throws Throwable
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        }
        finally
        {
            if (mediaMetadataRetriever != null)
            {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public static Drawable setADrawable(Activity activity, int drawableID) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21) {
            drawable = activity.getResources().getDrawable(drawableID,
                    activity.getApplicationContext().getTheme());
        } else {
            drawable = activity.getResources().getDrawable(drawableID);
        }
        return drawable;
    }

    public static void showAlertMessage(final Activity activity, String title, String message){
        final AlertDialog.Builder alertDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(activity,
                    android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(activity);
        }
        alertDialog.setCancelable(true)
                .setIcon(R.mipmap.ic_launcher).setMessage(message)
                .setTitle(title).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        GeneralUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    alertDialog.show();
                }catch (WindowManager.BadTokenException e){
                    e.printStackTrace();
                    Log.w(activity.getClass().getSimpleName(), e.fillInStackTrace());
                }
            }
        });
    }

    public static void transitionActivity(Activity oldActivity, Class newActivity) {
        Activity activity = oldActivity;
        if (Build.VERSION.SDK_INT >= 21) activity.finishAndRemoveTask();

        else activity.finish();
        oldActivity.startActivity(new Intent(oldActivity, newActivity));
        Animatoo.animateFade( oldActivity);
    }

    public static void transitionActivity(Activity oldActivity, Intent intent) {
        if (Build.VERSION.SDK_INT >= 21) oldActivity.finishAndRemoveTask();
        else oldActivity.finish();
        oldActivity.startActivity(intent);

        Animatoo.animateFade(oldActivity);
    }

    public static boolean isNetworkConnected(Context activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork !=null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI||
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)) {
            return activeNetwork.isConnected();
        }
        return false;
    }


    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(),
                View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static class ResizeAnimation extends Animation {
        private View mView;
        private float mToHeight;
        private float mFromHeight;

        private float mToWidth;
        private float mFromWidth;

        public ResizeAnimation(View v, float fromWidth, float fromHeight, float toWidth, float toHeight) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            float targetH = Math.max(mFromHeight, mToHeight);
            setDuration((int)(targetH / v.getContext().getResources().getDisplayMetrics().density));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height =
                    (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            ViewGroup.LayoutParams p = mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            mView.requestLayout();
        }
    }
}
