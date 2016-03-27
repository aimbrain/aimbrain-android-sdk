package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.FaceAuthenticateModel;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PhotosAuthenticateCallback implements PhotosCallback {
    public abstract void success(FaceAuthenticateModel response);

    public abstract  void failure(VolleyError error);

    @Override
    public void fireSuccessAction(JSONObject response) {
        FaceAuthenticateModel faceAuthenticateModel = null;
        try {
            faceAuthenticateModel = new FaceAuthenticateModel(response.getDouble("score"), response.getDouble("liveliness"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        success(faceAuthenticateModel);
    }
}
