package com.gionee.autotest.traversal.testcase.event;

import android.graphics.Rect;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import com.gionee.autotest.traversal.common.event.EventInfo;
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
 * basic event for traversal
 */

public class TraversalBasicEvent extends TraversalEvent{

    private UiDevice mDevice                    ;

//    private ObjectFilter  filter                ;

    private TraversalSourceRandom mEventSource   ;

    private int eventType                       ;

    public TraversalBasicEvent(TraversalSourceRandom mEventSource, UiDevice mDevice, EventCallback callback, int eventType) {
        super(eventType, callback);
        this.mDevice = mDevice ;
        this.eventType = eventType ;
        this.mEventSource = mEventSource ;
    }

    @Override
    public int injectEvent(long eventCount, ScreenshotTaker shotTaker) {
        VLog.d("do a clickable or long-clickable or input event...");
        ObjectFilter filter ;
        switch (eventType){
            case TraversalEvent.EVENT_TYPE_CLICK:
                filter = FILTER_CLICK ;
                break ;
            case TraversalEvent.EVENT_TYPE_INPUT:
                filter = FILTER_INPUT ;
                break ;
            case TraversalEvent.EVENT_TYPE_LONG_CLICK:
                filter = FILTER_LONG_CLICK ;
                break ;
            default:
                filter = FILTER_CLICK ;
                break ;
        }
        UiObject2 next = mEventSource.findNextActionableElement(mDevice, filter) ;
        if (next == null){
            VLog.i("can't found next actionable element, generate random key event");
            String screenshot = Config.sWorkSpace + File.separator + Constant.DIR_SCREENSHOT
                    + File.separator + eventCount + ".png" ;
            VLog.i("screenshot file name : " + screenshot);
            File file_shot = new File(screenshot) ;
            shotTaker.takeShot(file_shot) ;
            return INJECT_FAIL;
        }

        //take shot before take action
        String screenshot = Config.sWorkSpace + File.separator + Constant.DIR_SCREENSHOT
                + File.separator + eventCount + ".png" ;
        VLog.i("screenshot file name : " + screenshot);
        File file_shot = new File(screenshot) ;
        Rect rect = next.getVisibleBounds() ;
        shotTaker.takeShot(ScreenshotTaker.TYPE_BASIC, rect.left, rect.top, rect.right, rect.bottom, file_shot) ;

        //take action
        doAction(next, callback);
        return INJECT_SUCCESS;
    }

    private void doAction(UiObject2 element, EventCallback callback){
        String displayName = displayName(element) ;
        VLog.i(displayName);
        switch (eventType){
            case TraversalEvent.EVENT_TYPE_CLICK:
                VLog.i("uiObject exist, click it... ");
                element.click() ;
                callback.finishedEvent(new EventInfo("点击", displayName));
                break ;
            case TraversalEvent.EVENT_TYPE_LONG_CLICK:
                VLog.e("uiObject exist, long click it...");
                element.longClick();
                callback.finishedEvent(new EventInfo("长按", displayName));
                break ;
            case TraversalEvent.EVENT_TYPE_INPUT:
                int index_ = new Random().nextInt(Config.RANDOM_INPUT_TEXTS.length) ;
                String text = Config.RANDOM_INPUT_TEXTS[index_] ;
                VLog.e("uiObject exist, input random text : " + text);
                element.setText(text) ;
                callback.finishedEvent(new EventInfo("输入", displayName));
                break ;
            default:
                VLog.e("not supported yet....");
                break ;
        }
    }
}
