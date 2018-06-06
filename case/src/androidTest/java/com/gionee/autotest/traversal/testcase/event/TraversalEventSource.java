package com.gionee.autotest.traversal.testcase.event;

/**
 * Created by viking on 8/30/17.
 *
 * event source interface
 */

public interface TraversalEventSource {

    /**
     * @return the next traversal event from the source
     */
    TraversalEvent getNextEvent();

    void generateNextRandomKeyEvent() ;

    boolean validate() ;

    void finish() ;
}
