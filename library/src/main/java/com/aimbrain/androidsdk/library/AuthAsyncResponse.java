package com.aimbrain.androidsdk.library;

public class AuthAsyncResponse {
    public final Integer statusCode;
    public final String body;
    public final Double score;
    public final String method;
    public final String id;
    public final Long nonce;

    public AuthAsyncResponse(Integer statusCode, String body, Double score, String method, String id, Long nonce) {
        this.statusCode = statusCode;
        this.body = body;
        this.score = score;
        this.method = method;
        this.id = id;
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return body;
    }
}
