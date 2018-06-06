package com.gionee.autotest.traversal.common.util;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.gionee.autotest.traversal.common.event.EventInfo;
import com.google.common.collect.EvictingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by viking on 8/10/17.
 *
 * Utility class for common use cases
 */

public class Util {

    /**
     * Calculate two double's divide percentage
     * @param d1 number
     * @param d2 number
     */
    public static String getPercentage(double d1, double d2){
        double percent = d1 / d2;
        NumberFormat nt = NumberFormat.getPercentInstance();
        nt.setMinimumFractionDigits(2);
        return nt.format(percent) ;
    }

    /**
     * Credit goes to Cyril Mottier.
     * https://plus.google.com/+CyrilMottier/posts/FABaJhRMCuy
     *
     * @param view the {@link View} to animate.
     * @return an {@link ObjectAnimator} that will play a 'nope' animation.
     */
    public static ObjectAnimator nopeAnimation(View view, int delta) {
        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(1f, 0f)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).
                setDuration(500);
    }

    public static boolean isInstallApp(Context context, String packageName){
        return isInstallApp(context, packageName, null) ;
    }

    public static boolean isInstallApp(Context context, String packageName, String version){
        try {
            PackageManager mPm = context.getPackageManager() ;
            PackageInfo pi = mPm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES) ;
            if (version != null){
                Log.i(Constant.TAG, "version : " + version) ;
                String version_ = pi.versionName ;
                Log.i(Constant.TAG, "version_ : " + version_) ;
                return version_ != null && version.equals(version_) ;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isTimeStampExist(String timestamp) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return false ;
        }
        File root = new File(Environment.getExternalStorageDirectory(), Constant.ROOT_NAME) ;
        File dTimestamp = new File(root, timestamp) ;
        if (!dTimestamp.exists()) return false ;
        File result_html = new File(dTimestamp, Constant.DIR_REPORT + File.separator + Constant.DIR_REPORT_HTML) ;
        File result_json = new File(dTimestamp, Constant.DIR_REPORT + File.separator + Constant.DIR_REPORT_JSON) ;
        return result_html.exists() && result_json.exists();
    }

    public static String getFormatedTime(Date time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) ;
        return sdf.format(time) ;
    }

    public static List<String> getListFromString(String text){
        List<String> content = new ArrayList<>() ;
        if (text.contains("\n")){
            String[] texts = text.split("\n") ;
            content.addAll(Arrays.asList(texts));
        }else{
            content.add(text) ;
        }
        return content ;
    }

    public static List<EventInfo> getListFromEvictingQueue(EvictingQueue<EventInfo> eventQueue){
        if (eventQueue == null) return null ;
        if (eventQueue.isEmpty()) return null ;
        List<EventInfo> eventInfos = new ArrayList<>() ;
        eventInfos.addAll(eventQueue);
        return eventInfos ;
    }

    /** 获取两个时间的时间查 如1天2小时30分钟 */
    public static String getDatePoor(Date endDate, Date startDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000 ;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒
        long sec = diff % nd % nh % nm / ns ;
/*        VLog.i("diff : " + diff);
        VLog.i("day : " + day);
        VLog.i("hour : " + hour);
        VLog.i("min : " + min);
        VLog.i("sec : " + sec);*/

        StringBuilder time = new StringBuilder() ;
        if (day != 0){
            time.append(day) ;
            time.append("天") ;
        }

        if (hour != 0){
            time.append(hour) ;
            time.append("小时") ;
        }

        if (min != 0){
            time.append(min) ;
            time.append("分钟") ;
        }

        if (sec != 0){
            time.append(sec) ;
            time.append("秒") ;
        }

        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return time.toString();
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, defaultValue ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getFormattedPackageName(Context context, String packageName){
        PackageManager pm = context.getPackageManager() ;
        String name = packageName;
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
            name = name + "(" + packageName + ")" ;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
//            VLog.i("fetch current package's label name failure...");
        }
        return name;
    }

    public static <T> void writeDataToJsonFile(File file, T object){
        Gson gson = new GsonBuilder().setPrettyPrinting().create() ;
        String content = gson.toJson(object) ;
        try {
            FileUtils.write(file, content, Charset.forName("UTF-8"));
        } catch (IOException e) {
            //TODO define this error code
//            VLog.e("{EXCEPTION} write crash to file has failed.");
            e.printStackTrace();
        }
    }

    public static void createRootDirectory() {
        Log.i(Constant.TAG, "enter createRootDirectory") ;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return ;
        String traversal_path = Environment.getExternalStorageDirectory() + File.separator + Constant.ROOT_NAME ;
        File rootDir = new File(traversal_path) ;
        if (!rootDir.exists()){
            boolean success = rootDir.mkdirs() ;
            Log.i(Constant.TAG, "create traversal directory success or not : " + success) ;
        }
    }

    public static void copyFile(Context mContext, String fileName , String destName){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.i(Constant.TAG, "external storage mounted, check file exist or not") ;
            File file = new File(destName) ;
            if (file.exists()){
                Log.i(Constant.TAG, fileName + " exist, clean it...") ;
                if(!file.delete()){
                    Log.i(Constant.TAG, fileName + " delete it failed...") ;
                }
            }
            try{
                //first create it
                if (!file.createNewFile()){
                    Log.i(Constant.TAG, "create " + fileName + " failed...") ;
                    return ;
                }
                InputStream templateStream = mContext.getAssets().open(fileName) ;
//                FileUtils.copyFile(templateStream, file);
                OutputStream destination = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int remain;
                while ((remain = templateStream.read(buffer)) != -1) {
                    if (remain == 0) {
                        remain = templateStream.read();
                        if (remain < 0)
                            break;
                        destination.write(remain);
                        continue;
                    }
                    destination.write(buffer, 0, remain);
                }
                destination.close();
                Log.i(Constant.TAG, "finished copy " + fileName ) ;
            }catch (IOException e){
                Log.i(Constant.TAG, "copy " + fileName + " failure: " + e.getMessage())  ;
                e.printStackTrace();
            }
        }
    }
}
