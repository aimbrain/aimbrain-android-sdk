package com.aimbrain.sdk.mock;

import android.support.annotation.VisibleForTesting;

import com.aimbrain.sdk.collectors.MotionEventCollector;
import com.aimbrain.sdk.models.EventModel;

import java.util.HashMap;


public class MotionEventCollectorMock extends MotionEventCollector {
    public MotionEventCollectorMock() {
        super();
    }

    @VisibleForTesting
    public HashMap<Integer, Integer> getPointerIdMap() {
        return this.getmPointerIdMap();
    }

    public void addData(EventModel eventModel) {
        this.addCollectedData(eventModel);
    }

}
