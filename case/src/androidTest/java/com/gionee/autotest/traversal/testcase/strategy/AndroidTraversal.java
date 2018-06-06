package com.gionee.autotest.traversal.testcase.strategy;

import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;

import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.common.event.EventInfo;
import com.gionee.autotest.traversal.testcase.event.TraversalBasicEvent;
import com.gionee.autotest.traversal.testcase.event.TraversalEvent;
import com.gionee.autotest.traversal.testcase.event.TraversalEventSource;
import com.gionee.autotest.traversal.testcase.event.TraversalScrollEvent;
import com.gionee.autotest.traversal.testcase.event.TraversalSourceRandom;
import com.gionee.autotest.traversal.common.model.AInfo;
import com.gionee.autotest.traversal.common.model.ActivityScreen;
import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.common.report.ExceptionInfo;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.util.Debug;
import com.gionee.autotest.traversal.testcase.util.ReportUtil;
import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.TraversalUtil;
import com.gionee.autotest.traversal.testcase.util.UiHelper;
import com.gionee.autotest.traversal.common.util.Util;
import com.gionee.autotest.traversal.testcase.util.VLog;
import com.google.common.collect.EvictingQueue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by viking on 8/29/17.
 *
 * use random event to traversal
 */
public class AndroidTraversal extends Traversal implements EventCallback{
    private UiDevice mDevice                    ;
    private List<ActivityScreen> screens        ;
    private Date sStartTime                     ;
    private int sRestartAppTimes    = 0         ;
    private int eventCounter        = 0         ;

    private volatile boolean isFinished = false ;

    private String defaultInputMethod ;

    /** The random number seed **/
    private long mSeed = 0;

    private EvictingQueue<EventInfo> eventCycles ;
    /**
     * This is set by the ActivityController thread to request collection of ANR
     * trace files
     */
    private boolean mRequestAnrTraces = false;

    private boolean hasException = false ;

    private float[] mFactors = new float[TraversalSourceRandom.FACTORZ_COUNT];

    public AndroidTraversal(UiDevice mDevice){
        this.mDevice = mDevice ;
    }

