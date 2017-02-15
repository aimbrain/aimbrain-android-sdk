package com.aimbrain.sdk.server;

import android.util.Base64;

import com.aimbrain.sdk.models.VoiceAuthenticateModel;
import com.aimbrain.sdk.util.Logger;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class VoiceCapturesAuthenticateCallback implements VoiceCapturesCallback {
    public static final String TAG = VoiceCapturesAuthenticateCallback.class.getSimpleName();

    public abstract void success(VoiceAuthenticateModel response);

    public abstract  void failure(VolleyError error);

    @Override
    public void fireSuccessAction(JSONObject response) {
        VoiceAuthenticateModel voiceAuthenticateModel = null;
        try {
            double score = response.getDouble("score");
            double liveliness = response.getDouble("liveliness");
            byte[] metadata = null;
            if (response.has("metadata")) {
                String metadataString = response.getString("metadata");
                metadata = Base64.decode(metadataString, Base64.DEFAULT);
            }
            voiceAuthenticateModel = new VoiceAuthenticateModel(score, liveliness, metadata);
        } catch (JSONException e) {
            Logger.e(TAG, "json", e);
        }
        success(voiceAuthenticateModel);
    }
}
