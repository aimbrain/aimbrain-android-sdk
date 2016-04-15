package com.aimbrain.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;
import android.view.Window;

import com.aimbrain.sdk.faceCapture.PictureManager;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.server.FaceActions;
import com.aimbrain.sdk.server.FaceCompareCallback;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.FaceCapturesCallback;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.android.volley.Response;
import com.aimbrain.sdk.AMBNApplication.AMBNApplication;
import com.aimbrain.sdk.activityCallback.AMBNActivityLifecycleCallback;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.BehaviouralDataModel;
import com.aimbrain.sdk.motionEvent.AMBNWindowCallback;
import com.aimbrain.sdk.collectors.MotionEventCollector;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.server.AMBNResponseErrorListener;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.Server;
import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.server.SessionCallback;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;


/**
 * Class allows defining library configuration and interaction with the server.
 */
public class Manager {

    private static Manager manager;

    private WeakHashMap<Window, AMBNWindowCallback> windowsMap;
    private Server server;
    private Timer timer;
    private TimerTask timerTask;
    private AMBNActivityLifecycleCallback activityLifecycleCallback;
    private ArrayList<PrivacyGuard> privacyGuards;

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

    private Manager() {
        this.windowsMap = new WeakHashMap<>();
        this.privacyGuards = new ArrayList<>();
    }

    /**
     * Call this method to start gathering data.
     *
     * @param window window object that is currently displayed on top.
     */
    public void startCollectingData(Window window) {
        activityLifecycleCallback = new AMBNActivityLifecycleCallback();
        AMBNApplication.getInstance().registerActivityLifecycleCallbacks(activityLifecycleCallback);
        if (window != null)
            windowChanged(window);
    }

    /**
     * Method called in order to notify Manager object about top displayed window change.
     * It is called internally for Activities and should be called only for other than main activity's window.
     *
     * @param window window that is currently displayed as top window
     */
    public void windowChanged(Window window) {
        if (!this.windowsMap.containsKey(window))
            this.windowsMap.put(window, new AMBNWindowCallback(window));
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
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        submitCollectedData(scoreCallback);
                    } catch (InternalException | ConnectException | SessionException e) {
                        e.printStackTrace();
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
        AMBNApplication.getInstance().unregisterActivityLifecycleCallbacks(activityLifecycleCallback);
        activityLifecycleCallback = null;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        for (Window window : windowsMap.keySet()) {
            window.setCallback(windowsMap.get(window).getLocalCallback());
            windowsMap.remove(window);
        }
        TextEventCollector.getInstance().stop();
    }

    /**
     * Adds privacy guard.
     *
     * @param privacyGuard privacy guard to be added
     */
    public void addPrivacyGuard(PrivacyGuard privacyGuard) {
        if (!privacyGuards.contains(privacyGuard))
            privacyGuards.add(privacyGuard);
    }

    /**
     * Returns true if given view (or its parent) is added to any privacy guard defined.
     *
     * @param view view to be checked
     * @return true if view is ignored while collecting data
     */
    public boolean isViewIgnored(View view) {
        for (PrivacyGuard privacyGuard : privacyGuards) {
            if (privacyGuard.isViewIgnored(view))
                return true;
        }
        return false;
    }

    /**
     * Allows for passing server configuration. Needs to be called before sending any data or creating session.
     *
     * @param appId  application identifier
     * @param secret secret defined for given application id
     */
    public void configure(String appId, String secret) {
        this.server = new Server(appId, secret);
    }

    /**
     * Allows for creating session. Method needs to be called before sending gathered data. Calls create session with default error listener.
     *
     * @param userId          user identifier
     * @param context         context used to obtain display size
     * @param sessionCallback callback for successful session creation
     * @throws InternalException thrown when preparing request for server fails
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void createSession(String userId, Context context, SessionCallback sessionCallback) throws InternalException, ConnectException {
        this.server.createSession(userId, context, sessionCallback, new AMBNResponseErrorListener());
    }

    /**
     * Allows for creating session. Method needs to be called before sending gathered data.
     *
     * @param userId          user identifier
     * @param context         context used to obtain display size
     * @param sessionCallback callback for successful session creation
     * @param errorListener   callback for error handling
     * @throws InternalException thrown when preparing request for server fails
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void createSession(String userId, Context context, SessionCallback sessionCallback, Response.ErrorListener errorListener) throws InternalException, ConnectException {
        if (this.server == null)
            throw new IllegalStateException("Server is not configured properly.");
        this.server.createSession(userId, context, sessionCallback, errorListener);
    }

    /**
     * Submits data collected between method's invocations.
     *
     * @param scoreCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws ConnectException  thrown when connection problem occurs
     * @throws SessionException  thrown when session has not yet been created
     */
    public void submitCollectedData(ScoreCallback scoreCallback) throws InternalException, ConnectException, SessionException {
        if (this.server == null)
            throw new IllegalStateException("Server is not configured properly.");
        if (TextEventCollector.getInstance().hasData() || SensorEventCollector.getInstance().hasData() || MotionEventCollector.getInstance().hasData()) {
            BehaviouralDataModel behaviouralDataModel = new BehaviouralDataModel(TextEventCollector.getInstance().getCollectedData(), SensorEventCollector.getInstance().getCollectedData(), MotionEventCollector.getInstance().getCollectedData());
            this.server.submitData(behaviouralDataModel, scoreCallback);
        }
    }

