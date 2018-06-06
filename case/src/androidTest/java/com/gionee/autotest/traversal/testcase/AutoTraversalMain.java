package com.gionee.autotest.traversal.testcase;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import com.gionee.autotest.traversal.common.util.Util;
import com.gionee.autotest.traversal.testcase.strategy.AndroidAITraversal;
import com.gionee.autotest.traversal.testcase.strategy.AndroidTraversal;
import com.gionee.autotest.traversal.testcase.strategy.Traversal;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.util.UiHelper;
import com.gionee.autotest.traversal.testcase.util.VLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Author : Viking Den<dengwj@gionee.com>
 * Time : 7/24/17 11:12 AM
 *
 * AutoTraversal test using Android UiAutomator 2.0
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class AutoTraversalMain {

    private UiDevice mDevice ;
    /**
     * Maybe we should do some init staff, el. screenshot directory create
     */
    @Before
    public void setUp() {
        getArguments();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        VLog.d("AutoTraversal version : " + Config.VERSION);
        VLog.d("enter setUp.");
        VLog.d("fire start exception catch engine...");
        VLog.d("launch home app");
        UiHelper.launchHome(mDevice);
    }

    @Test
    public void crazy(){
        VLog.d("enter test main entry.");
        Traversal traversal = new AndroidTraversal(mDevice);
//        Traversal traversal = new AndroidAITraversal(mDevice);
        traversal.run();
        VLog.d("enter test main finished.");
    }

    /**
     * Maybe we should do some cleanup staff
     */
    @After
    public void tearDown() throws Exception {
        VLog.d("enter tearDown.");
        //kill target package
        if (mDevice != null)
            UiHelper.forceStopPackage(mDevice, Config.sTargetPackage);
        //end VLog
        VLog.terminal();
    }

    /**
     * Read all arguments here
     */
    private void getArguments(){
        VLog.d("enter getArguments.");
        Bundle arguments = InstrumentationRegistry.getArguments();
        if (arguments.getString("log-mode") != null){
            try{
                int value = Integer.valueOf(arguments.getString("log-mode")) ;
                if (!(value >= VLog.NONE && value <= VLog.ALL)){
                    throw new IllegalArgumentException("log-mode value should be larger than zero, and lower than three.") ;
                }
                Config.sLogMode = value ;
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("log-mode should greater than 0, and lower than 3") ;
            }

        }else{
            //TODO turn off this when release
            Config.sLogMode = VLog.ALL ;
        }
        initLog() ;

        if (arguments.getString("target") != null) {
            Config.sTargetPackage = arguments.getString("target");
            if (Config.sTargetPackage == null || Config.sTargetPackage.isEmpty()){
                throw new IllegalArgumentException("target application should not be empty, please specify it.") ;
            }
            if (!Util.isInstallApp(InstrumentationRegistry.getContext(), Config.sTargetPackage)){
                throw new IllegalArgumentException("target application not exist in system, please adjust it.") ;
            }
        }

        if (arguments.getString("max-steps") != null) {
            try{
                Config.sMaxSteps = Long.valueOf(arguments.getString("max-steps"));
                if (Config.sMaxSteps <= 10 && Config.sMaxSteps != 0){
                    throw new IllegalArgumentException("max steps should greater than 10.") ;
                }
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("max steps should greater than zero and less than Long.MAX_VALUE.") ;
            }
        }

        if (arguments.getString("max-runtime") != null) {
            try{
                Config.sMaxRuntime = Integer.valueOf(arguments.getString("max-runtime"));
                if (Config.sMaxRuntime < 60 && Config.sMaxRuntime != 0){
                    throw new IllegalArgumentException("max runtime should greater than one minute.") ;
                }
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("max runtime should greater than zero and less than Long.MAX_VALUE.") ;
            }
        }

        if (arguments.getString("max-restart-times") != null){
            try{
                Config.sRestartAppTimes = Integer.valueOf(arguments.getString("max-restart-times")) ;
                if ((Config.sRestartAppTimes < 50 || Config.sRestartAppTimes > 500) && Config.sRestartAppTimes != 0){
                    throw new IllegalArgumentException("max application restart times should greater than 50 or less than 500.") ;
                }
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("max application restart times should greater than zero and less than Long.MAX_VALUE.") ;
            }
        }

        if (arguments.getString("throttle") != null){
            try{
                Config.sThrottle = Integer.valueOf(arguments.getString("throttle")) ;
                if (Config.sThrottle < 100){
                    throw new IllegalArgumentException("throttle time lower than 100ms make no sense.") ;
                }
                if (Config.sThrottle > 10000){
                    throw new IllegalArgumentException("throttle time greater than 3000ms make no sense.") ;
                }
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("throttle time should greater than 100 and less than 3000.") ;
            }

        }

        if (Config.sMaxRuntime == -1 && Config.sMaxSteps == -1){
            throw new IllegalArgumentException("max runtime or max steps should not -1 at the same time.") ;
        }

        if (arguments.getString("command-mode") != null){
            Config.sCommandMode = false ;
        }

        if (arguments.getString("seed") != null){
            try{
                Config.sSeed = Integer.valueOf(arguments.getString("seed")) ;
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("seed should be positive and in range.") ;
            }
        }

        VLog.i("---------------all arguments list below---------------");
        VLog.i("---target application   : " + Config.sTargetPackage);
        VLog.i("---max runtime          : " + Config.sMaxRuntime);
        VLog.i("---max steps            : " + Config.sMaxSteps);
        VLog.i("---command mode         : " + Config.sCommandMode);
        VLog.i("---log mode             : " + Config.sLogMode);
        VLog.i("---throttle             : " + Config.sThrottle);
        VLog.i("---time stamp           : " + Config.sCurTimeStamp);
        VLog.i("---max app restart time : " + Config.sRestartAppTimes);
        VLog.i("---seed                 : " + Config.sSeed);
    }

    /**
     * this should load first as much as we can
     */
    private static void initLog() {
        Log.i(Constant.TAG, "initLog") ;
        //set current timestamp
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
        Config.sCurTimeStamp = mDateFormat.format(new Date()) ;
        //create directories
        File root = new File(Environment.getExternalStorageDirectory(), Constant.ROOT_NAME) ;
        if (!root.exists()) {
            VLog.i("traversal directory not exist , create it...");
            if(!root.mkdirs()){
                VLog.i("traversal directory create failed...");
                return ;
            }
        }
        //create timestamp directory
        File timestamp = new File(root, Config.sCurTimeStamp) ;
        if (!timestamp.exists()) {
            VLog.i("timestamp directory not exist , create it...");
            if(!timestamp.mkdirs()){
                VLog.i("timestamp directory create failed...");
                return ;
            }
        }
        //set current workspace
        Config.sWorkSpace = timestamp.getAbsolutePath() ;
        //create sub-directories under timestamp
        File log = new File(timestamp, Constant.DIR_LOG) ;
        if (!log.exists()) {
            VLog.i("log directory not exist , create it...");
            if(!log.mkdirs()){
                VLog.i("log directory create failed...");
                return ;
            }
        }
        File exception = new File(timestamp, Constant.DIR_EXCEPTION) ;
        if (!exception.exists()) {
            VLog.i("exception directory not exist , create it...");
            if(!exception.mkdirs()){
                VLog.i("exception directory create failed...");
                return ;
            }
        }
        File report = new File(timestamp, Constant.DIR_REPORT) ;
        if (!report.exists()) {
            VLog.i("report directory not exist , create it...");
            if(!report.mkdirs()){
                VLog.i("report directory create failed...");
                return ;
            }
        }
        File screenshot = new File(timestamp, Constant.DIR_SCREENSHOT) ;
        if (!screenshot.exists()) {
            VLog.i("screenshot directory not exist , create it...");
            if(!screenshot.mkdirs()){
                VLog.i("screenshot directory create failed...");
                return ;
            }
        }
        VLog.i("all directory create succeed.");
        if (Config.sLogMode == VLog.FILE || Config.sLogMode == VLog.ALL){
            VLog.i("log file mode enable.");
            File log_file = new File(log, "log.txt") ;
            try {
                if (!log_file.exists()){
                    if (!log_file.createNewFile()){
                        throw new IOException("create log file failure.") ;
                    }
                    VLog.i("log file create successfully.");
                }
                VLog.i("file log will be save in : " + log_file.getAbsolutePath());
                VLog.getInstance().setOutputFilename(log_file.getAbsolutePath());
            }catch (IOException e){
                VLog.i("create log file failure, change to logout mode only");
                Config.sLogMode = VLog.LOGCAT ;
            }
        }
        Log.i(Constant.TAG, "log mode : " + Config.sLogMode) ;
        Log.i(Constant.TAG, "log output mode : " + VLog.getInstance().isLogEnabled()) ;
        VLog.getInstance().setOutputMode(Config.sLogMode);
//        VLog.getInstance().setOutputMode(VLog.NONE);
    }


}
