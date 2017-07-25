package com.aimbrain.sdk;

import android.view.View;
import android.view.ViewGroup;

import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.mock.PrivacyGuardMock;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PrivacyGuardTest {

    @Test
    public void testIsAllViewIgnored() {
        PrivacyGuard guard = new PrivacyGuard(true);
        assertTrue(guard.isViewIgnored(null));
    }

    @Test
    public void testIsAllViewNotIgnored() {
        PrivacyGuard guard = new PrivacyGuard(false);
        assertFalse(guard.isViewIgnored(mock(View.class)));
    }

    @Test
    public void testIsViewIgnored() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(view);
        PrivacyGuard guard = new PrivacyGuard(viewSet);
        assertTrue(guard.isViewIgnored(view));
    }

    @Test
    public void testIsViewNoIgnoredWhenNothingAdded() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        PrivacyGuard guard = new PrivacyGuard(viewSet);
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
        PrivacyGuard guard = new PrivacyGuard(viewSet);
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
        PrivacyGuardMock guard = new PrivacyGuardMock(viewSet);
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
        PrivacyGuardMock guard = new PrivacyGuardMock(viewSet);
        assertFalse(guard.isViewIgnored(view));
    }

    @Test
    public void testInvalidate() {
        View view = mock(View.class);
        Set<View> viewSet = new HashSet<>();
        viewSet.add(view);
        PrivacyGuardMock guard = new PrivacyGuardMock(viewSet);
        assertTrue(guard.isViewIgnored(view));
        guard.invalidate();
        assertFalse(guard.isViewIgnored(view));
    }
}
