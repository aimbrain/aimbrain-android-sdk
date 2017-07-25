package com.aimbrain.sdk.server;


import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface RequestListener {
    void onSuccess(JSONObject response);

    void onError(VolleyError error);
}
