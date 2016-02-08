package com.aimbrain.sdk.sensorEvent;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.aimbrain.sdk.AMBNApplication.AMBNApplication;
import com.aimbrain.sdk.collectors.SensorEventCollector;


public class AccelerometerEventListener implements SensorEventListener{

    private SensorManager sensorManager;
    private long endTime;
    private int samplingPeriodMillis;
    private boolean registered;


    public AccelerometerEventListener(int samplingPeriodMillis) {
        this.sensorManager = (SensorManager) AMBNApplication.getInstance().getSystemService(Context.SENSOR_SERVICE);
        this.registered = false;
        this.samplingPeriodMillis = samplingPeriodMillis;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if(currentTime >= endTime) {
                synchronized (this) {
                    stop();
                    registered = false;
                }
                return;
            }
            SensorEventCollector.getInstance().accelerometerDataChanged(event, currentTime);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void startForPeriod(long period){
        long current = System.currentTimeMillis();
        if(current + period > endTime){
            endTime = current + period;
        }
        synchronized (this) {
            if (!registered) {
                start();
                registered = true;
            }
        }
    }
    private void start(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), samplingPeriodMillis * 1000);
    }
    private void stop() {
        sensorManager.unregisterListener(this);
    }
}
