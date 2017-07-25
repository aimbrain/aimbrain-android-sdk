package com.aimbrain.sdk.privacy;

import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;

import com.aimbrain.sdk.util.Logger;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;



/**
 * Class used to keep and free non-capturing guards on selected views.
 * Typical use case is to store reference to this object as long as there is need to exclude given views from capturing. Then call {@link #invalidate() invalidate} method and remove the reference.
 */
public class PrivacyGuard {
    public static final String TAG = PrivacyGuard.class.getSimpleName();

    private Set<WeakReference<View>> ignoredViews;
    private boolean ignoreAllViews;
    private boolean valid;

    /**
     * Creates instance of the class with given set of protected views.
     * @param ignoredViews set of protected views
     */
    public PrivacyGuard(Set<View> ignoredViews) {
        Logger.v(TAG, "privacy guard, " + ignoredViews.size() + " views");
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
        Logger.v(TAG, "privacy guard, all views");
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

    public void revokeEditTextFocus(){
        if(ignoredViews != null) {
            for(WeakReference<View> reference : ignoredViews)
            {
                View view = reference.get();
                if(view instanceof EditText && view.hasFocus()){
                    view.clearFocus();
                    view.requestFocus();
                    return;
                }
            }
        }
    }

    /**
     * Invalidates guard. Calling this method causes guard to stop preventing from capturing.
     */
    public void invalidate() {
        ignoredViews.clear();
        this.valid = false;
    }
}