    @Override
    public void run() {
        VLog.i("enter AndroidTraversal run ...");
        eventCycles = EvictingQueue.create(Config.sMaxEventCycle) ;
        //initial screenshot taker
        ScreenshotTaker screenshotTaker = new ScreenshotTaker(mDevice) ;
        //init all variables
        eventCounter    = 1 ;

        try {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
            sStartTime = mDateFormat.parse(Config.sCurTimeStamp) ;
        } catch (ParseException e) {
            VLog.i("parsing sCurTimeStamp error... abort it");
            e.printStackTrace();
            sStartTime      = new Date();
        }

        //enable input method
        enableDZInputMethod() ;

        int sRecycleTimes = 0;
        // The delay between event inputs
        long mThrottle = Config.sThrottle;
        // set a positive value, indicating none of the factors is provided yet
        for (int i = 0; i < TraversalSourceRandom.FACTORZ_COUNT; i++) {
            mFactors[i] = 1.0f;
        }

        mSeed = Config.sSeed ;
        if (mSeed == -1) {
            mSeed = System.currentTimeMillis() + System.identityHashCode(this);
        }
        VLog.i("mSeed : " + mSeed);
        Random mRandom = new Random(mSeed);

        TraversalEventSource mEventSource = new TraversalSourceRandom(mDevice, mRandom, this, mThrottle, false);
        // set any of the factors that has been set
        for (int i = 0; i < TraversalSourceRandom.FACTORZ_COUNT; i++) {
            if (mFactors[i] <= 0.0f) {
                ((TraversalSourceRandom) mEventSource).setFactors(i, mFactors[i]);
            }
        }

        // validate source generator
        if (!mEventSource.validate()) {
            VLog.i("events factor not satisfied, please try again.");
            return ;
        }

        VLog.i("enable exception controller...");
        ExceptionController controller = new ExceptionController();
        registerController(controller);

        //fetch all screens
        screens = UiHelper.getAllScreens() ;
        //just launch it, if not in target application , will be handle it later
        UiHelper.launchApp(mDevice, Config.sTargetPackage) ;

        //just do one check, maybe it's first launch
        checkAppConditions() ;

        VLog.i("begin traversal...");
        try {
            while(!isFinished){
                try{
                    synchronized (this) {
                        //dump trace files
                        if (mRequestAnrTraces) {
                            mRequestAnrTraces = false;
                            reportAnrTraces("traces_" + eventCounter + ".txt") ;
                        }
                        //maybe we should delete this, later will check it, but it will take effect now
                        if (hasException){
                            //restart application
                            if (!checkAppConditions()) {
                                VLog.i("Not in target application , should terminal it");
                            }
                            hasException = false ;
                        }else if (eventCounter != 0 && eventCounter % 10 == 0){
                            //first check state
                            if (!checkAppConditions()) {
                                VLog.e("not in target application , should terminal it");
                            }
                        }
                    }

                    //do event
                    TraversalEvent ev = mEventSource.getNextEvent();
                    if (ev != null) {
                        int injectCode = ev.injectEvent(eventCounter, screenshotTaker);
                        if (injectCode == TraversalEvent.INJECT_FAIL) {
                            VLog.e("    // Event Injection Failed");

                            if (ev instanceof TraversalBasicEvent || ev instanceof TraversalScrollEvent){
                                VLog.i("    // Retry Random Key Event");
                                mEventSource.generateNextRandomKeyEvent() ;
                            }

                        }else{
                            VLog.i("    // Event Injection Succeed");
                        }
                        // Don't count throttling as an event.
                        if (ev.shouldCounter()) {
                            VLog.i("current times : -----------------------------------------" + eventCounter);
                            eventCounter++;
                        }
                    }

                    //should we exit?
                    if (isExit()) {
                        //some exit condition is satisfied , exit
                        VLog.e("Auto traversal will be exit soon...");
                        isFinished = true ;
                        break ;
                    }
                }catch (StaleObjectException e){
                    VLog.e("{EXCEPTION} StaleObjectException : " + e.getMessage());
                    e.printStackTrace();
                }catch (Exception e){
                    VLog.e("{EXCEPTION} RestartException ");
                    sRecycleTimes++ ;
                    VLog.e("current recycle time : " + sRecycleTimes);
                    if (sRecycleTimes > Config.sMaxRecycleTimes){
                        sRecycleTimes = 0 ;
                        VLog.e("reached maximum recycle times , restart application now...");
                        VLog.e("current restart target application count : " + sRestartAppTimes);
                        sRestartAppTimes += 1 ;
                        restartApplication();
                    }
                    e.printStackTrace();
                }
            }
            VLog.d("print all test information");
            Debug.printAllScreenInformation(screens, sStartTime);
            ReportUtil.report(InstrumentationRegistry.getContext(), screens, sStartTime, eventCounter);
            VLog.i("finish traversal...");
        } catch (Exception e){
            //TODO custom it
            e.printStackTrace();
            VLog.e("catch exception, report it : " + e.getMessage());
            /*StackTraceElement[] traces = e.getStackTrace() ;
            StringBuilder errorMsg = new StringBuilder() ;
            errorMsg.append(e.getMessage() != null ? "Caused by : " + e.getMessage() + "\n" : "No Message.\n") ;
            if (traces != null && traces.length > 0){
                for (StackTraceElement trace : traces){
                    errorMsg.append(trace.toString()) ;
                    errorMsg.append("\n") ;
                }
            }*/
/*            ReportUtil.reportFail(InstrumentationRegistry.getContext(),
                    ErrorEvent.ERROR_UNKNOWN, errorMsg.toString(), screens, sStartTime, eventCounter); */
            ReportUtil.report(InstrumentationRegistry.getContext(), screens, sStartTime, eventCounter);
        } finally {
            mEventSource.finish();
            unregisterController();
            rollBackInputMethod();
        }
    }

    @Override
    public void finishedEvent(EventInfo eventInfo) {
        eventInfo.setEventCounter(eventCounter);
        eventCycles.add(eventInfo) ;
    }

