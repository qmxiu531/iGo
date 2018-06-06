package com.gionee.autotest.traversal.testcase.util;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;

import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.common.util.ShellUtil;
import com.gionee.autotest.traversal.common.util.Util;
import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.common.model.ActivityScreen;
import com.gionee.autotest.traversal.common.report.ConfigInfo;
import com.gionee.autotest.traversal.common.report.DeviceInfo;
import com.gionee.autotest.traversal.common.report.ErrorDetail;
import com.gionee.autotest.traversal.common.report.ErrorEvent;
import com.gionee.autotest.traversal.common.report.ExceptionInfo;
import com.gionee.autotest.traversal.common.report.ReportDetail;
import com.gionee.autotest.traversal.common.report.ReportSummary;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by viking on 9/1/17.
 *
 * assemble all test result and status
 */

public class ReportUtil {

    public static void report(Context context, List<ActivityScreen> screens, Date sStartTime, int eventCounter){
        reportFail(context, ErrorEvent.ERROR_UNKNOWN, null, screens, sStartTime, eventCounter);
    }

    public static void reportFail(Context context, int errorCode, String errorMsg, List<ActivityScreen> screens, Date sStartTime, int eventCounter){
        //assemble data
        //device information
        VLog.i("assemble device information");
        DeviceInfo deviceInfo = new DeviceInfo(Build.MODEL, Build.VERSION.RELEASE) ;

        //configuration information
        VLog.i("assemble configuration information");
        ConfigInfo configInfo = reportConfigInfo(sStartTime, eventCounter) ;

        //assemble error information
        ErrorDetail errorInfo = null ;
        if (errorCode != ErrorEvent.ERROR_UNKNOWN){
            errorInfo = new ErrorDetail(errorCode, errorMsg) ;
        }

        String testConverage = "N/A" ;
        String totalActivities = "N/A" ;

        //Activity information
        VLog.i("assemble activity information");
        ReportDetail.ActivityDetail activityInfo = null ;
        if (screens != null && screens.size() > 0) {
            totalActivities = screens.size() + "" ;
            List<ReportDetail.ActivityItem> traversaled = new ArrayList<>();
            List<ReportDetail.ActivityItem> notTraversaled = new ArrayList<>();
            int index = 0 ;
            for (ActivityScreen screen : screens) {
                if (screen.isVisited()) {
                    VLog.d("activity traversal : " + screen.getActivityInfo());
                    index ++ ;
                    traversaled.add(new ReportDetail.ActivityItem(index,
                            screen.getActivityInfo().name, screen.getActivityInfo().label));
                }
            }
            index = 0 ;
            for (ActivityScreen screen : screens) {
                if (!screen.isVisited()) {
                    VLog.d("activity not traversal : " + screen.getActivityInfo());
                    index ++ ;
                    notTraversaled.add(new ReportDetail.ActivityItem(index,
                            screen.getActivityInfo().name, screen.getActivityInfo().label));
                }
            }
            testConverage = Util.getPercentage((double)traversaled.size(), (double)screens.size()) ;
            activityInfo = new ReportDetail.ActivityDetail(traversaled, notTraversaled) ;
        }

        //exception information
        VLog.i("assemble exception information");
        ReportDetail.ExceptionDetail exceptionInfo = null ;
        //read all exceptions
        File dir_exception = new File(Config.sWorkSpace, Constant.DIR_EXCEPTION) ;
        if (dir_exception.exists()){
            File[] exceptions = dir_exception.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("exception_") && name.endsWith(".json");
                }
            }) ;
            if (exceptions != null && exceptions.length > 0){
                List<ExceptionInfo> exceptionInfos = new ArrayList<>() ;
                Gson gson = new GsonBuilder().setPrettyPrinting().create() ;
                int index = 0 ;
                for (File exception : exceptions){
                    try{
                        index ++ ;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(exception), "UTF-8")) ;
                        ExceptionInfo eInfo = gson.fromJson(reader, ExceptionInfo.class) ;
                        //add index
                        eInfo.index = index ;
                        exceptionInfos.add(eInfo) ;
                    }catch (FileNotFoundException e){
                        VLog.i("convert json file to object exception: FileNotFoundException ");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        VLog.i("convert json file to object exception: UnsupportedEncodingException ");
                    }
                }
                exceptionInfo = new ReportDetail.ExceptionDetail(exceptionInfos) ;
            }
        }

        //summary information
        VLog.i("assemble summary information");
        String packageName = Util.getFormattedPackageName(context, Config.sTargetPackage) ;
        String testTime = Util.getFormatedTime(sStartTime) ;
        String testDuration = Util.getDatePoor(new Date(), sStartTime) ;
        ReportDetail.SummaryInfo summaryInfo = new ReportDetail.SummaryInfo(packageName,
                testTime, testDuration, testConverage, eventCounter + "") ;

        //assemble all test information
        VLog.i("assemble all test information");
        ReportDetail reportInfo = new ReportDetail() ;
        reportInfo.summaryInfo = summaryInfo ;
        reportInfo.activityInfo = activityInfo ;
        reportInfo.exceptionInfo = exceptionInfo ;
        reportInfo.deviceInfo = deviceInfo ;
        reportInfo.configInfo = configInfo ;
        reportInfo.errorInfo  = errorInfo ;

        //determine current test result
        String test_result = "通过" ;

        String test_reason = null ;
        if (errorInfo != null || exceptionInfo != null){
            test_reason = "测试有报错或ANR" ;
        }else if (!testConverage.startsWith("100")){
            test_reason = "测试未覆盖所有界面" ;
        }

        if (errorInfo != null || exceptionInfo != null || !testConverage.startsWith("100")){
            test_result = "不通过" ;
        }
        ReportSummary reportSummary = new ReportSummary(Util.getFormatedTime(sStartTime),
                "iGo遍历自动化测试报告", "测试平台部 自动化组", test_result, test_reason, totalActivities, reportInfo) ;
        //convert it to json
        VLog.i("generate json file...");
        File fail = new File(Config.sWorkSpace + File.separator + Constant.DIR_REPORT
                + File.separator + Constant.DIR_REPORT_JSON) ;
        Util.writeDataToJsonFile(fail, reportSummary);

        //render it to html
        generateHtml(reportSummary) ;

        //send result to command line
        sendResultToCommand(reportSummary) ;

        //send broadcast to client
        VLog.i("current command mode : " + Config.sCommandMode);
        if (!Config.sCommandMode){
            sendNotification() ;
        }
    }

    /**
     * add test result to command output
     * @param reportSummary test result summary
     */
    private static void sendResultToCommand(ReportSummary reportSummary){
        VLog.i("enter sendResultToCommand");
        try {
            VLog.i("sendResultToCommand fetch instrumentation");
            Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation() ;
            if (mInstrumentation == null) return ;
            VLog.i("sendResultToCommand start");
            Bundle result = new Bundle() ;
            result.putString("testTime", reportSummary.testTime);
            result.putString("test_reason", reportSummary.test_reason);
            result.putString("test_result", reportSummary.test_result);
            result.putString("totalActivities", reportSummary.totalActivities);
            if (reportSummary.detail != null && reportSummary.detail.summaryInfo != null){
                result.putString("testConverage", reportSummary.detail.summaryInfo.testConverage);
                result.putString("testDuration", reportSummary.detail.summaryInfo.testDuration);
                result.putString("testEventCounter", reportSummary.detail.summaryInfo.testEventCounter);
            }
            int resultCode = 8888 ;
            mInstrumentation.sendStatus(resultCode, result);
            VLog.i("sendResultToCommand finish");
        }catch (Exception e){
            e.printStackTrace();
            VLog.e("sendResultToCommand exception : " + e.getMessage());
        }
        VLog.i("end sendResultToCommand");
    }

    private static void sendNotification() {
        //TODO turn off start client application
/*        try{
            //first start application
            //adb shell am start -n "com.gionee.autotest.traversal/com.gionee.autotest.traversal.ui.SplashActivity"
            // -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
            StringBuilder startAppCommand = new StringBuilder() ;
            startAppCommand.append("am start -n \"com.gionee.autotest.traversal/com.gionee.autotest.traversal.ui.SplashActivity\" ") ;
            startAppCommand.append("-a android.intent.action.MAIN -c android.intent.category.LAUNCHER") ;
            VLog.i("start app command : " + startAppCommand.toString());
            ShellUtil.execCommand(startAppCommand.toString(), false) ;
            //wait for a second
            Thread.sleep(3000);
        }catch (Exception e){
            VLog.e("start app command execution failure . " + e);
        }*/

        try {
            //am broadcast -a com.gionee.autotest.action.TEST_FINISHED -n "com.gionee.autotest.traversal/com.gionee.autotest.traversal.receiver.TestFinishReceiver"
            StringBuilder broadCommand = new StringBuilder() ;
            broadCommand.append("am broadcast -a com.gionee.autotest.action.TEST_FINISHED ") ;
            broadCommand.append("-n \"com.gionee.autotest.traversal/com.gionee.autotest.traversal.receiver.TestFinishReceiver\" ");
            broadCommand.append("-e ");
            broadCommand.append(Constant.EXTRA_TIMESTAMP);
            broadCommand.append(" ");
            broadCommand.append(Config.sCurTimeStamp);
            VLog.i("broadcast command : " + broadCommand.toString());
            ShellUtil.execCommand(broadCommand.toString(), false) ;
        }catch (Exception e){
            VLog.e("send broadcast command execution failure . " + e);
        }

        VLog.i("all command send...");
    }

    private static void generateHtml(ReportSummary reportSummary) {
        try{
            VLog.i("enter generate html...");
            MustacheFactory mustacheFactory = new DefaultMustacheFactory();
//            InputStream template = InstrumentationRegistry.getTargetContext().getAssets().open("report_template.html") ;
            String path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME + File.separator +
                    "report_template.html";
            File template_file = new File(path) ;
            if (!template_file.exists()){
                VLog.e("report_template.html is not exist, abort it...");
                return ;
            }
            InputStream template = new FileInputStream(template_file) ;
            Mustache mustache = mustacheFactory.compile(
                    new BufferedReader(new InputStreamReader(template, "UTF-8")), Constant.DIR_REPORT_HTML) ;
            String fileName = Config.sWorkSpace + File.separator + Constant.DIR_REPORT + File.separator + Constant.DIR_REPORT_HTML ;
            File report = new File(fileName) ;
            Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(report), "UTF-8")) ;
            mustache.execute(writer, reportSummary);

            writer.close();
        }catch (Exception e){
            VLog.i("render html exception : " + e);
        }
    }

    private static ConfigInfo reportConfigInfo(Date sStartTime, int eventCounter) {
        ConfigInfo configInfo = new ConfigInfo() ;
        configInfo.sTargetPackage       = Config.sTargetPackage ;
        configInfo.sLogMode             = Config.sLogMode + "" ;
        configInfo.sCurTimeStamp        = Config.sCurTimeStamp ;
        configInfo.sMaxRestartAppTimes  = Config.sRestartAppTimes + "" ;
        configInfo.sMaxRuntime          = Config.sMaxRuntime + "" ;
        configInfo.sMaxSteps            = Config.sMaxSteps + "";
        configInfo.sMaxRecycleTimes     = Config.sMaxRecycleTimes + "" ;
        configInfo.sMaxEventCycle       = Config.sMaxEventCycle + "" ;
        configInfo.sRuningCounter       = eventCounter + "";
        configInfo.sRuningTime          = Util.getDatePoor(new Date(), sStartTime) ;
        return configInfo ;
    }
}
