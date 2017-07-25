package com.aimbrain.sdk.mock;

import android.support.annotation.VisibleForTesting;

import com.aimbrain.sdk.exceptions.InvalidSignatureException;
import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.server.FaceCompareCallback;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.Server;
import com.aimbrain.sdk.server.SessionCallback;
import com.aimbrain.sdk.server.VoiceTokenCallback;

import org.json.JSONObject;

import java.net.URL;
import java.util.Map;


public class ServerMock extends Server {
    public ServerMock(SessionModel session) {
        super(session);
    }

    public void setApiAllowed(boolean apiCallsAllowed) {
        setApiCallsAllowed(apiCallsAllowed);
    }

    public void setBase64Helper(Base64Helper base64) {
        setBase64(base64);
    }

    public String calculateSignature(String httpMethod, String path, String httpbody, String key) throws InvalidSignatureException {
        return super.calculateSignature(httpMethod, path, httpbody, key);
    }

    public Map<String, String> getHeadersMap(JSONObject jsonObject, URL url) throws InvalidSignatureException {
        return super.getHeadersMap(jsonObject, url);
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void parseCreateSessionResponse(JSONObject response, SessionCallback sessionCallback) {
        super.parseCreateSessionResponse(response, sessionCallback);
    }

    public void parseScoreStatusResponse(JSONObject response, ScoreCallback scoreCallback, String sessionIdCopy) {
        super.parseScoreStatusResponse(response, scoreCallback, sessionIdCopy);
    }

    public void parseCompareFacesRequest(JSONObject response, FaceCompareCallback callback) {
        super.parseCompareFacesRequest(response, callback);
    }

    @VisibleForTesting
    public void parseVoiceTokenResponse(JSONObject response, VoiceTokenCallback tokenCallback) {
        super.parseVoiceTokenResponse(response, tokenCallback);
    }
}
