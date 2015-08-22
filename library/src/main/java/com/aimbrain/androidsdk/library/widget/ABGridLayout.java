package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridLayout;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the GridLayout widget.
 */
public class ABGridLayout extends GridLayout {
    public ABGridLayout(Context context) {
        super(context);
    }

    public ABGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        EventStore.addEvent(ev, "GL" + String.valueOf(this.getId()));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "GL" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
