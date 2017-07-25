package com.aimbrain.sdk.faceCapture.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.faceCapture.AutoFitTextureView;
import com.aimbrain.sdk.faceCapture.VideoFaceCaptureActivity;
import com.aimbrain.sdk.faceCapture.helpers.Camera2ResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.CameraChoiceStrategy;
import com.aimbrain.sdk.faceCapture.helpers.VideoSize;
import com.aimbrain.sdk.views.ProgressRecordButtonView;
import com.aimbrain.sdk.file.Files;
import com.aimbrain.sdk.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Fragment extends AbstractCameraPermissionFragment {

    public static final String TAG = Camera2Fragment.class.getSimpleName();

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * Root fragment view.
     */
    private View mRootView;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A refernce to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
            updateButtonPosition();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);

            updateButtonPosition();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };


    void updateButtonPosition() {
        if (getActivity() instanceof LayoutOverlayObserver) {
            ((LayoutOverlayObserver) getActivity()).setRecordButtonPosition(
                    new Rect(recordButton.getLeft(), recordButton.getTop(),
                            recordButton.getRight(), recordButton.getBottom()));
        }
    }
    /**
     * The {@link Size} of camera preview.
     */
    private VideoSize mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private VideoSize mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;


    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * Current recording configuration
     */
    private Integer mSensorOrientation;
    private CaptureRequest.Builder mPreviewBuilder;


    //overlay
    private TextSwitcher mLowerTextSwitcher;
    private TextView mUpperTextView;
    private ProgressBar mProgressBar;
    private View recordButton;

    public static Camera2Fragment newInstance(String upperText, String lowerText, String recordingHint,
                                              int duration) {
        Camera2Fragment fragment = new Camera2Fragment();
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
        mRootView = inflater.inflate(R.layout.fragment_camera2, container, false);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateTextureMarginsToFillParent();
                ViewTreeObserver vto = mRootView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    vto.removeGlobalOnLayoutListener(this);
                } else {
                    vto.removeOnGlobalLayoutListener(this);
                }
            }
        });
        return mRootView;
    }

    /**
     * Adds negative margins so texture view always fullt covers parent view. This is needed
     * due to software action bar on some devices or no preview matching screen aspect ratio.
     */
    private void updateTextureMarginsToFillParent() {
        if (mTextureView == null) {
            return;
        }
        if (mTextureView.getMeasuredWidth() == 0 || mTextureView.getMeasuredHeight() == 0) {
            return;
        }
        if (!mTextureView.hasValidAspectRatio()) {
            return;
        }

        int parentHeight = mRootView.getMeasuredHeight();
        int parentWidth = mRootView.getMeasuredWidth();

        float parentRatio = parentWidth / (float) parentHeight;
        float textureRatio = mTextureView.getAspectRatio();

        float marginH = 0;
        float marginV = 0;

        if (parentRatio < textureRatio) {
            // texture is wider, margins on sides
            float expectedTextureWidth = parentHeight * textureRatio;
            marginH = parentWidth - expectedTextureWidth;
        } else {
            // texture is higher, margins on top and bottom
            float expectedTextureHeight = parentWidth / textureRatio;
            marginV = parentHeight - expectedTextureHeight;
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTextureView.getLayoutParams();
        lp.topMargin = (int) (marginV / 2.0);
        lp.bottomMargin = (int) (marginV / 2.0);
        lp.leftMargin = (int) (marginH / 2.0);
        lp.rightMargin = (int) (marginH / 2.0);
        mTextureView.setLayoutParams(lp);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE)) {
            createWithPermissions();
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void setupCamera() {
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void createWithPermissions() {
        setupOverlay();

    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (mBackgroundHandler != null) {
            mBackgroundThread.quitSafely();
        }
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera(int displayWidth, int displayHeight) {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            String cameraId = CameraChoiceStrategy.getPreferredCameraId(manager);
            Logger.d(TAG, "Using camera" + cameraId);

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            assert map != null;

            Camera2ResolutionPicker resolutionPicker = createResolutionPicker(map);
            mVideoSize = resolutionPicker.getRecordSize();
            mPreviewSize = resolutionPicker.getPreviewSize(displayWidth, displayHeight);
            mTextureView.setAspectRatio(mPreviewSize.height, mPreviewSize.width);
            configureTransform(displayWidth, displayHeight);
            updateTextureMarginsToFillParent();
            mMediaRecorder = new MediaRecorder();

            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            mListener.displayErrorAndFinish("Cannot access the camera.");
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            mListener.displayErrorAndFinish("Camera2 is not supported on this device");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    /**
     * Creates resolution picker for given configuration
     */
    @NonNull
    private Camera2ResolutionPicker createResolutionPicker(StreamConfigurationMap map) {
        Camera2ResolutionPicker config = new Camera2ResolutionPicker();
        for (Size size : map.getOutputSizes(MediaRecorder.class)) {
            config.addVideoSize(size.getWidth(), size.getHeight());
        }
        for (Size size : map.getOutputSizes(SurfaceTexture.class)) {
            config.addPreviewSize(size.getWidth(), size.getHeight());
        }
        return config;
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    mListener.displayErrorAndFinish("Camera device configure Failed");
                }

            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Logger.e(TAG, "Error starting preview", e);
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Logger.e(TAG, "Error updating preview", e);
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.height, mPreviewSize.width);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.height,
                    (float) viewWidth / mPreviewSize.width);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(activity.openFileOutput(Files.TMP_VIDEO_FILE_NAME, Context.MODE_PRIVATE).getFD());

        int durationMillis = getArguments().getInt(VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS);
        mMediaRecorder.setMaxDuration(durationMillis);
        mMediaRecorder.setVideoEncodingBitRate(502000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.setOnInfoListener(mediaRecorderInfoListener);
        mMediaRecorder.setOnErrorListener(mediaRecorderErrorListener);
        mMediaRecorder.prepare();
    }

    private void startRecordingVideo() {
        if(recordButton instanceof ProgressRecordButtonView) {
            int durationMillis = getArguments().getInt(VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS);
            ((ProgressRecordButtonView)recordButton).showRecordingStarted(durationMillis / 1000);
        }
        Activity activity = getActivity();
        if (activity instanceof LayoutOverlayObserver) {
            ((LayoutOverlayObserver) activity).onRecordingStarted();
        }
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface mRecorderSurface = mMediaRecorder.getSurface();
            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mListener.displayErrorAndFinish("Failed");

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            Logger.e(TAG, "Error starting recording", e);
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        if(recordButton instanceof ProgressRecordButtonView) {
            ((ProgressRecordButtonView) recordButton).showRecordingStopped();
        }
        Activity activity = getActivity();
        if (activity instanceof LayoutOverlayObserver) {
            ((LayoutOverlayObserver) activity).onRecordingStopped();
        }

        closePreviewSession();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    //overlay

    private void setupOverlay() {
        final Activity activity = getActivity();
        if (null == activity || !(activity instanceof LayoutOverlayObserver)) {
            return;
        }

        LayoutOverlayObserver observer = (LayoutOverlayObserver) activity;

        View overlayView = observer.getLayoutOverlayView();

        if (overlayView == null) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            overlayView = inflater.inflate(R.layout.camera2_overlay, null);

            mUpperTextView = (TextView) overlayView.findViewById(R.id.upperTextView);
            mLowerTextSwitcher = (TextSwitcher) overlayView.findViewById(R.id.lowerTextSwitcher);

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

            mLowerTextSwitcher.removeAllViews();
            mLowerTextSwitcher.addView(recordingHintTextView);
            mLowerTextSwitcher.addView(lowerTextView);
            mLowerTextSwitcher.setInAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_in));

            mProgressBar = (ProgressBar) overlayView.findViewById(R.id.photoProgressBar);
            mProgressBar.setVisibility(View.GONE);

            recordButton = overlayView.findViewById(R.id.photoButton);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoButtonPressed(v);
                }
            });


            String upperText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_UPPER_TEXT);
            String lowerText = getArguments().getString(VideoFaceCaptureActivity.EXTRA_LOWER_TEXT);
            setupOverlayTexts(upperText, lowerText);
        }


        RelativeLayout container = ((RelativeLayout) getView().findViewById(R.id.overlayViewContainer));
        container.removeAllViews();
        container.addView(overlayView);

        if(recordButton == null) {
            recordButton =  getView().findViewById(R.id.buttonRecord);
            recordButton.setVisibility(View.VISIBLE);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoButtonPressed(v);
                }
            });
        }
    }

    private void setupOverlayTexts(String upperText, String lowerText) {
        if (upperText != null) {
            mUpperTextView.setText(upperText);
            mUpperTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        if (lowerText == null) {
            lowerText = "";
        }
        mLowerTextSwitcher.setText(lowerText);
    }


    public void photoButtonPressed(View view) {
        Logger.d(TAG, "photoButtonPressed");
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CAMERA_BUTTON)) {
            resumePhotoButtonPressedWithPermissions(view);
        }
    }

    private void resumePhotoButtonPressedWithPermissions(View view) {
        view.setEnabled(false);
        view.setClickable(false);
        String recordingHint = getArguments().getString(VideoFaceCaptureActivity.EXTRA_RECORDING_HINT);
        if (mLowerTextSwitcher != null) {
            mLowerTextSwitcher.setText(recordingHint == null ? "" : recordingHint);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        startRecordingVideo();
    }

    MediaRecorder.OnInfoListener mediaRecorderInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            stopRecordingVideo();

            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                Logger.d(TAG, "record success ");
                try {
                    byte[] video = Files.readAllBytes(getActivity().openFileInput(Files.TMP_VIDEO_FILE_NAME));
                    Logger.d(TAG, "video bytes size " + video.length);
                    mListener.setResultAndFinish(video);
                } catch (IOException e) {
                    Logger.e(TAG, "Unable to read saved video file.");
                    mListener.displayErrorAndFinish("Unable to read saved video file.");
                }
            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                Logger.e(TAG, "Maximum video file size reached.");
                mListener.displayErrorAndFinish("Maximum video file size reached.");
            } else {
                Logger.e(TAG, "Unknown error.");
                mListener.displayErrorAndFinish("Unknown error.");
            }
        }
    };

    MediaRecorder.OnErrorListener mediaRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            //stopRecordingVideo();
            //todo release media recorder
            Logger.e(TAG, "Unable to record video.");
            mListener.displayErrorAndFinish("Unable to record video.");
        }
    };

    //permissions handling

    public void onPermissionRequestCreate() {
        createWithPermissions();
    }

    public void onPermissionRequestResume() {
        createWithPermissions();
        setupCamera();
        relayoutOverlay(); // permission prompt messes up overlay sizes, layout & remeasure overlay
    }

    private void relayoutOverlay() {
        final View view = getView();
        if (view != null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateButtonPosition();
                    ViewTreeObserver vto = view.getViewTreeObserver();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeGlobalOnLayoutListener(this);
                    } else {
                        vto.removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }
}
