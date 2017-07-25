package com.aimbrain.sdk.voiceCapture;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aimbrain.aimbrain.R;

import java.util.concurrent.TimeUnit;

class DefaultVoiceOverlayView extends OverlayView {

    public interface OnCloseListener {
        void onClose();
    }

    private OnCloseListener onCloseListener;
    private TextView lowerTextView;
    private CountDownTimer countDownTimer;

    public DefaultVoiceOverlayView(Context context) {
        super(context);
        initView();
    }

    public DefaultVoiceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DefaultVoiceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public DefaultVoiceOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.default_voice_overlay, this);
        this.lowerTextView = (TextView) findViewById(R.id.lowerTextView);
        findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCloseListener != null) {
                    onCloseListener.onClose();
                }
            }
        });
    }

    @Override
    public ViewGroup getRecordButtonParent() {
        return (ViewGroup) findViewById(R.id.record_container);
    }

    @Override
    public void setHintText(String text) {
        TextView textView = (TextView) findViewById(R.id.upperTextView);
        textView.setText(text);
    }

    @Override
    public void setRecordedTokenText(String text) {
        TextView textView = (TextView) findViewById(R.id.hintTextView);
        textView.setText(text);
    }

    @Override
    public void setRecordingTime(int timeSeconds) {
        String recordTimeHint = getContext().getString(R.string.voice_capture_lower_text, timeSeconds);
        setStatusText(recordTimeHint);
    }

    private void setStatusText(String text) {
        lowerTextView.setText(text);
    }

    @Override
    public void showRecordingStarted(int timeLeftSeconds) {
        lowerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.color_circle_recording_active));
        setStatusText(getContext().getString(R.string.voice_capture_lower_text_recording, String.valueOf(timeLeftSeconds)));
        countDownOverlayProgress(timeLeftSeconds);
    }

    @Override
    public void showRecordingStopped() {
        lowerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.color_circle_recording_inactive));
        setStatusText("");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void countDownOverlayProgress(int timeLeftSeconds) {
        countDownTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(timeLeftSeconds), 500) {
            public void onTick(long millisUntilFinished) {
                String status = getContext().getString(R.string.voice_capture_lower_text_recording, String.valueOf((int) Math.ceil(millisUntilFinished / 1000f)));
                setStatusText(status);
            }

            public void onFinish() {
                String status = getContext().getString(R.string.voice_capture_lower_text_recording, "0");
                setStatusText(status);
            }
        }.start();
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }
}
