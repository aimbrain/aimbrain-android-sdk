package com.aimbrain.sdk.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class BehaviouralDataModel {

    private List<EventModel> textEvents;
    private List<EventModel> touches;
    private List<EventModel> accelerations;


    public BehaviouralDataModel(List<EventModel> textModels, List<EventModel> accelerometerModels, List<EventModel> touchModels) {
        this.textEvents = textModels;
        this.accelerations = accelerometerModels;
        this.touches = touchModels;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("touches", modelArrayToJSONArray(touches));
        jsonObject.put("accelerations", modelArrayToJSONArray(accelerations));
        jsonObject.put("textEvents", modelArrayToJSONArray(textEvents));
        return jsonObject;
    }

    private JSONArray modelArrayToJSONArray(List<EventModel> models) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(EventModel model : models) {
            JSONObject jsonObject = model.toJSON();
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

}
