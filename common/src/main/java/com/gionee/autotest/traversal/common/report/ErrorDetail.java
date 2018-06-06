package com.gionee.autotest.traversal.common.report;

import java.io.Serializable;

/**
 * Created by viking on 8/31/17.
 *
 * this is for report fail
 */

public class ErrorDetail implements Serializable{

    public final int errorCode       ;

//    private String errorName    ;

    public final String errorMsg     ;

    public ErrorDetail(int errorCode, /*String errorName, */String errorMsg) {
        this.errorCode = errorCode;
//        this.errorName = errorName;
        this.errorMsg = errorMsg;
    }
}
