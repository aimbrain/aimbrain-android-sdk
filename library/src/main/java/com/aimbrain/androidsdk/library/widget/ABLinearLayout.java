package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the LinearLayout widget.
 */
public class ABLinearLayout extends LinearLayout {
    public ABLinearLayout(Context context) {
        super(context);
    }

    public ABLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        EventStore.addEvent(ev, "LL" + String.valueOf(this.getId()));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "LL" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
