package com.aimbrain.sdk.faceCapture.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.aimbrain.sdk.array.Arrays;
import com.aimbrain.sdk.util.Logger;


public abstract class BaseCameraFragment extends Fragment {
    public static final String TAG = BaseCameraFragment.class.getSimpleName();

    protected static final int CAPTURE_TARGET_FPS = 24;
    protected static final int CAPTURE_MAX_FPS = 30;
    protected static final int VIDEO_FPS = 30;
    protected static final int VIDEO_BIT_RATE = 502000;
    protected static final int AUDIO_BIT_RATE = 128000;
    protected static final int AUDIO_SAMPLE_RATE = 22050;

    protected static final int DEFAULT_START_DELAY_WITH_AUDIO = 800;
    protected static final int DEFAULT_DURATION_WITH_AUDIO = 2500;
    protected static final int DEFAULT_DURATION = 2000;

    public static final String EXTRA_CAPTURE_AUDIO = "captureAudio";
    public static final String EXTRA_DURATION_MILLIS = "durationMillis";

    public static final int PERMISSIONS_REQUEST_CREATE = 2211;
    public static final int PERMISSIONS_REQUEST_RESUME = 2212;
    public static final int PERMISSIONS_REQUEST_CAMERA_BUTTON = 2213;

    private boolean requestPermissionPending;
    protected ActivityCallback mListener;

    protected int getAudioPrepareDelayParam() {
        boolean hasAudio = getArguments().getBoolean(EXTRA_CAPTURE_AUDIO, false);
        return hasAudio ? DEFAULT_START_DELAY_WITH_AUDIO : 0;
    }

    protected int getRecordingDurationParam() {
        boolean hasAudio = getArguments().getBoolean(EXTRA_CAPTURE_AUDIO, false);
        int defaultDuration = hasAudio ? DEFAULT_DURATION_WITH_AUDIO : DEFAULT_DURATION;
        return getArguments().getInt(EXTRA_DURATION_MILLIS, defaultDuration);
    }

    protected boolean hasAudioParam() {
        return getArguments().getBoolean(EXTRA_CAPTURE_AUDIO);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionPending = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof ActivityCallback)) {
            throw new IllegalArgumentException("Activity must implement ActivityCallBack");
        }
        Logger.d(TAG, "onAttach");
        mListener = (ActivityCallback) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Logger.d(TAG, "onAttach deprecated");
        if (!(activity instanceof ActivityCallback)) {
            throw new IllegalArgumentException("Activity must implement ActivityCallBack");
        }
        mListener = (ActivityCallback) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_RESUME)) {
            onPermissionRequestResume();
        }
    }

    protected boolean requestPermissionsNeeded(int code) {
        if (allPermissionsGranted(getRequestedPermissions(code))) {
            return false;
        }
        if (!requestPermissionPending) {
            requestPermissionPending = true;
            ActivityCompat.requestPermissions(getActivity(), getRequestedPermissions(code), code);
        }
        return true;
    }

    protected String[] getRequestedPermissions(int code) {
        boolean hasAudio = hasAudioParam();
        if (hasAudio) {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        }
        else {
            return new String[]{Manifest.permission.CAMERA};
        }
    }

    protected boolean allPermissionsGranted(String[] requestedPermissions) {
        for (String permission : requestedPermissions) {
            int status = ContextCompat.checkSelfPermission(getActivity(), permission);
            if (status != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CREATE:
            case PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0 && !Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    requestPermissionPending = false;
                    if (requestCode == PERMISSIONS_REQUEST_RESUME) {
                        onPermissionRequestResume();
                    } else if (requestCode == PERMISSIONS_REQUEST_CREATE) {
                        onPermissionRequestCreate();
                    }
                } else {
                    mListener.displayErrorAndFinish("Requested camera permissions are not granted.");
                }
            }
        }
    }

    public abstract void onPermissionRequestCreate();

    public abstract void onPermissionRequestResume();

    public interface ActivityCallback {
        void displayErrorAndFinish(String error);
        void setResultAndFinish(byte[] videoBytes);
    }
}
