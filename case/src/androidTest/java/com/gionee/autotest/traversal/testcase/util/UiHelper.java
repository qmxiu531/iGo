package com.gionee.autotest.traversal.testcase.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.common.model.AInfo;
import com.gionee.autotest.traversal.common.model.ActivityScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author : Viking Den<dengwj@gionee.com>
 * Time : 7/25/17 11:10 AM
 */

public class UiHelper {

    /**
     * bring device to home
     */
    public static void launchHome(UiDevice mDevice) {
        VLog.i("{Press} Home");
//        UiDevice uidevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressHome();
        String launcherPackage = mDevice.getLauncherPackageName();
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), Config.sLaunchTimeout);
        VLog.i("launch home success");
    }

    /**
     * launch a specify app through app's package name
     * @param targetPkg target app' package name
     */
    public static boolean launchApp(UiDevice mDevice, String targetPkg){
        VLog.i("{Launch} " + targetPkg);

//        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String launcherPackage = mDevice.getLauncherPackageName();
        if (launcherPackage.compareToIgnoreCase(targetPkg) == 0) {
            VLog.d("just launch home.");
            launchHome(mDevice);
            return true;
        }
        Context context = InstrumentationRegistry.getContext();
        //add by suse 2018.5.29,to test rpk
//        Intent intent = new Intent();
//        intent.setAction("com.gionee.agileapp.action.LAUNCH");
//        intent.putExtra("EXTRA_APP","com.VIP.VIPQuickAPP");
////        intent.putExtra("EXTRA_PATH","/");
////        intent.putExtra("EXTRA_LAUNCH_FROM","com.gionee.aora.market");
//        context.startActivity(intent);

        //modify by suse 2018.5.29
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(targetPkg);
        if (intent != null) {
            VLog.d("The given pkg name's intent exist.");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // Make sure each launch is a new task
            context.startActivity(intent);
            mDevice.wait(Until.hasObject(By.pkg(Config.sTargetPackage).depth(0)), Config.sLaunchTimeout);
        } else {
            String err = String.format("(%s) No launch Activity.\n", targetPkg);
            VLog.e(err);
            // send this error to command process
            Bundle bundle = new Bundle();
            bundle.putString("ERROR", err);
            InstrumentationRegistry.getInstrumentation().finish(1, bundle);
        }
        return true;
    }

    /**
     * Judge current is in app or not
     * @return if is in app , return true; or false
     */
    public static boolean isInApp(UiDevice mDevice, String pkgName) {
//        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String pkg = mDevice.getCurrentPackageName();
        return pkg != null && 0 == pkg.compareToIgnoreCase(pkgName) ;
    }

    public static boolean isInIgnoredActivity(String activityName) {
        for (String ignore : Config.IGNORED_ACTIVITY) {
            if (0 == ignore.compareTo(activityName)) {
                return true;
            }
        }
        return false;
    }

    public static void performBackAction(UiDevice mDevice){
        VLog.e("press back...");
        String curActivity = UiHelper.getTopActivityName(mDevice) ;
        //just goBack
        mDevice.pressBack() ;
        UiHelper.sleep(1);
        String afterActivity = UiHelper.getTopActivityName(mDevice) ;
        if (curActivity != null && curActivity.equals(afterActivity)){
            VLog.e("press back not working, press again...");
            mDevice.pressBack() ;
            UiHelper.sleep(1);
        }
    }

    /**
     * use shell command('dumpsys activity recents') to get current top activity class name
     * @return if parsed property , a top activity class name will be return, or empty string return
     */
    public static String getTopActivityName(UiDevice mDevice){
        String output = "" ;
        try {
//            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            output = mDevice.executeShellCommand("dumpsys activity activities") ;
            String[] lines = output.split("\n") ;
            for (String line : lines){
                if (line != null && line.contains("mFocusedActivity:")){
                    String[] items = line.split(" ") ;
                    if (items.length > 0){
                        for (String item : items){
//                            VLog.d("line : " + item);
                            if (item != null && item.contains("/")){
                                String packageName = item.split("/")[0] ;
                                String activityName = item.split("/")[1] ;
                                if (activityName.startsWith(".")){
                                    output = packageName + activityName ;
                                }else{
                                    output = activityName ;
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            VLog.e("get top activity name error : " + e.getMessage());
        }
        return output ;
    }

    /**
     * init all screens
     * @return a list of all activity screen
     */
    public static List<ActivityScreen> getAllScreens(){
        List<AInfo> activities = UiHelper.getAllActivities(Config.sTargetPackage) ;
        if (activities == null){
            throw new IllegalStateException("could not fetch all activities...") ;
        }
        List<ActivityScreen> screens = new ArrayList<>() ;
        for (AInfo a : activities){
            screens.add(new ActivityScreen(a)) ;
        }
        return screens ;
    }

    /**
     * force stop application with the given package name
     * @param mDevice UiDevice instance
     * @param packageName package name to force stop
     */
    public static void forceStopPackage(UiDevice mDevice , String packageName){
        try {
            String output = mDevice.executeShellCommand("am force-stop " + packageName) ;
            VLog.i("force-stop output message : " + output);
        } catch (IOException e) {
            e.printStackTrace();
            VLog.i("force-stop output exception : " + e.getMessage());
        }
    }

    /**
     * short hand for sleep action
     * @param seconds seconds to sleep
     */
    public static void sleep(int seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search all activities under the given packageName's application
     * @param packageName application's package to list
     * @return a list of activities
     */
    private static List<AInfo> getAllActivities(String packageName){
        Context context = InstrumentationRegistry.getContext() ;
        PackageManager pManager = context.getPackageManager() ;
        List<AInfo> activities = null ;
        try {
            ActivityInfo[] list = pManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities ;
            VLog.d("-----------------list all activities---------------------") ;
            activities = new ArrayList<>() ;
            for (ActivityInfo activityInfo : list) {
                AInfo aInfo = new AInfo();
                VLog.d("Activity name : " + activityInfo.name) ;
                aInfo.name = activityInfo.name ;
                CharSequence label = activityInfo.loadLabel(pManager) ;
                if ( label != null){
                    VLog.d("Activity label : " + label);
                    aInfo.label = label.toString() ;
                }
                activities.add(aInfo) ;
            }

        } catch (PackageManager.NameNotFoundException e) {
            VLog.d(packageName + " package name is not exist.");
            e.printStackTrace() ;
        }
        return activities ;
    }

    /**
     * return root select. The android.R.id.content ID value indicates the ViewGroup of the entire
     * content area of an Activity.
     * @return root select
     */
    public static BySelector getRootSelect(){
        return By.clazz("android.widget.FrameLayout").res("android:id/content") ;
    }

    public static boolean isInNegativeSituation(UiDevice mDevice){
        boolean hasClicked = false ;
        //permission authority activity or global system dialog, find a positive button and just click it
        for (String p_text : Config.POSITIVE_BUTTON_TEXTS){
            //weather it exist or not
            UiObject2 p_text_object = mDevice.findObject(By.text(p_text)) ;
            if (p_text_object != null){
                VLog.i(p_text + " positive button exist, click it...");
                p_text_object.clickAndWait(Until.newWindow(), Config.EVENT_TIMEOUT) ;
                VLog.i("jump out this loop");
                hasClicked = true ;
                break;
            }
        }
        return hasClicked ;
    }
    /**
     * there are some reasons we should restart target package
     *
     * 1. check system dialog , click positive button
     *
     * 2. if not system dialog showed up, then just restart target package
     */
    public static boolean restartTargetPackage(UiDevice mDevice) {
        if (!isInNegativeSituation(mDevice)){
            VLog.e("no matchable condition, just restart target application");
            launchApp(mDevice, Config.sTargetPackage) ;
            return true;
        }
        return false ;
    }
}
