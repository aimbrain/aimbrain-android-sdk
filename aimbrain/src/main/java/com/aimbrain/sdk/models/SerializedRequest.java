package com.aimbrain.sdk.models;

public class SerializedRequest {
    private final String requestJSON;

    public SerializedRequest(String requestJSON) {
        this.requestJSON = requestJSON;
    }


    public String getRequestJSON() {
        return requestJSON;
    }
}
