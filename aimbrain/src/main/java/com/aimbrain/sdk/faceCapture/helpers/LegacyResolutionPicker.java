package com.aimbrain.sdk.faceCapture.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolution picker for old devices
 */
public class LegacyResolutionPicker extends ResolutionPicker {
    protected static final int RECORD_WIDTH = 320;
    protected static final int RECORD_HEIGHT = 240;
    public static final double MAX_PREVIEW_SCALING = 2.0;

    public VideoSize getRecordSize() {
        VideoSize targetSize = new VideoSize(RECORD_WIDTH, RECORD_HEIGHT);
        if (recordSizes.isEmpty()) {
            // choose from preview sizes for devices without record sizes (very old ones)
            return getClosestByArea(previewSizes, targetSize);
        } else {
            List<VideoSize> choices = commonSizes(recordSizes, previewSizes);
            if (choices.isEmpty()) {
                choices = recordSizes;
            }
            return getClosestByArea(choices, targetSize);
        }
    }

    public VideoSize getPreviewSize(int displayWidth, int displayHeight) {
        VideoSize recordSize = getRecordSize();

        List<VideoSize> sameAspect = allWithAspect(previewSizes, recordSize);
        if (!sameAspect.isEmpty()) {
            VideoSize largest = largestArea(sameAspect);
            float scale = Math.max(displayWidth / largest.width, displayHeight / largest.height);
            if (scale <= MAX_PREVIEW_SCALING) {
                return largest;
            }
        }

        List<VideoSize> smallerThanDisplay = allSmaller(previewSizes, new VideoSize(displayWidth, displayHeight));
        return largestArea(smallerThanDisplay);
    }
}
