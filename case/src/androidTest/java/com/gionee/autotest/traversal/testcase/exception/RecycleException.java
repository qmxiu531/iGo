package com.gionee.autotest.traversal.testcase.exception;

/**
 * Created by viking on 8/29/17.
 *
 * use this to restart whole logic
 */

public class RecycleException extends RuntimeException {

    public RecycleException() {
        super();
    }

    public RecycleException(String message) {
        super(message);
    }
}
