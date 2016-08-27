package com.aimbrain.sdk.models;

public class FaceEnrollModel extends MetadataModel {

    private int correctCapturesCount;

    public FaceEnrollModel(int correctCapturesCount, byte[] metadata) {
        super(metadata);
        this.correctCapturesCount = correctCapturesCount;
    }

    public int getCorrectCapturesCount() {
        return correctCapturesCount;
    }

}
