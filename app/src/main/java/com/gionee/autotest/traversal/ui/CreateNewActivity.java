package com.gionee.autotest.traversal.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.util.Util;
import com.gionee.autotest.traversal.service.FireUiAutomatorTestService;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.widget.MarqueeTextView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by viking on 9/6/17.
 *
 * create new iGo task activity
 */

public class CreateNewActivity extends AppCompatActivity{

    private static final int REQUEST_CODE_TIME = 100 ;

    private SharedPreferences sp ;
    private SharedPreferences.Editor editor ;

    @Bind(R.id.choose_application)  ImageView chooseApp ;
    @Bind(R.id.config_pkg)          MarqueeTextView chooseAppView ;
    @Bind(R.id.choose_max_runtime)  ImageView chooseMaxRunTime ;
    @Bind(R.id.max_runtime_tv)      TextView maxRuntimeView ;
    /*@Bind(R.id.max_events_tv)       EditText maxEventsView ;
    @Bind(R.id.max_app_restart_tv)  EditText maxRestartTimesView ;
    @Bind(R.id.max_throttle)        EditText throttle ;*/
    @Bind(R.id.fab_done)            FloatingActionButton doneFab ;

    String app_name, app_pkg  ;
    String maxRuntime ;
/*    String maxEvents ;
    String maxRestartTimes ;
    String maxThrottle ;*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        ButterKnife.bind(this);
        sp = getSharedPreferences(Constant.SHARED_PERF, Context.MODE_PRIVATE) ;
        editor = sp.edit() ;
        setAllPreferencesValue() ;
    }

    private void setAllPreferencesValue(){
        //set package
        app_name = sp.getString(Constant.EXTRA_CHOOSE_APP_NAME, null) ;
        app_pkg  = sp.getString(Constant.EXTRA_CHOOSE_APP_PKG, null) ;
        if (app_name != null && app_pkg != null){
            chooseAppView.setText(String.format("%s(%s)", app_name, app_pkg));
        }

        //set max run time
        maxRuntime = sp.getString(Constant.EXTRA_MAX_RUNTIME, null) ;
        if (maxRuntime != null){
            maxRuntimeView.setText(maxRuntime);
        }

        //set max events
       /* maxEvents = sp.getString(Constant.EXTRA_MAX_EVENTS, "500") ;
        maxEventsView.setText(maxEvents);*/

        //set max restart app times
/*        maxRestartTimes = sp.getString(Constant.EXTRA_MAX_APP_RESTART_TIMES, "500") ;
        maxRestartTimesView.setText(maxRestartTimes);*/

