package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.FaceCompareModel;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class FaceCompareCallback {

    public abstract void success(FaceCompareModel response);

    public abstract void failure(VolleyError error);

}
