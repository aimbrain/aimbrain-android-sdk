package com.aimbrain.sdk.models;

/**
 *
 */
public class VoiceTokenModel extends MetadataModel {

    private String token;

    public VoiceTokenModel(String token, byte[] metadata) {
        super(metadata);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
