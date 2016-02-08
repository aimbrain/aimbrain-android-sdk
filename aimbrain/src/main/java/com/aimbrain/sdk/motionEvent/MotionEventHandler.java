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


public class MotionEventHandler {

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
        ViewGroup rootView = (ViewGroup)window.getDecorView();
        View view = findTargetView(rootView, motionEvent);

        if(!Manager.getInstance().isViewIgnored(view)) {
            SensorEventCollector.getInstance().startCollectingData(500);
            MotionEventCollector.getInstance().processMotionEvent(motionEvent, view, timestamp);
        }
    }

    private View findTargetView(ViewGroup viewGroup, MotionEvent motionEvent) {
        for (int i = viewGroup.getChildCount()-1; i >= 0; i--) {
            final View child = viewGroup.getChildAt(i);
            if(isInsideTheView(motionEvent, child)) {
                if (child instanceof ViewGroup) {
                    return findTargetView((ViewGroup) child, motionEvent);
                }
                return child;
            }
        }
        return viewGroup;
    }

    private boolean isInsideTheView(MotionEvent motionEvent, View view){
        float motionX = motionEvent.getRawX();
        float motionY = motionEvent.getRawY();
        Rect outRect = new Rect();
        int[] viewStartLocation = new int[2];
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(viewStartLocation);
        outRect.offset(viewStartLocation[0], viewStartLocation[1]);
        return outRect.contains((int)motionX, (int)motionY);
    }

}
