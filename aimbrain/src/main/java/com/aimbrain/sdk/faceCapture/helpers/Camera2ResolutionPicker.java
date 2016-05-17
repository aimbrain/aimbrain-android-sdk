package com.aimbrain.sdk.faceCapture.helpers;

import java.util.Collections;
import java.util.List;

public class Camera2ResolutionPicker extends ResolutionPicker {
    protected static final int RECORD_WIDTH = 320;
    protected static final int RECORD_HEIGHT = 240;

    public VideoSize getRecordSize() {
        return getClosestByArea(recordSizes, new VideoSize(RECORD_WIDTH, RECORD_HEIGHT));
    }

    public VideoSize getPreviewSize(int displayWidth, int displayHeight) {
        VideoSize display = new VideoSize(displayWidth, displayHeight);
        VideoSize record = getRecordSize();

        // search for same aspect ratio as recording
        List<VideoSize> sameAspect = allWithAspect(previewSizes, record);
        // and larger than display
        List<VideoSize> larger = allLarger(sameAspect, display);

        if (!larger.isEmpty()) {
            // pick the smallest of those, assuming we found any
            return Collections.min(larger);
        } else {
            //if not found return closest size ignoring the aspect ratio
            return getClosestByArea(previewSizes, display);
        }
    }
}
