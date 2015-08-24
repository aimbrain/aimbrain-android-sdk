package com.aimbrain.androidsdk.library;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Wrapper for the Activity class.
 */
public class ABActivity extends Activity {
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "AC" + getLocalClassName());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        EventStore.addEvent(keyCode, event, "AC" + getLocalClassName());
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "AC" + getLocalClassName());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EventStore.addEvent(event, "AC" + getLocalClassName());
        return super.onTouchEvent(event);
    }
}
