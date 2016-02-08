package com.aimbrain.sdk.privacy;

import android.view.View;
import android.view.ViewParent;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;



/**
 * Class used to keep and free non-capturing guards on selected views.
 * Typical use case is to store reference to this object as long as there is need to exclude given views from capturing. Then call {@link #invalidate() invalidate} method and remove the reference.
 */
public class PrivacyGuard {

    private Set<WeakReference<View>> ignoredViews;
    private boolean ignoreAllViews;
    private boolean valid;

    /**
     * Creates instance of the class with given set of protected views.
     * @param ignoredViews set of protected views
     */
    public PrivacyGuard(Set<View> ignoredViews) {
        this.ignoreAllViews = false;
        this.valid = true;
        this.ignoredViews = new HashSet<>();
        for(View view : ignoredViews)
            this.ignoredViews.add(new WeakReference<>(view));
    }

    /**
     * Allows creating instance of the class protecting all views.
     * @param ignoreAllViews true if all views in the application should be protected
     */
    public PrivacyGuard(boolean ignoreAllViews) {
        this.ignoreAllViews = ignoreAllViews;
        this.ignoredViews = new HashSet<>();
        this.valid = true;
    }

    /**
     * Method used to test whether given view is protected and should be ignored.
     * @param view view to test
     * @return true if view is protected by the privacy guard
     */
    public boolean isViewIgnored(View view) {

        if(!valid)
            return false;

        if(ignoreAllViews)
            return true;

        if(isDescendantOfIgnoredView(view))
            return true;

        return false;
    }

    private boolean isDescendantOfIgnoredView(View view) {
        for(WeakReference<View> reference : ignoredViews)
        {
            if(reference.get() == view)
                return true;
        }

        ViewParent parent = view.getParent();
        if( parent != null && parent instanceof View)
            return isDescendantOfIgnoredView((View)parent);

        return false;
    }

    /**
     * Invalidates guard. Calling this method causes guard to stop preventing from capturing.
     */
    public void invalidate() {
        ignoredViews.clear();
        this.valid = false;
    }
}
