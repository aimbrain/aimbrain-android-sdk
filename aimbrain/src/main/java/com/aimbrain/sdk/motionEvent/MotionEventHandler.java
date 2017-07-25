package com.aimbrain.sdk.motionEvent;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.collectors.MotionEventCollector;
import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.util.Logger;


public class MotionEventHandler {
    private static final String TAG = MotionEventHandler.class.getSimpleName();

    private static MotionEventHandler motionEventHandler;

    public static MotionEventHandler getInstance(){
        if(motionEventHandler == null){
            motionEventHandler = new MotionEventHandler();
        }
        return motionEventHandler;
    }

    private MotionEventHandler() {
    }

    public void touchCaptured(MotionEvent motionEvent, long timestamp, Window window) {

        View view = findViewBelowMotionEvent(motionEvent, window);
        Logger.v(TAG, "Touch captured: " + motionEvent.getAction() + ", view " + view);

        if(!Manager.getInstance().isViewIgnored(view)) {
            SensorEventCollector.getInstance().startCollectingData(500);
        }

        MotionEventCollector.getInstance().processMotionEvent(motionEvent, view, timestamp);
    }

    private View findViewBelowMotionEvent(MotionEvent event, Window window) {
        ViewGroup rootView = (ViewGroup)window.getDecorView();
        float x = event.getRawX();
        float y = event.getRawY();
        return findViewAtWindowPoint(rootView, x, y);
    }

    private View findViewAtWindowPoint(ViewGroup viewGroup, float x, float y) {
        for (int i = viewGroup.getChildCount()-1; i >= 0; i--) {
            final View child = viewGroup.getChildAt(i);
            if(Manager.getInstance().isTouchTrackingIgnored(child))
                continue;
            if(isPointInView(x, y, child)) {
                if (child instanceof ViewGroup) {
                    return findViewAtWindowPoint((ViewGroup) child, x, y);
                }
                return child;
            }
        }
        return viewGroup;
    }

    private boolean isPointInView(float x, float y, View view){
        Rect outRect = new Rect();
        view.getGlobalVisibleRect(outRect);
        return outRect.contains((int)x, (int)y);
    }
}
