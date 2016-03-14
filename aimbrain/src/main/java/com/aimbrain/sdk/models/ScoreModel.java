package com.aimbrain.sdk.models;



/**
 * Class representing score data received from server
 */
public class ScoreModel {

    public static final int AUTHENTICATING = 1;
    public static final int ENROLLING = 0;

    private String session;
    private double score;
    private int status;

    public ScoreModel(double score, int status, String session) {
        this.score = score;
        this.status = status;
        this.session = session;
    }

    /**
     * Gets session
     * @return session
     */
    public String getSession() {
        return session;
    }

    /**
     * Gets behavioural score
     * @return score received from server
     */
    public double getScore() {
        return score;
    }

    /**
     * Gets behavioural status
     * @return status received from server
     */
    public int getStatus() {
        return status;
    }
}
