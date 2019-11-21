package com.clocktower.lullaby.view.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.clocktower.lullaby.R;
import com.clocktower.lullaby.model.utilities.GeneralUtil;

public class ErrorActivity extends AppCompatActivity {

    private Button restartApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        restartApp= findViewById(R.id.buttonRestartApp);

        restartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneralUtil.transitionActivity(ErrorActivity.this, Alarm.class);
            }
        });
    }
}
