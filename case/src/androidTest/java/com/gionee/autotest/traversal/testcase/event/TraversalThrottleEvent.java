package com.gionee.autotest.traversal.testcase.event;

import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.VLog;

/**
 * Created by viking on 8/30/17.
 *
 * traversal throttle event
 */

public class TraversalThrottleEvent extends TraversalEvent{
    private long mThrottle;

    public TraversalThrottleEvent(long throttle) {
        super(TraversalEvent.EVENT_TYPE_THROTTLE, null);
        mThrottle = throttle;
    }

    @Override
    public boolean shouldCounter() {
        return false;
    }

    @Override
    public int injectEvent(long eventCount, ScreenshotTaker shotTaker) {
        VLog.i("Sleeping for " + mThrottle + " milliseconds");
        try {
            Thread.sleep(mThrottle);
        } catch (InterruptedException e1) {
            VLog.i("** Traversal interrupted in sleep.");
            return TraversalEvent.INJECT_FAIL;
        }
        return TraversalEvent.INJECT_SUCCESS;
    }
}
