package com.aimbrain.sdk.faceCapture.fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.faceCapture.views.CameraUiView;
import com.aimbrain.sdk.faceCapture.views.FixedAspectSurfaceView;
import com.aimbrain.sdk.faceCapture.views.RecordButton;
import com.aimbrain.sdk.faceCapture.views.FaceFinderSurfaceView;
import com.aimbrain.sdk.faceCapture.helpers.LayoutUtil;
import com.aimbrain.sdk.faceCapture.helpers.LegacyResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.ResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.VideoSize;
import com.aimbrain.sdk.file.Files;
import com.aimbrain.sdk.util.Logger;

import java.io.IOException;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 *
 */
@SuppressWarnings("deprecation")
public class CameraLegacyFragment extends BaseCameraFragment {
    public static final String TAG = CameraLegacyFragment.class.getSimpleName();

    protected ViewGroup mRootView;
    protected CameraUiView mCameraUiView;
    protected FixedAspectSurfaceView previewSurfaceView;
    protected FaceFinderSurfaceView faceFinderSurfaceView;
    protected SurfaceHolder previewHolder;
    protected SurfaceHolder overlayHolder;
    protected MediaRecorder mediaRecorder;
    protected Camera camera;
    protected boolean inPreview;
    protected VideoSize screenSize;

    public static CameraLegacyFragment newInstance(int duration, boolean captureAudio) {
        CameraLegacyFragment fragment = new CameraLegacyFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_DURATION_MILLIS, duration);
        args.putBoolean(EXTRA_CAPTURE_AUDIO, captureAudio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_camera_legacy, container, false);

        final Activity parent = getActivity();
        if (parent == null) {
            throw new IllegalStateException("no parent");
        }
        if (!(parent instanceof CameraUiViewProvider)) {
            throw new IllegalStateException("parent does not provide overlay");
        }

        CameraUiViewProvider overlayProvider = (CameraUiViewProvider) parent;
        mCameraUiView = overlayProvider.createUiView(parent);
        mCameraUiView.setOnRecordClickListener(new CameraUiView.OnRecordClickListener() {
            @Override
            public void onRecordClick(RecordButton view) {
                photoButtonPressed(view);
            }
        });

        mCameraUiView.setOnFaceFinderResizeListener(new CameraUiView.OnFaceFinderResizeListener() {
            @Override
            public void onFaceFinderResize(int width, int height) {
                faceFinderSurfaceView.setFinderSize(width, height);
            }
        });

        mRootView.addView(mCameraUiView, new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return mRootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Logger.d(TAG, "onViewCreated()");
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
        releaseMediaRecorder();
        releaseCamera();
    }

    protected void setupCamera() {
        try {
            camera = Camera.open(getFrontCameraIndex());
            setupSurfaceViewsSize();
        } catch (RuntimeException e) {
            Logger.w(TAG, "setup camera", e);
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
            Logger.d(TAG, screenSize.toString());
            ResolutionPicker resolution = getResolutionPicker(camera.getParameters());
            VideoSize previewSize = resolution.getPreviewSize(screenSize.width, screenSize.height);
            Logger.d(TAG, "camera preview size set to " + previewSize.width + "x" + previewSize.height);

            float sizeRatio = (float) previewSize.width / (float) previewSize.height;
            previewSurfaceView.setAspectRatio(previewSize.height, previewSize.width);
            previewHolder.setFixedSize(screenSize.width, (int) (screenSize.width * sizeRatio));

            if (faceFinderSurfaceView != null && overlayHolder != null) {
                faceFinderSurfaceView.setAspectRatio(previewSize.height, previewSize.width);
                overlayHolder.setFixedSize(screenSize.width, (int) (screenSize.width * sizeRatio));
            }

            mCameraUiView.setCameraPosition(0, 0, screenSize.width, (int) (screenSize.width * sizeRatio));
        }
    }

    private void createWithPermissions() {
        setupCameraPreview();
    }

