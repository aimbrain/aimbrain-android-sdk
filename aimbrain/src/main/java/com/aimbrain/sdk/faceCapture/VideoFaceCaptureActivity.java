package com.aimbrain.sdk.faceCapture;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.aimbrain.sdk.file.Files;

import java.io.IOException;
import java.util.List;

/**
 * This activty allows to capture face videos.
 * The result can obtained in  <code>onActivityResult</code> method from  <code>VideoFaceCaptureActivity.video</code> static field.
 */
public class VideoFaceCaptureActivity extends FaceCaptureActivity {

    /**
     * specify video length in ms. Default is 2000
     */
    public static final String DURATION_MILLIS = "durationMillis";
    private static final int TARGET_VIDEO_FPS = 24;
    private static final int MAX_CONSTANT_VIDEO_FPS = 30;
    private static final int VIDEO_SIZE_HEIGHT = 288;
    private static final int VIDEO_SIZE_WIDTH = 352;
    public static byte[] video;
    private int durationMillis;
    private MediaRecorder mediaRecorder;


    MediaRecorder.OnInfoListener mediaRecorderInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            releaseMediaRecorder();
            if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                try {
                    video = Files.readAllBytes(openFileInput(Files.TMP_VIDEO_FILE_NAME));
                    setResult(RESULT_OK);
                    finish();
                } catch (IOException e) {
                    displayError("Unable to read saved video file.");
                }
            }
            else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
            {
                displayError("Maximum video file size reached.");
            }
            else
            {
                displayError("Unknown error.");
            }
        }

    };

    private void displayError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        VideoFaceCaptureActivity.this.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    MediaRecorder.OnErrorListener mediaRecorderErrorListener = new MediaRecorder.OnErrorListener() {

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            releaseMediaRecorder();
            displayError("Unable to record video.");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        durationMillis = getIntent().getIntExtra(DURATION_MILLIS, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseMediaRecorder();
    }

    @Override
    protected void captureData() {
        captureVideo();
    }

    private void captureVideo() {
        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
            displayError("Camera not ready. Unable to start recording video.");
        }
    }

    private boolean prepareVideoRecorder(){
        try {
            Camera.Parameters cameraParameters = camera.getParameters();
            Camera.Size optimalSize = getVideoSize(cameraParameters);
            updateCaptureRate(TARGET_VIDEO_FPS, MAX_CONSTANT_VIDEO_FPS, cameraParameters);

            mediaRecorder = new MediaRecorder();

            camera.unlock();
            mediaRecorder.setCamera(this.camera);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOrientationHint((360 - getCameraDisplayOrientation(getFrontCameraIndex())) % 360);
            mediaRecorder.setVideoSize(optimalSize.width, optimalSize.height);
            mediaRecorder.setMaxDuration(durationMillis);
            mediaRecorder.setVideoEncodingBitRate(502000);
            mediaRecorder.setOutputFile(openFileOutput(Files.TMP_VIDEO_FILE_NAME, Context.MODE_PRIVATE).getFD());
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setOnInfoListener(mediaRecorderInfoListener);
            mediaRecorder.setOnErrorListener(mediaRecorderErrorListener);
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
        int [] chosenFpsRange = fpsRanges.get(Camera.Parameters.PREVIEW_FPS_MIN_INDEX);

        //second worse choice - first range with min fps > videoFps
        for(int[] currentRange : fpsRanges)
        {
            if(currentRange[0] > targetFps) {
                chosenFpsRange = currentRange;
                break;
            }
        }

        //third worse choice - narrowest range containing targetFps
        for(int[] currentRange : fpsRanges) {
            if(currentRange[0] >= targetFps && currentRange[1] <= targetFps) {
                if(chosenFpsRange[0] >= targetFps && chosenFpsRange[1] <= targetFps) {
                    if(chosenFpsRange[1] - chosenFpsRange[0] > currentRange[1] - currentRange[0])
                        chosenFpsRange = currentRange;
                }
                else {
                    chosenFpsRange = currentRange;
                }
            }
        }

        //best choice - find constant rate between videoFpsMin and videoFpsMax
        for(int[] currentRange : fpsRanges) {
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

    @Override
    protected String[] getRequestedPermissions(int requestCode) {
        return new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            if(camera != null)
                camera.lock();
        }
    }

    private Camera.Size getVideoSize(Camera.Parameters cameraParameters) {
        List<Camera.Size> previewsizes = cameraParameters.getSupportedPreviewSizes();
        List<Camera.Size> videosizes = cameraParameters.getSupportedVideoSizes();
        if(videosizes != null)
            return(getClosestSize(videosizes, VIDEO_SIZE_HEIGHT, VIDEO_SIZE_WIDTH));
        return getClosestSize(previewsizes, VIDEO_SIZE_HEIGHT, VIDEO_SIZE_WIDTH);
    }

    private Camera.Size getClosestSize(List<Camera.Size> sizes, int height, int width) {
        Camera.Size closestSize = sizes.get(0);
        int searchedArea = height * width;
        int closestSizeArea = closestSize.height * closestSize.width;
        for(Camera.Size currentSize : sizes)
        {
            if (currentSize.width == width && currentSize.height == height)
            {
                closestSize = currentSize;
                break;
            }
            int currentSizeArea = currentSize.height * currentSize.width;
            if (Math.abs(currentSizeArea - searchedArea) > Math.abs(closestSizeArea - searchedArea))
                closestSize = currentSize;
            closestSizeArea = closestSize.height * closestSize.width;
        }
        return closestSize;
    }
}
