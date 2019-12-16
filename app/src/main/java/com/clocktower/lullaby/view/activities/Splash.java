package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.transition.Explode;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        GeneralUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GeneralUtil.transitionActivity(Splash.this, Alarm.class);
            }
        }, 3000);
    }
}
