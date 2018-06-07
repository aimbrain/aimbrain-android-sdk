package com.aimbrain.sdk.models;


public class TextTokenModel extends MetadataModel {

    private String token;

    public TextTokenModel(String token, byte[] metadata) {
        super(metadata);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
