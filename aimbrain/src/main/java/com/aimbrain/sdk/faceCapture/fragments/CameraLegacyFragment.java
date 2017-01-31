package com.aimbrain.sdk.faceCapture.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.faceCapture.AutoFitSurfaceView;
import com.aimbrain.sdk.faceCapture.OverlaySurfaceView;
import com.aimbrain.sdk.faceCapture.VideoFaceCaptureActivity;
import com.aimbrain.sdk.faceCapture.helpers.LayoutUtil;
import com.aimbrain.sdk.faceCapture.helpers.LegacyResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.ResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.VideoSize;
import com.aimbrain.sdk.faceCapture.views.RecordButton;
import com.aimbrain.sdk.file.Files;

import java.io.IOException;
import java.util.List;

/**
 *
 */
@SuppressWarnings("deprecation")
public class CameraLegacyFragment extends AbstractCameraPermissionFragment {


    public static final String TAG = CameraLegacyFragment.class.getSimpleName();
    /**
     * specify video length in ms. Default is 2000
     */
    private static final int TARGET_VIDEO_FPS = 24;
    private static final int MAX_CONSTANT_VIDEO_FPS = 30;

    protected View mRootView;
    protected View mCameraOverlay;
    protected AutoFitSurfaceView previewSurfaceView;
    protected OverlaySurfaceView overlaySurfaceView;
    protected SurfaceHolder previewHolder;
    protected SurfaceHolder overlayHolder;
    protected MediaRecorder mediaRecorder;
    protected Camera camera;
    protected boolean inPreview;
    protected LayoutInflater controlInflater;
    protected VideoSize screenSize;
    protected TextSwitcher lowerTextSwitcher;
    protected TextView upperTextView;
    protected RelativeLayout lowerTextRelativeLayout;
    protected RelativeLayout upperTextRelativeLayout;
    protected View recordButton;
    protected ProgressBar progressBar;


    public static CameraLegacyFragment newInstance(String upperText, String lowerText, String recordingHint,
                                                   int duration) {
        CameraLegacyFragment fragment = new CameraLegacyFragment();
        Bundle args = new Bundle();
        args.putString(VideoFaceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
        args.putString(VideoFaceCaptureActivity.EXTRA_LOWER_TEXT, lowerText);
        args.putString(VideoFaceCaptureActivity.EXTRA_RECORDING_HINT, recordingHint);
        args.putInt(VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_camera_legacy, container, false);
        /*
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateSurfaceMarginsToFillParent();
            }
        });
        */
        return mRootView;
    }

    /**
     * Adds negative margins so surface view always covers parent view. This is needed
     * when surface view does not match screen aspect ratio
     */
    private void updateSurfaceMarginsToFillParent() {
        if (previewSurfaceView == null || overlaySurfaceView == null) {
            return;
        }
        if (previewSurfaceView.getMeasuredWidth() == 0 || previewSurfaceView.getMeasuredHeight() == 0) {
            return;
        }
        if (!previewSurfaceView.hasValidAspectRatio()) {
            return;
        }

        int parentHeight = mRootView.getMeasuredHeight();
        int parentWidth = mRootView.getMeasuredWidth();

        float parentRatio = parentWidth / (float) parentHeight;
        float textureRatio = previewSurfaceView.getAspectRatio();

        float marginH = 0;
        float marginV = 0;

        if (parentRatio < textureRatio) {
            // texture is wider, margins on sides
            float expectedWidth = parentHeight * textureRatio;
            marginH = parentWidth - expectedWidth;
        } else {
            // texture is higher, margins on top and bottom
            float expectedHeight = parentWidth / textureRatio;
            marginV = parentHeight - expectedHeight;
        }

        RelativeLayout.LayoutParams surfaceLp = (RelativeLayout.LayoutParams) previewSurfaceView.getLayoutParams();
        surfaceLp.topMargin = (int) (marginV / 2.0);
        surfaceLp.bottomMargin = (int) (marginV / 2.0);
        surfaceLp.leftMargin = (int) (marginH / 2.0);
        surfaceLp.rightMargin = (int) (marginH / 2.0);
        previewSurfaceView.setLayoutParams(surfaceLp);

        FrameLayout.LayoutParams overlayLp = (FrameLayout.LayoutParams) overlaySurfaceView.getLayoutParams();
        overlayLp.topMargin = (int) (marginV / 2.0);
        overlayLp.bottomMargin = (int) (marginV / 2.0);
        overlayLp.leftMargin = (int) (marginH / 2.0);
        overlayLp.rightMargin = (int) (marginH / 2.0);
        overlaySurfaceView.setLayoutParams(overlayLp);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE)) {
            createWithPermissions();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        screenSize = LayoutUtil.getScreenSize(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        releaseMediaRecorder();
    }

    protected void setupCamera() {
        try {
            camera = Camera.open(getFrontCameraIndex());
            setupSurfaceViewsSize();
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (camera != null) {
                camera.release();
                camera = null;
            }
            mListener.displayErrorAndFinish("Camera unavailable.");
        }
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
            Log.d(TAG, screenSize.toString());
            ResolutionPicker resolution = getResolutionPicker(camera.getParameters());
            VideoSize previewSize = resolution.getPreviewSize(screenSize.width, screenSize.height);
            Log.d(TAG, "camera preview size set to " + previewSize.width + "x" + previewSize.height);

            float sizeRatio = (float) previewSize.width / (float) previewSize.height;

            previewSurfaceView.setAspectRatio(previewSize.height, previewSize.width);
            previewHolder.setFixedSize(screenSize.width, (int) (screenSize.width * sizeRatio));

            if (overlaySurfaceView != null && overlayHolder != null) {
                overlaySurfaceView.setAspectRatio(previewSize.height, previewSize.width);
                overlayHolder.setFixedSize(screenSize.width, (int) (screenSize.width * sizeRatio));
            } else {
                final Activity activity = getActivity();
                if (null != activity && activity instanceof LayoutOverlayObserver) {
                    ((LayoutOverlayObserver) activity)
                            .setOverlayViewDimensions(previewSize.height, previewSize.width);
                }
            }
        }
    }

