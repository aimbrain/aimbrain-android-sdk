package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the Button widget.
 */
public class ABButton extends Button {
    public ABButton(Context context) {
        super(context);
    }

    public ABButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "BT" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }
}
