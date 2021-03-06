package com.clocktower.lullaby;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.clocktower.lullaby.model.utilities.ServiceUtil;
import com.clocktower.lullaby.view.activities.ErrorActivity;
import com.google.firebase.FirebaseApp;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class App extends Application {

    public static Context context;
    private static final String TAG = "Coza Family";

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .logErrorOnRestart(true)
                .errorActivity(ErrorActivity.class) //default: null (default error activity)
                .apply();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        if(ServiceUtil.isServiceAlreadyRunningAPI16(context)){
            ServiceUtil.stopService(this);
        }
        super.onTerminate();
    }

}
