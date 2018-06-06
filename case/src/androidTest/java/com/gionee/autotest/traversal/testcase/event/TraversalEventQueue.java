package com.gionee.autotest.traversal.testcase.event;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by viking on 8/30/17.
 *
 * class for keeping a traversal event queue
 */

public class TraversalEventQueue extends LinkedList<TraversalEvent> {

    private Random mRandom;
    private long mThrottle;
    private boolean mRandomizeThrottle;

    public TraversalEventQueue(Random random, long throttle, boolean randomizeThrottle) {
        super();
        mRandom = random;
        mThrottle = throttle;
        mRandomizeThrottle = randomizeThrottle;
    }

    @Override
    public void addLast(TraversalEvent e) {
        super.add(e);
        //need add a throttlable event or not
        if (e.isThrottlable()) {
            long throttle = mThrottle;
            if (mRandomizeThrottle && (mThrottle > 0)) {
                throttle = mRandom.nextLong();
                if (throttle < 0) {
                    throttle = -throttle;
                }
                throttle %= mThrottle;
                ++throttle;
            }
            super.add(new TraversalThrottleEvent(throttle));
        }
    }
}
