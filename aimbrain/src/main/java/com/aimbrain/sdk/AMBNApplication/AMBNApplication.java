package com.aimbrain.sdk.AMBNApplication;

import android.app.Application;
import android.content.Context;

import com.aimbrain.sdk.util.Logger;

/**
 * In order to integrate library into the project this class (or its descendant) should be used as an application class.
 * When extending this class, remember to call super.onCreate() in your onCreate() callback.
 */
public class AMBNApplication {
    private static final String TAG = AMBNApplication.class.getSimpleName();

    private static AMBNApplication instance;
    private static final Object lock = new Object();
    private Application application;

    public static AMBNApplication initialize(Application application) {
        synchronized (lock) {
            if (instance == null) {
                instance = new AMBNApplication(application);
            }
            return instance;
        }
    }

    private AMBNApplication(Application application) {
        Logger.d(TAG, "Initializing AMBNApplication");
        this.application = application;
    }

    /**
     * Returns singleton of the class
     * @return instance of AMBNApplication class
     */
    public static AMBNApplication getInstance() {
        synchronized (lock) {
            if (instance == null) {
                throw new RuntimeException("Must call initialize() method on " + AMBNApplication.class.getName() + " first.");
            }
            return instance;
        }
    }

    /**
     * Get the application's context.
     * @return the application's context.
     */
    public Context getAppContext() {
        return application;
    }

    /**
     * Get the application.
     * @return the application.
     */
    public Application getApp() {
        return application;
    }
}
