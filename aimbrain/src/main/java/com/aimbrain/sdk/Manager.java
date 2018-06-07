package com.aimbrain.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.VisibleForTesting;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.aimbrain.sdk.AMBNApplication.AMBNApplication;
import com.aimbrain.sdk.activityCallback.AMBNActivityLifecycleCallback;
import com.aimbrain.sdk.collectors.MotionEventCollector;
import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.faceCapture.PictureManager;
import com.aimbrain.sdk.memory.MemoryLimiter;
import com.aimbrain.sdk.models.BehaviouralDataModel;
import com.aimbrain.sdk.models.EventModel;
import com.aimbrain.sdk.models.FaceTokenType;
import com.aimbrain.sdk.models.SerializedRequest;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.models.VoiceTokenType;
import com.aimbrain.sdk.motionEvent.AMBNWindowCallback;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.privacy.TouchTrackingGuard;
import com.aimbrain.sdk.server.AMBNResponseErrorListener;
import com.aimbrain.sdk.server.FaceActions;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.FaceCapturesCallback;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.aimbrain.sdk.server.FaceCompareCallback;
import com.aimbrain.sdk.server.FaceTokenCallback;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.Server;
import com.aimbrain.sdk.server.SessionCallback;
import com.aimbrain.sdk.server.VoiceActions;
import com.aimbrain.sdk.server.VoiceCaptureEnrollCallback;
import com.aimbrain.sdk.server.VoiceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.VoiceCapturesCallback;
import com.aimbrain.sdk.server.VoiceTokenCallback;
import com.aimbrain.sdk.util.Logger;
import com.android.volley.Response;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class allows defining library configuration and interaction with the server.
 */
public class Manager {
    private static final String TAG = Manager.class.getSimpleName();
    public static final int MEMORY_USAGE_UNLIMITED = 0;
    private static Manager manager;

    private List<WeakReference<Window>> trackedWindows;
    @VisibleForTesting
    protected Server server;
    private Timer timer;
    private TimerTask timerTask;
    private AMBNActivityLifecycleCallback activityLifecycleCallback;
    @VisibleForTesting
    protected ArrayList<PrivacyGuard> privacyGuards;
    @VisibleForTesting
    protected ArrayList<TouchTrackingGuard> touchTrackingGuards;

    /**
     * Returns singleton object of the class
     *
     * @return instance of class Manager
     */
    public static Manager getInstance() {
        if (manager == null)
            manager = new Manager();
        return manager;
    }

    @VisibleForTesting
    protected Manager() {
        this.trackedWindows = new ArrayList<>();
        this.privacyGuards = new ArrayList<>();
        this.touchTrackingGuards = new ArrayList<>();
    }

    /**
     * Call this method to start gathering data.
     *
     * @param window window object that is currently displayed on top.
     */
    public void startCollectingData(Window window) {
        Logger.v(TAG, "Start collecting data in " + window);
        activityLifecycleCallback = new AMBNActivityLifecycleCallback();
        AMBNApplication.getInstance().registerActivityLifecycleCallbacks(activityLifecycleCallback);
        if (window != null) {
            windowChanged(window);
        }
    }

    /**
     * Method called in order to notify Manager object about top displayed window change.
     * It is called internally for Activities and should be called only for other than main activity's window.
     *
     * @param window window that is currently displayed as top window
     */
    public void windowChanged(Window window) {
        Logger.d(TAG, "Window changed " + window);
        beginTrackingWindow(window);
    }

