package com.aimbrain.sdk.activityCallback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.aimbrain.sdk.Manager;


public class AMBNActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        Manager.getInstance().windowChanged(activity.getWindow());
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
