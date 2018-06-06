package com.gionee.autotest.traversal.testcase.event;

import android.support.test.uiautomator.UiDevice;

/**
 * Created by viking on 1/12/18.
 *
 * Generate ai random events
 */

public class TraversalAISourceRandom implements TraversalEventSource {

    private UiDevice mDevice ;

    public TraversalAISourceRandom(UiDevice mDevice){
        this.mDevice = mDevice ;
    }

    @Override
    public TraversalEvent getNextEvent() {




        return null;
    }

    @Override
    public void generateNextRandomKeyEvent() {

    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public void finish() {

    }
}
