package com.gionee.autotest.traversal.testcase.event;

import android.graphics.Rect;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import com.gionee.autotest.traversal.common.event.EventInfo;
import com.gionee.autotest.traversal.common.model.Action;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.Config;
import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.VLog;

import java.io.File;
import java.util.Random;

/**
 * Created by viking on 8/30/17.
 *
 * traversal swipe event
 */

public class TraversalScrollEvent extends TraversalEvent {

    private UiDevice mDevice                    ;

    private Random mRandom                      ;

    private ObjectFilter  filter        ;

    private TraversalSourceRandom mEventSource ;

    public TraversalScrollEvent(TraversalSourceRandom mEventSource, UiDevice mDevice, Random mRandom, EventCallback callback) {
        super(EVENT_TYPE_SCROLL, callback);
        this.mRandom = mRandom ;
        this.mDevice = mDevice ;
        this.filter = FILTER_SCROLLABLE ;
        this.mEventSource = mEventSource ;
    }

    @Override
    public int injectEvent(long eventCount, ScreenshotTaker shotTaker) {
        VLog.d("do a scrollable event...");
        UiObject2 next = mEventSource.findNextActionableElement(mDevice, filter) ;
        if (next == null){
            VLog.i("can't found next scrollable element, generate random key event...");
            String screenshot = Config.sWorkSpace + File.separator + Constant.DIR_SCREENSHOT
                    + File.separator + eventCount + ".png" ;
            VLog.i("screenshot file name : " + screenshot);
            File file_shot = new File(screenshot) ;
            shotTaker.takeShot(file_shot) ;
            return INJECT_FAIL;
        }
        doAction(next, shotTaker, eventCount, mDevice, mRandom, callback);
        return INJECT_SUCCESS;
    }

    private static final int DIRECTION_COUNT = 6 ;
    private static final int HORIZONTAL  = 0 ;
    private static final int VERTICAL    = 1 ;

