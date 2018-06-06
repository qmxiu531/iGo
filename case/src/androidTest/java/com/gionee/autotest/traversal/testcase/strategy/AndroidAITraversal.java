package com.gionee.autotest.traversal.testcase.strategy;

import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import com.gionee.autotest.traversal.common.model.AInfo;
import com.gionee.autotest.traversal.common.model.ActivityScreen;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.testcase.event.TraversalAISourceRandom;
import com.gionee.autotest.traversal.testcase.event.TraversalEventSource;
import com.gionee.autotest.traversal.testcase.util.TraversalUtil;
import com.gionee.autotest.traversal.testcase.util.UiHelper;
import com.gionee.autotest.traversal.testcase.util.VLog;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by viking on 8/29/17.
 *
 * use random event to traversal
 */
public class AndroidAITraversal extends Traversal {
    private UiDevice mDevice                    ;
    private List<ActivityScreen> screens        ;
    private volatile boolean isFinished = false ;

    public AndroidAITraversal(UiDevice mDevice){
        this.mDevice = mDevice ;
    }

    @Override
    public void run() {
        VLog.i("enter AndroidTraversal run ...");
        TraversalEventSource mEventSource = new TraversalAISourceRandom(mDevice);

        VLog.i("enable exception controller...");
        ExceptionController controller = new ExceptionController();
        registerController(controller);

        //fetch all screens
        screens = UiHelper.getAllScreens() ;
        //just launch it, if not in target application , will be handle it later
        UiHelper.launchApp(mDevice, Config.sTargetPackage) ;

        VLog.i("begin traversal...");
        try {
            while(!isFinished){
                try{
                    //find event
                }catch (StaleObjectException e){
                    VLog.e("{EXCEPTION} StaleObjectException : " + e.getMessage());
                    e.printStackTrace();
                }catch (Exception e){
                    VLog.e("{EXCEPTION} RestartException ");
                    e.printStackTrace();
                }
            }
            VLog.i("finish traversal...");
        } catch (Exception e){
            e.printStackTrace();
            VLog.e("catch exception, report it : " + e.getMessage());
        } finally {
            mEventSource.finish();
            unregisterController();
        }
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
                Log.i(Constant.TAG, "activityStarting exception happened : " + e.getMessage()) ;
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
                Log.i(Constant.TAG, "activityResuming exception happened : " + e.getMessage()) ;
            }
            return allow;
        }

        @Override
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg,
                                  long timeMillis, String stackTrace) throws RemoteException {
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
            return 0;
        }

        @Override
        public int systemNotResponding(String msg) throws RemoteException {
            VLog.d("systemNotResponding: " + msg);
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
            VLog.e("{EXCEPTION} suse1 Failed to install custom IActivityController: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
