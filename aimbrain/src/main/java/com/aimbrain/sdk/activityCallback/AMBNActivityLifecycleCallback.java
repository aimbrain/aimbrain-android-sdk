package com.aimbrain.sdk.activityCallback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.util.Logger;


public class AMBNActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = AMBNActivityLifecycleCallback.class.getSimpleName();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Logger.v(TAG, "onActivityCreated " + activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Logger.v(TAG, "onActivityStarted " + activity);
        Manager.getInstance().windowChanged(activity.getWindow());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Logger.v(TAG, "onActivityResumed " + activity);
        Manager.getInstance().windowChanged(activity.getWindow());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Logger.v(TAG, "onActivityPaused " + activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Logger.v(TAG, "onActivityStopped " + activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Logger.v(TAG, "onActivitySaveInstanceState " + activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Logger.v(TAG, "onActivityDestroyed " + activity);
    }
}
