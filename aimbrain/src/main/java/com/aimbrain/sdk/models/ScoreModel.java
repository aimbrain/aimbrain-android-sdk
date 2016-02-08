package com.aimbrain.sdk.models;



/**
 * Class representing score data received from server
 */
public class ScoreModel {

    public static final int AUTHENTICATING = 1;
    public static final int ENROLLING = 0;

    private double score;
    private int status;

    public ScoreModel(double score, int status) {
        this.score = score;
        this.status = status;
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
