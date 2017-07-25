package com.aimbrain.sdk.voiceCapture;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class RecordButtonView extends FrameLayout {

    public RecordButtonView(Context context) {
        super(context);
    }

    public RecordButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RecordButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecordButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setWillNotDraw(false);
    }

    public abstract void showRecordingStarted(int recordingTimeSeconds);

    public abstract void showRecordingStopped();
}
