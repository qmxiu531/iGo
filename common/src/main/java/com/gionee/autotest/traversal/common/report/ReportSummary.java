package com.gionee.autotest.traversal.common.report;

import java.io.Serializable;

/**
 * Created by viking on 8/31/17.
 *
 * report for current test
 */

public class ReportSummary implements Serializable{

    public final String testTime ;

    public final String title ;

    public final String subtitle ;

    public final String test_result ;

    public final String test_reason ;

    public final String totalActivities ;

    public final ReportDetail detail ;

    public ReportSummary(String testTime, String title, String subtitle, String test_result,
                         String test_reason,
                         String totalActivities, ReportDetail detail) {
        this.testTime = testTime ;
        this.title = title ;
        this.subtitle = subtitle ;
        this.test_result = test_result ;
        this.test_reason = test_reason ;
        this.totalActivities = totalActivities ;
        this.detail = detail ;
    }
}
