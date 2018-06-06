package com.gionee.autotest.traversal.common.util;

/**
 * Created by viking on 8/23/17.
 *
 * Utility class to store common constant variables
 */

public class Constant {

    public static final String TAG = "AutoTraversal" ;

    public static final String LISTVIEW_CLASSNAME = "android.widget.ListView" ;

    public static final String GRIDVIEW_CLASSNAME = "android.widget.GridView" ;

    public static final String SCROLLVIEW_CLASSNAME = "android.widget.ScrollView" ;
    public static final String VIEWPAGER_CLASSNAME = "android.support.v4.view.ViewPager" ;
    public static final String RECYCLERVIEW_CLASSNAME = "android.support.v7.widget.RecyclerView" ;

    public static final String EDITTEXT_CLASSNAME = "android.widget.EditText" ;

    public static final String EDITTEXT_SUFFIX_NAME = ".EditText" ;

    public static final String DEFAULT_INPUT_METHOD = "com.dzsoft.smartrobot.uiauto.daemon.service/com.dzsoft.smart.daemon.service.Utf7ImeService" ;

    /*********************************************************************
     *  For file operation constants
     ********************************************************************/

    public static final String DIR_LOG              = "log" ;
    public static final String DIR_EXCEPTION        = "exception" ;
    public static final String DIR_SCREENSHOT       = "screenshot" ;
    public static final String DIR_REPORT           = "report" ;
    public static final String DIR_REPORT_JSON      = "report.json" ;
    public static final String DIR_REPORT_HTML      = "report.html" ;

    /*********************************************************************
     *  For broadcast receiver constants
     ********************************************************************/

    public static final String ROOT_NAME          = "traversal" ;

    public static final String SHARED_PERF                              = "igo_pref" ;

    public static final int REQUEST_CODE_CHOOSE_APPLICATION             = 1001 ;
    public static final String EXTRA_CHOOSE_APP_NAME                    = "APP_NAME" ;
    public static final String EXTRA_CHOOSE_APP_PKG                     = "APP_PKG" ;

    public static final String EXTRA_MAX_RUNTIME                        = "MAX_RUN_TIME" ;
    public static final String EXTRA_MAX_EVENTS                         = "MAX_RUN_EVENTS" ;
    public static final String EXTRA_MAX_APP_RESTART_TIMES              = "MAX_APP_RESTART_TIMES" ;
    public static final String EXTRA_MAX_THROTTLE_TIME                  = "MAX_THROTTLE_TIME" ;


    public static final String ACTION_TEST_FINSIHED                     = "com.gionee.autotest.action.TEST_FINISHED" ;
    public static final String EXTRA_TIMESTAMP                          = "EXTRA_TIMESTAMP" ;

    public static final String EXTRA_HOUR   = "com.gionee.autotest.extra.HOUR" ;
    public static final String EXTRA_MINUTE = "com.gionee.autotest.extra.MINUTE" ;
    public static final String EXTRA_SECOND = "com.gionee.autotest.extra.SECOND" ;

    public static final String SHOW_NOTICE                              = "SHOW_NOTICE" ;
}
