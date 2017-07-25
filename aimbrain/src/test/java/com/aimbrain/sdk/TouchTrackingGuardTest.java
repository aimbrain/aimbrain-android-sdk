package com.aimbrain.sdk;

import android.view.View;
import android.view.ViewGroup;

import com.aimbrain.sdk.privacy.TouchTrackingGuard;
import com.aimbrain.sdk.mock.TouchTrackingGuardMock;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TouchTrackingGuardTest {


    @Test
    public void testIsViewIgnored() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(view);
        TouchTrackingGuard guard = new TouchTrackingGuard(viewSet);
        assertTrue(guard.isViewIgnored(view));
    }

    @Test
    public void testIsViewNoIgnoredWhenNothingAdded() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        TouchTrackingGuard guard = new TouchTrackingGuard(viewSet);
        assertFalse(guard.isViewIgnored(view));
    }

    @Test
    public void testIsViewNoIgnoredWhenAddingSomeDifferentViews() {
        View view = mock(View.class);
        View view2 = mock(View.class);
        View view3 = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(view);
        viewSet.add(view2);
        TouchTrackingGuard guard = new TouchTrackingGuard(viewSet);
        assertFalse(guard.isViewIgnored(view3));
        assertTrue(guard.isViewIgnored(view));
        assertTrue(guard.isViewIgnored(view2));
    }

    @Test
    public void testIsDescendantOfIgnoredView() {
        ViewGroup parentView = mock(ViewGroup.class);
        View childView = mock(View.class);
        when(childView.getParent()).thenReturn(parentView);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(parentView);
        TouchTrackingGuardMock guard = new TouchTrackingGuardMock(viewSet);
        assertTrue(guard.isViewIgnored(childView));
    }

    @Test
    public void testIsDescendantOfIgnoredViewWhenNotAdded() {
        ViewGroup parentView = mock(ViewGroup.class);
        View childView = mock(View.class);
        View view = mock(View.class);
        when(childView.getParent()).thenReturn(parentView);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(parentView);
        TouchTrackingGuardMock guard = new TouchTrackingGuardMock(viewSet);
        assertFalse(guard.isViewIgnored(view));
    }

    @Test
    public void testInvalidate() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(view);
        TouchTrackingGuard guard = new TouchTrackingGuard(viewSet);
        assertTrue(guard.isViewIgnored(view));
        guard.invalidate();
        assertFalse(guard.isViewIgnored(view));
    }


}
