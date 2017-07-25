package com.aimbrain.sdk;

import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.models.TouchEventModel;
import com.aimbrain.sdk.util.JsonUtil;
import com.aimbrain.sdk.mock.TouchEventModelMock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TouchEventModelTest {

    @Test
    public void testToJSON() throws JSONException {
        MotionEvent event = mock(MotionEvent.class);
        View view = mock(View.class);

        when(event.getActionMasked()).thenReturn(MotionEvent.ACTION_UP);
        when(event.getX()).thenReturn(10.1f);
        when(event.getY()).thenReturn(5.5f);
        when(event.getPressure(any(int.class))).thenReturn(5.0f);
        when(event.getTouchMajor(any(int.class))).thenReturn(20f);

        TouchEventModel model = new TouchEventModel(1, 2, event, 123456, 3, view, true);
        JSONObject json = model.toJSON();
        assertNotNull(json);
        JsonUtil.assertJsonHasKey(json, "ids");
        JsonUtil.assertJsonHasKey(json, "gid");
        JsonUtil.assertJsonHasKey(json, "tid");
        JsonUtil.assertJsonHasKey(json, "t");
        JsonUtil.assertJsonHasKey(json, "r");
        JsonUtil.assertJsonHasKey(json, "x");
        JsonUtil.assertJsonHasKey(json, "y");
        JsonUtil.assertJsonHasKey(json, "rx");
        JsonUtil.assertJsonHasKey(json, "ry");
        JsonUtil.assertJsonHasKey(json, "f");
        JsonUtil.assertJsonHasKey(json, "p");

        assertEquals(2, json.getInt("gid"));
        assertEquals(1, json.getInt("tid"));
        assertEquals(123456, json.getLong("t"));
        assertEquals(3, json.getInt("p"));

    }

    @Test
    public void testConvertToIosPhase() throws JSONException {
        TouchEventModelMock model = new TouchEventModelMock(1, 2, mock(MotionEvent.class), 123456, 3, mock(View.class), true);
        assertEquals(0, model.convertToIosTouchPhase(MotionEvent.ACTION_DOWN));
        assertEquals(0, model.convertToIosTouchPhase(MotionEvent.ACTION_POINTER_DOWN));
        assertEquals(3, model.convertToIosTouchPhase(MotionEvent.ACTION_POINTER_UP));
        assertEquals(3, model.convertToIosTouchPhase(MotionEvent.ACTION_UP));
        assertEquals(1, model.convertToIosTouchPhase(MotionEvent.ACTION_MOVE));
        assertEquals(4, model.convertToIosTouchPhase(MotionEvent.ACTION_CANCEL));
        assertEquals(MotionEvent.ACTION_OUTSIDE, model.convertToIosTouchPhase(MotionEvent.ACTION_OUTSIDE));
    }
}
