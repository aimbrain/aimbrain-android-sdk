package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.FaceTokenModel;
import com.aimbrain.sdk.models.VoiceTokenModel;

/**
 * Interface implementation is used for collecting face token string from server.
 * Token is the text that user should read while recording her face.
 */
public interface FaceTokenCallback {

    /**
     * Method called after receiving token data without error
     * @param tokenModel object returned from server
     */
    void success(FaceTokenModel tokenModel);
}
