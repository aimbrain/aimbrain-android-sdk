package com.aimbrain.sdk.collectors;

import android.hardware.SensorEvent;
import android.util.Log;

import com.aimbrain.sdk.models.AccelerometerEventModel;
import com.aimbrain.sdk.sensorEvent.AccelerometerEventListener;


public class SensorEventCollector extends EventCollector {

    private static SensorEventCollector instance;

    private AccelerometerEventListener accelerometerEventListener;

    public static SensorEventCollector getInstance(){
        if(instance == null){
            instance = new SensorEventCollector();
        }
        return instance;
    }

    private SensorEventCollector() {
        accelerometerEventListener = new AccelerometerEventListener(10);
    }

    public void accelerometerDataChanged(SensorEvent sensorEvent, long timestamp)
    {
        Log.i("ACCELEROMETER", "data changed");
        AccelerometerEventModel accelerometerModel = new AccelerometerEventModel(sensorEvent, timestamp);
        addCollectedData(accelerometerModel);
    }

    public void startCollectingData(int samplingPeriodMs) {
        accelerometerEventListener.startForPeriod(samplingPeriodMs);
    }
}
