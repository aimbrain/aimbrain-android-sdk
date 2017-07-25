package com.aimbrain.sdk;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.EventModel;
import com.aimbrain.sdk.util.JsonUtil;
import com.aimbrain.sdk.mock.BehaviouralDataModelMock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BehaviouralDataModelTest {

    @Test
    public void testToJSON() throws JSONException {
        List<EventModel> textEvents = mock(List.class);
        List<EventModel> touches = mock(List.class);
        List<EventModel> accelerations = mock(List.class);
        byte[] metadata = "metadata".getBytes();
        Base64Helper base64 = mock(Base64Helper.class);
        when(base64.encodeToString(any(byte[].class), any(int.class))).thenReturn("metadata value");

        Iterator iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(mock(EventModel.class)).thenReturn(mock(EventModel.class)).thenReturn(mock(EventModel.class));

        when(textEvents.iterator()).thenReturn(iterator);
        when(touches.iterator()).thenReturn(iterator);
        when(accelerations.iterator()).thenReturn(iterator);

        BehaviouralDataModelMock behaviouralDataModel = new BehaviouralDataModelMock(textEvents, accelerations, touches, metadata);
        behaviouralDataModel.setBase64Helper(base64);
        JSONObject json = behaviouralDataModel.toJSON();
        assertNotNull(json);
        JsonUtil.assertJsonHasKey(json, "touches");
        JsonUtil.assertJsonHasKey(json, "accelerations");
        JsonUtil.assertJsonHasKey(json, "textEvents");
        JsonUtil.assertJsonHasKey(json, "metadata");
    }

    @Test
    public void testToJSONWithoutMetadata() throws JSONException {
        List<EventModel> textEvents = mock(List.class);
        List<EventModel> touches = mock(List.class);
        List<EventModel> accelerations = mock(List.class);
        Base64Helper base64 = mock(Base64Helper.class);
        when(base64.encodeToString(any(byte[].class), any(int.class))).thenReturn("metadata value");

        Iterator iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(mock(EventModel.class)).thenReturn(mock(EventModel.class)).thenReturn(mock(EventModel.class));

        when(textEvents.iterator()).thenReturn(iterator);
        when(touches.iterator()).thenReturn(iterator);
        when(accelerations.iterator()).thenReturn(iterator);

        BehaviouralDataModelMock behaviouralDataModel = new BehaviouralDataModelMock(textEvents, accelerations, touches, null);
        behaviouralDataModel.setBase64Helper(base64);
        JSONObject json = behaviouralDataModel.toJSON();
        assertNotNull(json);
        JsonUtil.assertJsonHasKey(json, "touches");
        JsonUtil.assertJsonHasKey(json, "accelerations");
        JsonUtil.assertJsonHasKey(json, "textEvents");
        JsonUtil.assertJsonDoesNotHaveKey(json, "metadata");
    }
}
