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
    private RequestQueue requestQueue;
    private String session;
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getSession(){
        return  session;
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
                                    Server.this.session = response.get("session").toString();
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
            final String sessionCopy = session;
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
                                scoreModel = new ScoreModel(response.getDouble("score"), response.getInt("status"), sessionCopy);
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
        jsonObject.put("session", session);
        return jsonObject;
    }

    public void getCurrentScore(final ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        try {
            if(isOnline()) {
                if(session != null) {
                    final String sessionCopy = session;
                    JSONObject jsonObject = wrapJSONObjectWithSession(new JSONObject());
                    AMBNObjectRequest jsonRequest = new AMBNObjectRequest
                            (getHeadersMap(jsonObject, this.scoreURL), Request.Method.POST, this.scoreURL.toString(), jsonObject, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (scoreCallback != null) {
                                            ScoreModel scoreModel = new ScoreModel(response.getDouble("score"), response.getInt("status"), sessionCopy);
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

    private void sendRequest(Request request)
    {
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }

}