    private void beginTrackingWindow(Window window) {
        Window existingTracked = null;
        for (WeakReference<Window> currentRef : trackedWindows) {
            Window currentWindow = currentRef.get();
            if (currentWindow != null && window.equals(currentWindow)) {
                existingTracked = currentWindow;
                break;
            }
        }

        if (existingTracked != null) {
            Window.Callback currentCallback = window.getCallback();
            if (currentCallback instanceof AMBNWindowCallback) {
                Logger.v(TAG, "AMBN callback as expected");
            } else if (currentCallback == null) {
                Logger.v(TAG, "No callback for tracked window!");
                new AMBNWindowCallback(window);
            } else {
                Logger.v(TAG, "Unexpected callback:" + currentCallback);
                new AMBNWindowCallback(window);
            }
        } else {
            new AMBNWindowCallback(window);
            this.trackedWindows.add(new WeakReference<>(window));
        }
    }

    /**
     * Allows scheduling data submission with given parameters. Data received from server will be ignored.
     *
     * @param delay  delay before first submission in milliseconds
     * @param period period between next data submissions in milliseconds
     */
    public void scheduleDataSubmission(long delay, long period) {
        scheduleDataSubmission(delay, period, null);
    }

    /**
     * Allows scheduling data submission with given parameters. Data received from server will be passed to given ScoreCallback.
     *
     * @param delay         delay before first submission in milliseconds
     * @param period        period between next data submissions in milliseconds
     * @param scoreCallback callback for receiving responses from server
     */
    public void scheduleDataSubmission(long delay, long period, final ScoreCallback scoreCallback) {
        Logger.v(TAG, "Schedule data submission after " + delay + ", every " + period);
        if (timer == null) {
            Logger.v(TAG, "Creating submission timer");
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Logger.v(TAG, "Submit scheduled data");
                    try {
                        submitCollectedData(scoreCallback);
                    } catch (InternalException | ConnectException | SessionException e) {
                        Logger.w(TAG, e);
                    }
                }
            };
            timer.schedule(timerTask, delay, period);
        }
    }

    /**
     * Allows to stop collecting data.
     */
    public void stopCollectingData() {
        Logger.v(TAG, "Stop collecting data");
        AMBNApplication.getInstance().unregisterActivityLifecycleCallbacks(activityLifecycleCallback);
        activityLifecycleCallback = null;
        if (timer != null) {
            Logger.v(TAG, "Stop submission timer");
            timer.cancel();
            timer = null;
        }

        Iterator<WeakReference<Window>> windowIterator = trackedWindows.iterator();
        while (windowIterator.hasNext()) {
            WeakReference<Window> item = windowIterator.next();
            Window window = item.get();
            if (window == null) {
                continue;
            }
            Window.Callback callback = window.getCallback();
            if (!(callback instanceof AMBNWindowCallback)) {
                Log.d(TAG, "unexpected window callback " + callback);
            } else {
                AMBNWindowCallback ambnCallback = (AMBNWindowCallback) callback;
                window.setCallback(ambnCallback.getWrappedCallback());
            }
            windowIterator.remove();
            Logger.v(TAG, "Removed callback from " + window);
        }

        TextEventCollector.getInstance().stop();
    }

    /**
     * Adds privacy guard.
     *
     * @param privacyGuard privacy guard to be added
     */
    public void addPrivacyGuard(PrivacyGuard privacyGuard) {
        if (!privacyGuards.contains(privacyGuard)) {
            Logger.v(TAG, "Add privacy guard");
            privacyGuards.add(privacyGuard);
        }
    }

    /**
     * Removes privacy guard.
     *
     * @param privacyGuard
     */
    public void removePrivacyGuard(PrivacyGuard privacyGuard) {
        if (privacyGuards.contains(privacyGuard)) {
            Logger.v(TAG, "Remove privacy guard");
            privacyGuards.remove(privacyGuard);
            privacyGuard.revokeEditTextFocus();
        }
    }

    /**
     * Returns true if given view (or its parent) is added to any privacy guard defined.
     *
     * @param view view to be checked
     * @return true if view is ignored while collecting data
     */
    public boolean isViewIgnored(View view) {
        for (PrivacyGuard privacyGuard : privacyGuards) {
            if (privacyGuard.isViewIgnored(view)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds touch tracking guard.
     *
     * @param touchTrackingGuard touch tracking guard to be added
     */
    public void addTouchTrackingGuard(TouchTrackingGuard touchTrackingGuard) {
        if (!touchTrackingGuards.contains(touchTrackingGuard)) {
            Logger.v(TAG, "Add touch tracking guard");
            touchTrackingGuards.add(touchTrackingGuard);
        }
    }

    /**
     * Returns true if given view (or its parent) is added to any touch tracking guard defined.
     *
     * @param view view to be checked
     * @return true if view is ignored while going through view hierarchy
     */
    public boolean isTouchTrackingIgnored(View view) {
        for (TouchTrackingGuard touchTrackingGuard : touchTrackingGuards) {
            if (touchTrackingGuard.isViewIgnored(view)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Allows for passing existing session id. Only serialization calls will be available in this mode.
     *
     * @param sessionId existing session id
     */
    public void configure(String sessionId) {
        Logger.v(TAG, "Configure with session id " + sessionId);
        SessionModel model = new SessionModel(sessionId, SessionModel.NOT_ENROLLED,
                SessionModel.NOT_ENROLLED, SessionModel.NOT_ENROLLED, null);
        this.server = new Server(model);
    }

    /**
     * Allows for passing server configuration. Needs to be called before sending any data or creating session.
     *
     * @param apiKey application identifier
     * @param secret secret defined for given application id
     */
    public void configure(String apiKey, String secret) {
        Logger.v(TAG, "Configure with api key " + apiKey);
        this.server = new Server(apiKey, secret);
    }

    /**
     * Allows for passing server configuration. Needs to be called before sending any data or creating session.
     *
     * @param apiKey     application identifier
     * @param secret     secret defined for given application id
     * @param apiBaseUrl base api server url.
     */
    public void configure(String apiKey, String secret, String apiBaseUrl) {
        Logger.v(TAG, "Configure with api key " + apiKey + ", base url " + apiBaseUrl);
        this.server = new Server(apiKey, secret, apiBaseUrl);
    }

    /**
     * Api call availability check.
     *
     * @return true if current configuration allows to make call to server.
     */
    public boolean isConfiguredForApiCalls() {
        return this.server != null && this.server.isConfiguredForApiCalls();
    }

    /**
     * Allows for creating session. Method needs to be called before sending gathered data. Calls create session with default error listener.
     *
     * @param userId          user identifier
     * @param context         context used to obtain display size
     * @param sessionCallback callback for successful session creation
     * @throws InternalException     thrown when preparing request for server fails
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void createSession(String userId, Context context, SessionCallback sessionCallback) throws InternalException, ConnectException {
        this.createSession(userId, null, context, sessionCallback, new AMBNResponseErrorListener());
    }

    /**
     * Allows for creating session. Method needs to be called before sending gathered data.
     *
     * @param userId          user identifier
     * @param context         context used to obtain display size
     * @param sessionCallback callback for successful session creation
     * @param errorListener   callback for error handling
     * @throws InternalException     thrown when preparing request for server fails
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void createSession(String userId, Context context, SessionCallback sessionCallback, Response.ErrorListener errorListener) throws InternalException, ConnectException {
        this.createSession(userId, null, context, sessionCallback, errorListener);
    }

    /**
     * Allows for creating session. Method needs to be called before sending gathered data.
     *
     * @param userId          user identifier
     * @param metadata        request metadata
     * @param context         context used to obtain display size
     * @param sessionCallback callback for successful session creation
     * @param errorListener   callback for error handling
     * @throws InternalException     thrown when preparing request for server fails
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void createSession(String userId, byte[] metadata, Context context, SessionCallback sessionCallback, Response.ErrorListener errorListener) throws InternalException, ConnectException {
        checkServerNotNull();
        Logger.v(TAG, "Create session with user id " + userId);
        this.server.createSession(userId, metadata, context, sessionCallback, errorListener);
    }

    /**
     * Retrieves serialized create session call.
     *
     * @param userId   user identifier
     * @param metadata request metadata
     * @param context  context used to obtain display size
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedCreateSession(String userId, byte[] metadata, Context context) throws InternalException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized create session with user id " + userId);
        return this.server.getSerializedCreateSession(userId, metadata, context);
    }

    /**
     * Submits data collected between method's invocations.
     *
     * @param scoreCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws ConnectException      thrown when connection problem occurs
     * @throws SessionException      thrown when session has not yet been created
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void submitCollectedData(ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        this.submitCollectedData(null, scoreCallback);
    }

    /**
     * Submits data collected between method's invocations.
     *
     * @param scoreCallback callback for receiving response from server
     * @param metadata      request metadata
     * @throws InternalException     thrown when preparing request for server fails
     * @throws ConnectException      thrown when connection problem occurs
     * @throws SessionException      thrown when session has not yet been created
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void submitCollectedData(byte[] metadata, ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Submit collected data");
        if (TextEventCollector.getInstance().hasData() || SensorEventCollector.getInstance().hasData() || MotionEventCollector.getInstance().hasData()) {
            List<EventModel> textData = TextEventCollector.getInstance().getCollectedData();
            List<EventModel> sensorData = SensorEventCollector.getInstance().getCollectedData();
            List<EventModel> motionData = MotionEventCollector.getInstance().getCollectedData();
            BehaviouralDataModel behaviouralDataModel = new BehaviouralDataModel(textData, sensorData, motionData, metadata);
            this.server.submitData(behaviouralDataModel, scoreCallback);
        }
    }

    /**
     * Retrieves serialized request for data collected between method's invocations
     *
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSubmitCollectedData(byte[] metadata) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized submit collected data");
        List<EventModel> textData = TextEventCollector.getInstance().getCollectedData();
        List<EventModel> sensorData = SensorEventCollector.getInstance().getCollectedData();
        List<EventModel> motionData = MotionEventCollector.getInstance().getCollectedData();
        BehaviouralDataModel behaviouralDataModel = new BehaviouralDataModel(textData, sensorData, motionData, metadata);
        return this.server.getSerializedSubmitData(behaviouralDataModel);
    }

    /**
     * Sends request for current score to the server.
     *
     * @param scoreCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getCurrentScore(ScoreCallback scoreCallback) throws InternalException, SessionException, ConnectException {
        this.getCurrentScore(null, scoreCallback);
    }

    /**
     * Sends request for current score to the server.
     *
     * @param metadata      request metadata
     * @param scoreCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getCurrentScore(byte[] metadata, ScoreCallback scoreCallback) throws InternalException, SessionException, ConnectException {
        checkServerNotNull();
        Logger.v(TAG, "Get current score");
        server.getCurrentScore(metadata, scoreCallback);
    }

    /**
     * Retrieves serialized request of request for current score.
     *
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedGetCurrentScore(byte[] metadata) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized get current score");
        return server.getSerializedGetCurrentScore(metadata);
    }

    /**
     * Sends request for face token to the server.
     *
     * @param tokenType     token type
     * @param tokenCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getFaceToken(FaceTokenType tokenType, FaceTokenCallback tokenCallback)
            throws InternalException, SessionException, ConnectException {
        this.getFaceToken(tokenType, null, tokenCallback);
    }

    /**
     * Sends request for face token to the server.
     *
     * @param tokenType     token type
     * @param metadata      request metadata
     * @param tokenCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getFaceToken(FaceTokenType tokenType, byte[] metadata, FaceTokenCallback tokenCallback)
            throws InternalException, SessionException, ConnectException {
        checkServerNotNull();
        Logger.v(TAG, "Voice token of type " + tokenType);
        server.getFaceToken(tokenType, metadata, tokenCallback);
    }

    /**
     * Retrieves serialized request of request for face token.
     *
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedFaceToken(FaceTokenType tokenType, byte[] metadata) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized face token of type " + tokenType);
        return server.getSerializedFaceToken(tokenType, metadata);
    }

    /**
     * Sends photo to enroll endpoint on the server.
     *
     * @param photos                     photos to send
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToEnroll(List<Bitmap> photos, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedFaceCapturesToEnroll(photos, null, faceCapturesEnrollCallback);
    }

    /**
     * Sends photo to enroll endpoint on the server.
     *
     * @param photos                     photos to send
     * @param metadata                   request metadata
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToEnroll(List<Bitmap> photos, byte[] metadata, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        Logger.v(TAG, "Send provided face captures to enroll");
        sendFaceCaptures(encodePhotos(photos), metadata, FaceActions.FACE_ENROLL, faceCapturesEnrollCallback);
    }

    /**
     * Retrieves serialized request of sending photo to enroll endpoint on the server.
     *
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedFaceCapturesToEnroll(List<Bitmap> photos, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendFaceCaptures(encodePhotos(photos), metadata, FaceActions.FACE_ENROLL);
    }

    /**
     * Sends photo to authentication endpoint on the server.
     *
     * @param photos                           photos to send
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToAuthenticate(List<Bitmap> photos, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedFaceCapturesToAuthenticate(photos, null, faceCapturesAuthenticateCallback);
    }

    /**
     * Sends photo to authentication endpoint on the server.
     *
     * @param photos                           photos to send
     * @param metadata                         request metadata
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToAuthenticate(List<Bitmap> photos, byte[] metadata, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodePhotos(photos), metadata, FaceActions.FACE_AUTH, faceCapturesAuthenticateCallback);
    }

    /**
     * Retrieves serialized request of sending photo to authentication endpoint on the server.
     *
     * @param photos   photos to send
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedFaceCapturesToAuthenticate(List<Bitmap> photos, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendFaceCaptures(encodePhotos(photos), metadata, FaceActions.FACE_AUTH);
    }

    /**
     * Sends video to enroll endpoint on the server.
     *
     * @param video                      video to send
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToEnroll(byte[] video, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedFaceCapturesToEnroll(video, null, faceCapturesEnrollCallback);
    }

    /**
     * Sends video to enroll endpoint on the server.
     *
     * @param video                      video to send
     * @param metadata                   request metadata
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToEnroll(byte[] video, byte[] metadata, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodeVideo(video), metadata, FaceActions.FACE_ENROLL, faceCapturesEnrollCallback);
    }

    /**
     * Retrieves serialized request of sending photo to enroll endpoint on the server.
     *
     * @param video    video to send
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedFaceCapturesToEnroll(byte[] video, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendFaceCaptures(encodeVideo(video), metadata, FaceActions.FACE_ENROLL);
    }

    /**
     * Sends video to authentication endpoint on the server.
     *
     * @param video                            video to send
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToAuthenticate(byte[] video, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedFaceCapturesToAuthenticate(video, null, faceCapturesAuthenticateCallback);
    }

    /**
     * Sends video to authentication endpoint on the server.
     *
     * @param video                            video to send
     * @param metadata                         request metadata
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedFaceCapturesToAuthenticate(byte[] video, byte[] metadata, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodeVideo(video), metadata, FaceActions.FACE_AUTH, faceCapturesAuthenticateCallback);
    }

    /**
     * Retrieves serialized request of sending photo to authentication endpoint on the server.
     *
     * @param video    video to send
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedFaceCapturesToAuthenticate(byte[] video, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendFaceCaptures(encodeVideo(video), metadata, FaceActions.FACE_AUTH);
    }

    /**
     * Compares two faces.
     *
     * @param firstFacePhotos  photos of the first face to compare
     * @param secondFacePhotos photos of the second face to compare
     * @param callback         callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void compareFacesPhotos(List<Bitmap> firstFacePhotos, List<Bitmap> secondFacePhotos, FaceCompareCallback callback) throws InternalException, ConnectException, SessionException {
        this.compareFacesPhotos(firstFacePhotos, secondFacePhotos, null, callback);
    }

    /**
     * Compares two faces.
     *
     * @param firstFacePhotos  photos of the first face to compare
     * @param secondFacePhotos photos of the second face to compare
     * @param metadata         request metadata
     * @param callback         callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void compareFacesPhotos(List<Bitmap> firstFacePhotos, List<Bitmap> secondFacePhotos, byte[] metadata, FaceCompareCallback callback) throws InternalException, ConnectException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Compare faces");
        server.compareFaces(encodePhotos(firstFacePhotos), encodePhotos(secondFacePhotos), metadata, callback);
    }

    /**
     * Retrieves serialized request to compare two faces.
     *
     * @param firstFacePhotos  photos of the first face to compare
     * @param secondFacePhotos photos of the second face to compare
     * @param metadata         request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedCompareFacesPhotos(List<Bitmap> firstFacePhotos, List<Bitmap> secondFacePhotos, byte[] metadata) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized compare faces");
        return server.getSerializedCompareFaces(encodePhotos(firstFacePhotos), encodePhotos(secondFacePhotos), metadata);
    }

    /**
     * Sends audio to enroll endpoint on the server.
     *
     * @param audio                       audio to send
     * @param voiceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedVoiceCapturesToEnroll(byte[] audio, VoiceCaptureEnrollCallback voiceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedVoiceCapturesToEnroll(audio, null, voiceCapturesEnrollCallback);
    }

    /**
     * Sends audio to enroll endpoint on the server.
     *
     * @param audio                       audio to send
     * @param metadata                    request metadata
     * @param voiceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedVoiceCapturesToEnroll(byte[] audio, byte[] metadata,
                                                  VoiceCaptureEnrollCallback voiceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        sendVoiceCaptures(encodeVideo(audio), metadata, VoiceActions.VOICE_ENROLL, voiceCapturesEnrollCallback);
    }

    /**
     * Retrieves serialized request of sending audio to enroll endpoint on the server.
     *
     * @param audio    audio to send
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedVoiceCapturesToEnroll(byte[] audio, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendVoiceCaptures(encodeVideo(audio), metadata, VoiceActions.VOICE_ENROLL);
    }

    /**
     * Sends audio to authentication endpoint on the server.
     *
     * @param audio                             audio to send
     * @param voiceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedVoiceCapturesToAuthenticate(byte[] audio, VoiceCapturesAuthenticateCallback voiceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        this.sendProvidedVoiceCapturesToAuthenticate(audio, null, voiceCapturesAuthenticateCallback);
    }

    /**
     * Sends audio to authentication endpoint on the server.
     *
     * @param audio                             audio to send
     * @param metadata                          request metadata
     * @param voiceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void sendProvidedVoiceCapturesToAuthenticate(byte[] audio, byte[] metadata,
                                                        VoiceCapturesAuthenticateCallback voiceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        sendVoiceCaptures(encodeVideo(audio), metadata, VoiceActions.VOICE_AUTH, voiceCapturesAuthenticateCallback);
    }

    /**
     * Retrieves serialized request of sending audio to authentication endpoint on the server.
     *
     * @param audio    audio to send
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedSendProvidedVoiceCapturesToAuthenticate(byte[] audio, byte[] metadata) throws InternalException, SessionException {
        return getSerializedSendVoiceCaptures(encodeVideo(audio), metadata, VoiceActions.VOICE_AUTH);
    }

    /**
     * Sends request for voice token to the server.
     *
     * @param tokenType     token type
     * @param tokenCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getVoiceToken(VoiceTokenType tokenType, VoiceTokenCallback tokenCallback)
            throws InternalException, SessionException, ConnectException {
        this.getVoiceToken(tokenType, null, tokenCallback);
    }

    /**
     * Sends request for current score to the server.
     *
     * @param tokenType     token type
     * @param metadata      request metadata
     * @param tokenCallback callback for receiving response from server
     * @throws InternalException     thrown when preparing request for server fails
     * @throws SessionException      thrown when session has not yet been created
     * @throws ConnectException      thrown when connection problem occurs
     * @throws IllegalStateException thrown when current configuration is for request serialization only.
     */
    public void getVoiceToken(VoiceTokenType tokenType, byte[] metadata, VoiceTokenCallback tokenCallback)
            throws InternalException, SessionException, ConnectException {
        checkServerNotNull();
        Logger.v(TAG, "Voice token of type " + tokenType);
        server.getVoiceToken(tokenType, metadata, tokenCallback);
    }

    /**
     * Retrieves serialized request of request for voice token.
     *
     * @param metadata request metadata
     * @throws InternalException thrown when preparing request fails
     */
    public SerializedRequest getSerializedVoiceToken(VoiceTokenType tokenType, byte[] metadata) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized voice token of type " + tokenType);
        return server.getSerializedVoiceToken(tokenType, metadata);
    }

    @VisibleForTesting
    protected StringListDataModel encodePhotos(List<Bitmap> photos) {
        ArrayList<String> encoded = new ArrayList<>();
        for (Bitmap photo : photos) {
            encoded.add(PictureManager.getEncodedCompressedPhoto(photo));
        }
        StringListDataModel encodedPhotosModel = new StringListDataModel();
        encodedPhotosModel.setData(encoded);
        return encodedPhotosModel;
    }

    @VisibleForTesting
    protected StringListDataModel encodeVideo(byte[] video) {
        ArrayList<String> encoded = new ArrayList<>();
        encoded.add(Base64.encodeToString(video, Base64.NO_WRAP));
        StringListDataModel encodedVideoModel = new StringListDataModel();
        encodedVideoModel.setData(encoded);
        return encodedVideoModel;
    }

    private void sendFaceCaptures(StringListDataModel captures, byte[] metadata, FaceActions faceAction, FaceCapturesCallback faceCapturesCallback) throws InternalException, ConnectException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Send face captures for " + faceAction);
        server.sendProvidedFaceCaptures(captures, metadata, faceCapturesCallback, faceAction);
    }

    private SerializedRequest getSerializedSendFaceCaptures(StringListDataModel captures, byte[] metadata, FaceActions faceAction) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized send face captures for " + faceAction);
        return server.getSerializedSendProvidedFaceCaptures(captures, metadata, faceAction);
    }

    public SessionModel getSession() {
        return server.getSession();
    }

    private void sendVoiceCaptures(StringListDataModel captures, byte[] metadata,
                                   VoiceActions voiceAction, VoiceCapturesCallback voiceCapturesCallback)
            throws InternalException, ConnectException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized send voice captures for " + voiceAction);
        server.sendProvidedVoiceCaptures(captures, metadata, voiceCapturesCallback, voiceAction);
    }

    private SerializedRequest getSerializedSendVoiceCaptures(StringListDataModel captures, byte[] metadata,
                                                             VoiceActions voiceAction) throws InternalException, SessionException {
        checkServerNotNull();
        Logger.v(TAG, "Serialized send voice captures for " + voiceAction);
        return server.getSerializedSendProvidedVoiceCaptures(captures, metadata, voiceAction);
    }

    private void checkServerNotNull() {
        if (this.server == null) {
            throw new IllegalStateException("Server is not configured properly.");
        }
    }

    /**
     * Limit data collection memory. Set unlimited memory usage with Manager.MEMORY_USAGE_UNLIMITED param. Default memory usage is unlimited
     * @param memoryUsageLimit kilobytes
     */
    public void setMemoryUsageLimitInKilobytes(int memoryUsageLimit) {
        MemoryLimiter.getInstance().setMemoryUsageLimitInKilobytes(memoryUsageLimit);
    }

}
