package com.aimbrain.sdk.faceCapture;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.array.Arrays;

/**
 * This activty allows to display camera preview. Subclasses may record video or capture images by
 * overrding <code>captureData</code> method.
 * The result can be obtained with use of <code>onActivityResult</code> method.
 */

public abstract class FaceCaptureActivity extends Activity {

    /**
     * specify text in upper hint view on camera overlay
     */
    public static final String EXTRA_UPPER_TEXT = "upperText";
    /**
     * specify text in lower hint view on camera overlay
     */
    public static final String EXTRA_LOWER_TEXT = "lowerText";
    /**
     * specify text in lower hint view on camera overlay while capturing face
     */
    public static final String RECORDING_HINT = "recordingHint";
    /**
    * Proportion of width for overlay and bounding box
    */
    public static final double BOX_WIDTH = 0.5;

    /**
    * Ration of height and width for overlay and bounding box
    */
    public static final double BOX_RATIO = 1.5;
    protected static final int PERMISSIONS_REQUEST_CREATE = 2211;
    protected static final int PERMISSIONS_REQUEST_RESUME = 2212;
    protected static final int PERMISSIONS_REQUEST_CAMERA_BUTTON = 2213;



    protected SurfaceView preview;
    protected SurfaceHolder previewHolder;
    protected SurfaceHolder overlaySurfaceHolder;
    protected Camera camera;
    protected boolean inPreview;
    protected LayoutInflater controlInflater;
    protected DisplayMetrics windowSize = new DisplayMetrics();
    protected OverlaySurfaceView overlaySurface;
    protected TextSwitcher lowerTextSwitcher;
    protected TextView upperTextView;
    protected RelativeLayout lowerTextRelativeLayout;
    protected RelativeLayout upperTextRelativeLayout;
    protected ImageButton captureButton;
    protected ProgressBar progressBar;
    protected String recordingHint;
    private boolean requestPermissionPending;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenParameters();
        setContentView(R.layout.activity_face_capture);
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(windowSize);
        requestPermissionPending = false;
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE)) {
            createActivityWithPermissions();
        }
    }

    protected boolean requestPermissionsNeeded(int requestCode) {
        if (!permissionsGranted(getRequestedPermissions(requestCode))) {
            if (!requestPermissionPending) {
                requestPermissionPending = true;
                ActivityCompat.requestPermissions(this,
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

    private void createActivityWithPermissions() {
        setupCameraPreview();
        setupOverlay();
        String upperText = getIntent().getStringExtra(EXTRA_UPPER_TEXT);
        String lowerText = getIntent().getStringExtra(EXTRA_LOWER_TEXT);
        recordingHint = getIntent().getStringExtra(RECORDING_HINT);
        setupOverlayTexts(upperText, lowerText);
    }

    protected boolean permissionsGranted(String[] requestedPermissions) {
        for(String requestedPermission : requestedPermissions)
        {
            if (ContextCompat.checkSelfPermission(this, requestedPermission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        if(!requestPermissionsNeeded(PERMISSIONS_REQUEST_RESUME)) {
            setupCamera();
        }

    }

    protected void setupCamera() {
        try {
            camera = Camera.open(getFrontCameraIndex());
            setupSurfaceViewsSize();
        } catch (RuntimeException e) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Camera unavailable.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            FaceCaptureActivity.this.finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CREATE:
            case PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0
                        && !Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    requestPermissionPending = false;
                    if(requestCode == PERMISSIONS_REQUEST_RESUME)
                        setupCamera();
                    else if(requestCode == PERMISSIONS_REQUEST_CREATE)
                        createActivityWithPermissions();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Face authentication needs requested permissions granted.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissionPending = false;
                                    setResult(RESULT_CANCELED);
                                    FaceCaptureActivity.this.finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                return;
            }

        }
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    protected void releaseCamera() {
        if (camera != null) {
            if (inPreview) {
                camera.stopPreview();
            }
            camera.release();
            camera = null;
        }
        inPreview = false;
    }

    protected void setupSurfaceViewsSize() {
        if (camera != null) {
            Camera.Size cameraSize = getBestPreviewSize(windowSize.widthPixels, windowSize.heightPixels, camera.getParameters());
            float sizeRatio = (float) cameraSize.width / (float) cameraSize.height;
            previewHolder.setFixedSize(windowSize.widthPixels, (int) (windowSize.widthPixels * sizeRatio));
            overlaySurfaceHolder.setFixedSize(windowSize.widthPixels, (int) (windowSize.widthPixels * sizeRatio));
        }
    }

    public void refreshOverlayElements() {
        RelativeLayout.LayoutParams lowerTextLayoutParams = (RelativeLayout.LayoutParams) lowerTextRelativeLayout.getLayoutParams();
        lowerTextLayoutParams.height = getLowerTextHeight();
        lowerTextLayoutParams.setMargins(0, getLowerTextTopMargin(), 0, 0);
        lowerTextSwitcher.requestLayout();
        RelativeLayout.LayoutParams upperTextLayoutParams = (RelativeLayout.LayoutParams) upperTextView.getLayoutParams();
        upperTextLayoutParams.height = (int) overlaySurface.getMaskBounds().top;
        upperTextView.requestLayout();
        RelativeLayout.LayoutParams photoButtonLayoutParams = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();
        photoButtonLayoutParams.setMargins(0, 0, 0, getPhotoButtonBottomMargin());
    }

    private int getLowerTextTopMargin() {
        return (int) overlaySurface.getMaskBounds().top + (int) overlaySurface.getMaskBounds().height();
    }

    private int getLowerTextHeight() {
        return windowSize.heightPixels - (int) overlaySurface.getMaskBounds().top - (int) overlaySurface.getMaskBounds().height() - getLowerTextBottomMargin();
    }

    private int getLowerTextBottomMargin() {
        int margin_size = 10 + windowSize.heightPixels - overlaySurfaceHolder.getSurfaceFrame().height();
        int photoButtonSpinnerTop = getPhotoButtonBottomMargin() + captureButton.getHeight() + 12;
        return margin_size > photoButtonSpinnerTop ? margin_size : photoButtonSpinnerTop;
    }

    private int getPhotoButtonBottomMargin() {
        return (int) (25.0f * (float) windowSize.heightPixels / 1280);
    }

    protected Integer getFrontCameraIndex() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return 0;
    }

    protected Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= height && size.height <= width) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    private void setupOverlay() {
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.camera_overlay, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        overlaySurface = (OverlaySurfaceView) findViewById(R.id.overlaySurfaceView);
        overlaySurfaceHolder = overlaySurface.getHolder();
        upperTextRelativeLayout = (RelativeLayout) findViewById(R.id.upperTextRelativeLayout);
        upperTextView = (TextView) findViewById(R.id.upperTextView);
        lowerTextRelativeLayout = (RelativeLayout) findViewById(R.id.lowerTextRelativeLayout);
        lowerTextSwitcher = (TextSwitcher) findViewById(R.id.lowerTextSwitcher);

        TextView lowerTextView = new TextView(this);
        lowerTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        lowerTextView.setGravity(Gravity.CENTER);
        lowerTextView.setTextColor(Color.WHITE);
        lowerTextView.setMaxLines(2);
        lowerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        lowerTextView.setEllipsize(TextUtils.TruncateAt.END);

        TextView recordingHintTextView = new TextView(this);
        recordingHintTextView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        recordingHintTextView.setGravity(Gravity.CENTER);
        recordingHintTextView.setTextColor(Color.WHITE);
        recordingHintTextView.setMaxLines(2);
        recordingHintTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        recordingHintTextView.setEllipsize(TextUtils.TruncateAt.END);

        lowerTextSwitcher.addView(recordingHintTextView);
        lowerTextSwitcher.addView(lowerTextView);
        lowerTextSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in));

        progressBar = (ProgressBar) this.findViewById(R.id.photoProgressBar);
        progressBar.setVisibility(View.GONE);
        captureButton = (ImageButton) findViewById(R.id.photoButton);
    }

    private void setupOverlayTexts(String upperText, String lowerText){
        if (upperText != null) {
            upperTextView.setText(upperText);
            upperTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        if (lowerText == null) {
            lowerText = "";
        }
        lowerTextSwitcher.setText(lowerText);
    }

    private void setupCameraPreview() {
        preview = (SurfaceView) findViewById(R.id.faceCaptureSurface);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
    }

    private void setScreenParameters() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {

        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            try {
                if (camera != null) {
                    camera.setDisplayOrientation(getCameraDisplayOrientation(getFrontCameraIndex()));
                    camera.setPreviewDisplay(previewHolder);
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = getBestPreviewSize(width, height,
                            parameters);
                    if (size != null) {
                        parameters.setPreviewSize(size.width, size.height);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        inPreview = true;
                    }
                }
            } catch (Throwable t) {
                Log.e("surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(FaceCaptureActivity.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    public void photoButtonPressed(View view) {
        if(!requestPermissionsNeeded(PERMISSIONS_REQUEST_CAMERA_BUTTON)) {
            resumePhotoButtonPressedWithPermissions(view);
        }

    }

    private void resumePhotoButtonPressedWithPermissions(View view) {
        ImageButton photoButton = (ImageButton) view;
        photoButton.setEnabled(false);
        photoButton.setClickable(false);
        if(recordingHint != null) {
            lowerTextSwitcher.setText(recordingHint);
        }
        progressBar.setVisibility(View.VISIBLE);
        captureData();
    }

    protected int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = FaceCaptureActivity.this.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    protected abstract void captureData();
}
