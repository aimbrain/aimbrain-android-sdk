package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.faceCapture.helpers.Camera2ResolutionPicker;
import com.aimbrain.sdk.faceCapture.helpers.VideoSize;

import java.util.List;


public class Camera2ResolutionPickerMock extends Camera2ResolutionPicker {
    public List<VideoSize> getPreviewSizes() {
        return previewSizes;
    }

    public List<VideoSize> getRecordSizes() {
        return recordSizes;
    }

    @Override
    public List<VideoSize> allLarger(List<VideoSize> sizes, VideoSize minSize) {
        return super.allLarger(sizes, minSize);
    }

    @Override
    public List<VideoSize> allSmaller(List<VideoSize> sizes, VideoSize maxSize) {
        return super.allSmaller(sizes, maxSize);
    }

    @Override
    public List<VideoSize> allWithAspect(List<VideoSize> sizes, VideoSize aspect) {
        return super.allWithAspect(sizes, aspect);
    }

    @Override
    public VideoSize largestArea(List<VideoSize> sizes) {
        return super.largestArea(sizes);
    }

    @Override
    public VideoSize getClosestByArea(List<VideoSize> sizes, VideoSize target) {
        return super.getClosestByArea(sizes, target);
    }
}
