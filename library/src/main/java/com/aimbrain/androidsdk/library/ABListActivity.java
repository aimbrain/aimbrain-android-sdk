package com.aimbrain.androidsdk.library;

import android.app.Activity;
import android.app.ListActivity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

/**
 * Wrapper for the ListActivity class.
 */
public class ABListActivity extends ListActivity {
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        EventStore.addEvent(keyCode, event, "A" + getLocalClassName());
        return super.onKeyUp(keyCode, event);
    }

}
