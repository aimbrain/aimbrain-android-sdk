package com.aimbrain.sdk.motionEvent;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
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


public class AMBNWindowCallback implements Window.Callback {

    private Window.Callback localCallback;
    private Window window;

    public AMBNWindowCallback(Window window) {
        this.localCallback = window.getCallback();
        this.window = window;
        this.window.setCallback(this);

        this.window.getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if(newFocus instanceof EditText){
                    TextEventCollector.getInstance().attachTextChangedListener((EditText)newFocus);
                }
            }
        });
    }

    public Window.Callback getLocalCallback() {
        return localCallback;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        MotionEventHandler.getInstance().touchCaptured(event, System.currentTimeMillis(), window);
        return localCallback.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return localCallback.dispatchKeyEvent(event);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return localCallback.dispatchKeyShortcutEvent(event);
    }


    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return localCallback.dispatchTrackballEvent(event);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return localCallback.dispatchGenericMotionEvent(event);
    }


    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return localCallback.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        return localCallback.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return localCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return localCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return localCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return localCallback.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        localCallback.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        localCallback.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        localCallback.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        localCallback.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        localCallback.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        localCallback.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return localCallback.onSearchRequested();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return localCallback.onSearchRequested(searchEvent);
    }

    @SuppressLint("NewApi")
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return localCallback.onWindowStartingActionMode(callback);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return localCallback.onWindowStartingActionMode(callback, type);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActionModeStarted(ActionMode mode) {
        localCallback.onActionModeStarted(mode);

    }

    @SuppressLint("NewApi")
    @Override
    public void onActionModeFinished(ActionMode mode) {
        localCallback.onActionModeFinished(mode);

    }
}