package com.aimbrain.androidsdk.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.aimbrain.androidsdk.library.EventStore;

/**
 * Wrapper for the EditText widget.
 */
public class ABEditText extends EditText {
    public ABEditText(Context context) {
        super(context);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EventStore.addEvent(event.getKeyCode(), event, "ET" + String.valueOf(v.getId()));
                return false;
            }
        });
    }

    public ABEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EventStore.addEvent(event.getKeyCode(), event, "ET" + String.valueOf(v.getId()));
                return false;
            }
        });
    }

    public ABEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EventStore.addEvent(event.getKeyCode(), event, "ET" + String.valueOf(v.getId()));
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ABEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EventStore.addEvent(event.getKeyCode(), event, "ET" + String.valueOf(v.getId()));
                return false;
            }
        });
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
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "ET" + String.valueOf(this.getId()));
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        long time = System.currentTimeMillis();
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        Log.d("TextChanged", "text" + Integer.toString(start) + Integer.toString(lengthBefore) + Integer.toString(lengthAfter));
        if (lengthAfter - lengthBefore == 1) {
            EventStore.addEvent(time, text.charAt(lengthBefore), "ET" + String.valueOf(this.getId()));
        }
        if (lengthAfter - lengthBefore == -1) {
            EventStore.addEvent(time, -1, "ET" + String.valueOf(this.getId()));
        }
    }
}
