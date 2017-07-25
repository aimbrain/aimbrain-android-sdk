package com.aimbrain.sdk.privacy;

import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewParent;

import com.aimbrain.sdk.util.Logger;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;



/**
 * Class used to keep and free non-iteration guards on selected views.
 * Typical use case is to store reference to this object as long as there is need to exclude given views from being iterated. Then call {@link #invalidate() invalidate} method and remove the reference.
 */
public class TouchTrackingGuard {
    public static final String TAG = TouchTrackingGuard.class.getSimpleName();

    private Set<WeakReference<View>> ignoredViews;
    private boolean valid;

    /**
     * Creates instance of the class with given set of protected views.
     * @param ignoredViews set of protected views
     */
    public TouchTrackingGuard(Set<View> ignoredViews) {
        this.valid = true;
        this.ignoredViews = new HashSet<>();
        if (ignoredViews != null) {
            Logger.v(TAG, "touch tracking guard, " + ignoredViews.size() + " views");
            for (View view : ignoredViews)
                this.ignoredViews.add(new WeakReference<>(view));
        }
    }


    /**
     * Method used to test whether given view is protected and should be ignored in view hierarchies.
     * @param view view to test
     * @return true if view is protected by the privacy guard
     */
    public boolean isViewIgnored(View view) {

        if(!valid)
            return false;

        if (view == null) {
            return false;
        }

        return isDescendantOfIgnoredView(view);

    }

    @VisibleForTesting
    protected boolean isDescendantOfIgnoredView(View view) {

        if (view == null) {
            return false;
        }

        for(WeakReference<View> reference : ignoredViews) {
            if(reference.get() == view)
                return true;
        }

        ViewParent parent = view.getParent();
        if ( parent != null && parent instanceof View)
            return isDescendantOfIgnoredView((View)parent);

        return false;
    }

    /**
     * Invalidates guard. Calling this method causes guard to stop preventing from being iterated over in view hierarchies.
     */
    public void invalidate() {
        ignoredViews.clear();
        this.valid = false;
    }
}
