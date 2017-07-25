package com.aimbrain.sdk.server;

import android.support.annotation.VisibleForTesting;

import com.aimbrain.sdk.util.Logger;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;


public class AMBNObjectRequest extends JsonObjectRequest {

    private Map<String, String> headersMap;
    private static final String TAG = AMBNObjectRequest.class.getSimpleName();
    private RequestListener requestListener;

    public AMBNObjectRequest(Map<String, String> headersMap, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        logJson("REQUEST_BODY:", jsonRequest);
        this.setShouldCache(false);
        this.headersMap = headersMap;
    }

    public AMBNObjectRequest(Map<String, String> headersMap, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        logJson("REQUEST_BODY:", jsonRequest);
        this.setShouldCache(false);
        this.headersMap = headersMap;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return this.headersMap;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            JSONObject jsonObject = new JSONObject(jsonString);
            logJson("RESPONSE_BODY:", jsonObject);
            return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        super.deliverResponse(response);
        if (requestListener != null) {
            requestListener.onSuccess(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
        if (requestListener != null) {
            requestListener.onError(error);
        }
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        requestListener = null;
    }

    private void logJson(String tag, JSONObject jsonObject) {
        if (Logger.LEVEL != Logger.VERBOSE) {
            return;
        }
        int maxLogSize = 1000;
        String jsonString = "";
        try {
            jsonString = jsonObject.toString(1);
            for (int i = 0; i <= jsonString.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > jsonString.length() ? jsonString.length() : end;
                Logger.v(TAG, tag + " " + jsonString.substring(start, end));
            }
        } catch (JSONException e) {
            Logger.e(TAG, "json", e);
        }
    }

    @VisibleForTesting
    public void setListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

}
