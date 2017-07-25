package com.aimbrain.sdk.collectors;

import android.hardware.SensorEvent;

import com.aimbrain.sdk.models.AccelerometerEventModel;
import com.aimbrain.sdk.sensorEvent.AccelerometerEventListener;
import com.aimbrain.sdk.util.Logger;


public class SensorEventCollector extends EventCollector {

    private static final String TAG = SensorEventCollector.class.getSimpleName();
    private static SensorEventCollector instance;

    private AccelerometerEventListener accelerometerEventListener;

    /**
     * average sensor event size in bytes
     */
    private final static int APPROXIMATE_SENSOR_EVENT_SIZE_BYTES = 32;

    public static SensorEventCollector getInstance() {
        if (instance == null) {
            instance = new SensorEventCollector();
        }
        return instance;
    }

    private SensorEventCollector() {
        accelerometerEventListener = new AccelerometerEventListener(10);
    }

    public void accelerometerDataChanged(SensorEvent sensorEvent, long timestamp) {
        Logger.v(TAG, "data changed (" + sensorEvent.values[0] + ", " + sensorEvent.values[1] + ", " + sensorEvent.values[2] + ")");
        AccelerometerEventModel accelerometerModel = new AccelerometerEventModel(sensorEvent, timestamp);
        addCollectedData(accelerometerModel);
    }

    public void startCollectingData(int samplingPeriodMs) {
        Logger.v(TAG, "start collecting data");
        accelerometerEventListener.startForPeriod(samplingPeriodMs);
    }

    @Override
    public int sizeOfElements() {
        return getCountOfElements() * APPROXIMATE_SENSOR_EVENT_SIZE_BYTES;
    }

    @Override
    int sizeOfElements(int count) {
        return count * APPROXIMATE_SENSOR_EVENT_SIZE_BYTES;
    }
}
