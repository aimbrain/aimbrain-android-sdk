package com.aimbrain.sdk.voiceCapture;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.array.Arrays;
import com.aimbrain.sdk.util.Logger;
import com.aimbrain.sdk.views.ProgressRecordButtonView;
import com.aimbrain.sdk.voiceCapture.helpers.WaveFileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

/**
 *
 */
public class VoiceCaptureActivity extends Activity {

    public static final String TAG = VoiceCaptureActivity.class.getSimpleName();

    /**
     * specify text in upper hint view
     */
    public static final String EXTRA_UPPER_TEXT = "upperText";
    /**
     * specify text in middle hint view while capturing voice
     */
    public static final String EXTRA_RECORDING_HINT = "recordingHint";
    /**
     * specify text in middle hint view while capturing voice
     */
    public static final String EXTRA_RECORDING_DURATION_MS = "recordingDurationMs";
    /**
     * specify button style
     */
    public static final String EXTRA_RECORDING_BUTTON_STYLE = "recordingButtonStyle";

    /**
     * Default button style
     */
    public static final String BUTTON_STYLE_DEFAULT = "default";
    /**
     * Ripple button style
     */
    public static final String BUTTON_STYLE_RIPPLE = "ripple";

    /**
     * voice recording time in seconds
     */
    public static final int DEFAULT_RECORDING_TIME_MS = 5000;

    private static final int PERMISSIONS_REQUEST_CREATE = 2221;
    private static final int PERMISSIONS_REQUEST_RESUME = 2222;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3;

    public static byte[] audio;

    private boolean requestPermissionPending;
    private AudioRecord mRecorder = null;
    private boolean isRecording;
    private int recordingTimeSeconds;
    private String buttonStyle;

    private OverlayView overlayView;
    private RecordButtonView recordButton;
    private RecordAudio recordTask;
    private int readBufferSize;
    private int recordingBufferSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        recordingTimeSeconds = getIntent().getIntExtra(EXTRA_RECORDING_DURATION_MS, DEFAULT_RECORDING_TIME_MS) / 1000;
        buttonStyle = getIntent().getStringExtra(EXTRA_RECORDING_BUTTON_STYLE);
        if (buttonStyle == null) {
            buttonStyle = BUTTON_STYLE_DEFAULT;
        }

        setWindowFlags();
        inflateViews();