    private void doAction(UiObject2 object, ScreenshotTaker shotTaker,  long eventCount, UiDevice mDevice, Random mRandom, EventCallback callback) {
        String displayName = displayName(object) ;
        VLog.i(displayName);
        String className = object.getClassName();
        float[] mFactors = new float[DIRECTION_COUNT];
        int type = getTypeBaseClassName(className);
        if (type == HORIZONTAL) {
            mFactors[0] = 0.25f;
            mFactors[1] = 0.60f;
            mFactors[2] = 0;
            mFactors[3] = 0f;
        } else {
            mFactors[0] = 0f;
            mFactors[1] = 0f;
            mFactors[2] = 0.60f;
            mFactors[3] = 0.25f;
        }
        mFactors[4] = 0.05f;
        mFactors[5] = 0.15f;
        float cls = mRandom.nextFloat();
        if (cls < mFactors[4]) {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_START, type);
        } else if (cls < mFactors[5]) {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_END, type);
        } else if (cls < mFactors[0] && type == HORIZONTAL) {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_LEFT, type);
        } else if (cls < mFactors[3]) {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_DOWN, type);
        } else if (cls < mFactors[1] && type == HORIZONTAL) {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_RIGHT, type);
        } else {
            swipe(shotTaker, eventCount, mDevice, callback, object, Action.ACTION_SCROLL_UP, type);
        }
    }

    private static final int PADDING = 10 ;
    private static final int STATUS_BAR_PADDING = 80 ;
    private static final int DELTA = 50 ;
    private static final int STEPS = 50 ;

    private static final int MAX_SWIPE = 5 ;

    private void swipe(ScreenshotTaker shotTaker, long eventCount,  UiDevice mDevice, EventCallback callback, UiObject2 uiObject, int direction, int type){
        Rect rect = uiObject.getVisibleBounds() ;
        if (rect == null) {
            VLog.e("scrollable ui object visible bounds is empty...");
            return ;
        }
        String displayName = displayName(uiObject) ;
        String coord ;
        int top = rect.top ;
        int left = rect.left ;
        int bottom = rect.bottom ;
        int right = rect.right ;
        int startX, startY , endX , endY ;

        switch (direction){
            case Action.ACTION_SCROLL_LEFT :
                if (right - left < DELTA){
                    VLog.e("width too small to swipe, skip it...");
                    return ;
                }
                startY  = top + (bottom - top) / 2 ;
                endY    = top + (bottom - top) / 2 ;
                startX  = left + PADDING ;
                endX    = right - PADDING ;

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                mDevice.swipe(startX, startY, endX, endY, STEPS) ;
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo("右滑", displayName + coord));
                break ;
            case Action.ACTION_SCROLL_RIGHT:
                if (right - left < DELTA){
                    VLog.e("width too small to swipe, skip it...");
                    return ;
                }
                startY  = top + (bottom - top) / 2 ;
                endY    = top + (bottom - top) / 2 ;
                startX  = right - PADDING;
                endX    = left + PADDING  ;

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                mDevice.swipe(startX, startY, endX, endY, STEPS) ;
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo("左滑", displayName + coord));
                break ;
            case Action.ACTION_SCROLL_UP:
                if (bottom - top < DELTA){
                    VLog.e("height too small to swipe, skip it...");
                    return ;
                }
                startX  = left + (right - left) / 2 ;
                endX    = left + (right - left) / 2 ;
                startY  = bottom - PADDING ;
                endY    = top + PADDING ;

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                mDevice.swipe(startX, startY, endX, endY, STEPS) ;
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo("上滑", displayName + coord));
                break ;
            case Action.ACTION_SCROLL_DOWN:
                if (bottom - top < DELTA){
                    VLog.e("height too small to swipe, skip it...");
                    return ;
                }
                startX  = left + (right - left) / 2 ;
                endX    = left + (right - left) / 2 ;
                startY  = top + PADDING ;
                // forbidden pull out notification area
                if (top == 0){
                    startY = top + STATUS_BAR_PADDING ;
                }
                endY    = bottom - PADDING ;

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                mDevice.swipe(startX, startY, endX, endY, STEPS) ;
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo("下滑", displayName + coord));
                break ;

            case Action.ACTION_SCROLL_START:
                String typeMsg = "下滑" ;
                if (type == HORIZONTAL){
                    if (right - left < DELTA){
                        VLog.e("width too small to swipe, skip it...");
                        return ;
                    }
                    typeMsg = "右滑" ;
                    startY  = top + (bottom - top) / 2 ;
                    endY    = top + (bottom - top) / 2 ;
                    startX  = left + PADDING ;
                    endX    = right - PADDING ;
                }else{
                    if (bottom - top < DELTA){
                        VLog.e("height too small to swipe, skip it...");
                        return ;
                    }
                    startX  = left + (right - left) / 2 ;
                    endX    = left + (right - left) / 2 ;
                    startY  = top + PADDING ;
                    // forbidden pull out notification area
                    if (top == 0){
                        startY = top + STATUS_BAR_PADDING ;
                    }
                    endY    = bottom - PADDING ;
                }

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                for (int i = 0 ; i < MAX_SWIPE ; i++){
                    if (!mDevice.swipe(startX, startY, endX, endY, STEPS)){
                        VLog.i("ACTION_SCROLL_START with : " + i);
                        break ;
                    }
                }
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo(typeMsg, displayName + coord));
                break ;

            case Action.ACTION_SCROLL_END:
                String typeMsg1 = "上滑" ;
                if (type == HORIZONTAL){
                    if (right - left < DELTA){
                        VLog.e("width too small to swipe, skip it...");
                        return ;
                    }
                    typeMsg1 = "左滑" ;
                    startY  = top + (bottom - top) / 2 ;
                    endY    = top + (bottom - top) / 2 ;
                    startX  = right - PADDING ;
                    endX    = left + PADDING ;
                }else{
                    if (bottom - top < DELTA){
                        VLog.e("height too small to swipe, skip it...");
                        return ;
                    }
                    startX  = left + (right - left) / 2 ;
                    endX    = left + (right - left) / 2 ;
                    startY  = bottom - PADDING ;
                    endY    = top + PADDING ;
                }

                //take shot before any action
                takeScreenshot(shotTaker, eventCount, startX, startY, endX, endY);

                for (int i = 0 ; i < MAX_SWIPE ; i++){
                    if (!mDevice.swipe(startX, startY, endX, endY, STEPS)){
                        VLog.i("ACTION_SCROLL_END with : " + i);
                        break ;
                    }
                }
                coord = ";start:[" + startX + "," + startY +"],end:[" + endX + "," + endY + "]" ;
                callback.finishedEvent(new EventInfo(typeMsg1, displayName + coord));
                break ;
        }
    }

    private void takeScreenshot(ScreenshotTaker shotTaker,  long eventCount, int startX, int startY, int endX, int endY){
        String screenshot = Config.sWorkSpace + File.separator + Constant.DIR_SCREENSHOT
                + File.separator + eventCount + ".png" ;
        VLog.i("screenshot file name : " + screenshot);
        File file_shot = new File(screenshot) ;
        shotTaker.takeShot(ScreenshotTaker.TYPE_SWIPE, startX, startY, endX, endY, file_shot) ;
    }

    private int getTypeBaseClassName(String className){
        if (Constant.VIEWPAGER_CLASSNAME.equals(className)){
            return HORIZONTAL ;
        }
        return VERTICAL ;
    }
}
