package com.gionee.autotest.traversal.testcase.util;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import com.gionee.autotest.traversal.common.util.DensityUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by viking on 9/11/17.
 *
 * class for take screenshot
 */

public class ScreenshotTaker {

    public static final int TYPE_NONE              = 0 ;
    public static final int TYPE_BASIC             = 1 ;
    public static final int TYPE_SWIPE             = 2 ;
    public static final int TYPE_TOUCH             = 3 ;

    private final Instrumentation mInstrumentation ;

    private final int width, height ;

    public ScreenshotTaker(UiDevice mDevice){
        mInstrumentation = InstrumentationRegistry.getInstrumentation() ;
        width = mDevice.getDisplayWidth() ;
        height = mDevice.getDisplayHeight() ;
    }

    public boolean takeShot(File storePath){
        return takeShot(TYPE_NONE, 0f, 0f, 0f, 0f, storePath, 90) ;
    }

    public boolean takeShot(int type, float left, float top, float right, float bottom, File storePath){
        return takeShot(type, left, top, right, bottom, storePath, 90) ;
    }

    public boolean takeShot(int type, float left, float top, float right, float bottom, File storePath, int quality){
        Bitmap screenshot = mInstrumentation.getUiAutomation().takeScreenshot() ;
        if (screenshot == null) {
            return false;
        }
        Bitmap newBitmap = null;
        BufferedOutputStream bos = null;
        try {
            Paint p = new Paint() ;
            p.setAlpha(150);
            p.setAntiAlias(true);
            p.setColor(Color.RED);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(DensityUtils.px2dp(mInstrumentation.getContext(), 25));
            int bitmapWidth = screenshot.getWidth() ;
            int bitmapHeight = screenshot.getHeight() ;
            VLog.i("device width : " + width) ;
            VLog.i("device height : " + height) ;
            VLog.i("bitmap width : " + bitmapWidth) ;
            VLog.i("bitmap height : " + bitmapHeight) ;
            newBitmap = Bitmap.createBitmap(bitmapWidth > bitmapHeight ? height : width, bitmapWidth > bitmapHeight ? width : height, Bitmap.Config.ARGB_8888) ;
            Canvas canvas = new Canvas(newBitmap) ;
            canvas.drawBitmap(screenshot, 0, 0, null);
            if (type == TYPE_BASIC) {
                drawRect(canvas, p, left, top, right, bottom);
            }else if (type == TYPE_SWIPE) {
//                left, top, right, bottom -> fromX, fromY, endX, endY
                drawArrow(canvas, p, left, top, right, bottom);
            }else if (type == TYPE_TOUCH){
                p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(left, top, 20, p);
            }

            canvas.save(Canvas.ALL_SAVE_FLAG) ;
            canvas.restore();

            bos = new BufferedOutputStream(new FileOutputStream(storePath));
            newBitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
            bos.flush();
        } catch (IOException ioe) {
            VLog.i("failed to save screen shot to file");
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                    //ignore it
                }
            }
            if (newBitmap != null)
                newBitmap.recycle();
            screenshot.recycle();
        }
        return true;
    }

    private void drawRect(Canvas canvas, Paint mPaint, float left, float top, float right, float bottom){
        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    private void drawArrow(Canvas canvas, Paint mPaint, float fromX, float fromY, float endX, float endY){
        double H = 12;
        double L = 5;

        double d = Math.atan(L / H);
        double arrow_len = Math.sqrt(L * L + H * H);
        double[] arrXY_1 = rotateVec(endX - fromX, endY - fromY, d, arrow_len);
        double[] arrXY_2 = rotateVec(endX - fromX, endY - fromY, -d, arrow_len);
        int x3 = (int) (endX - arrXY_1[0]);
        int y3 = (int) (endY - arrXY_1[1]);
        int x4 = (int) (endX - arrXY_2[0]);
        int y4 = (int) (endY - arrXY_2[1]);
        canvas.drawLine(fromX, fromY, endX, endY,mPaint);
        Path triangle = new Path();
        triangle.moveTo(endX, endY);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();
        canvas.drawPath(triangle, mPaint);
    }

    private double[] rotateVec(float px, float py, double ang, double newLen) {
        double result[] = new double[2];
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        double d = Math.sqrt(vx * vx + vy * vy);
        vx = vx / d * newLen;
        vy = vy / d * newLen;
        result[0] = vx;
        result[1] = vy;
        return result;
    }
}
