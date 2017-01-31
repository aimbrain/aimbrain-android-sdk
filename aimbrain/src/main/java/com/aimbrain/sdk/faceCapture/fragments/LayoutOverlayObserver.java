package com.aimbrain.sdk.faceCapture.fragments;

import android.graphics.Rect;
import android.view.View;

public interface LayoutOverlayObserver {

    View getLayoutOverlayView();
    void onRecordingStarted();
    void onRecordingStopped();
    void setOverlayViewDimensions(int height, int width);

    void setRecordButtonPosition(Rect position);

}
