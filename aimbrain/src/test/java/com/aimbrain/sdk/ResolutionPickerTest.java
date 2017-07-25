package com.aimbrain.sdk;

import com.aimbrain.sdk.faceCapture.helpers.VideoSize;
import com.aimbrain.sdk.mock.Camera2ResolutionPickerMock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;


public class ResolutionPickerTest {

    VideoSize videoSize_240_135 = new VideoSize(240, 135);
    VideoSize videoSize_320_240 = new VideoSize(320, 240);
    VideoSize videoSize_480_270 = new VideoSize(480, 270);
    VideoSize videoSize_480_320 = new VideoSize(480, 320);
    VideoSize videoSize_1920_1080 = new VideoSize(1920, 1080);

    @Test
    public void testAddPreviewSize() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        picker.addPreviewSize(320, 240);
        picker.addPreviewSize(320, 240);
        picker.addPreviewSize(320, 240);
        assertEquals(3, picker.getPreviewSizes().size());
    }

    @Test
    public void testAddRecordSize() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        picker.addVideoSize(320, 240);
        picker.addVideoSize(320, 240);
        picker.addVideoSize(320, 240);
        assertEquals(3, picker.getRecordSizes().size());
    }

    @Test
    public void testAllLarger() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allLargerSizes = picker.allLarger(sizes, new VideoSize(240, 180));
        assertEquals(3, allLargerSizes.size());
        assertEquals(videoSize_320_240, allLargerSizes.get(0));
        assertEquals(videoSize_480_320, allLargerSizes.get(1));
        assertEquals(videoSize_1920_1080, allLargerSizes.get(2));
    }

    @Test
    public void testOneLarger() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allLargerSizes = picker.allLarger(sizes, new VideoSize(480, 640));
        assertEquals(1, allLargerSizes.size());
        assertEquals(videoSize_1920_1080, allLargerSizes.get(0));
    }

    @Test
    public void testNoLarger() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allLargerSizes = picker.allLarger(sizes, new VideoSize(3264, 2448));
        assertEquals(0, allLargerSizes.size());
    }

    @Test
    public void testAllSmaller() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allSmaller = picker.allSmaller(sizes, new VideoSize(3264, 2448));
        assertEquals(3, allSmaller.size());
        assertEquals(videoSize_320_240, allSmaller.get(0));
        assertEquals(videoSize_480_320, allSmaller.get(1));
        assertEquals(videoSize_1920_1080, allSmaller.get(2));
    }

    @Test
    public void testOneSmaller() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allSmaller = picker.allSmaller(sizes, new VideoSize(380, 280));
        assertEquals(1, allSmaller.size());
        assertEquals(videoSize_320_240, allSmaller.get(0));
    }

    @Test
    public void testNoSmaller() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> allSmaller = picker.allSmaller(sizes, new VideoSize(280, 180));
        assertEquals(0, allSmaller.size());
    }

    @Test
    public void testAllWithAspect() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_240_135);
        sizes.add(videoSize_480_270);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> aspects = picker.allWithAspect(sizes, new VideoSize(3840, 2160));
        assertEquals(3, aspects.size());
        assertEquals(videoSize_240_135, aspects.get(0));
        assertEquals(videoSize_480_270, aspects.get(1));
        assertEquals(videoSize_1920_1080, aspects.get(2));
    }

    @Test
    public void testOneAspectRatio() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_320_240);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> aspects = picker.allWithAspect(sizes, new VideoSize(3840, 2160));
        assertEquals(1, aspects.size());
        assertEquals(videoSize_1920_1080, aspects.get(0));
    }

    @Test
    public void testNoAspectRatio() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_240_135);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        List<VideoSize> aspects = picker.allWithAspect(sizes, new VideoSize(640, 480));
        assertEquals(0, aspects.size());
    }

    @Test
    public void testLargestArea() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_240_135);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        VideoSize videoSize = picker.largestArea(sizes);
        assertEquals(videoSize_1920_1080, videoSize);
    }

    @Test
    public void testGetClosestByArea() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        List<VideoSize> sizes = new ArrayList<>();
        sizes.add(videoSize_240_135);
        sizes.add(videoSize_480_320);
        sizes.add(videoSize_1920_1080);
        VideoSize videoSize = picker.getClosestByArea(sizes, new VideoSize(480, 340));
        assertEquals(videoSize_480_320, videoSize);
        VideoSize videoSize2 = picker.getClosestByArea(sizes, new VideoSize(480, 320));
        assertEquals(videoSize_480_320, videoSize2);
    }

    @Test
    public void testGetPreviewVideoSize() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        picker.addVideoSize(320, 240);
        picker.addVideoSize(480, 240);
        picker.addVideoSize(640, 480);
        picker.addVideoSize(1240, 1080);

        picker.addPreviewSize(320, 240);
        picker.addPreviewSize(480, 240);
        picker.addPreviewSize(640, 480);
        picker.addPreviewSize(1920, 1080);

        VideoSize previewSize = picker.getPreviewSize(1920, 1080);
        assertEquals(1920, previewSize.width);
        assertEquals(1080, previewSize.height);

        VideoSize recordSize = picker.getRecordSize();
        assertEquals(320, recordSize.width);
        assertEquals(240, recordSize.height);
    }

    @Test
    public void testGetPreviewVideoSizeWhenNotEquals() throws Exception {
        Camera2ResolutionPickerMock picker = new Camera2ResolutionPickerMock();
        picker.addVideoSize(480, 240);
        picker.addVideoSize(640, 480);
        picker.addVideoSize(1240, 1080);

        picker.addPreviewSize(320, 240);
        picker.addPreviewSize(480, 240);
        picker.addPreviewSize(640, 480);
        picker.addPreviewSize(1240, 1080);

        VideoSize previewSize = picker.getPreviewSize(1920, 1080);
        assertEquals(1240, previewSize.width);
        assertEquals(1080, previewSize.height);

        VideoSize recordSize = picker.getRecordSize();
        assertEquals(480, recordSize.width);
        assertEquals(240, recordSize.height);
    }
}
