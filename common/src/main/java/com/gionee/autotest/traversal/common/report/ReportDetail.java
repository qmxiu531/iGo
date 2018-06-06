package com.gionee.autotest.traversal.common.report;

import java.io.Serializable;
import java.util.List;

/**
 * Created by viking on 9/4/17.
 *
 * detail information
 */

public class ReportDetail implements Serializable{

    public SummaryInfo          summaryInfo ;

    public ActivityDetail       activityInfo ;

    public ExceptionDetail      exceptionInfo ;

    public DeviceInfo           deviceInfo ;

    public ConfigInfo           configInfo ;

    public ErrorDetail          errorInfo ;

    public static final class SummaryInfo implements Serializable{
        public final String packageName ;

        public final String testTime ;

        public final String testDuration ;

        public final String testConverage ;

        public final String testEventCounter ;

        public SummaryInfo(String packageName, String testTime, String testDuration, String testConverage, String testEventCounter) {
            this.packageName = packageName;
            this.testTime = testTime;
            this.testDuration = testDuration;
            this.testConverage = testConverage;
            this.testEventCounter = testEventCounter;
        }
    }

    public static final class ActivityDetail implements Serializable{

        public final boolean hasTActivities ;

        public final boolean hasNActivities ;

        public final List<ActivityItem> tActivities ;

        public final List<ActivityItem> nActivities ;

        public ActivityDetail(List<ActivityItem> tActivities, List<ActivityItem> nActivities) {
            this.tActivities         =  tActivities ;
            this.nActivities         =  nActivities ;
            this.hasTActivities      =  tActivities != null && !tActivities.isEmpty() ;
            this.hasNActivities      =  nActivities != null && !nActivities.isEmpty() ;
        }
    }

    public static final class ActivityItem implements Serializable{
        public final int index ;

        public final String activityName ;

        public final String activityLabel ;

        public ActivityItem(int index, String activityName, String activityLabel) {
            this.index = index;
            this.activityName = activityName;
            this.activityLabel = activityLabel;
        }
    }

    public static final class ExceptionDetail implements Serializable{

        public final List<ExceptionInfo> exceptionInfos ;

        public ExceptionDetail(List<ExceptionInfo> exceptionInfos) {
            this.exceptionInfos = exceptionInfos;
        }
    }

}
