package com.aimbrain.sdk;


import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.mock.FaceCaptureEnrollCallbackMock;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FaceCaptureEnrollCallbackTest {
    private boolean successCalled = false;
    private boolean failureCalled = false;

    @Before
    public void setUp() throws Exception {
        successCalled = false;
        failureCalled = false;
    }

    @Test
    public void testFireSuccessActionWithMetadata() throws Exception {
        FaceCaptureEnrollCallbackMock callback = new FaceCaptureEnrollCallbackMock() {
            @Override
            public void success(FaceEnrollModel response) {
                successCalled = true;
                assertEquals(5, response.getCorrectCapturesCount());
                assertTrue(response.getMetadata() != null);
            }

            @Override
            public void failure(VolleyError error) {
                failureCalled = true;
            }
        };
        Base64Helper base64Helper = mock(Base64Helper.class);
        when(base64Helper.decode(any(String.class), any(int.class))).thenReturn("metadata_string".getBytes());
        callback.setBase64Helper(base64Helper);
        JSONObject json = mock(JSONObject.class);
        when(json.getInt("imagesCount")).thenReturn(5);
        when(json.getString("metadata")).thenReturn("metadata_string");
        when(json.has("metadata")).thenReturn(true);
        callback.fireSuccessAction(json);
        assertTrue(successCalled);
        assertFalse(failureCalled);

    }

    @Test
    public void testFireSuccessActionWithoutMetadata() throws Exception {
        FaceCaptureEnrollCallbackMock callback = new FaceCaptureEnrollCallbackMock() {
            @Override
            public void success(FaceEnrollModel response) {
                successCalled = true;
                assertEquals(5, response.getCorrectCapturesCount());
                assertTrue(response.getMetadata() == null);
            }

            @Override
            public void failure(VolleyError error) {
                failureCalled = true;
            }
        };
        Base64Helper base64Helper = mock(Base64Helper.class);
        when(base64Helper.decode(any(String.class), any(int.class))).thenReturn("metadata_string".getBytes());
        callback.setBase64Helper(base64Helper);
        JSONObject json = mock(JSONObject.class);
        when(json.getInt("imagesCount")).thenReturn(5);
        when(json.has("metadata")).thenReturn(false);
        callback.fireSuccessAction(json);
        assertTrue(successCalled);
        assertFalse(failureCalled);

    }

    @Test
    public void testFireSuccessActionBadJSON() throws Exception {
        FaceCaptureEnrollCallbackMock callback = new FaceCaptureEnrollCallbackMock() {
            @Override
            public void success(FaceEnrollModel response) {
                successCalled = true;
                assertTrue(response == null);
            }

            @Override
            public void failure(VolleyError error) {
                failureCalled = true;
            }
        };
        Base64Helper base64Helper = mock(Base64Helper.class);
        when(base64Helper.decode(any(String.class), any(int.class))).thenReturn("metadata_string".getBytes());
        callback.setBase64Helper(base64Helper);
        JSONObject json = mock(JSONObject.class);
        when(json.getInt("imagesCount")).thenThrow(new JSONException("test message"));
        when(json.has("metadata")).thenReturn(false);
        callback.fireSuccessAction(json);
        assertTrue(successCalled);
        assertFalse(failureCalled);

    }
}
