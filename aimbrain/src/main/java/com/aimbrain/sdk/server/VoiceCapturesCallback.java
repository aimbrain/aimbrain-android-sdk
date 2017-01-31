package com.aimbrain.sdk.server;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface VoiceCapturesCallback {
    public void fireSuccessAction(JSONObject response);

    public void failure(VolleyError error);
}
