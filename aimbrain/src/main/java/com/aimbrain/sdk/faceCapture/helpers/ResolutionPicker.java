package com.aimbrain.sdk.faceCapture.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Base resolution picker class with helper methods
 */
public abstract class ResolutionPicker {
    protected List<VideoSize> previewSizes = new ArrayList<>();
    protected List<VideoSize> recordSizes = new ArrayList<>();

    public abstract VideoSize getRecordSize();

    public abstract VideoSize getPreviewSize(int displayWidth, int displayHeight);

    public void addPreviewSize(int width, int height) {
        previewSizes.add(new VideoSize(width, height));
    }

    public void addVideoSize(int width, int height) {
        recordSizes.add(new VideoSize(width, height));
    }

    protected List<VideoSize> allLarger(List<VideoSize> sizes, VideoSize minSize) {
        List<VideoSize> result = new ArrayList<>();
        for (VideoSize option : sizes) {
            boolean larger = option.width >= minSize.width && option.height >= minSize.height;
            if (larger) {
                result.add(option);
            }
        }
        return result;
    }

    protected List<VideoSize> allSmaller(List<VideoSize> sizes, VideoSize maxSize) {
        List<VideoSize> result = new ArrayList<>();
        for (VideoSize option : sizes) {
            boolean larger = option.width <= maxSize.width && option.height <= maxSize.height;
            if (larger) {
                result.add(option);
            }
        }
        return result;
    }

    protected List<VideoSize> allWithAspect(List<VideoSize> sizes, VideoSize aspect) {
        List<VideoSize> result = new ArrayList<>();
        for (VideoSize option : sizes) {
            boolean sameAspect = option.height == option.width * aspect.height / aspect.width;
            if (sameAspect) {
                result.add(option);
            }
        }
        return result;
    }

    protected VideoSize largestArea(List<VideoSize> sizes) {
        VideoSize result = null;
        for (VideoSize option : sizes) {
            if (result == null) {
                result = option;
            }
            else {
                int resultArea = result.width * result.height;
                int optionArea = option.width * option.height;
                if (optionArea > resultArea) {
                    result = option;
                }
            }
        }
        return result;
    }

    protected VideoSize getClosestByArea(List<VideoSize> sizes, VideoSize target) {
        int targetArea = target.height * target.width;
        VideoSize closest = sizes.get(0);
        for (VideoSize currentSize : sizes) {
            if (currentSize.equals(target)) {
                closest = currentSize;
                break;
            }
            int closestArea = closest.height * closest.width;
            int currentArea = currentSize.height * currentSize.width;
            if (Math.abs(currentArea - targetArea) < Math.abs(closestArea - targetArea)) {
                closest = currentSize;
            }
        }
        return closest;
    }

    protected List<VideoSize> commonSizes(List<VideoSize> a, List<VideoSize> b) {
        List<VideoSize> intersection = new ArrayList<>(a);
        intersection.retainAll(b);
        return intersection;
    }
}
