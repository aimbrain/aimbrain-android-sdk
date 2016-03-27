package com.aimbrain.sdk.models;

public class FaceAuthenticateModel {

    private double score;
    private double liveliness;

    public FaceAuthenticateModel(double score, double liveliness) {
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
