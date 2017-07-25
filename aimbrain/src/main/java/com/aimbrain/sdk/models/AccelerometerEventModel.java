package com.aimbrain.sdk.models;

import android.hardware.SensorEvent;

import org.json.JSONException;
import org.json.JSONObject;


public class AccelerometerEventModel extends EventModel {

    private float x;
    private float y;
    private float z;

    public AccelerometerEventModel(SensorEvent sensorEvent, long timestamp) {
        this.x = sensorEvent.values[0];
        this.y = sensorEvent.values[1];
        this.z = sensorEvent.values[2];
        this.timestamp = timestamp;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("t", timestamp);
        jsonObject.put("x", x);
        jsonObject.put("y", y);
        jsonObject.put("z", z);
        return jsonObject;
    }
}
