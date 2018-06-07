package com.aimbrain.sdk.faceCapture.fragments;

import android.content.Context;
import android.view.SurfaceView;
import android.view.View;

import com.aimbrain.sdk.faceCapture.views.CameraUiView;

public interface CameraUiViewProvider {
    CameraUiView createUiView(Context context);
}
