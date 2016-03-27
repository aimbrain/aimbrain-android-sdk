package com.aimbrain.sdk.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class StringListDataModel {

    private List<String> data;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public JSONArray toJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(String stringData : data) {
            jsonArray.put(stringData);
        }
        return jsonArray;
    }
}
