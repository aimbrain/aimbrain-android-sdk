package com.aimbrain.sdk.server;


import com.aimbrain.sdk.models.SessionModel;

/**
 * Callback for created session event
 */
public interface SessionCallback {
    /**
     * Method is called after successful session creation
     * @param session created session model
     */
    public void onSessionCreated(SessionModel session);
}
