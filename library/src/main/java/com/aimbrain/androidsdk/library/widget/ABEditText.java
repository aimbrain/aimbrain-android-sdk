package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the EditText widget.
 */
public class ABEditText extends EditText {
    public ABEditText(Context context) {
        super(context);
    }

    public ABEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ABEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        EventStore.addEvent(event, "ET" + String.valueOf(this.getId()));
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "ET" + String.valueOf(this.getId()));
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "ET" + String.valueOf(this.getId()));
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "ET" + String.valueOf(this.getId()));
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        long time = System.currentTimeMillis();
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (lengthAfter - lengthBefore == 1) {
            EventStore.addEvent(time, text.charAt(start + lengthBefore), "ET" + String.valueOf(this.getId()));
        }
        if (lengthAfter - lengthBefore == -1) {
            EventStore.addEvent(time, 8, "ET" + String.valueOf(this.getId()));
        }
    }
}
