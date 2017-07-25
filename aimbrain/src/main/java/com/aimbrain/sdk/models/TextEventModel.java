package com.aimbrain.sdk.models;

import android.view.View;

import com.aimbrain.sdk.viewManager.ViewIdMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;


public class TextEventModel extends EventModel {

    private String text;
    private LinkedList<String> viewPath;

    public TextEventModel(String text, long timestamp, View view) {
        this.text = text;
        this.timestamp = timestamp;
        viewPath = ViewIdMapper.getInstance().extractViewPath(view);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ids", new JSONArray(viewPath));
        jsonObject.put("t", timestamp);
        jsonObject.put("tx", text);
        return jsonObject;
    }
}
