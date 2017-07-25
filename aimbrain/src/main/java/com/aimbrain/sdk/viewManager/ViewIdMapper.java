package com.aimbrain.sdk.viewManager;

import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewParent;

import com.aimbrain.sdk.privacy.SensitiveViewGuard;

import java.util.LinkedList;
import java.util.WeakHashMap;



/**
 * Class is used to allow assigning custom ids to views.
 */
public class ViewIdMapper {
    private WeakHashMap<View, String> idMap;
    private static ViewIdMapper instance;

    @VisibleForTesting
    protected ViewIdMapper() {
        instance = this;
        this.idMap = new WeakHashMap<>();
    }

    /**
     * Gets singleton instance
     * @return instance of ViewIdMapper
     */
    public static ViewIdMapper getInstance() {
        if(instance == null)
            instance = new ViewIdMapper();
        return instance;
    }

    /**
     * Returns true if this object contains a mapping for the specified view.
     * @param view view whose presence is to be tested
     * @return true if object contains a mapping for the specified view.
     */
    public boolean containsView(View view) {
        return idMap.containsKey(view);
    }

    /**
     * Creates mapping for view with specified id.
     * @param view view with which the specified id is to be associated
     * @param id id to be associated with the specified view
     */
    public void putViewId(View view, String id) {
        idMap.put(view, id);
    }

    /**
     * Removes the mapping for a view from object.
     * @param view view whose mapping is to be removed from map
     * @return the previous id associated with view, or null if there was no mapping for view
     */
    public String removeViewId(View view) {
        return idMap.remove(view);
    }

    /**
     * Gets secure id of the specified view.
     * If no custom id is specified for view, original view identifier is returned
     * @param view view whose associated value is to be returned
     * @return string used to identify view
     */
    public String getSecureViewId(View view) {
        if(getViewId(view) == null)
            return null;
        if(SensitiveViewGuard.isViewSensitive(view))
            return SensitiveViewGuard.calculateHash(getViewId(view) + SensitiveViewGuard.getSalt());
        return getViewId(view);
    }

    public String getViewId(View view) {
        if (view == null) {
            return "";
        }
        if (idMap.containsKey(view))
            return idMap.get(view);
        return Integer.toString(view.getId());
    }

    public LinkedList<String> extractViewPath(View view) {
        LinkedList<String> path = new LinkedList<>();
        if (view == null) {
            return path;
        }
        if(!getViewId(view).equals(Integer.toString(View.NO_ID)))
            path.addLast(getSecureViewId(view));
        ViewParent parent = view.getParent();
        while(parent != null) {
            if(parent instanceof View) {
                String id = getViewId((View) parent);
                if(!id.equals(Integer.toString(View.NO_ID)))
                    path.addLast(getSecureViewId((View)parent));
            }
            parent = parent.getParent();
        }

        return path;
    }

}
