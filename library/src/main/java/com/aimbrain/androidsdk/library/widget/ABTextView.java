package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the TextView widget.
 */
public class ABTextView extends TextView{
    public ABTextView(Context context) {
        super(context);
    }

    public ABTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "TV" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