    /**
     * WTF, why system popup that permission dialog!!!
     */
    private void enableDZInputMethod(){
        //enable it
        try {
            defaultInputMethod = getDefaultInputMethod() ;
            VLog.i("default input method is : " + defaultInputMethod) ;
            if (defaultInputMethod != null && defaultInputMethod.equals(Constant.DEFAULT_INPUT_METHOD)){
                VLog.i("default input method is dongzhou method, ignore it");
                return ;
            }
            //settings put secure default_input_method com.dzsoft.smartrobot.uiauto.daemon.service/com.dzsoft.smart.daemon.service.Utf7ImeService
            VLog.i("enable dz input method") ;
            String command = "settings put secure default_input_method com.dzsoft.smartrobot.uiauto.daemon.service/com.dzsoft.smart.daemon.service.Utf7ImeService" ;
            mDevice.executeShellCommand(command) ;
            BySelector selector = By.text("继续").pkg("com.android.packageinstaller").res("com.android.packageinstaller:id/continue_button") ;
            boolean exist = mDevice.wait(Until.hasObject(selector), 3000) ;
            if (exist){
                VLog.i("click continue button");
                UiObject2 continueBtn = mDevice.findObject(selector) ;
                if (continueBtn != null) continueBtn.clickAndWait(Until.newWindow(), 1000) ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rollBackInputMethod(){
        if (defaultInputMethod != null && !"".equals(defaultInputMethod)){
            String command = "settings put secure default_input_method " + defaultInputMethod;
            try {
                VLog.i("rollback input method to : " + defaultInputMethod);
                mDevice.executeShellCommand(command) ;
                BySelector selector = By.text("继续").pkg("com.android.packageinstaller").res("com.android.packageinstaller:id/continue_button") ;
                boolean exist = mDevice.wait(Until.hasObject(selector), 3000) ;
                if (exist){
                    VLog.i("click continue button");
                    UiObject2 continueBtn = mDevice.findObject(selector) ;
                    if (continueBtn != null) continueBtn.clickAndWait(Until.newWindow(), 1000) ;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getDefaultInputMethod(){
        try {
            Log.i(Constant.TAG, "enable dz input method") ;
            String command = "settings get secure default_input_method" ;
            return mDevice.executeShellCommand(command) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null ;
    }

    /**
     * check current state is in target application or not
     * @return if in target application return true; or false
     */
    private boolean checkAppConditions(){
        VLog.i("check in target application or not");
        if (UiHelper.isInApp(mDevice, Config.sTargetPackage)) return true ;
        //if still not in target application we must restart application anyway
        return UiHelper.restartTargetPackage(mDevice) ;
    }

    /**
     * restart application , before restart it do back key event first, see can it return to target
     * application or not
     */
    private void restartApplication(){
        VLog.e("force-stop target application .....");
        UiHelper.forceStopPackage(mDevice, Config.sTargetPackage);
        VLog.e("sleep five seconds....");
        UiHelper.sleep(5);
        VLog.e("restart target application....");
        if (UiHelper.launchApp(mDevice, Config.sTargetPackage)){
            VLog.e("launch target application success, continue all loop again...");
        }
    }

    /**
     * check current exit status
     * @return if one condition is satisfied, return true , or false;
     */
    private boolean isExit(){
        //a lot of exit state judgement
        if (Config.sMaxSteps > 0 && eventCounter > Config.sMaxSteps){
            VLog.e("{Stop} reached maximum step: " + Config.sMaxSteps);
            return true ;
        }else if (Config.sRestartAppTimes > 0 && sRestartAppTimes > Config.sRestartAppTimes){
            VLog.e("{Stop} reached maximum restart application times: " + Config.sRestartAppTimes);
            return true ;
        }else if (Config.sMaxRuntime > 0 && (new Date().getTime() - sStartTime.getTime()) / 1000 > Config.sMaxRuntime) {
            VLog.e("{Stop} reached maximum run-time second: " + Config.sMaxRuntime);
            return true ;
        }
        return false ;
    }

    private class ExceptionController extends IActivityController.Stub {
        /**
         * According current activity name to get AScreen
         * @param curActivity current activity name
         * @return if activity name exist in AScreens return it, or null will be return
         */
        private ActivityScreen getActivityScreen(String curActivity){
            if (screens == null) return null;
            for (ActivityScreen screen : screens){
                AInfo aInfo = screen.getActivityInfo() ;
                if (aInfo != null && aInfo.name.equals(curActivity)){
                    return screen ;
                }
            }
            return null ;
        }

        /**
         * set current activity screen visited flag
         */
        private void setActivityVisited(String curActivity){
            ActivityScreen curScreen = getActivityScreen(curActivity) ;
            if (curScreen != null) curScreen.setVisited(true);
        }

        @Override
        public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
            boolean allow = true ;
            try{
                allow = TraversalUtil.getPackageFilter().checkEnteringPackage(pkg) ;
                VLog.d("    // " + (allow ? "Allowing" : "Rejecting") + " start of "
                        + intent + " in package " + pkg);
                ComponentName cn = intent.getComponent() ;
                if (cn != null){
                    String className = cn.getClassName() ;
                    VLog.d("cn class name : " + className);
                    setActivityVisited(className) ;
                }
            }catch (Throwable e){
                e.printStackTrace();
                android.util.Log.i(Constant.TAG, "activityStarting exception happened : " + e.getMessage()) ;
            }
            return allow;
        }

        @Override
        public boolean activityResuming(String pkg) throws RemoteException {
            boolean allow = true ;
            try {
                VLog.d("    // activityResuming(" + pkg + ")");
                allow = TraversalUtil.getPackageFilter().checkEnteringPackage(pkg)
                /*|| (DEBUG_ALLOW_ANY_RESTARTS != 0)*/;
                if (!allow) {
                    VLog.d("    // " + "Rejecting resume of package " + pkg);
                }
            }catch (Throwable e){
                e.printStackTrace();
                android.util.Log.i(Constant.TAG, "activityResuming exception happened : " + e.getMessage()) ;
            }
            return allow;
        }

        @Override
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg,
                                  long timeMillis, String stackTrace) throws RemoteException {
            try{
                synchronized (AndroidTraversal.this){
                    ExceptionInfo crashInfo = new ExceptionInfo(Util.getFormatedTime(new Date()), eventCounter, "CRASH", processName,
                            pid + "", shortMsg, longMsg, stackTrace,
                            Util.getListFromEvictingQueue(eventCycles)) ;

                    //Object to Json file
                    File crash = new File(Config.sWorkSpace + File.separator + Constant.DIR_EXCEPTION
                            + File.separator + "exception_" + eventCounter + ".json") ;
                    Util.writeDataToJsonFile(crash, crashInfo);

                    VLog.d("// CRASH: " + processName + " (pid " + pid + ")");
                    VLog.d("// Short Msg: " + shortMsg);
                    VLog.d("// Long Msg: " + longMsg);
                    VLog.d("// " + stackTrace.replace("\n", "\n// "));
                    hasException = true ;
                }
            }catch (Throwable e){
                e.printStackTrace();
                android.util.Log.i(Constant.TAG, "appCrashed exception happened : " + e.getMessage()) ;
            }

            return false;
        }

        @Override
        public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
            VLog.d("appEarlyNotResponding: " + processName + ":" + pid + " " + annotation);
            // return 0 to continue with normal ANR processing
            // we'll block the ANR dialog from appearing later, when appNotResponding is called
            return 0;
        }

        @Override
        public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
            try{
                VLog.d("// NOT RESPONDING: " + processName + " (pid " + pid + ")");
                VLog.d(processStats);

                synchronized (AndroidTraversal.this){
                    ExceptionInfo anrInfo = new ExceptionInfo(Util.getFormatedTime(new Date()), eventCounter, "ANR", processName, pid + "",
                            processStats, Util.getListFromEvictingQueue(eventCycles)) ;
                    //Object to Json file
                    File anr = new File(Config.sWorkSpace + File.separator + Constant.DIR_EXCEPTION
                            + File.separator + "exception_" + eventCounter + ".json") ;
                    Util.writeDataToJsonFile(anr, anrInfo);
                    mRequestAnrTraces = true;
                    hasException = true ;
                }
            }catch (Throwable e){
                e.printStackTrace();
                android.util.Log.i(Constant.TAG, "appNotResponding exception happened : " + e.getMessage()) ;
            }
            return 0;
        }

        @Override
        public int systemNotResponding(String msg) throws RemoteException {
            VLog.d("systemNotResponding: " + msg);
            hasException = true ;
            // return -1 to let the system continue with its normal kill
            return -1;
        }
    }

    /**
     * Install an instance of this class as the {@link IActivityController} to monitor the ActivityManager
     */
    private void registerController(@Nullable IActivityController activityController) {
        VLog.i("register activity controller");
        setActivityController(activityController);
    }

    /**
     * Remove any installed {@link IActivityController} to reset the ActivityManager to the default state
     */
    private void unregisterController() {
        VLog.i("unregister activity controller");
        setActivityController(null);
    }

    private static final int BUILD_VERSION_CODES_O = 26 ;
    private static final int BUILD_VERSION_CODES_N = 24 ;

    /**
     * Use reflection to call the hidden api and set a custom {@link IActivityController}:
     * ActivityManagerNative.getDefault().setActivityController(activityController);
     */
    private static void setActivityController(@Nullable IActivityController activityController) {
        try {
            VLog.i("trying start activity controller succeed.");
            Object am ;
            VLog.i("Build.VERSION.SDK_INT : " + Build.VERSION.SDK_INT);
            VLog.i("Build.VERSION_CODES.N : " + Build.VERSION_CODES.N);
            if (Build.VERSION.SDK_INT >= BUILD_VERSION_CODES_O){
                @SuppressLint("PrivateApi") Class<?> amClass = Class.forName("android.app.ActivityManager") ;
                Method getService = amClass.getMethod("getService") ;
                am = getService.invoke(null) ;
            }else{
                @SuppressLint("PrivateApi") Class<?> amClass = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = amClass.getMethod("getDefault");
                am = getDefault.invoke(null);
            }
            if (am == null) return ;
            Method setMethod ;
            if (Build.VERSION.SDK_INT >= BUILD_VERSION_CODES_N){
                VLog.i("setMethod greater than N, two params");
                setMethod = am.getClass().getMethod("setActivityController", IActivityController.class, boolean.class);
                setMethod.invoke(am, activityController, true);
            }else{
                VLog.i("setMethod lower than N, one params");
                setMethod = am.getClass().getMethod("setActivityController", IActivityController.class);
                setMethod.invoke(am, activityController);
            }

//            Method setMethod = am.getClass().getMethod("setActivityController", IActivityController.class, boolean.class);

            VLog.i("start activity controller succeed.");
        } catch (Throwable e) {
            VLog.e("{EXCEPTION} Failed to install custom IActivityController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run "cat /data/anr/traces.txt". Wait about 5 seconds first, to let the
     * asynchronous report writing complete.
     */
    private void reportAnrTraces(String fileName) {
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            //do nothing
        }
        commandLineReport(fileName, "cat /data/anr/traces.txt");
    }

    /**
     * Print report from a single command line.
     * <p>
     * streams (might be important for some command lines)
     *
     * @param reportName Simple tag that will print before the report and in
     *            various annotations.
     * @param command Command line to execute.
     */
    private void commandLineReport(String reportName, String command) {
        VLog.i("anr trace reportName : "  + reportName);
        Writer logOutput = null;
        try {
            // Process must be fully qualified here because android.os.Process
            // is used elsewhere
            java.lang.Process p = Runtime.getRuntime().exec(command);
            File anr = new File(Config.sWorkSpace + File.separator + Constant.DIR_EXCEPTION
                    + File.separator + reportName) ;
            if (!anr.exists()){
                if (!anr.createNewFile()){
                    VLog.i("create trace file failed : ");
                    return;
                }
            }
            logOutput = new BufferedWriter(new FileWriter(anr, true));
            // pipe everything from process stdout -> System.err
            InputStream inStream = p.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            while ((s = inBuffer.readLine()) != null) {
                try {
                    // When no space left on the device the write will
                    // occurs an I/O exception, so we needed to catch it
                    // and continue to read the data of the sync pipe to
                    // avoid the bug report hang forever.
                    logOutput.write(s);
                    logOutput.write("\n");
                } catch (IOException e) {
                    while(inBuffer.readLine() != null) {
                        VLog.i("no space left...");
                    }
                    break;
                }
            }
            int status = p.waitFor();
            VLog.i("// " + reportName + " status was " + status);
        } catch (Exception e) {
            VLog.i("// Exception from " + reportName + ":");
            VLog.i(e.toString());
        }finally {
            if (logOutput != null) {
                try {
                    logOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
