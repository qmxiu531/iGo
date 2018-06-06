package com.gionee.autotest.traversal.testcase.exception;

/**
 * Created by viking on 8/30/17.
 *
 * restart target application
 */

public class RestartException extends RuntimeException{

    public RestartException() {
        super();
    }

    public RestartException(String message) {
        super(message);
    }
}
