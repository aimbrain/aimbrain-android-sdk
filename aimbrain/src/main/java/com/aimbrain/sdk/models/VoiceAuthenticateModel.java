package com.aimbrain.sdk.models;

public class VoiceAuthenticateModel extends FaceAuthenticateModel {

    public VoiceAuthenticateModel(double score, double liveliness, byte[] metadata) {
        super(score, liveliness, metadata);
    }
}
