package com.gionee.autotest.traversal.testcase.event;


import android.graphics.PointF;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.gionee.autotest.traversal.testcase.exception.RecycleException;
import com.gionee.autotest.traversal.testcase.protocal.EventCallback;
import com.gionee.autotest.traversal.testcase.util.UiHelper;
import com.gionee.autotest.traversal.testcase.util.VLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Created by viking on 8/30/17.
 *
 * traversal event enqueue
 */

public class TraversalSourceRandom implements TraversalEventSource {

    private Random mRandom;
    private TraversalEventQueue mQ;

    private UiDevice mDevice ;

    private EventCallback callback ;

    private static final int FACTOR_CLICK        = 0 ;
    private static final int FACTOR_SCROLL       = 1 ;
    private static final int FACTOR_INPUT        = 2 ;
    private static final int FACTOR_LONG_CLICK   = 3 ;
    private static final int FACTOR_TOUCH        = 4 ;
    public static final int FACTORZ_COUNT        = 5 ;    // should be last+1

    private static final int GESTURE_TAP = 0;
    private static final int GESTURE_DRAG = 1;
    private static final int GESTURE_PINCH_OR_ZOOM = 2;

    /** percentages for each type of event.  These will be remapped to working
     * values after we read any optional values.
     **/
    private float[] mFactors = new float[FACTORZ_COUNT];

    private ExecutorService exec ;

    static String getKeyName(int mKeyCode){
        return KeyEvent.keyCodeToString(mKeyCode) ;
    }

    public TraversalSourceRandom(UiDevice mDevice , Random random, EventCallback callback,
                                 long throttle, boolean randomizeThrottle){
        // default values for random distributions
        // note, these are straight percentages, to match user input (cmd line args)
        // but they will be converted to 0..1 values before the main loop runs.
        mFactors[FACTOR_CLICK]  = 0.8f;
        mFactors[FACTOR_SCROLL] = 0.1f;
        mFactors[FACTOR_INPUT] = 0.05f;
        mFactors[FACTOR_LONG_CLICK] = 0.05f;
        mFactors[FACTOR_TOUCH]  = 0f;
        this.callback = callback ;
        this.mDevice = mDevice ;
        this.mRandom = random;
        this.mQ = new TraversalEventQueue(random, throttle, randomizeThrottle);
        this.exec = Executors.newFixedThreadPool(1);
    }