    /**
     * Sends request for current score to the server.
     *
     * @param scoreCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */

    public void getCurrentScore(ScoreCallback scoreCallback) throws InternalException, SessionException, ConnectException {
        server.getCurrentScore(scoreCallback);
    }

    /**
     * Sends photo to enroll endpoint on the server.
     *
     * @param photos               photos to send
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void sendProvidedFaceCapturesToEnroll(List<Bitmap> photos, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodePhotos(photos), FaceActions.FACE_ENROLL, faceCapturesEnrollCallback);
    }

    /**
     * Sends photo to authentication endpoint on the server.
     *
     * @param photos                     photos to send
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void sendProvidedFaceCapturesToAuthenticate(List<Bitmap> photos, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodePhotos(photos), FaceActions.FACE_AUTH, faceCapturesAuthenticateCallback);
    }

    /**
     * Sends video to enroll endpoint on the server.
     *
     * @param video               video to send
     * @param faceCapturesEnrollCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void sendProvidedFaceCapturesToEnroll(byte[] video, FaceCapturesEnrollCallback faceCapturesEnrollCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodeVideo(video), FaceActions.FACE_ENROLL, faceCapturesEnrollCallback);
    }

    /**
     * Sends photo to authentication endpoint on the server.
     *
     * @param video                     video to send
     * @param faceCapturesAuthenticateCallback callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void sendProvidedFaceCapturesToAuthenticate(byte[] video, FaceCapturesAuthenticateCallback faceCapturesAuthenticateCallback) throws InternalException, ConnectException, SessionException {
        sendFaceCaptures(encodeVideo(video), FaceActions.FACE_AUTH, faceCapturesAuthenticateCallback);
    }

    /**
     * Compares two faces.
     *
     * @param firstFacePhotos  photos of the first face to compare
     * @param secondFacePhotos photos of the second face to compare
     * @param callback         callback for receiving response from server
     * @throws InternalException thrown when preparing request for server fails
     * @throws SessionException  thrown when session has not yet been created
     * @throws ConnectException  thrown when connection problem occurs
     */
    public void compareFacesPhotos(List<Bitmap> firstFacePhotos, List<Bitmap> secondFacePhotos, FaceCompareCallback callback) throws InternalException, ConnectException, SessionException {
        server.compareFaces(encodePhotos(firstFacePhotos), encodePhotos(secondFacePhotos), callback);
    }

    private StringListDataModel encodePhotos(List<Bitmap> photos) {
        ArrayList<String> encoded = new ArrayList<>();
        for (Bitmap photo : photos) {
            encoded.add(PictureManager.getEncodedCompressedPhoto(photo));
        }
        StringListDataModel encodedPhotosModel = new StringListDataModel();
        encodedPhotosModel.setData(encoded);
        return encodedPhotosModel;
    }

    private StringListDataModel encodeVideo(byte[] video) {
        ArrayList<String> encoded = new ArrayList<>();
        encoded.add(Base64.encodeToString(video, Base64.NO_WRAP));
        StringListDataModel encodedVideoModel = new StringListDataModel();
        encodedVideoModel.setData(encoded);
        return encodedVideoModel;
    }

    private void sendFaceCaptures(StringListDataModel captures, FaceActions faceAction, FaceCapturesCallback faceCapturesCallback) throws InternalException, ConnectException, SessionException {
        server.sendProvidedFaceCaptures(captures, faceCapturesCallback, faceAction);
    }

    public SessionModel getSession() {
        return server.getSession();
    }
}

