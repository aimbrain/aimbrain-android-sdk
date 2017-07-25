package com.aimbrain.sdk;

import android.view.View;

import com.aimbrain.sdk.models.TextEventModel;
import com.aimbrain.sdk.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


public class TextEventModelTest {

    @Test
    public void testToJSON() throws JSONException {
        TextEventModel model = new TextEventModel("some text", 123456, mock(View.class));
        JSONObject json = model.toJSON();
        assertNotNull(json);
        JsonUtil.assertJsonHasKey(json, "ids");
        JsonUtil.assertJsonHasKey(json, "t");
        JsonUtil.assertJsonHasKey(json, "tx");
    }
}
