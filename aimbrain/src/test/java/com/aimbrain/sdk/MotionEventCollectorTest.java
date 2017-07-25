package com.aimbrain.sdk;

import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.models.EventModel;
import com.aimbrain.sdk.models.TouchEventModel;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.mock.MotionEventCollectorMock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MotionEventCollectorTest {

    private long timestamp = 123456;

    @Test
    public void testCollectedDataOnePointer() throws JSONException {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(MotionEvent.ACTION_DOWN);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().get(pointerId).intValue());
        assertEquals(1, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(1, collectedData.size());

        TouchEventModel eventModel = (TouchEventModel) collectedData.get(0);
        JSONObject json = eventModel.toJSON();

        assertEquals(0, json.getInt("tid"));
        assertEquals(timestamp, json.getInt("t"));

        when(event.getActionMasked()).thenReturn(MotionEvent.ACTION_UP);
        motionEventCollector.processMotionEvent(event, view, timestamp);
        assertEquals(0, motionEventCollector.getPointerIdMap().size());
    }

    @Test
    public void testCollectedDataOnePointerWithIgnoredView() throws JSONException {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);
        HashSet<View> ignoredViews = new HashSet<>();
        ignoredViews.add(view);
        PrivacyGuard oneViewPrivacyGuard = new PrivacyGuard(ignoredViews);
        Manager.getInstance().addPrivacyGuard(oneViewPrivacyGuard);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(MotionEvent.ACTION_DOWN);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().get(pointerId).intValue());
        assertEquals(1, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(0, collectedData.size());

        when(event.getActionMasked()).thenReturn(MotionEvent.ACTION_UP);
        motionEventCollector.processMotionEvent(event, view, timestamp);
        assertEquals(0, motionEventCollector.getPointerIdMap().size());
    }

    @Test
    public void checkOrAddCollectedDataActionDown() {
        checkOrAddCollectedData(MotionEvent.ACTION_DOWN);
    }

    @Test
    public void checkOrAddCollectedDataActionPointerDown() {
        checkOrAddCollectedData(MotionEvent.ACTION_POINTER_DOWN);
    }

    @Test
    public void checkOrAddCollectedDataActionMove() {
        checkOrAddCollectedData(MotionEvent.ACTION_MOVE);
    }

    @Test
    public void checkOrAddCollectedDataPointerUp() {
        checkOrAddCollectedDataOnActionUp(MotionEvent.ACTION_POINTER_UP);
    }

    @Test
    public void checkOrAddCollectedDataActionUp() {
        checkOrAddCollectedDataOnActionUp(MotionEvent.ACTION_UP);
    }

    @Test
    public void checkOrIgnoreViewOnActionDown() {
        checkOrIgnoreView(MotionEvent.ACTION_DOWN);
    }

    @Test
    public void checkOrIgnoreViewOnActionPointerDown() {
        checkOrIgnoreView(MotionEvent.ACTION_POINTER_DOWN);
    }

    @Test
    public void checkOrIgnoreViewOnActionMove() {
        checkOrIgnoreView(MotionEvent.ACTION_MOVE);
    }

    @Test
    public void checkOrIgnoreViewOnActionPointerUp() {
        checkOrIgnoreViewAndRemovePointerIds(MotionEvent.ACTION_POINTER_UP);
    }

    @Test
    public void checkOrIgnoreViewOnActionUp() {
        checkOrIgnoreViewAndRemovePointerIds(MotionEvent.ACTION_UP);
    }

    private void checkOrAddCollectedData(int action) {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(action);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().get(pointerId).intValue());
        assertEquals(1, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(1, collectedData.size());
    }

    private void checkOrAddCollectedDataOnActionUp(int action) {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(action);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(1, collectedData.size());
    }

    private void checkOrIgnoreView(int action) {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);
        HashSet<View> ignoredViews = new HashSet<>();
        ignoredViews.add(view);
        PrivacyGuard oneViewPrivacyGuard = new PrivacyGuard(ignoredViews);
        Manager.getInstance().addPrivacyGuard(oneViewPrivacyGuard);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(action);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().get(pointerId).intValue());
        assertEquals(1, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(0, collectedData.size());
    }

    private void checkOrIgnoreViewAndRemovePointerIds(int action) {
        int pointerId = 1;
        int actionIndex = 2;
        int pointerCount = 1;
        View view = mock(View.class);
        HashSet<View> ignoredViews = new HashSet<>();
        ignoredViews.add(view);
        PrivacyGuard oneViewPrivacyGuard = new PrivacyGuard(ignoredViews);
        Manager.getInstance().addPrivacyGuard(oneViewPrivacyGuard);

        MotionEventCollectorMock motionEventCollector = new MotionEventCollectorMock();
        MotionEvent event = mock(MotionEvent.class);
        when(event.getActionMasked()).thenReturn(action);
        when(event.getPointerCount()).thenReturn(pointerCount);
        when(event.getActionIndex()).thenReturn(actionIndex);
        when(event.getPointerId(any(int.class))).thenReturn(pointerId);
        motionEventCollector.processMotionEvent(event, view, timestamp);

        assertEquals(0, motionEventCollector.getPointerIdMap().size());
        List<EventModel> collectedData = motionEventCollector.getCollectedData();
        assertEquals(0, collectedData.size());
    }
}
