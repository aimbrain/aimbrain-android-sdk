package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the RelativeLayout widget.
 */
public class ABRelativeLayout extends RelativeLayout {
    public ABRelativeLayout(Context context) {
        super(context);
    }

    public ABRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        EventStore.addEvent(ev, "RL" + String.valueOf(this.getId()));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "RL" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
