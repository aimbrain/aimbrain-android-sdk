package com.aimbrain.sdk;

import android.hardware.SensorEvent;

import com.aimbrain.sdk.models.AccelerometerEventModel;
import com.aimbrain.sdk.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class AccelerometerEventModelTest {

    @Test
    public void testToJSON() throws JSONException {
        SensorEvent sensorEvent = mock(SensorEvent.class);
        long timestamp = 123456;
        float[] array = {1.1f, 1.2f, 1.3f};
        try {
            Field valuesField = SensorEvent.class.getField("values");
            valuesField.setAccessible(true);
            try {
                valuesField.set(sensorEvent, array);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        AccelerometerEventModel accelerometerEventModel = new AccelerometerEventModel(sensorEvent, timestamp);
        JSONObject json = accelerometerEventModel.toJSON();
        assertTrue(json.has("t"));
        assertTrue(json.has("x"));
        assertTrue(json.has("y"));
        assertTrue(json.has("z"));
        assertEquals(json.getLong("t"), timestamp);
        JsonUtil.assertJSONFloatEquals(json.getDouble("x"), 1.1f);
        JsonUtil.assertJSONFloatEquals(json.getDouble("y"), 1.2f);
        JsonUtil.assertJSONFloatEquals(json.getDouble("z"), 1.3f);
    }


}
