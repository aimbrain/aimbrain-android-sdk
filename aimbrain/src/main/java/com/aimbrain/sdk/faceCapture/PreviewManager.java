package com.aimbrain.sdk.faceCapture;

import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class PreviewManager {
    public static void setPreviewTopBottomMargins(int previewHeight, SurfaceView surfaceView){
        ViewParent previewParent = surfaceView.getParent();
        if(previewParent instanceof View) {
            ViewGroup.MarginLayoutParams previewLayoutParams = (ViewGroup.MarginLayoutParams) surfaceView.getLayoutParams();
            int margin = (((View) previewParent).getHeight() - previewHeight)/2;
            if(margin < 0) {
                previewLayoutParams.bottomMargin = margin;
                previewLayoutParams.topMargin = margin;
                surfaceView.setLayoutParams(previewLayoutParams);
                surfaceView.requestLayout();
            }
        }
    }

}
