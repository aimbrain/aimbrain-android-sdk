package com.aimbrain.sdk;

import android.widget.EditText;

import com.aimbrain.sdk.mock.TextEventCollectorMock;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class TextEventCollectorTest {

    @Test
    public void testAttachTextChangedListener() {
        TextEventCollectorMock textEventCollectorMock = new TextEventCollectorMock();
        textEventCollectorMock.attachTextChangedListener(mock(EditText.class));
        assertEquals(textEventCollectorMock.getEventListeners().size(), 1);
    }

    @Test
    public void testAttach2sameTextChangedListener() {
        TextEventCollectorMock textEventCollectorMock = new TextEventCollectorMock();
        EditText editText = mock(EditText.class);
        textEventCollectorMock.attachTextChangedListener(editText);
        textEventCollectorMock.attachTextChangedListener(editText);
        assertEquals(textEventCollectorMock.getEventListeners().size(), 1);
    }

    @Test
    public void testAttach2differentTextChangedListener() {
        TextEventCollectorMock textEventCollectorMock = new TextEventCollectorMock();
        textEventCollectorMock.attachTextChangedListener(mock(EditText.class));
        textEventCollectorMock.attachTextChangedListener(mock(EditText.class));
        assertEquals(textEventCollectorMock.getEventListeners().size(), 2);
    }

    @Test
    public void testStop() {
        TextEventCollectorMock textEventCollectorMock = new TextEventCollectorMock();
        textEventCollectorMock.attachTextChangedListener(mock(EditText.class));
        textEventCollectorMock.attachTextChangedListener(mock(EditText.class));
        assertEquals(textEventCollectorMock.getEventListeners().size(), 2);
        textEventCollectorMock.stop();
        assertEquals(textEventCollectorMock.getEventListeners().size(), 0);
    }

}
