package com.aimbrain.sdk.exceptions;



/**
 * Exception thrown while trying to connect to server without created session.
 */
public class SessionException extends Exception {
    public SessionException(String description) {
        super(description);
    }
}
