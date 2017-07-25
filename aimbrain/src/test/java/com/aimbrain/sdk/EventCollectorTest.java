package com.aimbrain.sdk;

import android.view.MotionEvent;
import android.view.View;

import com.aimbrain.sdk.mock.MotionEventCollectorMock;
import com.aimbrain.sdk.models.EventModel;
import com.aimbrain.sdk.models.TouchEventModel;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class EventCollectorTest {
    @Test
    public void testAddCollectedData() {
        MotionEventCollectorMock eventCollector = new MotionEventCollectorMock();
        eventCollector.addData(mock(EventModel.class));
        assertTrue(eventCollector.hasData());
        assertEquals(eventCollector.getCollectedData().size(), 1);
    }

    @Test
    public void testEmptyCollectionList() {
        MotionEventCollectorMock eventCollector = new MotionEventCollectorMock();
        assertFalse(eventCollector.hasData());
        assertEquals(eventCollector.getCollectedData().size(), 0);
    }

    @Test
    public void testClearOldDataByTimestamp() {
        MotionEventCollectorMock eventCollector = new MotionEventCollectorMock();
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 100000, 1, mock(View.class), false));
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 200000, 1, mock(View.class), false));
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 300000, 1, mock(View.class), false));
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 400000, 1, mock(View.class), false));
        assertTrue(eventCollector.hasData());
        assertEquals(100000, eventCollector.getOldestEventTimestamp());
        assertEquals(192, eventCollector.sizeOfElements());
        eventCollector.removeElementsOlderThan(200000);
        assertTrue(eventCollector.hasData());
        assertEquals(96, eventCollector.sizeOfElements());
        assertEquals(300000, eventCollector.getOldestEventTimestamp());
        eventCollector.removeElementsOlderThan(400000);
        assertFalse(eventCollector.hasData());
        assertEquals(eventCollector.getCollectedData().size(), 0);
        assertEquals(0, eventCollector.sizeOfElements());
    }

    @Test
    public void testGetOldestEventTimestamp() {
        MotionEventCollectorMock eventCollector = new MotionEventCollectorMock();
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 100000, 1, mock(View.class), false));
        eventCollector.addData(new TouchEventModel(0, 0, mock(MotionEvent.class), 200000, 1, mock(View.class), false));
        assertTrue(eventCollector.hasData());
        assertEquals(100000, eventCollector.getOldestEventTimestamp());
        eventCollector.removeElementsOlderThan(200000);
        assertEquals(System.currentTimeMillis(), eventCollector.getOldestEventTimestamp());
    }
}
