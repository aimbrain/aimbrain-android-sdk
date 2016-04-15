package com.aimbrain.sdk.models;

public class FaceEnrollModel {

    private int correctCapturesCount;

    public FaceEnrollModel(int correctCapturesCount) {
        this.correctCapturesCount = correctCapturesCount;
    }

    public int getCorrectCapturesCount() {
        return correctCapturesCount;
    }

}