        requestPermissionPending = false;
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE)) {
            continueWithPermissionsGranted();
        }
    }

    @Override
    protected void onPause() {
        Logger.d(TAG, "on stop stop recording");
        stopRecording();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    protected boolean requestPermissionsNeeded(int requestCode) {
        if (!permissionsGranted(getRequestedPermissions())) {
            if (!requestPermissionPending) {
                requestPermissionPending = true;
                ActivityCompat.requestPermissions(this, getRequestedPermissions(), requestCode);
            }
            return true;
        }
        return false;
    }

    protected String[] getRequestedPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    protected boolean permissionsGranted(String[] requestedPermissions) {
        for (String requestedPermission : requestedPermissions) {
            if (ContextCompat.checkSelfPermission(this, requestedPermission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setWindowFlags() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void inflateViews() {
        setContentView(R.layout.activity_voice_capture);
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        OverlayView overlayView = inflateOverlayView(this, container);

        String topHint = getIntent().getStringExtra(EXTRA_UPPER_TEXT);
        overlayView.setHintText(topHint);
        String recordedToken = getIntent().getStringExtra(EXTRA_RECORDING_HINT);
        overlayView.setRecordedTokenText(recordedToken);
        overlayView.setRecordingTime(recordingTimeSeconds);

        FrameLayout.LayoutParams overlayLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(overlayView, overlayLp);

        ViewGroup recordContainer = overlayView.getRecordButtonParent();
        RecordButtonView recordButton = inflateRecordButtonView(this, recordContainer);
        FrameLayout.LayoutParams buttonLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        recordContainer.addView(recordButton, buttonLp);

        this.overlayView = overlayView;
        this.recordButton = recordButton;
    }

    protected OverlayView inflateOverlayView(Context context, ViewGroup parent) {
        DefaultVoiceOverlayView defaultOverlayView = new DefaultVoiceOverlayView(context);
        defaultOverlayView.setOnCloseListener(new DefaultVoiceOverlayView.OnCloseListener() {
            @Override
            public void onClose() {
                Logger.d(TAG, "close button stop recording");
                stopRecording();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        return defaultOverlayView;
    }

    private RecordButtonView inflateRecordButtonView(Context context, ViewGroup parent) {
        if (BUTTON_STYLE_RIPPLE.equals(buttonStyle)) {
            ProgressRecordButtonView button = new ProgressRecordButtonView(this);
            return button;
        } else {
            return new DefaultVoiceRecordButtonView(this);
        }
    }

    private void continueWithPermissionsGranted() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    if (BUTTON_STYLE_RIPPLE.equals(buttonStyle)) {
                        // cancel if ripple button is used to match button states
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                } else {

                    try {
                        startRecording();
                        recordButton.showRecordingStarted(recordingTimeSeconds);
                        overlayView.showRecordingStarted(recordingTimeSeconds);
                    } catch (IOException e) {
                        Logger.e(TAG, "Record start", e);
                        displayErrorAndFinish("Error starting recording.");
                    }
                }
            }
        });
    }

    private void startRecording() throws IOException {
        Logger.d(TAG, "start recording");
        stopRecording();
        recordTask = new RecordAudio();
        recordTask.execute();
    }

    private void stopRecording() {
        isRecording = false;
        if (recordTask != null)
            recordTask.cancel(true);
        recordButton.showRecordingStopped();
        overlayView.showRecordingStopped();
    }

    public void setResultAndFinish(byte[] audioBytes) {
        audio = audioBytes;
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CREATE:
            case PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0
                        && !Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    requestPermissionPending = false;
                    if (requestCode == PERMISSIONS_REQUEST_CREATE) {
                        continueWithPermissionsGranted();
                    }
                } else {
                    requestPermissionPending = false;
                    displayNoPermissionsAndFinish();
                }
            }
        }
    }

    public void displayErrorAndFinish(String error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        VoiceCaptureActivity.this.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void displayNoPermissionsAndFinish() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Face authentication needs requested permissions granted.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        VoiceCaptureActivity.this.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private int determineCalculatedBufferSize(final int sampleRate, int encoding, int numSamplesInBuffer) {
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, WaveFileHelper.AUDIO_CHANNEL, encoding);
        if(minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE){
            return minBufferSize;
        }
        int bufferSize;
        // each sample takes two bytes, need a bigger buffer
        if (encoding == AudioFormat.ENCODING_PCM_16BIT) {
            bufferSize = numSamplesInBuffer * 2;
        } else {
            bufferSize = numSamplesInBuffer;
        }

        if (bufferSize < minBufferSize) {
            Logger.v(TAG, "Increasing buffer to hold enough samples "
                    + minBufferSize + " was: " + bufferSize);
            bufferSize = minBufferSize;
        }

        return bufferSize;
    }

    private void releaseRecorder() {
        if (mRecorder != null && mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private class RecordAudio extends AsyncTask<Void, Integer, byte[]> {
        String error = null;

        @Override
        protected byte[] doInBackground(Void... params) {
            Logger.d(TAG, "RecordAudio ");
            isRecording = true;
            ByteArrayOutputStream outByte = new ByteArrayOutputStream();

            mRecorder = getAudioRecorder();
            if (mRecorder == null || mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                return null;
            }

            try {
                final byte[] readBuffer = new byte[readBufferSize];

                mRecorder.startRecording();
                while (isRecording) {
                    int bufferResult = mRecorder.read(readBuffer, 0, readBufferSize);
                    outByte.write(readBuffer);
                    if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION) {
                        Logger.e(TAG, "error reading: ERROR_INVALID_OPERATION");
                        isRecording = false;
                    } else if (bufferResult == AudioRecord.ERROR_BAD_VALUE) {
                        Logger.e(TAG, "error reading: ERROR_BAD_VALUE");
                        isRecording = false;
                    } else {
                        if (outByte.size() >= recordingBufferSize) {
                            isRecording = false;
                        }
                    }
                }
                return WaveFileHelper.getAudioBytesWithWaveHeaders(outByte.toByteArray(), mRecorder.getSampleRate());

            } catch (IOException e) {
                Logger.e(TAG, "Recording Failed", e);
                error = "Recording failed " + e.getMessage();
            } finally {
                if (outByte != null) {
                    try {
                        outByte.close();
                    } catch (IOException e) {
                        Logger.e("AudioRecord", "Failed to close bytearray", e);
                    }
                }
            }
            return null;
        }

        private int getIncreasedRecordingBufferSize(int frequency) {
            readBufferSize = (int) ((float) frequency * recordingTimeSeconds);
            recordingBufferSize = determineCalculatedBufferSize(frequency, WaveFileHelper.AUDIO_ENCODING, readBufferSize);

            if (recordingBufferSize == AudioRecord.ERROR_BAD_VALUE || recordingBufferSize == AudioRecord.ERROR) {
                Logger.e(TAG, "Bad encoding value, see logcat");
                return recordingBufferSize;
            }

            // give it extra space to prevent overflow
            return recordingBufferSize * DEFAULT_BUFFER_INCREASE_FACTOR;
        }

        private AudioRecord getAudioRecorder() {
            AudioRecord audioRecorder = null;
            for (int frequency : WaveFileHelper.AUDIO_FREQUENCIES) {
                if (frequency == WaveFileHelper.FREQUENCY_NATIVE) {
                    frequency = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM);
                }
                //some device throw IllegalArgumentException when sample rate not supported. Some device just return AudioRecord.STATE_UNINITIALIZED
                try {
                    int bufferSize = getIncreasedRecordingBufferSize(frequency);
                    if(bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE){
                        continue;
                    }
                    audioRecorder = new AudioRecord(WaveFileHelper.AUDIO_SOURCE, frequency, WaveFileHelper.AUDIO_CHANNEL, WaveFileHelper.AUDIO_ENCODING, bufferSize);
                } catch (Exception e) {
                    Logger.e(TAG, "Audio recorder fail ", e);
                }
                if (audioRecorder != null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    return audioRecorder;
                }
            }
            return audioRecorder;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Logger.d(TAG, "on Record task canceled");
            isRecording = false;
            releaseRecorder();
        }

        protected void onPostExecute(byte[] result) {
            releaseRecorder();
            Logger.d(TAG, "Post execute");
            if (error != null) {
                displayErrorAndFinish(error);
            } else {
                setResultAndFinish(result);
            }
        }
    }
}
