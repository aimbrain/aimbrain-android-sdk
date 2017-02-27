package com.aimbrain.sdk.motionEvent;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;

import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.util.Logger;

import java.lang.ref.WeakReference;


public class AMBNWindowCallback implements Window.Callback {
    private static final String TAG = AMBNWindowCallback.class.getSimpleName();

    private Window.Callback wrappedCallback;
    private WeakReference<Window> window;

    public AMBNWindowCallback(Window window) {
        Window.Callback wrappedCallback = window.getCallback();
        window.setCallback(this);
        window.getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (newFocus instanceof EditText){
                    Logger.v(TAG, "attach text listener " + newFocus);
                    TextEventCollector.getInstance().attachTextChangedListener((EditText)newFocus);
                }
            }
        });

        Logger.v(TAG, "wrapped window callback " + wrappedCallback);
        this.wrappedCallback = wrappedCallback;
        this.window = new WeakReference<>(window);
    }

    public Window.Callback getWrappedCallback() {
        return wrappedCallback;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Window window = this.window.get();
        if (window != null) {
            MotionEventHandler.getInstance().touchCaptured(event, System.currentTimeMillis(), window);
        } else {
            Log.v(TAG, "dispatch with disposed window");
        }
        return wrappedCallback.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return wrappedCallback.dispatchKeyEvent(event);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return wrappedCallback.dispatchKeyShortcutEvent(event);
    }


    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return wrappedCallback.dispatchTrackballEvent(event);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return wrappedCallback.dispatchGenericMotionEvent(event);
    }


    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return wrappedCallback.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        return wrappedCallback.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return wrappedCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return wrappedCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return wrappedCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return wrappedCallback.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        wrappedCallback.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        wrappedCallback.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        wrappedCallback.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        wrappedCallback.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        wrappedCallback.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        wrappedCallback.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return wrappedCallback.onSearchRequested();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return wrappedCallback.onSearchRequested(searchEvent);
    }

    @SuppressLint("NewApi")
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return wrappedCallback.onWindowStartingActionMode(callback);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return wrappedCallback.onWindowStartingActionMode(callback, type);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActionModeStarted(ActionMode mode) {
        wrappedCallback.onActionModeStarted(mode);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActionModeFinished(ActionMode mode) {
        wrappedCallback.onActionModeFinished(mode);
    }
}