    private void setupCameraPreview() {
        previewSurfaceView = (FixedAspectSurfaceView) mRootView.findViewById(R.id.faceCaptureSurface);
        previewHolder = previewSurfaceView.getHolder();
        previewHolder.addCallback(surfaceCallback);

        faceFinderSurfaceView = (FaceFinderSurfaceView) mRootView.findViewById(R.id.overlaySurfaceView);
        overlayHolder = faceFinderSurfaceView.getHolder();
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                if (camera != null) {
                    camera.setDisplayOrientation(getCameraDisplayOrientation(getFrontCameraIndex()));
                    camera.setPreviewDisplay(previewHolder);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setRecordingHint(true);

                    ResolutionPicker resolutionPicker = getResolutionPicker(parameters);

                    VideoSize preview = resolutionPicker.getPreviewSize(width, height);
                    if (preview != null) {
                        Logger.d(TAG, "surface changed, best size " + preview.width + "x" + preview.height);

                        previewSurfaceView.setAspectRatio(preview.height, preview.width);
                        if (faceFinderSurfaceView != null) {
                            faceFinderSurfaceView.setAspectRatio(preview.height, preview.width);
                        }
                        float sizeRatio = (float) preview.width / (float) preview.height;
                        mCameraUiView.setCameraPosition(0, 0, screenSize.width, (int) (screenSize.width * sizeRatio));

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

                        Logger.v(TAG, "Camera parameters before preview:");
                        for (String param : parameters.flatten().split(";")) {
                            Logger.v(TAG, param);
                        }
                        camera.startPreview();
                        inPreview = true;
                    }
                }
            } catch (Throwable t) {
                Logger.e(TAG, "Exception in setPreviewDisplay()", t);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
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
        Logger.i(TAG, "Chosen picture size: " + pictureSize.width + "x" + pictureSize.height);
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
        Logger.d(TAG, "result " + result);
        return result;
    }

    MediaRecorder.OnInfoListener mediaRecorderInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                releaseMediaRecorder();
                mCameraUiView.setRecordEnded();
                try {
                    byte[] video = Files.readAllBytes(getActivity().openFileInput(Files.TMP_VIDEO_FILE_NAME));
                    mListener.setResultAndFinish(video);
                } catch (IOException e) {
                    mListener.displayErrorAndFinish("Unable to read saved video file.");
                }

            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                releaseMediaRecorder();
                mCameraUiView.setRecordEnded();
                mListener.displayErrorAndFinish("Maximum video file size reached.");
            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
                releaseMediaRecorder();
                mCameraUiView.setRecordEnded();
                mListener.displayErrorAndFinish("Unknown error.");
            } else {
                Logger.d(TAG, "unreckognized onInfo, what = " + what + " extra = " + extra);
            }
        }

    };

    MediaRecorder.OnErrorListener mediaRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Logger.d(TAG, "onError, what = " + what + " extra = " + extra);
            releaseMediaRecorder();
            mListener.displayErrorAndFinish("Unable to record video.");
        }
    };

    protected void captureData() {
        captureVideo();
    }

    private void captureVideo() {
        int durationMillis = getRecordingDurationParam();
        int prepareMillis = getAudioPrepareDelayParam();
        mCameraUiView.setRecordStarted(prepareMillis, durationMillis);
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
        updateCaptureRate(CAPTURE_TARGET_FPS, CAPTURE_MAX_FPS, cameraParameters);

        mediaRecorder = new MediaRecorder();

        camera.unlock();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        boolean hasAudio = hasAudioParam();
        if (hasAudio) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        }
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoFrameRate(VIDEO_FPS);
        mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        mediaRecorder.setVideoEncodingBitRate(VIDEO_BIT_RATE);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (hasAudio) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE);
            mediaRecorder.setAudioEncodingBitRate(AUDIO_BIT_RATE);
        }

        try {
            mediaRecorder.setOutputFile(getActivity().openFileOutput(Files.TMP_VIDEO_FILE_NAME, Context.MODE_PRIVATE).getFD());
        } catch (IOException e) {
            return false;
        }

        int durationMillis = getRecordingDurationParam() + getAudioPrepareDelayParam();
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

    @Override
    public void onPermissionRequestCreate() {
        createWithPermissions();
    }

    @Override
    public void onPermissionRequestResume() {
        if (previewSurfaceView == null) {
            setupCameraPreview();
        }
        setupCamera();
    }
}
