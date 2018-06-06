package com.gionee.autotest.traversal.testcase.event;

import android.view.InputDevice;
import android.view.KeyEvent;

import com.gionee.autotest.traversal.testcase.protocal.EventCallback;

/**
 * Created by viking on 9/11/17.
 *
 * traversal touch event
 */

public class TraversalTouchEvent extends TraversalMotionEvent{

    public TraversalTouchEvent(int action, EventCallback callback) {
        super(callback, EVENT_TYPE_TOUCH, InputDevice.SOURCE_TOUCHSCREEN, action);
    }

    @Override
    public boolean shouldCounter() {
        return getAction() == KeyEvent.ACTION_UP;
    }

    @Override
    protected String getTypeLabel() {
        return "Touch";
    }
}
