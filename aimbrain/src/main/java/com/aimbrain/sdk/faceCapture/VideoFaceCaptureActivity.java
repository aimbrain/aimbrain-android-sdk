package com.aimbrain.sdk.faceCapture;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.array.Arrays;
import com.aimbrain.sdk.faceCapture.fragments.Camera2Fragment;
import com.aimbrain.sdk.faceCapture.fragments.CameraUiViewProvider;
import com.aimbrain.sdk.faceCapture.fragments.CameraLegacyFragment;
import com.aimbrain.sdk.faceCapture.fragments.BaseCameraFragment;
import com.aimbrain.sdk.faceCapture.helpers.CameraChoiceStrategy;
import com.aimbrain.sdk.faceCapture.helpers.CameraChoiceStrategy.CameraChoice;
import com.aimbrain.sdk.faceCapture.views.CameraUiView;
import com.aimbrain.sdk.util.Logger;


public class VideoFaceCaptureActivity extends Activity
        implements BaseCameraFragment.ActivityCallback, CameraUiViewProvider {

    private static final String TAG = VideoFaceCaptureActivity.class.getSimpleName();

    /**
     * Recorded video duration.
     */
    public static final String EXTRA_DURATION_MILLIS = "durationMillis";

    /**
     * Upper hint text on camera overlay.
     */
    public static final String EXTRA_UPPER_TEXT = "upperText";

    /**
     * Lower hint text on camera overlay.
     */
    public static final String EXTRA_LOWER_TEXT = "lowerText";

    /**
     * Lower hint text on camera overlay shown while capturing face
     */
    public static final String EXTRA_RECORDING_HINT = "recordingHint";

    /**
     * Lower hint text on camera overlay shown while capturing face with audio
     */
    public static final String EXTRA_RECORDING_TOKEN_HINT = "tokenText";

    /**
     * Specify to force used camera api
     */
    public static final String EXTRA_FORCE_CAMERA_API = "forceCameraApi";

    /**
     * Value for EXTRA_FORCE_CAMERA_API to use Camera API.
     */
    public static final String EXTRA_CAMERA_LEGACY = CameraChoice.CAMERA_LEGACY.name();

    /**
     * Value for EXTRA_FORCE_CAMERA_API to use Camera2 API.
     */
    public static final String EXTRA_CAMERA2 = CameraChoice.CAMERA2.name();

    public static byte[] video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenParameters();
        setContentView(R.layout.activity_video_capture);

        int durationMillis = getIntent().getIntExtra(EXTRA_DURATION_MILLIS, 2000);
        boolean captureAudio = getIntent().getStringExtra(EXTRA_RECORDING_TOKEN_HINT) != null;

        CameraChoice choice = CameraChoiceStrategy.chooseCamera(getApplicationContext());

        String forceChoice = getIntent().getStringExtra(EXTRA_FORCE_CAMERA_API);
        if (forceChoice != null) {
            choice = CameraChoice.valueOf(forceChoice);
        }

        Fragment cameraFragment;
        if (choice == CameraChoice.CAMERA2) {
            cameraFragment = Camera2Fragment.newInstance(durationMillis, captureAudio);
        } else {
            cameraFragment = CameraLegacyFragment.newInstance(durationMillis, captureAudio);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content, cameraFragment)
                .commit();
    }

    private void setScreenParameters() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void displayErrorAndFinish(String error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        VideoFaceCaptureActivity.this.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void setResultAndFinish(byte[] videoBytes) {
        video = videoBytes;
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.i(TAG, "onRequestPermissionsResult in activity");
        switch (requestCode) {
            case BaseCameraFragment.PERMISSIONS_REQUEST_CREATE:
            case BaseCameraFragment.PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0
                        && Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    displayErrorAndFinish("Face authentication needs requested permissions granted.");
                }
            }
        }
    }

    @Override
    public CameraUiView createUiView(Context context) {
        Intent i = getIntent();
        String upperText = i.getStringExtra(EXTRA_UPPER_TEXT);
        String lowerText = i.getStringExtra(EXTRA_LOWER_TEXT);
        String recordHint = i.getStringExtra(EXTRA_RECORDING_HINT);
        String tokenText = i.getStringExtra(EXTRA_RECORDING_TOKEN_HINT);

        if (tokenText != null) {
            recordHint = tokenText;
        }

        CameraUiView uiView = new CameraUiView(context);
        uiView.setUpperText(upperText);
        uiView.setLowerText(lowerText);
        uiView.setRecordHintText(recordHint);
        uiView.setRecordProgressText("Capturing\n{0} s. left");
        return uiView;
    }
}
