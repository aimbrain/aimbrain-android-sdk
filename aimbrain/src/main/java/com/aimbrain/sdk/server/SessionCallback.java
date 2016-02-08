package com.aimbrain.sdk.server;



/**
 * Callback for created session event
 */
public interface SessionCallback {
    /**
     * Method is called after successful session creation
     * @param session created session string
     */
    public void onSessionCreated(String session);
}
