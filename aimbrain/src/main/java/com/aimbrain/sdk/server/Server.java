package com.aimbrain.sdk.server;


import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.aimbrain.sdk.models.FaceCompareModel;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
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
    private String appId;
    private String secret;
    private URL baseURL;
    private URL sessionURL;
    private URL submitBehaviouralURL;
    private URL scoreURL;
    private URL faceCompare;
    private HashMap<FaceActions, URL> faceActionsURLs;
    private RequestQueue requestQueue;
    private SessionModel session;
    private LinkedList<BehaviouralDataModel> dataQueue;



    public Server(String appId, String secret) {
        this.appId = appId;
        this.secret = secret;
        setUpRequestQueue();
        this.dataQueue = new LinkedList<>();
        try {
            this.baseURL = new URL("https://api.aimbrain.com:443/v1/");
            this.sessionURL = new URL(baseURL, "sessions");
            this.submitBehaviouralURL = new URL(baseURL, "behavioural");
            this.scoreURL = new URL(baseURL, "score");
            this.faceCompare = new URL(baseURL, "face/compare");
            this.faceActionsURLs = new HashMap<>();
            this.faceActionsURLs.put(FaceActions.FACE_AUTH, new URL(baseURL, "face/auth"));
            this.faceActionsURLs.put(FaceActions.FACE_ENROLL, new URL(baseURL, "face/enroll"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public SessionModel getSession(){
        return session;
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
        headersMap.put("X-aimbrain-apikey", this.appId);
        headersMap.put("X-aimbrain-signature", String.valueOf(calculateSignature("POST", url.getPath(), jsonObject.toString(), this.secret)));
        return headersMap;
    }

    public void createSession(String userId, Context context, final SessionCallback sessionCallback, Response.ErrorListener errorListener) throws InternalException, ConnectException {
        try {
            if(isOnline()) {
                WindowManager windowManager = (WindowManager)  context.getSystemService(Context.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                Point screenSize = new Point();
                display.getSize(screenSize);
                String system = "Android " + Build.VERSION.RELEASE;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                jsonObject.put("device", android.os.Build.MODEL);
                jsonObject.put("system", system);
                jsonObject.put("screenWidth", screenSize.x);
                jsonObject.put("screenHeight", screenSize.y);

                AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                        (getHeadersMap(jsonObject, this.sessionURL), Request.Method.POST, this.sessionURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Server.this.session = new SessionModel(response.get("session").toString(), response.getInt("face"), response.getInt("behaviour"));
                                    if(sessionCallback != null)
                                        sessionCallback.onSessionCreated(Server.this.session);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, errorListener);
                sendRequest(jsonRequest);
            }
            else
                throw new ConnectException("Unable to connect to server (check network settings).");
        } catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        } catch (InvalidSignatureException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    public void submitData(BehaviouralDataModel behaviouralDataModel, ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        this.dataQueue.add(behaviouralDataModel);
        if(isOnline()) {
            if(session != null) {
                sendAllDataFromQueue(scoreCallback);
            }
            else
                throw new SessionException("Unintialized session.");
        }
        else
            throw new ConnectException("Unable to connect to server (check network settings).");
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) AMBNApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void sendAllDataFromQueue(final ScoreCallback scoreCallback) throws InternalException {
        try {
            final String sessionIdCopy = session.getSessionId();
            while(!this.dataQueue.isEmpty())
            {
                final BehaviouralDataModel behaviouralDataModel = this.dataQueue.removeFirst();
                JSONObject jsonObject = wrapJSONObjectWithSession(behaviouralDataModel.toJSON());
                AMBNObjectRequest objectRequest = new AMBNObjectRequest(getHeadersMap(jsonObject, this.submitBehaviouralURL), Request.Method.POST, this.submitBehaviouralURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(scoreCallback != null) {
                            ScoreModel scoreModel = null;
                            try {
                                scoreModel = new ScoreModel(response.getDouble("score"), response.getInt("status"), sessionIdCopy);
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

    private JSONObject wrapJSONObjectWithSession(JSONObject jsonObject) throws JSONException {
        jsonObject.put("session", session.getSessionId());
        return jsonObject;
    }

    public void getCurrentScore(final ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        try {
            if(isOnline()) {
                if(session != null) {
                    final String sessionIdCopy = session.getSessionId();
                    JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
                    AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                            (getHeadersMap(jsonObject, this.scoreURL), Request.Method.POST, this.scoreURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (scoreCallback != null) {
                                            ScoreModel scoreModel = new ScoreModel(response.getDouble("score"), response.getInt("status"), sessionIdCopy);
                                            scoreCallback.success(scoreModel);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new AMBNResponseErrorListener());

                    sendRequest(jsonRequest);
                }
                else
                    throw new SessionException("Uninitialized session.");
            }
            else
                throw new ConnectException("Unable to connect to server (check network settings).");
        } catch (JSONException | InvalidSignatureException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    public void sendProvidedFaceCaptures(StringListDataModel photos, final FaceCapturesCallback callback, FaceActions faceAction)  throws InternalException, ConnectException, SessionException {
        try {
            if(isOnline()) {
                if(session != null) {
                    JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
                    jsonObject.put("faces", photos.toJSON());

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
                }
                else
                    throw new SessionException("Uninitialized session.");
            }
            else
                throw new ConnectException("Unable to connect to server (check network settings).");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void compareFaces(StringListDataModel firstFace, StringListDataModel secondFace, final FaceCompareCallback callback) throws InternalException, ConnectException, SessionException {
        try{
            if(isOnline()) {
                if(session != null) {
                    JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
                    jsonObject.put("faces1", firstFace.toJSON());
                    jsonObject.put("faces2", secondFace.toJSON());
                    AMBNObjectRequest jsonRequest = null;
                    try {
                        jsonRequest = new AMBNObjectRequest
                                (getHeadersMap(jsonObject, this.faceCompare), Request.Method.POST, this.faceCompare.toString(), jsonObject, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("Response", "response: " + response.toString());
                                        try {
                                            if (callback!= null) {
                                                callback.success(new FaceCompareModel(response.getDouble("score"), response.getDouble("liveliness1"), response.getDouble("liveliness2")));
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
                }
                else
                    throw new SessionException("Uninitialized session.");
            }
            else
                throw new ConnectException("Unable to connect to server (check network settings).");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(Request request) {
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }

}
