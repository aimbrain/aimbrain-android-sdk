package com.aimbrain.sdk.models;

public class VoiceEnrollModel extends MetadataModel {

    private int voiceSamples;

    public VoiceEnrollModel(int voiceSamples, byte[] metadata) {
        super(metadata);
        this.voiceSamples = voiceSamples;
    }

    public int getVoiceSamples() {
        return voiceSamples;
    }
}
