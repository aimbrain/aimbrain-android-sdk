package com.aimbrain.sdk.mock;

import android.view.View;

import com.aimbrain.sdk.privacy.TouchTrackingGuard;

import java.util.Set;


public class TouchTrackingGuardMock extends TouchTrackingGuard {
    public TouchTrackingGuardMock(Set<View> ignoredViews) {
        super(ignoredViews);
    }

    @Override
    protected boolean isDescendantOfIgnoredView(View view) {
        return super.isDescendantOfIgnoredView(view);
    }
}
