package com.aimbrain.sdk.textEvent;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.aimbrain.sdk.collectors.SensorEventCollector;
import com.aimbrain.sdk.collectors.TextEventCollector;
import com.aimbrain.sdk.util.Logger;

import java.lang.ref.WeakReference;


public class TextEventListener implements TextWatcher {
    private static final String TAG = TextEventListener.class.getSimpleName();

    private WeakReference<View> viewRef;

    public TextEventListener(View view) {
        this.viewRef = new WeakReference<>(view);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        View view = viewRef.get();
        if (view != null) {
            SensorEventCollector.getInstance().startCollectingData(500);
            TextEventCollector.getInstance().textChanged(s.toString(), System.currentTimeMillis(), view);
            Logger.v(TAG, s.toString());
        }
    }
}
