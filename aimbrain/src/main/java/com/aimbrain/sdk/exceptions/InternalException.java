package com.aimbrain.sdk.exceptions;



/**
 * Exception thrown when internal error occurs during connecting to server
 */
public class InternalException extends Exception {
    public InternalException(String description) {
        super(description);
    }
}
