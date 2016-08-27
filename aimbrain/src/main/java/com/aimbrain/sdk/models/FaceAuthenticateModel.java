package com.aimbrain.sdk.models;

public class FaceAuthenticateModel extends MetadataModel {
    private double score;
    private double liveliness;

    public FaceAuthenticateModel(double score, double liveliness, byte[] metadata) {
        super(metadata);
        this.score = score;
        this.liveliness = liveliness;
    }

    public double getScore() {
        return score;
    }

    public double getLiveliness() {
        return liveliness;
    }
}
