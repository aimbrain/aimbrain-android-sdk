package com.aimbrain.sdk.models;

public class SessionModel extends MetadataModel {

    public final static int NOT_ENROLLED = 0;
    public final static int ENROLLED = 1;
    public final static int BUILDING = 2;

    private String sessionId;
    private int faceStatus;
    private int behaviourStatus;

    public SessionModel(String sessionId, int faceStatus, int behaviourStatus, byte[] metadata) {
        super(metadata);
        this.sessionId = sessionId;
        this.faceStatus = faceStatus;
        this.behaviourStatus = behaviourStatus;
    }

    public int getBehaviourStatus() {
        return behaviourStatus;
    }

    public int getFaceStatus() {
        return faceStatus;
    }

    public String getSessionId() {
        return sessionId;
    }
}