    private void createWithPermissions() {
        setupOverlay();
        setupCameraPreview();
    }

    private void setupOverlay() {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing() ||
                !(activity instanceof LayoutOverlayObserver)) {
            return;
        }

        LayoutOverlayObserver observer = (LayoutOverlayObserver) activity;

        mCameraOverlay = observer.getLayoutOverlayView();
        if (mCameraOverlay == null) {
            controlInflater = LayoutInflater.from(activity.getBaseContext());
            mCameraOverlay = controlInflater.inflate(R.layout.camera_overlay, null);
            overlaySurfaceView = (OverlaySurfaceView) mCameraOverlay.findViewById(R.id.overlaySurfaceView);
            overlayHolder = overlaySurfaceView.getHolder();
            overlayHolder.addCallback(overlayCallback);

            upperTextRelativeLayout = (RelativeLayout) mCameraOverlay.findViewById(R.id.upperTextRelativeLayout);
            upperTextView = (TextView) mCameraOverlay.findViewById(R.id.upperTextView);
            lowerTextRelativeLayout = (RelativeLayout) mCameraOverlay.findViewById(R.id.lowerTextRelativeLayout);
            lowerTextSwitcher = (TextSwitcher) mCameraOverlay.findViewById(R.id.lowerTextSwitcher);

            TextView lowerTextView = new TextView(activity);
            lowerTextView.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
            lowerTextView.setGravity(Gravity.CENTER);
            lowerTextView.setTextColor(Color.WHITE);
            lowerTextView.setMaxLines(2);
            lowerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            lowerTextView.setEllipsize(TextUtils.TruncateAt.END);

            TextView recordingHintTextView = new TextView(activity);
            recordingHintTextView.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
            recordingHintTextView.setGravity(Gravity.CENTER);
            recordingHintTextView.setTextColor(Color.WHITE);
            recordingHintTextView.setMaxLines(2);
            recordingHintTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            recordingHintTextView.setEllipsize(TextUtils.TruncateAt.END);

            lowerTextSwitcher.addView(recordingHintTextView);
            lowerTextSwitcher.addView(lowerTextView);
            lowerTextSwitcher.setInAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_in));

            String upperText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_UPPER_TEXT);
            String lowerText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_LOWER_TEXT);
            setupOverlayTexts(upperText, lowerText);

            progressBar = (ProgressBar) mCameraOverlay.findViewById(R.id.photoProgressBar);
            progressBar.setVisibility(View.GONE);
            recordButton = mCameraOverlay.findViewById(R.id.photoButton);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoButtonPressed(v);
                }
            });
        }

        if (recordButton == null) {
            recordButton = getView().findViewById(R.id.buttonRecord);
            recordButton.setVisibility(View.VISIBLE);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoButtonPressed(v);
                }
            });
        }

        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ((ViewGroup) getView().getParent()).addView(mCameraOverlay, layoutParamsControl);
    }

    private void setupOverlayTexts(String upperText, String lowerText) {
        if (upperText != null) {
            upperTextView.setText(upperText);
            upperTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        if (lowerText == null) {
            lowerText = "";
        }
        if (lowerTextSwitcher != null) {
            lowerTextSwitcher.setText(lowerText);
        }
    }

    private void setupCameraPreview() {
        View v = getView();
        previewSurfaceView = (AutoFitSurfaceView) v.findViewById(R.id.faceCaptureSurface);
        previewHolder = previewSurfaceView.getHolder();
        previewHolder.addCallback(surfaceCallback);
    }


    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {

        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            refreshOverlayElements();

            try {
                if (camera != null) {
                    camera.setDisplayOrientation(getCameraDisplayOrientation(getFrontCameraIndex()));
                    camera.setPreviewDisplay(previewHolder);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setRecordingHint(true);

                    ResolutionPicker resolutionPicker = getResolutionPicker(parameters);

                    VideoSize preview = resolutionPicker.getPreviewSize(width, height);
                    if (preview != null) {
                        Log.d(TAG, "surface changed, best size " + preview.width + "x" + preview.height);

                        previewSurfaceView.setAspectRatio(preview.height, preview.width);
                        if (overlaySurfaceView != null) {
                            overlaySurfaceView.setAspectRatio(preview.height, preview.width);
                        } else {
                            final Activity activity = getActivity();
                            if (null != activity && activity instanceof LayoutOverlayObserver) {
                                ((LayoutOverlayObserver) activity)
                                        .setOverlayViewDimensions(preview.height, preview.width);
                            }
                        }

                        Camera.Size pictureSize = getPictureSize(preview, parameters);
                        parameters.setPreviewSize(preview.width, preview.height);
                        parameters.setPictureSize(pictureSize.width, pictureSize.height);

                        /* Set camera parameter "video-size" to prevent distorted video on some
                           device when setting recording-hint=true. Check if value is in allowed
                           resolution list first */
                        VideoSize videoSize = resolutionPicker.getRecordSize();
                        String videoSizeValue = videoSize.width + "x" + videoSize.height;
                        String allowedVideoSizeValues = parameters.get("video-size-values");
                        if (allowedVideoSizeValues == null || allowedVideoSizeValues.contains(videoSizeValue)) {
                            parameters.set("video-size", videoSizeValue);
                        }

                        camera.setParameters(parameters);

                        Log.v(TAG, "Camera parameters before preview:");
                        for (String param : parameters.flatten().split(";")) {
                            Log.v(TAG, param);
                        }
                        camera.startPreview();
                        inPreview = true;
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    SurfaceHolder.Callback overlayCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {

        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            refreshOverlayElements();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    private Camera.Size getPictureSize(VideoSize previewSize, Camera.Parameters parameters) {
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size pictureSize = supportedPictureSizes.get(0);
        float requiredRatio = (float) previewSize.width / (float) previewSize.height;
        float bestFoundRatio = (float) pictureSize.width / (float) pictureSize.width;

        for (Camera.Size currentSize : supportedPictureSizes) {
            float currentRatio = (float) currentSize.width / (float) currentSize.height;
            if (Math.abs(currentRatio - requiredRatio) < Math.abs(currentRatio - bestFoundRatio) ||
                    (Math.abs(currentRatio - requiredRatio) == Math.abs(currentRatio - bestFoundRatio) &&
                            currentSize.width >= pictureSize.width &&
                            currentSize.height >= pictureSize.width)) {
                pictureSize = currentSize;
                bestFoundRatio = currentRatio;
            }
        }
        Log.i("PICTURE SIZE", "Chosen picture size: " + pictureSize.width + "x" + pictureSize.height);
        return pictureSize;
    }

    public void photoButtonPressed(View view) {
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CAMERA_BUTTON)) {
            resumePhotoButtonPressedWithPermissions(view);
        }
    }

    private void resumePhotoButtonPressedWithPermissions(View view) {
        view.setEnabled(false);
        view.setClickable(false);

        CharSequence recordingHint = getArguments().getString(VideoFaceCaptureActivity.EXTRA_RECORDING_HINT);
        if (recordingHint != null) {
            lowerTextSwitcher.setText(recordingHint);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        captureData();
    }

    protected int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
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
        Log.d("FaceCaptureActivity", "result " + result);
        return result;
    }

    private void notifyParentRecordingStopped() {
        if (recordButton instanceof RecordButton) {
            ((RecordButton) recordButton).stopRecording();
        }
        Activity activity = getActivity();
        if (activity instanceof LayoutOverlayObserver) {
            ((LayoutOverlayObserver) activity).onRecordingStopped();
        }
    }

    MediaRecorder.OnInfoListener mediaRecorderInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                releaseMediaRecorder();
                notifyParentRecordingStopped();
                try {
                    byte[] video = Files.readAllBytes(getActivity().openFileInput(Files.TMP_VIDEO_FILE_NAME));
                    mListener.setResultAndFinish(video);
                } catch (IOException e) {
                    mListener.displayErrorAndFinish("Unable to read saved video file.");
                }

            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                releaseMediaRecorder();
                notifyParentRecordingStopped();
                mListener.displayErrorAndFinish("Maximum video file size reached.");
            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
                releaseMediaRecorder();
                notifyParentRecordingStopped();
                mListener.displayErrorAndFinish("Unknown error.");
            } else {
                Log.d("FaceCaptureActivity", "unregkognized onInfo, what = " + what + " extra = " + extra);
            }
        }

    };

    MediaRecorder.OnErrorListener mediaRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d("FaceCaptureActivity", "onError, what = " + what + " extra = " + extra);
            releaseMediaRecorder();
            mListener.displayErrorAndFinish("Unable to record video.");
        }
    };

    protected void captureData() {
        captureVideo();
    }

    private void captureVideo() {
        if (recordButton instanceof RecordButton) {
            ((RecordButton) recordButton).startRecording();
        }
        Activity activity = getActivity();
        if (activity instanceof LayoutOverlayObserver) {
            ((LayoutOverlayObserver) activity).onRecordingStarted();
        }

        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
            mListener.displayErrorAndFinish("Camera not ready. Unable to start recording video.");
        }
    }

    private ResolutionPicker getResolutionPicker(Camera.Parameters parameters) {
        LegacyResolutionPicker picker = new LegacyResolutionPicker();
        List<Camera.Size> preview = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : preview) {
            picker.addPreviewSize(size.width, size.height);
        }

        List<Camera.Size> video = parameters.getSupportedVideoSizes();
        if (video != null) {
            for (Camera.Size size : video) {
                picker.addVideoSize(size.width, size.height);
            }
        }

        return picker;
    }

    private boolean prepareVideoRecorder() {
        Camera.Parameters cameraParameters = camera.getParameters();
        ResolutionPicker resolution = getResolutionPicker(cameraParameters);

        VideoSize videoSize = resolution.getRecordSize();
        updateCaptureRate(TARGET_VIDEO_FPS, MAX_CONSTANT_VIDEO_FPS, cameraParameters);

        mediaRecorder = new MediaRecorder();

        camera.unlock();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        mediaRecorder.setVideoEncodingBitRate(502000);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        try {
            mediaRecorder.setOutputFile(getActivity().openFileOutput(Files.TMP_VIDEO_FILE_NAME, Context.MODE_PRIVATE).getFD());
        } catch (IOException e) {
            return false;
        }

        int durationMillis = getArguments().getInt(VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS);
        mediaRecorder.setMaxDuration(durationMillis);
        mediaRecorder.setOrientationHint((360 - getCameraDisplayOrientation(getFrontCameraIndex())) % 360);

        mediaRecorder.setOnInfoListener(mediaRecorderInfoListener);
        mediaRecorder.setOnErrorListener(mediaRecorderErrorListener);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void updateCaptureRate(int targetFps, int maxConstantFps, Camera.Parameters cameraParameters) {
        List<int[]> fpsRanges = cameraParameters.getSupportedPreviewFpsRange();
        targetFps = targetFps * 1000;
        maxConstantFps = maxConstantFps * 1000;

        //last choice - first range from list
        int[] chosenFpsRange = fpsRanges.get(Camera.Parameters.PREVIEW_FPS_MIN_INDEX);

        //second worse choice - first range with min fps > videoFps
        for (int[] currentRange : fpsRanges) {
            if (currentRange[0] > targetFps) {
                chosenFpsRange = currentRange;
                break;
            }
        }

        //third worse choice - narrowest range containing targetFps
        for (int[] currentRange : fpsRanges) {
            if (currentRange[0] >= targetFps && currentRange[1] <= targetFps) {
                if (chosenFpsRange[0] >= targetFps && chosenFpsRange[1] <= targetFps) {
                    if (chosenFpsRange[1] - chosenFpsRange[0] > currentRange[1] - currentRange[0])
                        chosenFpsRange = currentRange;
                } else {
                    chosenFpsRange = currentRange;
                }
            }
        }

        //best choice - find constant rate between videoFpsMin and videoFpsMax
        for (int[] currentRange : fpsRanges) {
            if (currentRange[0] == currentRange[1] &&
                    currentRange[0] >= targetFps &&
                    currentRange[0] <= maxConstantFps) {
                chosenFpsRange = currentRange;
                break;
            }
        }

        cameraParameters.setPreviewFpsRange(chosenFpsRange[0], chosenFpsRange[1]);
        camera.setParameters(cameraParameters);
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

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            if (camera != null)
                camera.lock();
        }
    }

    public void refreshOverlayElements() {
        if (lowerTextRelativeLayout != null) {
            RelativeLayout.LayoutParams lowerTextLayoutParams = (RelativeLayout.LayoutParams) lowerTextRelativeLayout.getLayoutParams();
            lowerTextLayoutParams.height = getLowerTextHeight();
            lowerTextLayoutParams.setMargins(0, getLowerTextTopMargin(), 0, 0);
            lowerTextSwitcher.requestLayout();
        }

        if (upperTextView != null) {
            RelativeLayout.LayoutParams upperTextLayoutParams = (RelativeLayout.LayoutParams) upperTextView.getLayoutParams();
            upperTextLayoutParams.height = (int) overlaySurfaceView.getMaskBounds().top;
            upperTextView.requestLayout();
        }

        if (recordButton != null) {
            LinearLayout.LayoutParams photoButtonLayoutParams = (LinearLayout.LayoutParams) recordButton.getLayoutParams();

            int padding = getPhotoButtonBottomMargin();
            photoButtonLayoutParams.setMargins(0, 0, 0, padding);

            recordButton.setLayoutParams(photoButtonLayoutParams);
            recordButton.requestLayout();
            if (getActivity() instanceof LayoutOverlayObserver) {
                ((LayoutOverlayObserver) getActivity()).setRecordButtonPosition(
                        new Rect(recordButton.getLeft(), recordButton.getTop() - padding,
                                recordButton.getRight(), recordButton.getBottom() - padding));
            }
        }
    }

    private int getLowerTextTopMargin() {
        return (int) overlaySurfaceView.getMaskBounds().top + (int) overlaySurfaceView.getMaskBounds().height();
    }

    private int getLowerTextHeight() {
        return screenSize.height - (int) overlaySurfaceView.getMaskBounds().top - (int) overlaySurfaceView.getMaskBounds().height() - getLowerTextBottomMargin();
    }

    private int getLowerTextBottomMargin() {
        int margin_size = 10 + screenSize.height - overlayHolder.getSurfaceFrame().height();
        int photoButtonSpinnerTop = getPhotoButtonBottomMargin() + recordButton.getHeight() + 12;
        return margin_size > photoButtonSpinnerTop ? margin_size : photoButtonSpinnerTop;
    }

    private int getPhotoButtonBottomMargin() {
        if (!previewSurfaceView.hasValidAspectRatio()) {
            return 0;
        }
        int totalHeight = mRootView.getMeasuredHeight() - previewSurfaceView.getHeight();
        return  (totalHeight - recordButton.getHeight()) / 2;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @Override
    public void onPermissionRequestCreate() {
        createWithPermissions();
    }

    @Override
    public void onPermissionRequestResume() {
        if (previewSurfaceView == null) {
            setupCameraPreview();
        }
        if (mCameraOverlay == null) {
            setupOverlay();
            String upperText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_UPPER_TEXT);
            String lowerText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_LOWER_TEXT);
            setupOverlayTexts(upperText, lowerText);
        }
        setupCamera();
    }
}
