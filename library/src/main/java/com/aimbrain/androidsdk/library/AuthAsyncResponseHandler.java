package com.aimbrain.androidsdk.library;

public interface AuthAsyncResponseHandler {
    void onSuccess(AuthAsyncResponse response);

    void onFailure(int statusCode, String message, Throwable exception);
}
