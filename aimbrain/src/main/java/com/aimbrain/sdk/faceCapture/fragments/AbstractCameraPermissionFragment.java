package com.aimbrain.sdk.faceCapture.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.aimbrain.sdk.array.Arrays;
import com.aimbrain.sdk.util.Logger;


public abstract class AbstractCameraPermissionFragment extends Fragment {

    public static final String TAG = AbstractCameraPermissionFragment.class.getSimpleName();

    public static final int PERMISSIONS_REQUEST_CREATE = 2211;
    public static final int PERMISSIONS_REQUEST_RESUME = 2212;
    public static final int PERMISSIONS_REQUEST_CAMERA_BUTTON = 2213;

    private boolean requestPermissionPending;

    protected ActivityCallback mListener;

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

    protected boolean requestPermissionsNeeded(int requestCode) {
        if (!permissionsGranted(getRequestedPermissions(requestCode))) {
            if (!requestPermissionPending) {
                requestPermissionPending = true;
                ActivityCompat.requestPermissions(getActivity(),
                        getRequestedPermissions(requestCode),
                        requestCode);
            }
            return true;
        }
        return false;
    }

    protected String[] getRequestedPermissions(int requestCode) {
        return new String[]{Manifest.permission.CAMERA};
    }

    protected boolean permissionsGranted(String[] requestedPermissions) {
        for (String requestedPermission : requestedPermissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), requestedPermission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CREATE:
            case PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0
                        && !Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    requestPermissionPending = false;
                    if (requestCode == PERMISSIONS_REQUEST_RESUME) {
                        onPermissionRequestResume();
                    } else if (requestCode == PERMISSIONS_REQUEST_CREATE) {
                        onPermissionRequestCreate();
                    }
                } else {
                    mListener.displayErrorAndFinish("Face authentication needs requested permissions granted.");
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
