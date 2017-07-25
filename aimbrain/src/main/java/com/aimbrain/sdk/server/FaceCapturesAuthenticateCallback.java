package com.aimbrain.sdk.server;

import android.support.annotation.VisibleForTesting;
import android.util.Base64;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.FaceAuthenticateModel;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class FaceCapturesAuthenticateCallback implements FaceCapturesCallback {
    public abstract void success(FaceAuthenticateModel response);

    public abstract void failure(VolleyError error);

    @VisibleForTesting
    protected Base64Helper base64 = new Base64Helper();

    @Override
    public void fireSuccessAction(JSONObject response) {
        FaceAuthenticateModel faceAuthenticateModel = null;
        try {
            double score = response.getDouble("score");
            double liveliness = response.getDouble("liveliness");
            byte[] metadata = null;
            if (response.has("metadata")) {
                String metadataString = response.getString("metadata");
                metadata = base64.decode(metadataString, Base64.DEFAULT);
            }
            faceAuthenticateModel = new FaceAuthenticateModel(score, liveliness, metadata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        success(faceAuthenticateModel);
    }

    @VisibleForTesting
    protected void setBase64(Base64Helper base64) {
        this.base64 = base64;
    }
}
