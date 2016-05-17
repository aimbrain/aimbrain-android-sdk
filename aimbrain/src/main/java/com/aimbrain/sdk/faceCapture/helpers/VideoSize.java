package com.aimbrain.sdk.faceCapture.helpers;

import android.support.annotation.NonNull;

public class VideoSize implements Comparable<VideoSize> {
    public final int width;
    public final int height;

    public VideoSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoSize videoSize = (VideoSize) o;
        return width == videoSize.width && height == videoSize.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public int compareTo(@NonNull VideoSize other) {
        return Long.signum((long) width * height - (long) other.width * other.height);
    }
}