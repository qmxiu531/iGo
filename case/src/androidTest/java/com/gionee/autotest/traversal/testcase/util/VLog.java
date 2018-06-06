package com.gionee.autotest.traversal.testcase.util;

import android.util.Log;

import com.gionee.autotest.traversal.common.util.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author : Viking Den<dengwj@gionee.com>
 * Time : 7/25/17 10:37 AM
 *
 * wrapper class for Logcat, VLog is turned on by default and mode is LOGCAT
 */

public final class VLog {

//    private static final String TAG = "AutoTraversal" ;

    public static final int NONE        = 0 ;
    public static final int FILE        = 1 ;
    public static final int LOGCAT      = 2 ;
    public static final int ALL         = 3 ;

    private interface LogSink {
        void log(int type, String message);
        void close();
    }
    private class FileSink implements LogSink {

        private PrintWriter mOut;

        private SimpleDateFormat mDateFormat;

        FileSink(File file) throws FileNotFoundException {
            mOut = new PrintWriter(file);
            mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        }

        public void log(int type, String message) {
            mOut.printf("%s %s\n", mDateFormat.format(new Date()), message);
        }

        public void close() {
            mOut.close();
        }

    }

    private class LogcatSink implements LogSink {

        public void log(int type, String message) {
            printDefault(type, Constant.TAG, message);
        }

        public void close() {
            // nothing is needed
        }
    }

    private int mCurrentMode = NONE;
    private List<LogSink> mSinks = new ArrayList<>();
    private File mOutputFile;
    private static VLog mInstance = null;

    public static VLog getInstance() {
        if (mInstance == null) {
            mInstance = new VLog();
        }
        return mInstance;
    }

    public static void i(String message) {
        VLog.getInstance().doLog(I, message);
    }

    public static void d(String message){
        VLog.getInstance().doLog(D, message);
    }

    public static void e(String message){
        VLog.getInstance().doLog(E, message);
    }

    public static void terminal(){
        if (mInstance != null){
            mInstance.closeSinks();
        }
    }

    private void doLog(int type, String message) {
        if (mCurrentMode == NONE) {
            return;
        }
        log(type, message);
    }

    private void log(int type, String message) {
        for (LogSink sink : mSinks) {
            sink.log(type, message);
        }
    }

    /**
     * Sets where the log output will go. Can be either be logcat or a file or
     * both. Setting this to NONE will turn off log out.
     *
     */
    public void setOutputMode(int mode) {
        closeSinks();
        mCurrentMode = mode;
        try {
            switch (mode) {
                case FILE:
                    if (mOutputFile == null) {
                        throw new IllegalArgumentException("Please provide a filename before " +
                                "attempting write trace to a file");
                    }
                    mSinks.add(new FileSink(mOutputFile));
                    break;
                case LOGCAT:
                    mSinks.add(new LogcatSink());
                    break;
                case ALL:
                    mSinks.add(new LogcatSink());
                    if (mOutputFile == null) {
                        throw new IllegalArgumentException("Please provide a filename before " +
                                "attempting write trace to a file");
                    }
                    mSinks.add(new FileSink(mOutputFile));
                    break;
                default:
                    break;
            }
        } catch (FileNotFoundException e) {
            Log.w(Constant.TAG, "Could not open log file: " + e.getMessage());
        }
    }

    private void closeSinks() {
        for (LogSink sink : mSinks) {
            sink.close();
        }
        mSinks.clear();
    }

    /**
     * Sets the name of the log file where log output will be written if the
     * log is set to write to a file.
     *
     * @param filename name of the log file.
     */
    public void setOutputFilename(String filename) {
        mOutputFile = new File(filename);
    }

    /**
     * Queries whether the log is enabled.
     * @return true if log is enabled, false otherwise.
     */
    public boolean isLogEnabled() {
        return mCurrentMode != NONE;
    }

    private static String join(String separator, Object[] strings) {
        if (strings.length == 0)
            return "";
        StringBuilder builder = new StringBuilder(objectToString(strings[0]));
        for (int i = 1; i < strings.length; i++) {
            builder.append(separator);
            builder.append(objectToString(strings[i]));
        }
        return builder.toString();
    }

    private static String objectToString(Object obj) {
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return Arrays.deepToString((Object[])obj);
            } else {
                return "[...]";
            }
        } else {
            return obj.toString();
        }
    }
    static final int D = 0x1;
    static final int I = 0x2;
    static final int E = 0x3;

    private static void printDefault(int type, String tag, String msg) {
        switch (type) {
            case VLog.D:
                Log.d(tag, msg);
                break;
            case VLog.I:
                Log.i(tag, msg);
                break;
            case VLog.E:
                Log.e(tag, msg);
                break;
            default:
                //do nothing
                break;
        }
    }
}