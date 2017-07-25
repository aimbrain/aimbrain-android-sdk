package com.aimbrain.sdk.server;

import android.support.annotation.VisibleForTesting;
import android.util.Base64;

import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.aimbrain.sdk.util.Logger;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class FaceCapturesEnrollCallback implements FaceCapturesCallback {

    public static final String TAG = FaceCapturesEnrollCallback.class.getSimpleName();

    public abstract void success(FaceEnrollModel response);

    public abstract void failure(VolleyError error);

    @VisibleForTesting
    protected Base64Helper base64 = new Base64Helper();


    @Override
    public void fireSuccessAction(JSONObject response) {
        FaceEnrollModel faceEnrollModel = null;
        try {
            int imagesCount = response.getInt("imagesCount");
            byte[] metadata = null;
            if (response.has("metadata")) {
                String metadataString = response.getString("metadata");
                metadata = base64.decode(metadataString, Base64.DEFAULT);
            }
            faceEnrollModel = new FaceEnrollModel(imagesCount, metadata);
        } catch (JSONException e) {
            Logger.e(TAG, "parse response", e);
        }
        success(faceEnrollModel);
    }

    @VisibleForTesting
    protected void setBase64(Base64Helper base64) {
        this.base64 = base64;
    }

}
