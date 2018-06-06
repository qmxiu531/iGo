package com.gionee.autotest.traversal.testcase;

/**
 * Author : Viking Den<dengwj@gionee.com>
 * Time : 7/25/17 10:20 AM
 */

public class Config {

    static final String VERSION                 = "1.0.5" ;

    public static long sMaxSteps                = 20;
    public static int sMaxRuntime               = 7200;
    public static int sLaunchTimeout            = 5000;
    public static int sMaxRecycleTimes          = 10 ;
    public static int EVENT_TIMEOUT             = 1000 ;

    public static boolean sCommandMode          = true ;

    public static int sSeed                     = -1 ;

    public static int sLogMode                  = 2 ;
    public static int sMaxEventCycle            = 10 ;

    public static int sRestartAppTimes          = 100 ;
    public static int sThrottle                 = 500 ;

    public static String sCurTimeStamp          = "" ;

    public static String sWorkSpace             = "" ;

//    public static String sTargetPackage         = "com.android.calculator2";
//    public static String sTargetPackage         = "com.android.settings";
//    public static String sTargetPackage         = "com.qqgame.happymj";
//    public static String sTargetPackage         = "com.android.contacts";
//    public static String sTargetPackage         = "com.gionee.note";
//    public static String sTargetPackage         = "com.android.deskclock";
//    public static String sTargetPackage         = "com.android.soundrecorder";
//    public static String sTargetPackage         = "com.android.calendar";
//    public static String sTargetPackage         = "com.gionee.video";

    public static String sTargetPackage  ;

      //use to test exceptions
//    public static String sTargetPackage         = "com.gionee.autotest.traversal.exceptions";

    // Activities to be ignored
    public static final String[] IGNORED_ACTIVITY = {
            "Help & Feedback"
    };

    public static final String[] POSITIVE_BUTTON_TEXTS = {
            "继续",
            "Continue",
            "确定",
            "Ok",
            "允许",
            "Allow",
            "同意",
            "Agree",
            "暂不升级",
            "始终同意"
    };

    public static final String[] RANDOM_INPUT_TEXTS = {
            "RANDOM TEXT" ,
            "12345678" ,
            "NUMBER 5678" ,
            "&^%$#"
    };

}
