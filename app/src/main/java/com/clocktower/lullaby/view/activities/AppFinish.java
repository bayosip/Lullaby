package com.clocktower.lullaby.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import com.clocktower.lullaby.R;

public class AppFinish extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_finish);

            if(Build.VERSION.SDK_INT >= 21)finishAndRemoveTask();
            else finish();
    }
}
