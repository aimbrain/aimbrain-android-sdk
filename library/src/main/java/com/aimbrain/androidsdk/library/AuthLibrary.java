package com.aimbrain.androidsdk.library;

import android.os.AsyncTask;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AuthLibrary {
    private static final String API_URL = "https://api.aimbrain.com/1/";

    private String mApikey;

    public AuthLibrary(String apikey) {
        this.mApikey = apikey;
    }

    // https://developer.android.com/reference/android/view/MotionEvent.html
    public void getAuthAsync(String userid,List<MotionEvent> events, AuthAsyncResponseHandler handler) {
        getAuthAsync(userid, events, System.currentTimeMillis(), handler);
    }

    public void getAuthAsync(String userid, List<MotionEvent> events, Long nonce, AuthAsyncResponseHandler handler) {
        // Parse all events
        JSONArray eventsObj = new JSONArray();

        if (events != null) {
            for (MotionEvent event : events) {
                // Consume historical events
                final int historySize = event.getHistorySize();
                final int pointerCount = event.getPointerCount();

                for (int h = 0; h < historySize; h++) {
                    for (int p = 0; p < pointerCount; p++) {
                        try {
                            JSONObject eventObj = new JSONObject()
                                    .put("a", actionToString(event.getAction()))
                                    .put("t", event.getHistoricalEventTime(h))
                                    .put("i", event.getPointerId(p))
                                    .put("x", event.getHistoricalX(p, h))
                                    .put("y", event.getHistoricalY(p, h))
                                    .put("s", event.getHistoricalSize(p, h))
                                    .put("j", event.getHistoricalTouchMajor(p, h))
                                    .put("n", event.getHistoricalTouchMinor(p, h))
                                    .put("p", event.getHistoricalPressure(p, h))
                                    .put("o", event.getHistoricalOrientation(p, h));

                            eventsObj.put(eventObj);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } // end Consume historical events

                // Consume current event
                for (int p = 0; p < pointerCount; p++) {
                    try {
                        JSONObject eventObj = new JSONObject()
                                .put("a", actionToString(event.getAction()))
                                .put("t", event.getEventTime())
                                .put("i", event.getPointerId(p))
                                .put("x", event.getX(p))
                                .put("y", event.getY(p))
                                .put("s", event.getSize(p))
                                .put("j", event.getTouchMajor(p))
                                .put("n", event.getTouchMinor(p))
                                .put("p", event.getPressure(p))
                                .put("o", event.getOrientation(p));

                        eventsObj.put(eventObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } // end  Consume current event
            } // end for (MotionEvent event : events)
        } // end if (events != null) {

        // Construct POST request body
        JSONObject body = new JSONObject();

        try {
            body.put("apikey", mApikey);
            body.put("id", userid);
            body.put("nonce", nonce);
            body.put("events", eventsObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("REQUEST BODY:", body.toString());
        new CallAPI().execute(new CallAPIRequest("auth", body.toString(), handler));
    }

    // AsyncTask wrapper for API call
    private class CallAPI extends AsyncTask<CallAPIRequest, Void, CallAPIResponse> {

        @Override
        protected CallAPIResponse doInBackground(CallAPIRequest... params) {
            CallAPIRequest req = params[0];

            CallAPIResponse result = new CallAPIResponse();
            result.setHandler(req.getHandler());

            InputStream in;
            HttpsURLConnection urlConnection;

            // Make HTTP POST request
            try {
                URL url = new URL(req.getURL());
                urlConnection = (HttpsURLConnection) url.openConnection();

                // Configure connection timeout
                urlConnection.setConnectTimeout(2000);
                urlConnection.setReadTimeout(3000);

                // Configure POST request
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Send POST request body
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(req.getBody().getBytes("UTF-8"));
                out.close();

            } catch (Exception e) {
                result.setException(e);
                result.setMessage("Failed to send http request");
                return result;
            }

            // Read response body
            StringBuilder sb = new StringBuilder();
            try {
                if (urlConnection.getResponseCode() == 200) {
                    in = new BufferedInputStream(urlConnection.getInputStream());
                } else {
                    in = new BufferedInputStream(urlConnection.getErrorStream());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                in.close();

            } catch (Exception e) {
                result.setException(e);
                result.setMessage("Failed to parse HTTP reply stream");
                return result;
            }

            // Check HTTP response code and parse error message, if any
            try {
                result.setStatusCode(urlConnection.getResponseCode());

                // Parse error message
                if (urlConnection.getResponseCode() != 200) {
                    result.setException(new Exception(urlConnection.getResponseMessage()));

                    JSONObject jsonBody = new JSONObject(sb.toString());
                    result.setMessage(jsonBody.getString("error"));

                    return result;
                }

                result.setBody(sb.toString());

            } catch (Exception e) {
                result.setException(e);
                result.setMessage("Failed to parse HTTP reply body");
                return result;
            }

            return result;
        }

        protected void onPostExecute(CallAPIResponse result) {
            if (result.isSuccess()) {
                // Received HTTP response -> parse & validate JSON object
                JSONObject jsonBody;
                AuthAsyncResponse parsedResponse;
                try {
                    jsonBody = new JSONObject(result.getBody());

                    parsedResponse = new AuthAsyncResponse(
                            result.getStatusCode(),
                            result.getBody(),
                            jsonBody.getDouble("score"),
                            jsonBody.getString("method"),
                            jsonBody.getString("id"),
                            jsonBody.getLong("nonce"));
                } catch (JSONException e) {
                    // Parsing failed
                    e.printStackTrace();
                    result.getHandler().onFailure(500, e.getMessage(), e);
                    return;
                }

                // Parsing succeed
                result.getHandler().onSuccess(parsedResponse);
                return;
            }

            // HTTP request failed
            result.getHandler().onFailure(result.getStatusCode(), result.getMessage(), result.getException());
        }

    } // end CallAPI

    private class CallAPIRequest {
        private String endpoint;
        private String body;
        private AuthAsyncResponseHandler handler;

        public CallAPIRequest(String endpoint, String body, AuthAsyncResponseHandler handler) {
            this.endpoint = API_URL + endpoint;
            this.body = body;
            this.handler = handler;
        }

        public String getURL() {
            return endpoint;
        }

        public String getBody() {
            return body;
        }

        public AuthAsyncResponseHandler getHandler() {
            return handler;
        }
    } // end CallAPIRequest

    private class CallAPIResponse {
        private Boolean success = false;
        private Integer statusCode = 0;
        private Exception exception = null;
        private String message = "";
        private String body = "";
        private AuthAsyncResponseHandler handler = null;

        public void setStatusCode(Integer code) {
            this.statusCode = code;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setBody(String body) {
            this.body = body;
            this.success = true;
        }

        public String getBody() {
            return body;
        }

        public void setException(Exception e) {
            this.exception = e;
            this.success = false;
        }

        public Exception getException() {
            return exception;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public Boolean isSuccess() {
            return success;
        }

        public void setHandler(AuthAsyncResponseHandler handler) {
            this.handler = handler;
        }

        public AuthAsyncResponseHandler getHandler() {
            return handler;
        }
    } // end CallAPIResponse


    private static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "ACTION_HOVER_EXIT";
        }
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }
}
