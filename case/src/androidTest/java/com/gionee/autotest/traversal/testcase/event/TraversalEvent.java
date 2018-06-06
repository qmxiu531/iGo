package com.gionee.autotest.traversal.testcase.event;

import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.support.test.uiautomator.UiObject2;
import android.text.TextUtils;
import android.view.InputEvent;
import android.view.MotionEvent;

import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.testcase.util.ScreenshotTaker;
import com.gionee.autotest.traversal.testcase.util.VLog;

import java.lang.reflect.Method;

/**
 * Created by viking on 8/30/17.
 *
 * abstract class for traversal event
 */

public abstract class TraversalEvent {

    protected int eventType;

    protected EventCallback callback ;

    public static final int EVENT_TYPE_KEY = 0;
    public static final int EVENT_TYPE_TOUCH = 1;
    public static final int EVENT_TYPE_TRACKBALL = 2;
    public static final int EVENT_TYPE_ROTATION = 3;  // Screen rotation
    public static final int EVENT_TYPE_ACTIVITY = 4;
    public static final int EVENT_TYPE_FLIP = 5; // Keyboard flip
    public static final int EVENT_TYPE_THROTTLE = 6;
    public static final int EVENT_TYPE_PERMISSION = 7;
    public static final int EVENT_TYPE_NOOP = 8;

    public static final int EVENT_TYPE_CLICK = 9 ;
    public static final int EVENT_TYPE_LONG_CLICK = 10 ;
    public static final int EVENT_TYPE_INPUT = 11 ;
    public static final int EVENT_TYPE_SCROLL = 12 ;

    public static final int INJECT_SUCCESS = 1;
    public static final int INJECT_FAIL = 0;

    public TraversalEvent(int type, EventCallback callback) {
        eventType = type;
        this.callback = callback ;
    }
    /**
     * @return event type
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * @return true if it is safe to throttle after this event, and false otherwise.
     */
    public boolean isThrottlable() {
        return true;
    }

    public boolean shouldCounter(){
        return true ;
    }

    /**
     * a method for injecting event
     *
     * @return INJECT_SUCCESS if it goes through, and INJECT_FAIL if it fails
     *         in the case of exceptions, return its corresponding error code
     */
    public abstract int injectEvent(long eventCount, ScreenshotTaker shotTaker);


    int injectKeyEvent(InputEvent event){
        try{
            String methodName = "getInstance";
            Object[] objArr = new Object[0];
            InputManager im = (InputManager) InputManager.class.getDeclaredMethod(methodName, new Class[0])
                    .invoke(null, objArr);


            //Make MotionEvent.obtain() method accessible
            methodName = "obtain";
            MotionEvent.class.getDeclaredMethod(methodName, new Class[0]).setAccessible(true);

            //Get the reference to injectInputEvent method
            methodName = "injectInputEvent";
            Method injectInputEventMethod = InputManager.class.getMethod(methodName, new Class[]{InputEvent.class, Integer.TYPE});
            Object result = injectInputEventMethod.invoke(im, new Object[]{event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT});
            boolean resultLast = (boolean) result;
            if (!resultLast){
                return INJECT_FAIL;
            }
            return INJECT_SUCCESS;
        }catch (Exception e){
            VLog.i("inject key event fail : " + e.getMessage());
            e.printStackTrace();
        }
        return INJECT_FAIL;
    }

    /**
     * check is editable or not
     * @param className class name to judge
     * @return if class name is end with .EditText , return true; or false
     */
    protected static boolean isEditable(String className){
        return !TextUtils.isEmpty(className) && className.endsWith(Constant.EDITTEXT_SUFFIX_NAME) ;
    }

    //TODO need add more scrollable view to here
    protected static boolean isScrollableView(String className){
        return className.equals(Constant.LISTVIEW_CLASSNAME)
                || className.equals(Constant.GRIDVIEW_CLASSNAME)
                || className.equals(Constant.SCROLLVIEW_CLASSNAME)
                || className.equals(Constant.VIEWPAGER_CLASSNAME)
                || className.equals(Constant.RECYCLERVIEW_CLASSNAME);
    }

    /**
     * Input Event Injection Synchronization Mode: Wait for result.
     * Waits for previous events to be dispatched so that the input dispatcher can
     * determine whether input event injection will be permitted based on the current
     * input focus.  Does not wait for the input event to finish being handled
     * by the application.
     */
    static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;  // see InputDispatcher.h

    public interface ObjectFilter {

        boolean filter(UiObject2 uiObject);
    }

    static final ObjectFilter FILTER_CLICK = new ObjectFilter() {
        @Override
        public boolean filter(UiObject2 uiObject) {
            return uiObject.isClickable() ;
        }
    } ;

    static final ObjectFilter FILTER_LONG_CLICK = new ObjectFilter() {
        @Override
        public boolean filter(UiObject2 uiObject) {
            return uiObject.isLongClickable();
        }
    } ;

    static final ObjectFilter FILTER_INPUT = new ObjectFilter() {
        @Override
        public boolean filter(UiObject2 uiObject) {
            return isEditable(uiObject.getClassName());
        }
    } ;

    static final ObjectFilter FILTER_SCROLLABLE = new ObjectFilter() {
        @Override
        public boolean filter(UiObject2 uiObject) {
            return isScrollableView(uiObject.getClassName());
        }
    } ;

    String displayName(UiObject2 uiObject) {
        String className = uiObject.getClassName();
        if (className == null){
            return "NoClassName";
        }
//        String pInfo ;
        String text = uiObject.getText();
        String desc = uiObject.getContentDescription();
        String resId = uiObject.getResourceName() ;
        Rect rect = uiObject.getVisibleBounds() ;
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(className);
        builder.append(";");

        if (text != null && !text.isEmpty()
                && !Constant.EDITTEXT_CLASSNAME.equals(className)) {
            builder.append("text:");
            builder.append(text);
            builder.append(";") ;
        }
        if (resId != null && !resId.isEmpty()) {
            builder.append("resId:");
            builder.append(resId);
            builder.append(";");
        }

        if (desc != null && ! desc.isEmpty()){
            builder.append("desc:");
            builder.append(desc);
            builder.append(";");
        }

        if (rect != null){
            builder.append("[") ;
            builder.append(rect.left) ;
            builder.append(",") ;
            builder.append(rect.top) ;
            builder.append("][") ;
            builder.append(rect.right) ;
            builder.append(",") ;
            builder.append(rect.bottom) ;
            builder.append("];") ;
        }
        builder.append("]");
        return builder.toString();
    }

}
