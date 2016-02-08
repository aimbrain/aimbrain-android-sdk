package com.aimbrain.sdk.server;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;


public class AMBNObjectRequest extends JsonObjectRequest {

    private Map<String, String> headersMap;

    public AMBNObjectRequest(Map<String, String> headersMap, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.setShouldCache(false);
        this.headersMap = headersMap;
    }

    public AMBNObjectRequest(Map<String, String> headersMap, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        this.setShouldCache(false);
        this.headersMap = headersMap;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return this.headersMap;
    }
}
