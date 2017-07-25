package com.aimbrain.sdk;

import android.view.View;
import android.view.ViewGroup;

import com.aimbrain.sdk.mock.SensitiveViewGuardMock;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SensitiveViewGuardTest {

    @Test
    public void testIsViewSensitive() {
        SensitiveViewGuardMock sensitiveViewGuard = new SensitiveViewGuardMock();
        View view = mock(View.class);
        sensitiveViewGuard.addView(view);
        assertTrue(sensitiveViewGuard.isViewSensitive(view));
    }

    @Test
    public void testIsViewNotSensitiveWhenNothingAdded() {
        SensitiveViewGuardMock sensitiveViewGuard = new SensitiveViewGuardMock();
        assertFalse(sensitiveViewGuard.isViewSensitive(mock(View.class)));
    }

    @Test
    public void testIsViewNoSensitiveWhenAddingSomeDifferentViews() {
        SensitiveViewGuardMock sensitiveViewGuard = new SensitiveViewGuardMock();
        sensitiveViewGuard.addView(mock(View.class));
        sensitiveViewGuard.addView(mock(View.class));
        assertFalse(sensitiveViewGuard.isViewSensitive(mock(View.class)));
    }

    @Test
    public void testIsViewSensitiveWhenAddingSomeDifferentViews() {
        SensitiveViewGuardMock sensitiveViewGuard = new SensitiveViewGuardMock();
        View view = mock(View.class);
        sensitiveViewGuard.addView(mock(View.class));
        sensitiveViewGuard.addView(view);
        sensitiveViewGuard.addView(mock(View.class));
        assertTrue(sensitiveViewGuard.isViewSensitive(view));
    }

    @Test
    public void testIsViewSensitiveWhenAddedToParent() {
        ViewGroup parentView = mock(ViewGroup.class);
        View childView = mock(View.class);
        when(childView.getParent()).thenReturn(parentView);
        SensitiveViewGuardMock sensitiveViewGuard = new SensitiveViewGuardMock();
        sensitiveViewGuard.addView(parentView);
        assertTrue(sensitiveViewGuard.isViewSensitive(childView));
    }

}
