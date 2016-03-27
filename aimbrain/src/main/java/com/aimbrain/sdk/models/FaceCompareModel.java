package com.aimbrain.sdk.models;

public class FaceCompareModel {

    private double similarity;
    private double firstLiveliness;
    private double secondLiveliness;

    public FaceCompareModel(double similarity, double firstLiveliness, double secondLiveliness) {
        this.similarity = similarity;
        this.firstLiveliness = firstLiveliness;
        this.secondLiveliness = secondLiveliness;
    }

    public double getSimilarity() {
        return similarity;
    }

    public double getFirstLiveliness() {
        return firstLiveliness;
    }

    public double getSecondLiveliness() {
        return secondLiveliness;
    }

}
