package com.gionee.autotest.traversal.testcase.event;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.VLog;

/**
 * Created by viking on 9/11/17.
 *
 * random key event
 */

public class TraversalKeyEvent extends TraversalEvent {

    private int mDeviceId;
    private long mEventTime;
    private long mDownTime;
    private int mAction;
    private int mKeyCode;
    private int mScanCode;
    private int mMetaState;
    private int mRepeatCount;

    private KeyEvent mKeyEvent;

    public TraversalKeyEvent(EventCallback callback, int action, int keyCode) {
        this(callback, -1, -1, action, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0);
    }

    public TraversalKeyEvent(EventCallback callback, long downTime, long eventTime, int action,
                          int keyCode, int repeatCount, int metaState,
                          int device, int scanCode) {
        super(EVENT_TYPE_KEY, callback);
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mKeyCode = keyCode;
        mRepeatCount = repeatCount;
        mMetaState = metaState;
        mDeviceId = device;
        mScanCode = scanCode;
    }

    public TraversalKeyEvent(EventCallback callback, KeyEvent e) {
        super(EVENT_TYPE_KEY, callback);
        mKeyEvent = e;
    }

    public int getKeyCode() {
        return mKeyEvent != null ? mKeyEvent.getKeyCode() : mKeyCode;
    }

    public int getAction() {
        return mKeyEvent != null ? mKeyEvent.getAction() : mAction;
    }

    public long getDownTime() {
        return mKeyEvent != null ? mKeyEvent.getDownTime() : mDownTime;
    }

    public long getEventTime() {
        return mKeyEvent != null ? mKeyEvent.getEventTime() : mEventTime;
    }

    public void setDownTime(long downTime) {
        if (mKeyEvent != null) {
            throw new IllegalStateException("Cannot modify down time of this key event.");
        }
        mDownTime = downTime;
    }

    public void setEventTime(long eventTime) {
        if (mKeyEvent != null) {
            throw new IllegalStateException("Cannot modify event time of this key event.");
        }
        mEventTime = eventTime;
    }

    @Override
    public boolean isThrottlable() {
        return (getAction() == KeyEvent.ACTION_UP);
    }

    @Override
    public int injectEvent(long eventCount, ScreenshotTaker shotTaker) {
        String note;
        if (mAction == KeyEvent.ACTION_UP) {
            note = "ACTION_UP";
        } else {
            note = "ACTION_DOWN";
        }
        try {
            VLog.i(":Sending Key (" + note + "): "
                    + mKeyCode + "    // "
                    + TraversalSourceRandom.getKeyName(mKeyCode));
        } catch (ArrayIndexOutOfBoundsException e) {
            VLog.e(":Sending Key (" + note + "): "
                    + mKeyCode + "    // Unknown key event");
        }
        KeyEvent keyEvent = mKeyEvent;
        if (keyEvent == null) {
            long eventTime = mEventTime;
            if (eventTime <= 0) {
                eventTime = SystemClock.uptimeMillis();
            }
            long downTime = mDownTime;
            if (downTime <= 0) {
                downTime = eventTime;
            }
            keyEvent = new KeyEvent(downTime, eventTime, mAction, mKeyCode,
                    mRepeatCount, mMetaState, mDeviceId, mScanCode,
                    KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD);
            return injectKeyEvent(keyEvent) ;
        }
        return INJECT_FAIL ;
    }

}
