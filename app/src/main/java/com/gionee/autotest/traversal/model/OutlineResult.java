package com.gionee.autotest.traversal.model;

/**
 * Created by viking on 9/14/17.
 *
 * outline model of main activity
 */

public class OutlineResult {

    private String timestamp ;

    private String pkg ;

    private String testresult ;

    private String testDuration ;

    private String testCoverage ;

    private String testException ;

    public OutlineResult(String timestamp, String pkg, String testresult, String testDuration, String testCoverage, String testException) {
        this.timestamp = timestamp;
        this.pkg = pkg ;
        this.testresult = testresult;
        this.testDuration = testDuration;
        this.testCoverage = testCoverage;
        this.testException = testException;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPkg() {
        return pkg;
    }

    public String getTestresult() {
        return testresult;
    }

    public String getTestDuration() {
        return testDuration;
    }

    public String getTestCoverage() {
        return testCoverage;
    }

    public String getTestException() {
        return testException;
    }
}
