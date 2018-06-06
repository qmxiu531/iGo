package com.gionee.autotest.traversal.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.gionee.autotest.traversal.common.model.AppEntry;
import com.gionee.autotest.traversal.common.util.Constant;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FetchAllAppsTask extends AsyncTask<Void, Void, List<AppEntry>> {

    private PackageManager mPM ;

    private Context context ;

    public FetchAllAppsTask(Context context){
        this.context = context ;
        mPM = context.getPackageManager() ;
    }

    private String ensureLabel(ApplicationInfo info) {
        CharSequence label = info.loadLabel(context.getPackageManager());
        return label != null ? label.toString() : info.packageName;
    }

    @Override
    protected List<AppEntry> doInBackground(Void... voids) {
        int mOwnerRetrieveFlags = PackageManager.GET_UNINSTALLED_PACKAGES |
                PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS;
        @SuppressLint("WrongConstant") List<ApplicationInfo> mApplications = mPM.getInstalledApplications(mOwnerRetrieveFlags) ;
        Log.i(Constant.TAG, "fetch all application size : " + mApplications.size()) ;
        List<AppEntry> mAppEntries = new ArrayList<>() ;
        AppEntry mAppEntry ;
        for (ApplicationInfo app : mApplications){
            //filter owner apps
            if ("com.gionee.autotest.traversal".equals(app.packageName)
                    || "com.gionee.autotest.traversal.testcase.test".equals(app.packageName)
                    || "com.gionee.autotest.traversal.testcase".equals(app.packageName)){
                continue;
            }
            mAppEntry = new AppEntry() ;
            mAppEntry.info = app ;
            mAppEntry.packageName = app.packageName ;
            mAppEntry.label = ensureLabel(app) ;
            mAppEntries.add(mAppEntry) ;

        }

        //tag launcher app
        Intent launchIntent = new Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> intents = mPM.queryIntentActivities(
                launchIntent, PackageManager.GET_DISABLED_COMPONENTS);

        for (ResolveInfo resolveInfo : intents){
            String packageName = resolveInfo.activityInfo.packageName;
            Log.i(Constant.TAG, "CATEGORY_LAUNCHER : " + packageName);
            CharSequence label = resolveInfo.loadLabel(mPM) ;
            if (label != null){
                Log.i(Constant.TAG, "CATEGORY_LAUNCHER label : " + label);
            }
        }

        for (AppEntry entry : mAppEntries){
            final int N = intents.size();
            for (int j = 0; j < N; j++) {
                String packageName = intents.get(j).activityInfo.packageName;
                if (entry != null && entry.packageName.equals(packageName)) {
                    entry.hasLauncherEntry = true;
                    CharSequence label = intents.get(j).loadLabel(mPM) ;
                    if (label != null){
                        entry.label = label.toString() ;
                    }
                }
            }
        }
        AppFilter filter = FILTER_EVERYTHING ;
        //NO SYSTEM APP
        filter = new CompoundFilter(filter, FILTER_DOWNLOADED_AND_LAUNCHER);

        List<AppEntry> apps = new ArrayList<>(mAppEntries);
        ArrayList<AppEntry> filteredApps = new ArrayList<>();
        for (int i = 0 ; i< apps.size() ; i++) {
            AppEntry entry = apps.get(i);
            if (entry != null && filter.filterApp(entry)) {
                filteredApps.add(entry);
            }
        }
        Collections.sort(filteredApps, ALPHA_COMPARATOR);

        for (AppEntry app : filteredApps){
            Log.i(Constant.TAG, "application : " + app.packageName + " " + app.label) ;
        }
        return filteredApps;
    }

    public interface AppFilter{
        boolean filterApp(AppEntry entry) ;
    }

    public static class CompoundFilter implements AppFilter {

        private final AppFilter mFirstFilter;

        private final AppFilter mSecondFilter;

        CompoundFilter(AppFilter first, AppFilter second) {
            mFirstFilter = first;
            mSecondFilter = second;
        }

        @Override
        public boolean filterApp(AppEntry info) {
            return mFirstFilter.filterApp(info) && mSecondFilter.filterApp(info);
        }
    }

    private static final AppFilter FILTER_EVERYTHING = new AppFilter() {
        @Override
        public boolean filterApp(AppEntry entry) {
            return true;
        }
    };

    /**
     * Displays a combined list with "downloaded" and "visible in launcher" apps only.
     */
    private static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER = new AppFilter() {

        @Override
        public boolean filterApp(AppEntry entry) {
            if ((entry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            } else if ((entry.info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return true;
            } else if (entry.hasLauncherEntry) {
                return true;
            } /*else if ((entry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && entry.isHomeApp) {
                return true;
            }*/
            return false;
        }
    };

    private static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.label, object2.label);
        }
    };
}