package com.gionee.autotest.traversal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Author Viking Den <dengwj@gionee.com>
 * Version 1.0
 * Time 2016/9/7 0007 15:33
 */
public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}