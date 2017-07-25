package com.aimbrain.sdk.mock;

import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.models.TouchEventModel;


public class TouchEventModelMock extends TouchEventModel {
    public TouchEventModelMock(int touchId, int groupId, MotionEvent event, long timestamp, int pointerIndex, View view, boolean sensitive) {
        super(touchId, groupId, event, timestamp, pointerIndex, view, sensitive);
    }

    @Override
    public int convertToIosTouchPhase(int actionMasked) {
        return super.convertToIosTouchPhase(actionMasked);
    }
}
