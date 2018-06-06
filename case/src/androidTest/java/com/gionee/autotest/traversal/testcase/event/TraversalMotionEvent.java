package com.gionee.autotest.traversal.testcase.event;

import android.os.SystemClock;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.gionee.autotest.traversal.common.event.EventInfo;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.VLog;

import java.io.File;

/**
 * Created by viking on 9/11/17.
 *
 * traversal motion event
 */

public abstract class TraversalMotionEvent extends TraversalEvent {

    private long mDownTime;
    private long mEventTime;
    private int mAction;
    private SparseArray<MotionEvent.PointerCoords> mPointers;
    private int mMetaState;
    private float mXPrecision;
    private float mYPrecision;
    private int mDeviceId;
    private int mSource;
    private int mFlags;
    private int mEdgeFlags;

    //If true, this is an intermediate step (more verbose logging, only)
    private boolean mIntermediateNote;

    protected TraversalMotionEvent(EventCallback callback, int type, int source, int action) {
        super(type, callback);
        mSource = source;
        mDownTime = -1;
        mEventTime = -1;
        mAction = action;
        mPointers = new SparseArray<>();
        mXPrecision = 1;
        mYPrecision = 1;
    }

    public TraversalMotionEvent addPointer(int id, float x, float y) {
        return addPointer(id, x, y, 0, 0);
    }

    public TraversalMotionEvent addPointer(int id, float x, float y,
                                        float pressure, float size) {
        MotionEvent.PointerCoords c = new MotionEvent.PointerCoords();
        c.x = x;
        c.y = y;
        c.pressure = pressure;
        c.size = size;
        mPointers.append(id, c);
        return this;
    }

    public float[] getRectF(){
        if (mPointers != null && mPointers.size() > 0){
            return new float[]{mPointers.get(0).x, mPointers.get(0).y};
        }
        return null ;
    }

    public TraversalMotionEvent setIntermediateNote(boolean b) {
        mIntermediateNote = b;
        return this;
    }

    public boolean getIntermediateNote() {
        return mIntermediateNote;
    }

    public int getAction() {
        return mAction;
    }

    public long getDownTime() {
        return mDownTime;
    }

    public long getEventTime() {
        return mEventTime;
    }

    public TraversalMotionEvent setDownTime(long downTime) {
        mDownTime = downTime;
        return this;
    }

    public TraversalMotionEvent setEventTime(long eventTime) {
        mEventTime = eventTime;
        return this;
    }

    public TraversalMotionEvent setMetaState(int metaState) {
        mMetaState = metaState;
        return this;
    }

    public TraversalMotionEvent setPrecision(float xPrecision, float yPrecision) {
        mXPrecision = xPrecision;
        mYPrecision = yPrecision;
        return this;
    }

    public TraversalMotionEvent setDeviceId(int deviceId) {
        mDeviceId = deviceId;
        return this;
    }

    public TraversalMotionEvent setEdgeFlags(int edgeFlags) {
        mEdgeFlags = edgeFlags;
        return this;
    }

    /**
     *
     * @return instance of a motion event
     */
    private MotionEvent getEvent() {
        int pointerCount = mPointers.size();
        int[] pointerIds = new int[pointerCount];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            pointerIds[i] = mPointers.keyAt(i);
            pointerCoords[i] = mPointers.valueAt(i);
        }
        return MotionEvent.obtain(mDownTime,
                mEventTime < 0 ? SystemClock.uptimeMillis() : mEventTime,
                mAction, pointerCount, pointerIds, pointerCoords,
                mMetaState, mXPrecision, mYPrecision, mDeviceId, mEdgeFlags, mSource, mFlags);
    }

    @Override
    public boolean isThrottlable() {
        return (getAction() == MotionEvent.ACTION_UP);
    }

    @Override
    public int injectEvent(long eventCount, ScreenshotTaker shotTaker) {
        MotionEvent me = getEvent();
        StringBuilder msg = new StringBuilder(":Sending ");
        msg.append(getTypeLabel()).append(" (");
        switch (me.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                msg.append("ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                msg.append("ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                msg.append("ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                msg.append("ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                msg.append("ACTION_POINTER_DOWN ").append(me.getPointerId(me.getActionIndex()));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                msg.append("ACTION_POINTER_UP ").append(me.getPointerId(me.getActionIndex()));
                break;
            default:
                msg.append(me.getAction());
                break;
        }
        msg.append("):");
        int pointerCount = me.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            msg.append(" ").append(me.getPointerId(i));
            msg.append(":(").append(me.getX(i)).append(",").append(me.getY(i)).append(")");
        }
        VLog.i(msg.toString());
        try {

            float[] coords = getRectF() ;
            if (coords != null && shouldCounter()){
                String screenshot = Config.sWorkSpace + File.separator + Constant.DIR_SCREENSHOT
                        + File.separator + eventCount + ".png" ;
                VLog.i("screenshot file name : " + screenshot);
                callback.finishedEvent(new EventInfo("随机点击", "[" + coords[0] + "," + coords[1] + "]"));
                File file_shot = new File(screenshot) ;
                shotTaker.takeShot(ScreenshotTaker.TYPE_TOUCH, coords[0], coords[1], 0f, 0f, file_shot) ;
            }
            return injectKeyEvent(me) ;
        } finally {
            me.recycle();
        }
    }

    abstract String getTypeLabel();
}