        //set throttle time
/*        maxThrottle = sp.getString(Constant.EXTRA_MAX_THROTTLE_TIME, "500") ;
        throttle.setText(maxThrottle);*/
    }

    @OnClick(R.id.choose_application)
    public void onChooseAppClicked(View view){
        Intent chooseAppIntent = new Intent(this, ChooseAppActivity.class) ;
        startActivityForResult(chooseAppIntent, Constant.REQUEST_CODE_CHOOSE_APPLICATION);
    }

    @OnClick(R.id.choose_max_runtime)
    public void onMaxRunTimeClicked(View view){
        startActivityForResult(new Intent(CreateNewActivity.this, CreateTimeActivity.class), REQUEST_CODE_TIME);
    }

    @OnClick(R.id.fab_done)
    public void onDoneFABClicked(View view){
        if (!checkAllArguments()){
            return ;
        }
        Util.createRootDirectory() ;
        //copy template file
        String traversal_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME
                + File.separator + "report_template.html";
        Util.copyFile(getApplicationContext(), "report_template.html" , traversal_path);

        //stop first
        Intent stop = new Intent(this, FireUiAutomatorTestService.class) ;
        startService(stop) ;

        //set all values
        maxRuntime = maxRuntimeView.getText().toString() ;
        if (maxRuntime.isEmpty()){
            maxRuntime = "0" ;
        }

/*        maxEvents = maxEventsView.getText().toString() ;
        if (maxEvents.isEmpty()){
            maxEvents = "0" ;
        }*/

/*        maxRestartTimes = maxRestartTimesView.getText().toString() ;

        maxThrottle = throttle.getText().toString() ;*/

        //fire it
        Log.i(Constant.TAG, "package name : "           + app_pkg) ;
        Log.i(Constant.TAG, "app name : "               + app_name) ;
        Log.i(Constant.TAG, "max run time : "           + maxRuntime) ;
/*        Log.i(Constant.TAG, "max run events : "         + maxEvents) ;
        Log.i(Constant.TAG, "max app restart times : "  + maxRestartTimes) ;
        Log.i(Constant.TAG, "event throttle time : "    + maxThrottle) ;*/

        editor.putString(Constant.EXTRA_CHOOSE_APP_NAME, app_name) ;
        editor.putString(Constant.EXTRA_CHOOSE_APP_PKG, app_pkg) ;
        editor.putString(Constant.EXTRA_MAX_RUNTIME, maxRuntime);
        //set default value
/*        editor.putString(Constant.EXTRA_MAX_EVENTS, "0");
        editor.putString(Constant.EXTRA_MAX_APP_RESTART_TIMES, "500");
        editor.putString(Constant.EXTRA_MAX_THROTTLE_TIME, "1000");*/
        editor.commit() ;

        //start service
        Log.i(Constant.TAG, "stop service first...") ;
        Intent start = new Intent(this, FireUiAutomatorTestService.class) ;
        startService(start) ;

        //finish it
        finish();
    }

    private boolean checkAllArguments(){
        // check application set or not
        if (app_pkg == null || app_pkg.isEmpty()){
            Log.i(Constant.TAG, "application not set error") ;
            Toast.makeText(this, R.string.error_no_pkg, Toast.LENGTH_SHORT).show();
            return false ;
        }
        maxRuntime = maxRuntimeView.getText().toString() ;
//        maxEvents = maxEventsView.getText().toString() ;
        boolean runtimeSet = !(maxRuntime.isEmpty() || "0".equals(maxRuntime)) ;
//        boolean maxEventsSet = !(maxEvents == null || maxEvents.isEmpty() || "0".equals(maxEvents)) ;

        Log.i(Constant.TAG, "runtimeSet value : " + runtimeSet) ;
//        Log.i(Constant.TAG, "maxEventsSet value : " + maxEventsSet) ;

        //check runtime
        if (!runtimeSet){
            Log.i(Constant.TAG, "max run time or max event not set at the same time") ;
            Toast.makeText(this, R.string.error_no_runtime_and_events_count, Toast.LENGTH_SHORT).show();
            return false ;
        }

        //check min value setup
        try {
            int maxRuntimeSec = Integer.parseInt(maxRuntime) ;
            if (maxRuntimeSec < 60){
                throw new NumberFormatException("I did it") ;
            }
        }catch (NumberFormatException e){
            Log.i(Constant.TAG, "convert max runtime exception.") ;
            Toast.makeText(this, R.string.error_no_runtime_less, Toast.LENGTH_SHORT).show();
            return false ;
        }



        /*//check event count max value
        String filledSteps = maxEventsView.getText().toString() ;
        try {
            if (!filledSteps.isEmpty()){
                Log.i(Constant.TAG, "check max events value ") ;
                int result = Integer.parseInt(filledSteps) ;
                Log.i(Constant.TAG, "check max events value : " + result) ;
                if (result != 0 && result <= 10){
                    throw new NumberFormatException("I throw it") ;
                }
            }
        }catch (NumberFormatException e){
            Log.i(Constant.TAG, "number format exception on maxEventsView") ;
            Toast.makeText(this, R.string.error_max_events, Toast.LENGTH_SHORT).show();
            return false ;
        }

        //check restart app times
        String filledRestartTime = maxRestartTimesView.getText().toString() ;
        if (filledRestartTime.isEmpty()){
            Log.i(Constant.TAG, "restart application times not set") ;
            Toast.makeText(this, R.string.no_application_restart_times, Toast.LENGTH_SHORT).show();
            return false ;
        }
        try{
            int num = Integer.parseInt(filledRestartTime) ;
            if (num < 50 || num > 500){
                throw new NumberFormatException("I throw it") ;
            }
        }catch (NumberFormatException e){
            Log.i(Constant.TAG, "number format exception on maxRestartTimesView") ;
            Toast.makeText(this, R.string.error_max_restart_times, Toast.LENGTH_SHORT).show();
            return false ;
        }

        //check throttle
        String filledThrottle = throttle.getText().toString() ;
        if (filledThrottle.isEmpty()){
            Log.i(Constant.TAG, "throttle not set") ;
            Toast.makeText(this, R.string.no_throttle_times, Toast.LENGTH_SHORT).show();
            return false ;
        }
        try{
            int num = Integer.parseInt(filledThrottle) ;
            if (num < 100 || num > 10000){
                throw new NumberFormatException("I throw it") ;
            }
        }catch (NumberFormatException e){
            Log.i(Constant.TAG, "number format exception on maxRestartTimesView") ;
            Toast.makeText(this, R.string.error_throttle_times, Toast.LENGTH_SHORT).show();
            return false ;
        }*/
        Log.i(Constant.TAG, "all argument check passed") ;
        return true ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == Constant.REQUEST_CODE_CHOOSE_APPLICATION){
                app_name = data.getStringExtra(Constant.EXTRA_CHOOSE_APP_NAME) ;
                app_pkg  = data.getStringExtra(Constant.EXTRA_CHOOSE_APP_PKG)  ;
                Log.i(Constant.TAG, "name : " + app_name) ;
                Log.i(Constant.TAG, "pkg : " + app_pkg) ;
                chooseAppView.setText(String.format("%s(%s)", app_name, app_pkg));
            }else if (requestCode == REQUEST_CODE_TIME){
                int hour = data.getIntExtra(Constant.EXTRA_HOUR , 0) ;
                int minute = data.getIntExtra(Constant.EXTRA_MINUTE , 0) ;
                int second = data.getIntExtra(Constant.EXTRA_SECOND , 0) ;
                int totalSeconds = 0 ;
                if (hour != 0){
                    totalSeconds = hour * 60 * 60 ;
                }
                if (minute != 0){
                    totalSeconds += minute * 60 ;
                }
                if (second != 0){
                    totalSeconds += second ;
                }
                Log.i(Constant.TAG, "total seconds is : " + totalSeconds) ;
                String text = totalSeconds + "" ;
                if (!text.isEmpty() && !text.equals(maxRuntime)){
                    maxRuntime = text ;
                    maxRuntimeView.setText(maxRuntime);
                }
            }
        }else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_TIME){
            maxRuntime = "0" ;
            maxRuntimeView.setText(maxRuntime);
        }
    }
}
