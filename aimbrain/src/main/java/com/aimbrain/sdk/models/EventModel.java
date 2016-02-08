package com.aimbrain.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class EventModel {
    public abstract JSONObject toJSON() throws JSONException;
}