    /**
     * Adjust the percentages (after applying user values) and then normalize to a 0..1 scale.
     */
    private boolean adjustEventFactors() {
        // go through all values and compute totals for user & default values
        float userSum = 0.0f;
        float defaultSum = 0.0f;
        int defaultCount = 0;
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            if (mFactors[i] <= 0.0f) {   // user values are zero or negative
                userSum -= mFactors[i];
            } else {
                defaultSum += mFactors[i];
                ++defaultCount;
            }
        }
        // if the user request was > 100%, reject it
        if (userSum > 100.0f) {
            VLog.e("** Event weights > 100%");
            return false;
        }
        // if the user specified all of the weights, then they need to be 100%
        if (defaultCount == 0 && (userSum < 99.9f || userSum > 100.1f)) {
            VLog.e("** Event weights != 100%");
            return false;
        }
        // compute the adjustment necessary
        float defaultsTarget = (100.0f - userSum);
        float defaultsAdjustment = defaultsTarget / defaultSum;
        // fix all values, by adjusting defaults, or flipping user values back to >0
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            if (mFactors[i] <= 0.0f) {   // user values are zero or negative
                mFactors[i] = -mFactors[i];
            } else {
                mFactors[i] *= defaultsAdjustment;
            }
        }
        // if verbose, show factors
        VLog.i("// Event percentages:");
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            VLog.i("//   " + i + ": " + mFactors[i] + "%");
        }

        // finally, normalize and convert to running sum
        float sum = 0.0f;
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            sum += mFactors[i] / 100.0f;
            mFactors[i] = sum;
        }

        VLog.i("// Event percentages convert to running sum :");
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            VLog.i("//   " + i + ": " + mFactors[i]);
        }
        return true;
    }

    public boolean validate() {
        return adjustEventFactors();
    }

    @Override
    public void finish() {
        exec.shutdown();
    }

    public void setFactors(int index, float v) {
        mFactors[index] = v;
    }

    /**
     * if the queue is empty, we generate events first
     * @return the first event in the queue
     */
    @Override
    public TraversalEvent getNextEvent() {
        if (mQ.isEmpty()) {
            generateEvents();
        }
        TraversalEvent e = mQ.getFirst();
        mQ.removeFirst();
        return e;
    }

    /**
     * generate a random event based on mFactor
     */
    private void generateEvents() {
        float cls = mRandom.nextFloat();
        VLog.i("current random float value : " + cls);
        if (cls < mFactors[FACTOR_CLICK] ){
            generateClickEvent() ;
        } else if (cls < mFactors[FACTOR_SCROLL] && isScrollObjectExist(mDevice)){
            generateScrollEvent() ;
        } else if (cls < mFactors[FACTOR_INPUT]){
            generateInputEvent() ;
        } else if (cls < mFactors[FACTOR_LONG_CLICK]){
            generateLongClickEvent() ;
        } else{
            generateClickEvent() ;
        }
    }

    private void generateClickEvent(){
        mQ.addLast(new TraversalBasicEvent(this, mDevice, callback, TraversalEvent.EVENT_TYPE_CLICK));
    }

    private void generateInputEvent(){
        mQ.addLast(new TraversalBasicEvent(this, mDevice, callback, TraversalEvent.EVENT_TYPE_INPUT));
    }

    private void generateLongClickEvent(){
        mQ.addLast(new TraversalBasicEvent(this, mDevice, callback, TraversalEvent.EVENT_TYPE_LONG_CLICK));
    }

    /**
     * generate scroll event
     */
    private void generateScrollEvent(){
        mQ.addLast(new TraversalScrollEvent(this, mDevice, mRandom, callback));
    }

    /**
     * Generates a random motion event. This method counts a down, move, and up as multiple events.
     *
     * TODO:  Test & fix the selectors when non-zero percentages
     * TODO:  Long press.
     * TODO:  Fling.
     * TODO:  Meta state
     * TODO:  More useful than the random walk here would be to pick a single random direction
     * and distance, and divvy it up into a random number of segments.  (This would serve to
     * generate fling gestures, which are important).
     *
     * @param random Random number source for positioning
     * @param gesture The gesture to perform.
     *
     */
    private void generatePointerEvent(Random random, int gesture) {
        int height = mDevice.getDisplayHeight() ;
        int width = mDevice.getDisplayWidth() ;
        PointF p1 = randomPoint(random, width, height);
        PointF v1 = randomVector(random);
        long downAt = SystemClock.uptimeMillis();
        mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_DOWN, callback)
                .setDownTime(downAt)
                .addPointer(0, p1.x, p1.y)
                .setIntermediateNote(false));
        // sometimes we'll move during the touch
        if (gesture == GESTURE_DRAG) {
            int count = random.nextInt(10);
            for (int i = 0; i < count; i++) {
                randomWalk(random, width, height, p1, v1);
                mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_MOVE, callback)
                        .setDownTime(downAt)
                        .addPointer(0, p1.x, p1.y)
                        .setIntermediateNote(true));
            }
        } else if (gesture == GESTURE_PINCH_OR_ZOOM) {
            PointF p2 = randomPoint(random, width, height);
            PointF v2 = randomVector(random);
            randomWalk(random, width, height, p1, v1);
            mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_POINTER_DOWN
                    | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT), callback)
                    .setDownTime(downAt)
                    .addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y)
                    .setIntermediateNote(true));
            int count = random.nextInt(10);
            for (int i = 0; i < count; i++) {
                randomWalk(random, width, height, p1, v1);
                randomWalk(random, width, height, p2, v2);
                mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_MOVE, callback)
                        .setDownTime(downAt)
                        .addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y)
                        .setIntermediateNote(true));
            }
            randomWalk(random, width, height, p1, v1);
            randomWalk(random, width, height, p2, v2);
            mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_POINTER_UP
                    | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT), callback)
                    .setDownTime(downAt)
                    .addPointer(0, p1.x, p1.y).addPointer(1, p2.x, p2.y)
                    .setIntermediateNote(true));
        }
        randomWalk(random, width, height, p1, v1);
        mQ.addLast(new TraversalTouchEvent(MotionEvent.ACTION_UP, callback)
                .setDownTime(downAt)
                .addPointer(0, p1.x, p1.y)
                .setIntermediateNote(false));
    }

    private PointF randomPoint(Random random, int width, int height) {
        return new PointF(random.nextInt(width), random.nextInt(height));
    }

    private PointF randomVector(Random random) {
        return new PointF((random.nextFloat() - 0.5f) * 50, (random.nextFloat() - 0.5f) * 50);
    }

    private void randomWalk(Random random, int width, int height, PointF point, PointF vector) {
        point.x = Math.max(Math.min(point.x + random.nextFloat() * vector.x, width ), 0);
        point.y = Math.max(Math.min(point.y + random.nextFloat() * vector.y, height), 0);
    }

    public void generateNextRandomKeyEvent() {
        generatePointerEvent(mRandom, GESTURE_TAP);
    }

    private boolean isScrollObjectExist(UiDevice mDevice){
        boolean exist =  mDevice.findObject(By.clazz(Pattern.compile("android.widget.ListView|" +
                "android.support.v7.widget.RecyclerView|android.support.v4.view.ViewPager|android.widget.ScrollView|android.widget.GridView"))) != null ;
        VLog.d("scroll object exist or not : " + exist);
        return exist ;
    }

    /**
     * find next operation ui element with the given filter
     *
     * @return if next ui element exist , return it; or null will be return
     */
    UiObject2 findNextActionableElement(final UiDevice mDevice, final TraversalEvent.ObjectFilter filter){

        Callable<UiObject2> call = new Callable<UiObject2>() {
            public UiObject2 call() throws Exception {
                List<UiObject2> nodes = new ArrayList<>() ;
                if (filter == TraversalEvent.FILTER_SCROLLABLE){
                    nodes = mDevice.findObjects(By.scrollable(true)) ;
                }else{
                    //first get current root UiObject
                    UiObject2 rootObject = mDevice.findObject(UiHelper.getRootSelect()) ;
                    if (rootObject == null) throw new RecycleException("root ui object should not be null.") ;
                    traversalNode(nodes, rootObject, filter) ;
                }
                if (nodes.size() == 0) return null;
                int randomIndex = mRandom.nextInt(nodes.size()) ;
                return nodes.get(randomIndex) ;
            }
        };

        try {
            Future<UiObject2> future = exec.submit(call);
            UiObject2 obj = future.get(5000, TimeUnit.MILLISECONDS);
            VLog.i("dump current screen ui object success.");
            return obj ;
        } catch (TimeoutException ex) {
            VLog.e("findNextActionableElement TimeoutException...." + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            VLog.e("findNextActionableElement exception..." + e.getMessage());
            e.printStackTrace();
        }
        return null ;
    }

    /**
     * traversal all childrens
     * @param nodes list to restore all actionable children
     * @param root ui element to traversal
     */
    private void traversalNode(List<UiObject2> nodes, UiObject2 root, TraversalEvent.ObjectFilter filter){
        if (root == null || root.getChildCount() == 0) return ;
        List<UiObject2> childs = root.getChildren() ;
        for (UiObject2 child : childs){
            //check is clickable or not
            if (filter.filter(child)){
                //if any one has satisfied , add it to newElements list
                nodes.add(child) ;
            }
            traversalNode(nodes, child, filter);
        }
    }
}
