package com.gionee.autotest.traversal.testcase.util;

import android.annotation.SuppressLint;
import android.app.IActivityController;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.gionee.autotest.traversal.common.model.AInfo;
import com.gionee.autotest.traversal.common.model.ActivityScreen;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by viking on 8/30/17.
 *
 * Monitor operations happening in the system.
 */

public class ExceptionController extends IActivityController.Stub {

    private List<ActivityScreen> screens ;

    public ExceptionController(List<ActivityScreen> screens){
        this.screens = screens ;
    }


    /**
     * According current activity name to get AScreen
     * @param curActivity current activity name
     * @return if activity name exist in AScreens return it, or null will be return
     */
    private ActivityScreen getActivityScreen(String curActivity){
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
        boolean allow = TraversalUtil.getPackageFilter().checkEnteringPackage(pkg) ;
        VLog.d("    // " + (allow ? "Allowing" : "Rejecting") + " start of "
                + intent + " in package " + pkg);
        ComponentName cn = intent.getComponent() ;
        if (cn != null){
            String className = cn.getClassName() ;
            VLog.d("cn class name : " + className);
            setActivityVisited(className) ;
        }
        return allow;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        VLog.d("    // activityResuming(" + pkg + ")");
        boolean allow = TraversalUtil.getPackageFilter().checkEnteringPackage(pkg)
                /*|| (DEBUG_ALLOW_ANY_RESTARTS != 0)*/;
        if (!allow) {
            VLog.d("    // " + "Rejecting resume of package " + pkg);
        }
        return allow;
    }

    @Override
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg,
                              long timeMillis, String stackTrace) throws RemoteException {
        VLog.d("// CRASH: " + processName + " (pid " + pid + ")");
        VLog.d("// Short Msg: " + shortMsg);
        VLog.d("// Long Msg: " + longMsg);
        VLog.d("// " + stackTrace.replace("\n", "\n// "));
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
        VLog.d("// NOT RESPONDING: " + processName + " (pid " + pid + ")");
        VLog.d(processStats);
        return 0;
    }

    @Override
    public int systemNotResponding(String msg) throws RemoteException {
        VLog.d("systemNotResponding: " + msg);
        // return -1 to let the system continue with its normal kill
        return -1;
    }

    /**
     * Install an instance of this class as the {@link IActivityController} to monitor the ActivityManager
     */
    public static void install(@Nullable IActivityController activityController) {
        VLog.i("register activity controller");
        setActivityController(activityController);
    }

    /**
     * Remove any installed {@link IActivityController} to reset the ActivityManager to the default state
     */
    public static void uninstall() {
        VLog.i("unregister activity controller");
        setActivityController(null);
    }

    /**
     * Use reflection to call the hidden api and set a custom {@link IActivityController}:
     * ActivityManagerNative.getDefault().setActivityController(activityController);
     */
    private static void setActivityController(@Nullable IActivityController activityController) {
        try {
            VLog.i("trying start activity controller succeed.");
            @SuppressLint("PrivateApi") Class<?> amClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefault = amClass.getMethod("getDefault");
            Object am = getDefault.invoke(null);
            Method setMethod = am.getClass().getMethod("setActivityController", IActivityController.class, boolean.class);
            setMethod.invoke(am, activityController, true);
            VLog.i("start activity controller succeed.");
        } catch (Throwable e) {
            VLog.e("Failed to install custom IActivityController: " + e.getMessage());
        }
    }
}
