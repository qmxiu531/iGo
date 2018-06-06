package com.gionee.autotest.traversal.testcase.util;

import com.gionee.autotest.traversal.testcase.Config;

/**
 * Created by viking on 8/30/17.
 *
 * Misc utilities.
 */

public class TraversalUtil {


    private static PackageFilter sFilter;

    public static PackageFilter getPackageFilter() {
        if (sFilter == null) {
            sFilter = new PackageFilter();
        }
        return sFilter;
    }

    public static class PackageFilter {

        /**
         * Check whether we should run against the given package.
         *
         * @param pkg The package name.
         * @return Returns true if we should run against pkg.
         */
        public boolean checkEnteringPackage(String pkg) {
            /*if (mInvalidPackages.size() > 0) {
                if (mInvalidPackages.contains(pkg)) {
                    return false;
                }
            } else if (mValidPackages.size() > 0) {
                if (!mValidPackages.contains(pkg)) {
                    return false;
                }
            }*/
            return Config.sTargetPackage.equals(pkg)
                    || "com.gionee.security".equals(pkg)
                    || "com.android.packageinstaller".equals(pkg)
                    || "com.gionee.autotest.traversal".equals(pkg)
                    || "com.gionee.autotest.traversal.testcase".equals(pkg)
                    || "com.gionee.autotest.traversal.testcase.test".equals(pkg);
        }
    }


}
