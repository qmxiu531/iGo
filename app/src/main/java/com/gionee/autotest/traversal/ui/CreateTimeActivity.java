package com.gionee.autotest.traversal.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayout;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.util.Constant;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

/**
 * Created by viking on 9/7/17.
 *
 * create new timer
 */

public class CreateTimeActivity extends BaseActivity{

    private static final int FIELD_LENGTH = 2;

    @Bind(R.id.edit_fields_layout) ViewGroup mEditFieldsLayout;
    @Bind(R.id.hour)  EditText mHour;
    @Bind(R.id.minute) EditText mMinute;
    @Bind(R.id.second) EditText mSecond;
    @Bind(R.id.hour_label) TextView mHourLabel;
    @Bind(R.id.minute_label) TextView mMinuteLabel;
    @Bind(R.id.second_label) TextView mSecondLabel;
    @Bind(R.id.focus_grabber) View mFocusGrabber;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.num_pad) GridLayout mNumpad;

    @Override
    protected int layoutResId() {
        return R.layout.layout_new_timer;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    protected boolean isDisplayHomeUpEnabled() {
        return false;
    }

    @OnClick({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    void onClick(TextView view) {
        Log.i(Constant.TAG, "enter number onClick function") ;
        if (mFocusGrabber.isFocused())
            return;
        Log.i(Constant.TAG, "enter set text") ;
        EditText field = getFocusedField();
        int at = field.getSelectionStart();
        field.getText().replace(at, at + 1, view.getText());
        field.setSelection(at + 1);
        if (field.getSelectionStart() == FIELD_LENGTH) {
            // At the end of the current field, so try to focus to the next field.
            // The search will return null if no view can be focused next.
            View next = field.focusSearch(View.FOCUS_RIGHT);
            if (next != null) {
                next.requestFocus();
                if (next instanceof EditText) {
                    // Should always start off at the beginning of the field
                    ((EditText) next).setSelection(0);
                }
            }
        }
    }

    private EditText getFocusedField() {
        return (EditText) mEditFieldsLayout.findFocus();
    }

    @OnTouch({ R.id.hour, R.id.minute, R.id.second })
    boolean switchField(EditText field, MotionEvent event) {
        int inType = field.getInputType(); // backup the input type
        field.setInputType(InputType.TYPE_NULL); // disable soft input
        boolean result = field.onTouchEvent(event); // call native handler
        field.setInputType(inType); // restore input type (to show cursor)
        return result;
    }

    @OnClick(R.id.backspace)
    void backspace() {
        if (mFocusGrabber.isFocused()) {
            mEditFieldsLayout.focusSearch(mFocusGrabber, View.FOCUS_LEFT).requestFocus();
        }
        EditText field = getFocusedField();
        if (field == null)
            return;
        int at = field.getSelectionStart();
        if (at == 0) {
            // At the beginning of current field, so move focus
            // to the preceding field
            View prev = field.focusSearch(View.FOCUS_LEFT);
            if (null == prev) {
                // Reached the beginning of the hours field
                return;
            }
            if (prev.requestFocus()) {
                if (prev instanceof EditText) {
                    // Always move the cursor to the end when moving focus back
                    ((EditText) prev).setSelection(FIELD_LENGTH);
                }
                // Recursively backspace on the newly focused field
                backspace();
            }
        } else {
            field.getText().replace(at - 1, at, "0");
            field.setSelection(at - 1);
        }
    }

    @OnLongClick(R.id.backspace)
    boolean clear() {
        mHour.setText(R.string.zero_text);
        mMinute.setText(R.string.zero_text);
        mSecond.setText(R.string.zero_text);
        mHour.requestFocus();
        mHour.setSelection(0);
        mMinute.setSelection(0);
        mSecond.setSelection(0);
        return true;
    }

    @OnClick(R.id.fab)
    void startTimer() {
        int hour = Integer.parseInt(mHour.getText().toString());
        int minute = Integer.parseInt(mMinute.getText().toString());
        int second = Integer.parseInt(mSecond.getText().toString());
        if (hour == 0 && minute == 0 && second == 0)
            return;
        Intent data = new Intent()
                .putExtra(Constant.EXTRA_HOUR, hour)
                .putExtra(Constant.EXTRA_MINUTE, minute)
                .putExtra(Constant.EXTRA_SECOND, second) ;
        setResult(RESULT_OK, data);
        finish();
    }
}
