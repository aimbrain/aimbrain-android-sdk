package com.aimbrain.androidsdk.library;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Wrapper for the AppCompatActivity class.
 */
public class ABAppCompatActivity extends AppCompatActivity {
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EventStore.addEvent(event, "A" + getLocalClassName());
        return super.onTouchEvent(event);
    }
}
