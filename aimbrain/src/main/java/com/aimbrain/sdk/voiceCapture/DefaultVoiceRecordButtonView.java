package com.aimbrain.sdk.voiceCapture;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.aimbrain.aimbrain.R;
import com.aimbrain.sdk.views.CircleView;

class DefaultVoiceRecordButtonView extends RecordButtonView {
    private CircleView circleView;
    private ImageView micImageView;

    public DefaultVoiceRecordButtonView(Context context) {
        super(context);
        initView();
    }

    public DefaultVoiceRecordButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DefaultVoiceRecordButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DefaultVoiceRecordButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.default_voice_record_button, this);
        this.circleView = (CircleView) findViewById(R.id.micButton);
        this.micImageView = (ImageView)findViewById(R.id.micImage);
    }

    @Override
    public void showRecordingStarted(int recordingTimeSeconds) {
        micImageView.setImageResource(R.drawable.ic_mic_active);
        circleView.start(recordingTimeSeconds);
    }

    @Override
    public void showRecordingStopped() {
        micImageView.setImageResource(R.drawable.ic_mic_inactive);
        circleView.stop();
    }
}
