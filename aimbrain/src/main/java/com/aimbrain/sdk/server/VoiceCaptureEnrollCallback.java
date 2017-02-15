package com.aimbrain.sdk.server;

import android.util.Base64;

import com.aimbrain.sdk.models.FaceEnrollModel;
import com.aimbrain.sdk.models.VoiceEnrollModel;
import com.aimbrain.sdk.util.Logger;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public abstract class VoiceCaptureEnrollCallback implements VoiceCapturesCallback {
    public static final String TAG = VoiceCaptureEnrollCallback.class.getSimpleName();

    public abstract void success(VoiceEnrollModel response);

    @Override
    public void fireSuccessAction(JSONObject response) {
        VoiceEnrollModel voiceEnrollModel = null;
        try {
            int voiceSamples = response.getInt("voiceSamples");
            byte[] metadata = null;
            if (response.has("metadata")) {
                String metadataString = response.getString("metadata");
                metadata = Base64.decode(metadataString, Base64.DEFAULT);
            }
            voiceEnrollModel = new VoiceEnrollModel(voiceSamples, metadata);
        } catch (JSONException e) {
            Logger.e(TAG, "json", e);
        }
        success(voiceEnrollModel);
    }
}
