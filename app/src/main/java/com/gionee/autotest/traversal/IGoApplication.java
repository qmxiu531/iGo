package com.gionee.autotest.traversal;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.common.util.ShellUtil;
import com.gionee.autotest.traversal.common.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by viking on 9/8/17.
 *
 * main entry for iGo application
 */

public class IGoApplication extends Application {

    private static final String VERSION_CODE = "1.0.8" ;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constant.TAG, "enter IGoApplication...") ;
        Util.createRootDirectory() ;

        //copy template file
        String traversal_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME
                + File.separator + "report_template.html";
        Util.copyFile(getApplicationContext(), "report_template.html" , traversal_path);

        //install cast main apk
        boolean needRemove = !Util.isInstallApp(getApplicationContext(), "com.gionee.autotest.traversal.testcase", VERSION_CODE) ;
        if (needRemove){
            final String apk_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME
                    + File.separator + "case-debug.apk";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Util.copyFile(getApplicationContext(), "case-debug.apk", apk_path);
                    slientInstallByPM(apk_path) ;
                }
            }).start();
        }

        //install cast executable apk, sorry about VERSION_NAME can't apply to android test apk!!!!!!!
        if (needRemove){
            final String apk_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME
                    + File.separator + "case-debug-androidTest.apk";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Util.copyFile(getApplicationContext(), "case-debug-androidTest.apk", apk_path);
                    slientInstallByPM(apk_path) ;
                }
            }).start();
        }

        //install input method apk
        if (!Util.isInstallApp(getApplicationContext(), "com.dzsoft.smartrobot.uiauto.daemon.service", null)){
            final String apk_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME
                    + File.separator + "input.apk";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Util.copyFile(getApplicationContext(), "input.apk", apk_path);
                    slientInstallByPM(apk_path) ;
                }
            }).start();
        }
    }



    /**
     * install application by package manager
     * @param filePath apk archive path to install
     * @return if install succeed , return true ; or false
     */
    public static boolean slientInstallByPM(String filePath){
        try {
            Log.i(Constant.TAG, "install cast apk start...") ;
            Log.i(Constant.TAG, "apk path : " + filePath) ;
            ShellUtil.execCommand("pm install -r " + filePath, false);
            Log.i(Constant.TAG, "install cast apk end...") ;
            return true;
        } catch (Exception e) {
            Log.e(Constant.TAG, "install exception : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


}
