package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.VoiceTokenModel;

/**
 * Interface implementation is used for collecting voice token string from server.
 * Token is the text that user should read while recording her voice.
 */
public interface VoiceTokenCallback {

    /**
     * Method called after receiving token data without error
     * @param tokenModel object returned from server
     */
    void success(VoiceTokenModel tokenModel);
}
