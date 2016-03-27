package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.FaceEnrollModel;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PhotosEnrollCallback implements PhotosCallback{

    public abstract void success(FaceEnrollModel response);

    public abstract void failure(VolleyError error);

    @Override
    public void fireSuccessAction(JSONObject response) {
        FaceEnrollModel faceEnrollModel = null;
        try {
            faceEnrollModel = new FaceEnrollModel(response.getInt("imagesCount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        success(faceEnrollModel);
    }

}
