package com.aimbrain.sdk.AMBNApplication;

import android.app.Application;



/**
 * In order to integrate library into the project this class (or its descendant) should be used as an application class.
 * When extending this class, remember to call super.onCreate() in your onCreate() callback.
 */
public class AMBNApplication extends Application {

    private static AMBNApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * Returns singleton of the class
     * @return instance of AMBNApplication class
     */
    public static AMBNApplication getInstance(){
        return instance;
    }
}
