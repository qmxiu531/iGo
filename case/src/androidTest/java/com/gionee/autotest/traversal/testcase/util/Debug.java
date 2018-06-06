package com.gionee.autotest.traversal.testcase.util;

import com.gionee.autotest.traversal.common.model.ActivityScreen;
import com.gionee.autotest.traversal.common.util.Util;

import java.util.Date;
import java.util.List;

/**
 * Created by viking on 8/24/17.
 *
 * class for debug only
 */

public class Debug {

    /**
     * print all screen information
     */
    public static void printAllScreenInformation(List<ActivityScreen> screens, Date sStartTime){
        VLog.d("--------------print all screen information----------------");
        VLog.d("***************************************************");
        VLog.d("all activities : " + screens.size());
        int tSize = 0 ;
        for (ActivityScreen screen : screens){
            if (screen.isVisited()){
                VLog.d("activity traversal : " + screen.getActivityInfo());
                tSize ++ ;
            }
        }

        VLog.d("***************************************************");
        for (ActivityScreen screen : screens){
            if (!screen.isVisited()){
                VLog.d("activity not traversal : " + screen.getActivityInfo());
            }
        }

        VLog.d("traversal activities : " + tSize);
        VLog.d("traversal activity percentage : " + Util.getPercentage((double)tSize, (double)screens.size()));
        long speedTime = new Date().getTime() - sStartTime.getTime() ;
        VLog.d("Speed time : " + (speedTime / 1000 ) + " s.");
        VLog.d("***************************************************");
    }
}
