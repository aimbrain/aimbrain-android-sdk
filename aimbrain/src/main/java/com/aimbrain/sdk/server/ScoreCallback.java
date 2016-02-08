package com.aimbrain.sdk.server;

import com.aimbrain.sdk.models.ScoreModel;



/**
 * Interface implementation is used for collecting score data from server.
 */
public interface ScoreCallback {
    /**
     * Method called after receiving score data without error
     * @param scoreModel object returned from server
     */
    public void success(ScoreModel scoreModel);
}
