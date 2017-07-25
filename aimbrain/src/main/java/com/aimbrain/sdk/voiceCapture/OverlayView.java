package com.aimbrain.sdk.voiceCapture;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class OverlayView extends FrameLayout {

    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract ViewGroup getRecordButtonParent();

    public abstract void setHintText(String text);

    public abstract void setRecordedTokenText(String text);

    public abstract void setRecordingTime(int timeSeconds);

    public abstract void showRecordingStarted(int timeLeftSeconds);

    public abstract void showRecordingStopped();
}
