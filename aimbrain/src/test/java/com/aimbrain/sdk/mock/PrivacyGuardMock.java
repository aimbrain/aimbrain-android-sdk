package com.aimbrain.sdk.mock;

import android.view.View;

import com.aimbrain.sdk.privacy.PrivacyGuard;

import java.util.Set;


public class PrivacyGuardMock extends PrivacyGuard {
    public PrivacyGuardMock(Set<View> ignoredViews) {
        super(ignoredViews);
    }

    public PrivacyGuardMock(boolean ignoreAllViews) {
        super(ignoreAllViews);
    }

    @Override
    protected boolean isDescendantOfIgnoredView(View view) {
        return super.isDescendantOfIgnoredView(view);
    }
}
