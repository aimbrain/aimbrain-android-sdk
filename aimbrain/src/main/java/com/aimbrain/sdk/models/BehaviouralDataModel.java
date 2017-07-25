package com.aimbrain.sdk.models;

import android.support.annotation.VisibleForTesting;
import android.util.Base64;

import com.aimbrain.sdk.helper.Base64Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class BehaviouralDataModel {

    private List<EventModel> textEvents;
    private List<EventModel> touches;
    private List<EventModel> accelerations;
    private byte[] metadata;
    private Base64Helper base64 = new Base64Helper();

    public BehaviouralDataModel(List<EventModel> textModels, List<EventModel> accelerometerModels, List<EventModel> touchModels, byte[] metadata) {
        this.textEvents = textModels;
        this.accelerations = accelerometerModels;
        this.touches = touchModels;
        this.metadata = metadata;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("touches", modelArrayToJSONArray(touches));
        jsonObject.put("accelerations", modelArrayToJSONArray(accelerations));
        jsonObject.put("textEvents", modelArrayToJSONArray(textEvents));
        if (metadata != null) {
            jsonObject.put("metadata", base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }

    private JSONArray modelArrayToJSONArray(List<EventModel> models) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (EventModel model : models) {
            JSONObject jsonObject = model.toJSON();
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    @VisibleForTesting
    protected void setBase64(Base64Helper base64) {
        this.base64 = base64;
    }

}
