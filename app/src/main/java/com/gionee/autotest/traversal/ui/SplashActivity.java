package com.gionee.autotest.traversal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.gionee.autotest.traversal.R;

/**
 * Created by viking on 9/7/17.
 *
 * show splash screen only
 */

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
