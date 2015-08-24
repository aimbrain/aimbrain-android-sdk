package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the ListView widget.
 */
public class ABListView extends ListView {
    public ABListView(Context context) {
        super(context);
    }

    public ABListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        EventStore.addEvent(ev, "LV" + String.valueOf(this.getId()));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "LV" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
