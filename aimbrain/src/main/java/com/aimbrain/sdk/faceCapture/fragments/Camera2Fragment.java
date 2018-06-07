package com.aimbrain.sdk.faceCapture.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
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
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.faceCapture.views.FixedAspectTextureView;
import com.aimbrain.sdk.faceCapture.helpers.Camera2ResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.CameraChoiceStrategy;
import com.aimbrain.sdk.faceCapture.helpers.VideoSize;
import com.aimbrain.sdk.faceCapture.views.CameraUiView;
import com.aimbrain.sdk.faceCapture.views.RecordButton;
import com.aimbrain.sdk.faceCapture.views.FaceFinderView;
import com.aimbrain.sdk.file.Files;
import com.aimbrain.sdk.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Fragment extends BaseCameraFragment {
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
            layoutOverlay();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
            layoutOverlay();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
    };

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

    /**
     * UI views
     */
    private FixedAspectTextureView mTextureView;
    private FaceFinderView mFinderView;
    private RelativeLayout mRootView;
    private CameraUiView mCameraUiView;

    public static Camera2Fragment newInstance(int duration, boolean captureAudio) {
        Camera2Fragment fragment = new Camera2Fragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_DURATION_MILLIS, duration);
        args.putBoolean(EXTRA_CAPTURE_AUDIO, captureAudio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_camera2, container, false);
        mTextureView = (FixedAspectTextureView) mRootView.findViewById(R.id.texture);
        mFinderView = (FaceFinderView) mRootView.findViewById(R.id.finder_view);
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
                performRecordPressed(view);
            }
        });

        mCameraUiView.setOnFaceFinderResizeListener(new CameraUiView.OnFaceFinderResizeListener() {
            @Override
            public void onFaceFinderResize(int width, int height) {
                mFinderView.setFinderSize(width, height);
            }
        });

        mRootView.addView(mCameraUiView, new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
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

        RelativeLayout.LayoutParams flp = (RelativeLayout.LayoutParams) mFinderView.getLayoutParams();
        flp.topMargin = (int) (marginV / 2.0);
        flp.bottomMargin = (int) (marginV / 2.0);
        flp.leftMargin = (int) (marginH / 2.0);
        flp.rightMargin = (int) (marginH / 2.0);
        mFinderView.setLayoutParams(lp);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE);
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
            mFinderView.setAspectRatio(mPreviewSize.height, mPreviewSize.width);
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
        boolean hasAudio = getArguments().getBoolean(EXTRA_CAPTURE_AUDIO);

        if (hasAudio) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(activity.openFileOutput(Files.TMP_VIDEO_FILE_NAME, Context.MODE_PRIVATE).getFD());

        int durationMillis = getRecordingDurationParam() + getAudioPrepareDelayParam();

        mMediaRecorder.setMaxDuration(durationMillis);
        mMediaRecorder.setVideoEncodingBitRate(VIDEO_BIT_RATE);
        mMediaRecorder.setVideoFrameRate(VIDEO_FPS);
        mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (hasAudio) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioSamplingRate(AUDIO_SAMPLE_RATE);
            mMediaRecorder.setAudioEncodingBitRate(AUDIO_BIT_RATE);
        }
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
        int durationMillis = getRecordingDurationParam();
        int prepareMillis = getAudioPrepareDelayParam();
        mCameraUiView.setRecordStarted(prepareMillis, durationMillis);
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
        mCameraUiView.setRecordEnded();
        closePreviewSession();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private void layoutOverlay() {
        mCameraUiView.setCameraPosition(mTextureView.getLeft(), mTextureView.getTop(),
                mTextureView.getRight(), mTextureView.getBottom());
    }

    public void performRecordPressed(View view) {
        Logger.d(TAG, "performRecordPressed");
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CAMERA_BUTTON)) {
            resumePhotoButtonPressedWithPermissions(view);
        }
    }

    private void resumePhotoButtonPressedWithPermissions(View view) {
        view.setEnabled(false);
        view.setClickable(false);
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
                Logger.e(TAG, "Unknown error (" + what + ", " + extra + ")");
                mListener.displayErrorAndFinish("Unknown error (" + what + ", " + extra + ")");
            }
        }
    };

    MediaRecorder.OnErrorListener mediaRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Logger.e(TAG, "Unable to record video.");
            mListener.displayErrorAndFinish("Error to recording video (" + what + ", " + extra + ")");
        }
    };

    // permissions handling

    public void onPermissionRequestCreate() {
        layoutOverlay();
    }

    public void onPermissionRequestResume() {
        setupCamera();
        layoutOverlay();
    }
}
