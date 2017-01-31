package com.aimbrain.sdk.server;


import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.aimbrain.sdk.models.FaceCompareModel;
import com.aimbrain.sdk.models.SerializedRequest;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.models.VoiceTokenModel;
import com.aimbrain.sdk.models.VoiceTokenType;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.aimbrain.sdk.AMBNApplication.AMBNApplication;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.InvalidSignatureException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.BehaviouralDataModel;
import com.aimbrain.sdk.models.ScoreModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class Server {
    private static final int SOCKET_TIMEOUT = 10000; //[ms]
    private static final int MAX_RETRIES = 1;
    public static final String DEFAULT_API_BASE_URL = "https://api.aimbrain.com:443/v1/";

    private boolean apiCallsAllowed;
    private String apiKey;
    private String secret;
    private URL baseURL;
    private URL sessionURL;
    private URL submitBehaviouralURL;
    private URL scoreURL;
    private URL faceCompare;
    private URL voiceTokenURL;
    private HashMap<FaceActions, URL> faceActionsURLs;
    private HashMap<VoiceActions, URL> voiceActionsURLs;
    private RequestQueue requestQueue;
    private SessionModel session;
    private LinkedList<BehaviouralDataModel> dataQueue;

    public Server(SessionModel session) {
        this.apiCallsAllowed = false;
        this.session = session;
    }

    public Server(String apiKey, String secret) {
        this(apiKey, secret, DEFAULT_API_BASE_URL);
    }

    public Server(String apiKey, String secret, String apiBaseUrl) {
        this.apiCallsAllowed = true;
        this.apiKey = apiKey;
        this.secret = secret;
        setUpRequestQueue();
        this.dataQueue = new LinkedList<>();
        try {
            this.baseURL = new URL(apiBaseUrl);
            this.sessionURL = new URL(baseURL, "sessions");
            this.submitBehaviouralURL = new URL(baseURL, "behavioural");
            this.scoreURL = new URL(baseURL, "score");
            this.faceCompare = new URL(baseURL, "face/compare");
            this.faceActionsURLs = new HashMap<>();
            this.faceActionsURLs.put(FaceActions.FACE_AUTH, new URL(baseURL, "face/auth"));
            this.faceActionsURLs.put(FaceActions.FACE_ENROLL, new URL(baseURL, "face/enroll"));
            this.voiceTokenURL = new URL(baseURL, "voice/token");
            this.voiceActionsURLs = new HashMap<>();
            this.voiceActionsURLs.put(VoiceActions.VOICE_AUTH, new URL(baseURL, "voice/auth"));
            this.voiceActionsURLs.put(VoiceActions.VOICE_ENROLL, new URL(baseURL, "voice/enroll"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public SessionModel getSession(){
        return session;
    }

    public boolean isConfiguredForApiCalls() {
        return apiCallsAllowed;
    }
    
    private void setUpRequestQueue(){
        Network network = new BasicNetwork(new HurlStack());
        this.requestQueue = new RequestQueue(new NoCache(), network);
        this.requestQueue.start();
    }

    private String calculateSignature(String httpMethod, String path, String httpbody, String key) throws InvalidSignatureException {
        try {
            String message = httpMethod.toUpperCase() + "\n" + path.toLowerCase() + "\n" + httpbody;
            byte [] messageBytes = message.getBytes("UTF-8");
            Mac sha256_HMAC = null;
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte [] data = sha256_HMAC.doFinal(messageBytes);
            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new InvalidSignatureException("Unable to calculate signature.");
    }

    private Map<String, String> getHeadersMap(JSONObject jsonObject, URL url) throws InvalidSignatureException {
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("X-aimbrain-apikey", this.apiKey);
        headersMap.put("X-aimbrain-signature", String.valueOf(calculateSignature("POST", url.getPath(), jsonObject.toString(), this.secret)));
        return headersMap;
    }

    public void createSession(String userId, byte[] metadata, Context context, final SessionCallback sessionCallback, Response.ErrorListener errorListener) throws InternalException, ConnectException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }

        try {
            JSONObject jsonObject = JSONForCreateSession(userId, metadata, context);
            AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                    (getHeadersMap(jsonObject, this.sessionURL), Request.Method.POST, this.sessionURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String session = response.get("session").toString();
                                int face = response.getInt("face");
                                int voice = response.getInt("voice");
                                int behaviour = response.getInt("behaviour");
                                byte[] metadata = null;
                                if (response.has("metadata")) {
                                    String metadataString = response.getString("metadata");
                                    metadata = Base64.decode(metadataString, Base64.DEFAULT);
                                }
                                Server.this.session = new SessionModel(session, face, behaviour, voice, metadata);
                                if (sessionCallback != null) {
                                    sessionCallback.onSessionCreated(Server.this.session);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, errorListener);

            sendRequest(jsonRequest);
        } catch (JSONException | InvalidSignatureException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    public SerializedRequest getSerializedCreateSession(String userId, byte[] metadata, Context context) throws InternalException {
        try {
            JSONObject jsonObject = JSONForCreateSession(userId, metadata, context);
            return new SerializedRequest(jsonObject.toString());
        } catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    private JSONObject JSONForCreateSession(String userId, byte[] metadata, Context context) throws JSONException {
        WindowManager windowManager = (WindowManager)  context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        String system = "Android " + Build.VERSION.RELEASE;
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("device", android.os.Build.MODEL);
        json.put("system", system);
        json.put("screenWidth", screenSize.x);
        json.put("screenHeight", screenSize.y);
        if (metadata != null) {
            json.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return json;
    }

    public void submitData(BehaviouralDataModel behaviouralDataModel, ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        this.dataQueue.add(behaviouralDataModel);

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }

        if (session == null) {
            throw new SessionException("Unintialized session.");
        }

        sendAllDataFromQueue(scoreCallback);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) AMBNApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void sendAllDataFromQueue(final ScoreCallback scoreCallback) throws InternalException {
        try {
            final String sessionIdCopy = session.getSessionId();
            while (!this.dataQueue.isEmpty()) {
                final BehaviouralDataModel behaviouralDataModel = this.dataQueue.removeFirst();
                JSONObject jsonObject = wrapJSONObjectWithSession(behaviouralDataModel.toJSON());
                AMBNObjectRequest objectRequest = new AMBNObjectRequest(getHeadersMap(jsonObject, this.submitBehaviouralURL), Request.Method.POST, this.submitBehaviouralURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (scoreCallback != null) {
                            ScoreModel scoreModel;
                            try {
                                double score = response.getDouble("score");
                                int status = response.getInt("status");
                                byte[] metadata = null;
                                if (response.has("metadata")) {
                                    String metadataString = response.getString("metadata");
                                    metadata = Base64.decode(metadataString, Base64.DEFAULT);
                                }
                                scoreModel = new ScoreModel(score, status, sessionIdCopy, metadata);
                                scoreCallback.success(scoreModel);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new AMBNResponseErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Server.this.dataQueue.add(behaviouralDataModel);
                        super.onErrorResponse(error);
                    }
                });
                sendRequest(objectRequest);
            }
        } catch (InvalidSignatureException | JSONException e) {
            throw new InternalException("Unable to submit data.");
        }
    }

    public SerializedRequest getSerializedSubmitData(BehaviouralDataModel behaviouralDataModel) throws InternalException, SessionException {
        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            JSONObject jsonObject = wrapJSONObjectWithSession(behaviouralDataModel.toJSON());
            return new SerializedRequest(jsonObject.toString());
        } catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    private JSONObject wrapJSONObjectWithSession(JSONObject jsonObject) throws JSONException {
        jsonObject.put("session", session.getSessionId());
        return jsonObject;
    }

    public void getCurrentScore(byte[] metadata, final ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }
        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            final String sessionIdCopy = session.getSessionId();
            JSONObject jsonObject = JSONForGetCurrentScore(metadata);
            AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                    (getHeadersMap(jsonObject, this.scoreURL), Request.Method.POST, this.scoreURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (scoreCallback != null) {
                                    double score = response.getDouble("score");
                                    int status = response.getInt("status");
                                    byte[] metadata = null;
                                    if (response.has("metadata")) {
                                        String metadataString = response.getString("metadata");
                                        metadata = Base64.decode(metadataString, Base64.DEFAULT);
                                    }
                                    ScoreModel scoreModel = new ScoreModel(score, status, sessionIdCopy, metadata);
                                    scoreCallback.success(scoreModel);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new AMBNResponseErrorListener());

            sendRequest(jsonRequest);
        } catch (JSONException | InvalidSignatureException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    public SerializedRequest getSerializedGetCurrentScore(byte[] metadata) throws InternalException, SessionException {
        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            JSONObject jsonObject = JSONForGetCurrentScore(metadata);
            return new SerializedRequest(jsonObject.toString());
        } catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    private JSONObject JSONForGetCurrentScore(byte[] metadata) throws JSONException {
        JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
        if (metadata != null) {
            jsonObject.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }

    public void sendProvidedFaceCaptures(StringListDataModel photos, byte[] metadata, final FaceCapturesCallback callback, FaceActions faceAction)  throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }

        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            JSONObject jsonObject = JSONForSendFaceCaptures(photos, metadata);
            AMBNObjectRequest jsonRequest = null;
            try {
                jsonRequest = new AMBNObjectRequest
                        (getHeadersMap(jsonObject, this.faceActionsURLs.get(faceAction)), Request.Method.POST, this.faceActionsURLs.get(faceAction).toString(), jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Response", "response: " + response.toString());
                                callback.fireSuccessAction(response);
                            }
                        }, new AMBNResponseErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON error", error.networkResponse.data.toString());
                                super.onErrorResponse(error);
                                callback.failure(error);
                            }
                        });
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
            sendRequest(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SerializedRequest getSerializedSendProvidedFaceCaptures(StringListDataModel captures, byte[] metadata, FaceActions faceAction) throws InternalException {
        try {
            JSONObject jsonObject = JSONForSendFaceCaptures(captures, metadata);
            return new SerializedRequest(jsonObject.toString());
        }
        catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    @NonNull
    private JSONObject JSONForSendFaceCaptures(StringListDataModel photos, byte[] metadata) throws JSONException {
        JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
        jsonObject.put("faces", photos.toJSON());
        if (metadata != null) {
            jsonObject.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }

    public void compareFaces(StringListDataModel firstFace, StringListDataModel secondFace, byte[] metadata, final FaceCompareCallback callback) throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }

        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try{
            JSONObject jsonObject = JSONForCompareFaces(firstFace, secondFace, metadata);
            AMBNObjectRequest jsonRequest = null;
            try {
                jsonRequest = new AMBNObjectRequest
                        (getHeadersMap(jsonObject, this.faceCompare), Request.Method.POST, this.faceCompare.toString(), jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Response", "response: " + response.toString());
                                try {
                                    if (callback != null) {
                                        double score = response.getDouble("score");
                                        double liveliness1 = response.getDouble("liveliness1");
                                        double liveliness2 = response.getDouble("liveliness2");
                                        byte[] metadata = null;
                                        if (response.has("metadata")) {
                                            String metadataString = response.getString("metadata");
                                            metadata = Base64.decode(metadataString, Base64.DEFAULT);
                                        }
                                        callback.success(new FaceCompareModel(score, liveliness1, liveliness2, metadata));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new AMBNResponseErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON error", error.networkResponse.data.toString());
                                super.onErrorResponse(error);
                                callback.failure(error);
                            }
                        });
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
            sendRequest(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SerializedRequest getSerializedCompareFaces(StringListDataModel firstFace, StringListDataModel secondFace, byte[] metadata) throws InternalException {
        try {
            JSONObject jsonObject = JSONForCompareFaces(firstFace, secondFace, metadata);
            return new SerializedRequest(jsonObject.toString());
        }
        catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    @NonNull
    private JSONObject JSONForCompareFaces(StringListDataModel firstFace, StringListDataModel secondFace, byte[] metadata) throws JSONException {
        JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
        jsonObject.put("faces1", firstFace.toJSON());
        jsonObject.put("faces2", secondFace.toJSON());
        if (metadata != null) {
            jsonObject.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }

    private void sendRequest(Request request) {
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }

    public void getVoiceToken(VoiceTokenType tokenType, byte[] metadata, final VoiceTokenCallback tokenCallback) throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }
        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            JSONObject jsonObject = JSONForVoiceToken(tokenType, metadata);
            AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                    (getHeadersMap(jsonObject, this.voiceTokenURL), Request.Method.POST,
                            this.voiceTokenURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (tokenCallback != null) {
                                    String token = response.getString("token");
                                    byte[] metadata = null;
                                    if (response.has("metadata")) {
                                        String metadataString = response.getString("metadata");
                                        metadata = Base64.decode(metadataString, Base64.DEFAULT);
                                    }
                                    VoiceTokenModel tokenModel = new VoiceTokenModel(token, metadata);
                                    tokenCallback.success(tokenModel);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new AMBNResponseErrorListener());

            sendRequest(jsonRequest);
        } catch (JSONException | InvalidSignatureException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    public SerializedRequest getSerializedVoiceToken(VoiceTokenType tokenType, byte[] metadata) throws InternalException {
        try {
            JSONObject jsonObject = JSONForVoiceToken(tokenType, metadata);
            return new SerializedRequest(jsonObject.toString());
        }
        catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    @NonNull
    private JSONObject JSONForVoiceToken(VoiceTokenType tokenType, byte[] metadata) throws JSONException {
        JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
        jsonObject.put("tokentype", tokenType.toString());
        if (metadata != null) {
            jsonObject.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }

    public void sendProvidedVoiceCaptures(StringListDataModel voices, byte[] metadata,
                                          final VoiceCapturesCallback callback, VoiceActions voiceAction)
            throws InternalException, ConnectException, SessionException {
        if (!apiCallsAllowed) {
            throw new IllegalStateException("Current configuration does not allow API calls");
        }

        if (!isOnline()) {
            throw new ConnectException("Unable to connect to server (check network settings).");
        }

        if (session == null) {
            throw new SessionException("Uninitialized session.");
        }

        try {
            JSONObject jsonObject = JSONForSendVoiceCaptures(voices, metadata);
            AMBNObjectRequest jsonRequest = null;
            try {
                jsonRequest = new AMBNObjectRequest
                        (getHeadersMap(jsonObject, this.voiceActionsURLs.get(voiceAction)),
                                Request.Method.POST, this.voiceActionsURLs.get(voiceAction).toString(),
                                jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Response", "response: " + response.toString());
                                callback.fireSuccessAction(response);
                            }
                        }, new AMBNResponseErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON error", error.networkResponse.data.toString());
                                super.onErrorResponse(error);
                                callback.failure(error);
                            }
                        });
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
            sendRequest(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SerializedRequest getSerializedSendProvidedVoiceCaptures(StringListDataModel captures,
                                                                    byte[] metadata, VoiceActions voiceAction)
            throws InternalException {
        try {
            JSONObject jsonObject = JSONForSendVoiceCaptures(captures, metadata);
            return new SerializedRequest(jsonObject.toString());
        }
        catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    @NonNull
    private JSONObject JSONForSendVoiceCaptures(StringListDataModel voice, byte[] metadata) throws JSONException {
        JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
        jsonObject.put("voices", voice.toJSON());
        if (metadata != null) {
            jsonObject.put("metadata", Base64.encodeToString(metadata, Base64.NO_WRAP));
        }
        return jsonObject;
    }
}
