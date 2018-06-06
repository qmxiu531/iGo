package com.gionee.autotest.traversal.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.ui.MainActivity;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.common.util.ShellUtil;

/**
 * Created by viking on 9/8/17.
 *
 * use this class to start a new iGo test service
 */

public class FireUiAutomatorTestService extends Service {

    private static final int NOTIFICATION_ID = 111 ;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SharedPreferences sp ;

    private String app_pkg ;
    private String max_run_time ;
    private String max_run_events ;
    private String max_app_restart_times ;
    private String max_throttle ;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Constant.TAG, "enter fire service onStartCommand") ;
        getArguments() ;
        fire() ;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    //adb shell am instrument -w -r   -e debug false -e class com.gionee.autotest.traversal.AutoTraversalMain
    // com.gionee.autotest.traversal.test/android.support.test.runner.AndroidJUnitRunner
    private void fire() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(Constant.TAG, "assemble command now...") ;
                StringBuilder command = new StringBuilder() ;
                command.append("am instrument --user 0 -w -r -e debug false") ;
                command.append(" -e class com.gionee.autotest.traversal.testcase.AutoTraversalMain") ;
                command.append(" -e command-mode false ") ;
                command.append(" -e target " );
                command.append(app_pkg) ;
                command.append(" -e max-runtime " );
                command.append(max_run_time) ;
                command.append(" -e max-steps " );
                command.append(max_run_events) ;
                command.append(" -e max-restart-times " );
                command.append(max_app_restart_times) ;
                command.append(" -e throttle " );
                command.append(max_throttle) ;
                command.append(" com.gionee.autotest.traversal.testcase.test/android.support.test.runner.AndroidJUnitRunner") ;
                Log.i(Constant.TAG, "command : " + command.toString()) ;
                ShellUtil.execCommand(command.toString(), false) ;
                Log.i(Constant.TAG, "execution command success...") ;
            }
        }).start();
    }

    private void startForegroundService(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(getString(R.string.foreground_service_title));
        builder.setContentText(getString(R.string.foreground_service_subtitle));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void getArguments() {
        Log.i(Constant.TAG, "enter fire service onCreate") ;
        startForegroundService();
        sp                      = getSharedPreferences(Constant.SHARED_PERF, Context.MODE_PRIVATE) ;
        app_pkg                 = sp.getString(Constant.EXTRA_CHOOSE_APP_PKG, null) ;
        max_run_time            = sp.getString(Constant.EXTRA_MAX_RUNTIME, null) ;
        max_run_events          = sp.getString(Constant.EXTRA_MAX_EVENTS, "0") ;
        max_app_restart_times   = sp.getString(Constant.EXTRA_MAX_APP_RESTART_TIMES, "0") ;
        max_throttle            = sp.getString(Constant.EXTRA_MAX_THROTTLE_TIME, "1000") ;

        Log.i(Constant.TAG, "In service package name : " + app_pkg) ;
        Log.i(Constant.TAG, "In service max run time : " + max_run_time) ;
        Log.i(Constant.TAG, "In service max run events : " + max_run_events) ;
        Log.i(Constant.TAG, "In service max app restart times : " + max_app_restart_times) ;
        Log.i(Constant.TAG, "In service max throttle : " + max_throttle) ;
    }
}
