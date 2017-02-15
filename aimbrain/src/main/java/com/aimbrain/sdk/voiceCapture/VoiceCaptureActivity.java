package com.aimbrain.sdk.voiceCapture;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.array.Arrays;
import com.aimbrain.sdk.util.Logger;
import com.aimbrain.sdk.voiceCapture.helpers.WaveFileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class VoiceCaptureActivity extends Activity {
    private static final String TAG = VoiceCaptureActivity.class.getSimpleName();

    /**
     * specify text in upper hint view
     */
    public static final String EXTRA_UPPER_TEXT = "upperText";
    /**
     * specify text in middle hint view while capturing voice
     */
    public static final String EXTRA_RECORDING_HINT = "recordingHint";
    /**
     * voice recording time in seconds
     */
    public static final int RECORDING_TIME = 5;

    private static final int PERMISSIONS_REQUEST_CREATE = 2221;
    private static final int PERMISSIONS_REQUEST_RESUME = 2222;

    public static byte[] audio;

    private boolean requestPermissionPending;
    private AudioRecord mRecorder = null;
    private boolean isRecording;
    private CircleView recordButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setScreenParameters();
        setContentView(R.layout.activity_voice_capture);

        requestPermissionPending = false;
        if (!requestPermissionsNeeded(PERMISSIONS_REQUEST_CREATE)) {
            createActivityWithPermissions();
        }
    }

    @Override
    protected void onPause() {
        if (recordButton!=null) {
            recordButton.stop();
        }
        Logger.d(TAG, "on stop stop recording");
        stopRecording();
        super.onPause();
    }

    protected boolean requestPermissionsNeeded(int requestCode) {
        if (!permissionsGranted(getRequestedPermissions())) {
            if (!requestPermissionPending) {
                requestPermissionPending = true;
                ActivityCompat.requestPermissions(this,
                        getRequestedPermissions(),
                        requestCode);
            }
            return true;
        }
        return false;
    }

    protected String[] getRequestedPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    protected boolean permissionsGranted(String[] requestedPermissions) {
        for(String requestedPermission : requestedPermissions)
        {
            if (ContextCompat.checkSelfPermission(this, requestedPermission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void createActivityWithPermissions() {
        String upperText = getIntent().getStringExtra(EXTRA_UPPER_TEXT);
        String recordingHint = getIntent().getStringExtra(EXTRA_RECORDING_HINT);
        setupTexts(upperText, recordingHint);
    }

    private void setupTexts(String upperText, String recordingHint) {
        TextView upperTextView = (TextView) findViewById(R.id.upperTextView);
        upperTextView.setText(upperText);
        TextView recordingHintView = (TextView) findViewById(R.id.hintTextView);
        recordingHintView.setText(recordingHint);
        final TextView lowerTextView = (TextView) findViewById(R.id.lowerTextView);
        lowerTextView.setText(getString(R.string.voice_capture_lower_text, RECORDING_TIME));
        final ImageView micImageView = (ImageView) findViewById(R.id.micImage);
        recordButton = (CircleView) findViewById(R.id.micButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing if we are already recording
                if (isRecording) {
                    return;
                }
                try {
                    recordButton.start(RECORDING_TIME);
                    startRecording();
                    micImageView.setImageResource(R.drawable.ic_mic_active);
                    lowerTextView.setText(getString(R.string.voice_capture_lower_text_recording,
                            RECORDING_TIME));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lowerTextView.setTextColor(getResources().getColor(R.color.color_circle_recording_active, getTheme()));
                    } else {
                        lowerTextView.setTextColor(getResources().getColor(R.color.color_circle_recording_active));
                    }
                    animateTextView(RECORDING_TIME-1, 0, lowerTextView);
                } catch (IOException e) {
                    Logger.e(TAG, "save recording", e);
                }
            }
        });
        ImageView closeButton = (ImageView) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(TAG, "close button stop recording");
                stopRecording();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void animateTextView(int initialValue, int finalValue, final TextView  textview) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(TimeUnit.SECONDS.toMillis(RECORDING_TIME));
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                textview.setText(getString(R.string.voice_capture_lower_text_recording,
                        valueAnimator.getAnimatedValue().toString()));

            }
        });
        valueAnimator.start();

    }

    private void setScreenParameters() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void startRecording() throws IOException {
        Logger.d(TAG, "start recording");
        RecordAudio recordTask = new RecordAudio();
        recordTask.execute();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isRecording = false;
            }
        }, 5000);
   }

    private void stopRecording() {
        isRecording = false;

    }

    public void setResultAndFinish(byte[] audioBytes) {
        audio = audioBytes;
        setResult(RESULT_OK);
        finish();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CREATE:
            case PERMISSIONS_REQUEST_RESUME: {
                if (grantResults.length > 0
                        && !Arrays.contains(grantResults, PackageManager.PERMISSION_DENIED)) {
                    requestPermissionPending = false;
                    if(requestCode == PERMISSIONS_REQUEST_CREATE)
                        createActivityWithPermissions();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Face authentication needs requested permissions granted.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissionPending = false;
                                    setResult(RESULT_CANCELED);
                                    VoiceCaptureActivity.this.finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        }
    }

    private class RecordAudio extends AsyncTask<Void, Integer, byte[]> {

        String error = null;

        @Override
        protected byte[] doInBackground(Void... params) {
            Logger.d(TAG, "RecordAudio ");
            isRecording = true;
            ByteArrayOutputStream outByte = null;
            try {
                outByte = new ByteArrayOutputStream();
                int bufferSize = AudioRecord.getMinBufferSize(WaveFileHelper.AUDIO_FREQUENCY,
                        WaveFileHelper.AUDIO_CHANNEL, WaveFileHelper.AUDIO_ENCODING);
                mRecorder = new AudioRecord(WaveFileHelper.AUDIO_SOURCE,
                        WaveFileHelper.AUDIO_FREQUENCY, WaveFileHelper.AUDIO_CHANNEL,
                        WaveFileHelper.AUDIO_ENCODING, bufferSize);

                byte[] buffer = new byte[bufferSize];
                mRecorder.startRecording();
                while (isRecording) {
                    mRecorder.read(buffer, 0, bufferSize);
                    outByte.write(buffer);
                }
                Logger.d(TAG, "do in bg stop recording");
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                return WaveFileHelper.getAudioBytesWithWaveHeaders(outByte.toByteArray());

            } catch (IOException e) {
                Logger.e(TAG, "Recording Failed", e);
                error = "Recording failed " + e.getMessage();
            }finally {
                if (outByte != null) {
                    try {
                        outByte.close();
                    } catch (IOException e) {
                        Logger.e(TAG, "Failed to close bytearray", e);
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(byte[] result) {
            Logger.d(TAG, "Post execute");
            if (error != null) {
                displayErrorAndFinish(error);
            } else {
                setResultAndFinish(result);
            }
        }
    }

}
